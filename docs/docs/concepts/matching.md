# Mock Matching

When an HTTP request comes in, Mockcat runs a two-stage matching process to decide which mock rule (if any) to apply.

---

## Stage 1 — Database filter

The Room DAO runs a SQL query that returns all **enabled** mocks that match the request's base URL and HTTP method:

```sql
SELECT * FROM mocks
WHERE isEnabled = 1
  AND url = :baseUrl
  AND httpMethod = :method
```

`baseUrl` is the request URL with the query string stripped. So `https://api.example.com/users?page=2` becomes `https://api.example.com/users`.

This is intentionally simple — it returns a small candidate list efficiently without doing anything complex in SQL.

---

## Stage 2 — Fine-grained matching

`MockMatcher.findBestMatch()` receives the candidates from stage 1 and applies header and query parameter matching:

```kotlin
val sorted = candidates
    .filter { headerMatches(request, it) && queryMatches(request, it) }
    .sortedByDescending {
        (it.requiredHeaders?.size ?: 0) + (it.requiredQueryParams?.size ?: 0)
    }
return sorted.firstOrNull()
```

**Step 1 — Filter:** Keep only mocks where all declared constraints are satisfied.

**Step 2 — Sort:** Among the survivors, sort by total constraint count (descending). More constraints = higher specificity = higher priority.

**Step 3 — Pick first:** Return the most-constrained match, or `null` if nothing survived filtering.

---

## Header matching rules

```kotlin
private fun headerMatches(request: HttpRequestMetadata, mock: MockEntry): Boolean {
    if (mock.requiredHeaders.isNullOrEmpty()) return true
    return mock.requiredHeaders.all { (key, requiredValue) ->
        request.headerValue(key) == requiredValue
    }
}
```

- If `requiredHeaders` is null or empty: the mock matches any request regardless of headers.
- Otherwise: **every** declared header must be present in the request with the **exact** value.
- Key comparison: **case-insensitive** (HTTP header names are case-insensitive by spec).
- Value comparison: **exact, case-sensitive**.
- The request may have additional headers beyond those declared — they are ignored.

---

## Query parameter matching rules

```kotlin
private fun queryMatches(request: HttpRequestMetadata, mock: MockEntry): Boolean {
    if (mock.requiredQueryParams.isNullOrEmpty()) return true
    val reqQ = request.queryParameters
    return mock.requiredQueryParams.all { (key, v) -> reqQ[key] == v }
}
```

- If `requiredQueryParams` is null or empty: the mock matches any query string (including no query string at all).
- Otherwise: **every** declared param must be present in the request query with the **exact** value.
- Both key and value comparisons are **case-sensitive**.
- The request may have additional query params beyond those declared — they are ignored.

---

## Priority examples

### Example 1 — Same URL, different constraint levels

**Request:** `GET https://api.example.com/users?role=admin`

| Mock | `requiredQueryParams` | `requiredHeaders` | Total constraints | Matches? |
|------|-----------------------|-------------------|:-----------------:|:--------:|
| A | — | — | 0 | ✓ |
| B | `{ role: admin }` | — | 1 | ✓ |
| C | `{ role: admin }` | `{ Authorization: Bearer x }` | 2 | ✗ (header missing) |

**Result:** Mock B wins (1 constraint, highest among matches).

---

### Example 2 — All constraints satisfied

**Request:** `POST https://api.example.com/orders`
Headers: `Authorization: Bearer xyz`, `X-Tenant: acme`
Query: `version=2`

| Mock | Constraints | Matches? |
|------|-------------|:--------:|
| A | — | ✓ (0) |
| B | query `version=2` | ✓ (1) |
| C | header `Authorization: Bearer xyz` | ✓ (1) |
| D | query `version=2` + header `Authorization: Bearer xyz` | ✓ (2) |
| E | query `version=2` + header `Authorization: Bearer xyz` + header `X-Tenant: acme` | ✓ (3) |

**Result:** Mock E wins (3 constraints). Among B and C (both 1 constraint), the sort is stable — whichever appears first in the sorted list is returned, but in practice you'd give them different response bodies for different scenarios.

---

### Example 3 — No match

**Request:** `GET https://api.example.com/items`
Headers: `Authorization: Bearer wrong-token`

| Mock | Constraints | Matches? |
|------|-------------|:--------:|
| A | header `Authorization: Bearer correct-token` | ✗ (value mismatch) |

**Result:** No match → `MockcatResult.PassThrough` → real network call.

---

## URL matching details

The `url` field is matched against the **base URL** (scheme + host + path, no query string). The comparison is exact string equality.

```
Request URL:  https://api.example.com/users?page=2&sort=asc
Base URL:     https://api.example.com/users
```

**Does not match:**
- `https://api.example.com/users/` (trailing slash)
- `https://api.example.com/users/1` (extra path segment)
- `http://api.example.com/users` (different scheme)
- `https://API.EXAMPLE.COM/users` (different host casing)

---

## Query string parsing

The query string is split on `&`, then each segment on `=`. Percent-encoded characters are decoded (`%20` → space, `+` → space). Duplicate keys keep the last value.

```
?a=1&b=hello+world&c=%2F
→ { a: "1", b: "hello world", c: "/" }
```

Multi-value query params (e.g. `?tag=a&tag=b`) are not currently supported for constraints — if a key appears twice, only the last value is used for matching.

---

## No match behavior

If no mock rule matches (or all mocks are disabled), `MockcatStore.resolveWithMatcher()` returns `MockcatResult.PassThrough` and the interceptor calls `chain.proceed(request)`. The request goes to the real network unchanged.

---

## Redirect loop prevention

Before entering stage 1, the interceptor checks for the `X-Mockcat-Redirected` header. If present, it immediately returns `PassThrough` without querying the database. This header is added internally to every redirected request to prevent loops.
