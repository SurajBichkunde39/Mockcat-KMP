# Mock Files

Mock rules can be defined as JSON files on your development machine and pushed to a connected device using the [Gradle plugin](gradle-plugin.md). Each file can contain a single mock entry or multiple entries in a wrapper object.

---

## File formats

### Single entry

```json
{
  "url": "https://api.example.com/users",
  "httpMethod": "GET",
  "responseCode": 200,
  "responseBody": [
    { "id": 1, "name": "Alice" }
  ]
}
```

### Multiple entries

```json
{
  "entries": [
    {
      "url": "https://api.example.com/users",
      "httpMethod": "GET",
      "responseCode": 200,
      "responseBody": [{ "id": 1, "name": "Alice" }]
    },
    {
      "url": "https://api.example.com/users/1",
      "httpMethod": "GET",
      "responseCode": 200,
      "responseBody": { "id": 1, "name": "Alice", "role": "admin" }
    }
  ]
}
```

Both formats are accepted in the same `mocks/` directory. The Gradle plugin merges entries from all JSON files before pushing.

---

## Field reference

| Field | Type | Required | Default | Description |
|-------|------|:--------:|---------|-------------|
| `url` | string | **yes** | — | Base URL **without** query string. Must match the request URL exactly. |
| `httpMethod` | string | **yes** | — | HTTP method: `GET`, `POST`, `PUT`, `DELETE`, `PATCH`, `HEAD`, `OPTIONS` |
| `label` | string | no | `""` | Human-readable name shown in the mock editor UI |
| `isEnabled` | boolean | no | `true` | Disabled rules are stored in the DB but never matched |
| `mockType` | string | no | `"STATIC"` | `STATIC` or `REDIRECT` |
| `responseCode` | integer | no | — | HTTP status code for static responses (e.g. `200`, `404`) |
| `responseBody` | any JSON | no | — | Response body — can be an object, array, string, number, boolean, or null |
| `delayMs` | integer | no | — | Milliseconds to wait before returning the response |
| `redirectUrl` | string | no | — | Target URL when `mockType` is `REDIRECT` |
| `requiredHeaders` | object | no | — | `{ "Header-Name": "value" }` — all must be present on the request |
| `requiredQueryParams` | object | no | — | `{ "param": "value" }` — all must be present in the request query string |
| `staticResponse` | object | no | — | Full response snapshot (overrides `responseCode`/`responseBody`) |

---

## URL matching

The `url` field must match the base URL of the incoming request, **without** the query string.

```json
// Matches: https://api.example.com/users
// Matches: https://api.example.com/users?page=1&sort=asc
// Does NOT match: https://api.example.com/users/1
{ "url": "https://api.example.com/users", "httpMethod": "GET" }
```

To constrain on query parameters, use `requiredQueryParams`.

---

## Static responses

### Simple response

```json
{
  "url": "https://api.example.com/status",
  "httpMethod": "GET",
  "responseCode": 200,
  "responseBody": { "status": "healthy" }
}
```

### Error response

```json
{
  "url": "https://api.example.com/item/99",
  "httpMethod": "GET",
  "responseCode": 404,
  "responseBody": { "error": "Not found" }
}
```

### Slow response

```json
{
  "url": "https://api.example.com/data",
  "httpMethod": "GET",
  "responseCode": 200,
  "responseBody": { "data": "ok" },
  "delayMs": 3000
}
```

### Full response with custom headers

Use `staticResponse` when you need to set specific response headers or the full HTTP version:

```json
{
  "url": "https://api.example.com/data",
  "httpMethod": "GET",
  "staticResponse": {
    "statusCode": 200,
    "reasonPhrase": "OK",
    "protocol": "HTTP/1.1",
    "headers": [
      { "name": "Content-Type", "value": "application/json; charset=utf-8" },
      { "name": "X-Request-Id", "value": "abc-123" },
      { "name": "Cache-Control", "value": "no-store" }
    ],
    "body": { "text": "{\"result\": \"ok\"}" }
  }
}
```

When `staticResponse` is present it takes precedence over `responseCode` and `responseBody`.

---

## Redirect mocks

A redirect mock causes the interceptor to re-execute the request against a different URL. The redirected request passes through the same matching pipeline — if the target URL also has a mock, that mock is applied.

```json
{
  "url": "https://api.example.com/old-endpoint",
  "httpMethod": "GET",
  "mockType": "REDIRECT",
  "redirectUrl": "https://api.example.com/new-endpoint"
}
```

!!! warning
    Redirect loops are prevented by the `X-Mockcat-Redirected` header that Mockcat adds internally. If the redirected request matches another redirect mock pointing back to the original, it will pass through to the real network.

---

## Header constraints

Use `requiredHeaders` to match only requests that carry specific header values. Matching is case-insensitive on the key, exact on the value.

```json
{
  "url": "https://api.example.com/secure",
  "httpMethod": "GET",
  "requiredHeaders": {
    "Authorization": "Bearer test-token",
    "X-Api-Version": "2"
  },
  "responseCode": 200,
  "responseBody": { "data": "protected" }
}
```

This mock only matches requests that include both headers with exactly those values. Requests without the headers, or with different values, do not match this rule (they may match a less-constrained rule for the same URL).

---

## Query parameter constraints

Use `requiredQueryParams` to match only requests whose URL contains specific query parameters.

```json
{
  "url": "https://api.example.com/users",
  "httpMethod": "GET",
  "requiredQueryParams": {
    "role": "admin",
    "status": "active"
  },
  "responseCode": 200,
  "responseBody": [{ "id": 1, "name": "Admin" }]
}
```

The request query string may contain additional parameters — only the declared ones need to match. For example, `GET /users?role=admin&status=active&page=1` still matches.

---

## Combining constraints

You can combine `requiredHeaders` and `requiredQueryParams` on the same rule. When multiple rules match the same request, the one with the most total constraints wins. See [Mock Matching](../concepts/matching.md) for the full priority algorithm.

```json
{
  "entries": [
    {
      "url": "https://api.example.com/data",
      "httpMethod": "GET",
      "responseCode": 200,
      "responseBody": { "tier": "default" }
    },
    {
      "url": "https://api.example.com/data",
      "httpMethod": "GET",
      "requiredQueryParams": { "tier": "premium" },
      "responseCode": 200,
      "responseBody": { "tier": "premium", "features": ["a", "b"] }
    },
    {
      "url": "https://api.example.com/data",
      "httpMethod": "GET",
      "requiredQueryParams": { "tier": "premium" },
      "requiredHeaders": { "X-Beta": "true" },
      "responseCode": 200,
      "responseBody": { "tier": "premium-beta", "features": ["a", "b", "c"] }
    }
  ]
}
```

- `GET /data` → `{ "tier": "default" }`
- `GET /data?tier=premium` → `{ "tier": "premium", ... }`
- `GET /data?tier=premium` + `X-Beta: true` → `{ "tier": "premium-beta", ... }`

---

## Disabled rules

Setting `"isEnabled": false` keeps the rule in the database (visible in the mock editor) but excludes it from matching. Useful for quickly toggling scenarios without deleting and re-importing.

```json
{
  "url": "https://api.example.com/users",
  "httpMethod": "GET",
  "isEnabled": false,
  "responseCode": 500,
  "responseBody": { "error": "server error" }
}
```

---

## JSON parser behavior

The JSON parser used for mock files is lenient:

- Accepts single quotes, trailing commas, and unquoted keys
- Ignores unknown fields
- Coerces compatible types (e.g. `"200"` → `200` for integer fields)

This means adding new fields in a future version won't break existing mock files.
