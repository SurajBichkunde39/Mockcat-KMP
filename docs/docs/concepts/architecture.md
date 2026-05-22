# Architecture

Mockcat is organized into small, focused modules with a strict layering rule: shared types live in `mockcat-api` (KMP, no platform code), and everything else depends on it. No module introduces a DI framework.

---

## Layer diagram

```
┌────────────────────────────────────────────────────────────────┐
│                        Your Application                        │
└───────────────────┬────────────────────────┬───────────────────┘
                    │                        │
                    ▼                        ▼
  ┌───────────────────────────┐   ┌───────────────────────────┐
  │      Intercept Layer      │   │       Logging Layer       │
  │                           │   │                           │
  │  mockcat-intercept-okhttp │   │  mockcat-logger-okhttp    │
  │  mockcat-intercept-ktor   │   │  mockcat-logger-ktor      │
  │  mockcat-intercept-       │   │  mockcat-logger-          │
  │  urlsession               │   │  urlsession               │
  └────────────┬──────────────┘   └────────────┬──────────────┘
               │                               │
               ▼                               ▼
  ┌───────────────────────────┐   ┌───────────────────────────┐
  │        Persistence        │   │     Log Persistence       │
  │                           │   │                           │
  │  mockcat-intercept-       │   │  mockcat-logger-          │
  │  persistence              │   │  persistence              │
  └────────────┬──────────────┘   └────────────┬──────────────┘
               │                               │
               └───────────────┬───────────────┘
                               │
                               ▼
                  ┌────────────────────────────┐
                  │        mockcat-api         │  ← shared types only (KMP)
                  │                            │
                  │  MockEntry                 │
                  │  MockcatStore              │
                  │  MockMatcher               │
                  │  HttpRequestMetadata       │
                  │  LoggedHttpCall            │
                  │  HttpLogReader             │
                  └────────────────────────────┘
```

---

## Module responsibilities

### `mockcat-api` — Shared contract

Contains all types shared across platforms: data classes, interfaces, and pure logic. **No platform types, no DI, no Android/iOS imports.** Everything else depends on this module; this module depends on nothing Mockcat-specific.

Key exports:
- `MockEntry` / `MockFileEntry` — mock rule data classes
- `MockcatStore` — interface for CRUD and mock resolution
- `MockMatcher` — pure matching algorithm
- `HttpRequestMetadata` — portable request snapshot
- `MockcatResult` — sealed class: PassThrough / ApplyStatic / Redirect / Error
- `LoggedHttpCall`, `HttpLogReader`, `HttpLogWriter` — logging contracts
- `HttpLogReaderRegistry` — process-wide reader reference

### Intercept modules

Each intercept module is thin. It:
1. Converts the platform's request type to `HttpRequestMetadata`
2. Calls `MockcatStore.resolveWithMatcher(metadata)`
3. Acts on the `MockcatResult`

The matching algorithm lives entirely in `MockMatcher` (in `mockcat-api`). Interceptors don't make matching decisions.

### Logger modules

Each logger module captures request + response data, builds a `LoggedHttpCall`, and emits it to an `HttpLogWriter`. They never read from the log store — that's the viewer's job.

### Persistence modules

`mockcat-intercept-persistence` implements `MockcatStore` with Room. It owns the SQLite database for mock rules. `mockcat-logger-persistence` implements `HttpLogWriter + HttpLogReader` with a separate Room database for HTTP call history.

Both expose process-wide singleton factories (`ProcessMockcatStore`, `ProcessHttpLogStore`) so app code doesn't manage lifecycle.

### UI modules

`mockcat-intercept-ui` — Compose screens + `MockcatActivity` for viewing/editing mock rules.
`mockcat-logger-ui` — Compose screens + `HttpLogListActivity` for the HTTP log viewer.

Both use `StateFlow` + `MockcatViewModel` / `HttpLogViewModel` to drive UI. No ViewModel framework beyond Compose's `remember`/`collectAsState`.

### `mockcat-noop-android`

Identical public API, zero implementation. Replaces all other modules in production. Empty manifest — no activities, receivers, or permissions.

---

## Data flow: intercept

```
OkHttp request
    │
    ▼
MockcatOkHttpInterceptor.intercept()
    │
    ├── toRequestMetadata()         converts OkHttp Request → HttpRequestMetadata
    │
    ├── store.resolveWithMatcher()  queries DB + runs MockMatcher
    │       │
    │       ├── dao.findMatchingMockCandidates(baseUrl, method)
    │       │   (Room query: isEnabled=1, url=?, httpMethod=?)
    │       │
    │       └── MockMatcher.findBestMatch(request, candidates)
    │           (filter by headers + query params, pick most-constrained)
    │
    ├── MockcatResult.ApplyStatic  → buildStaticResponse()  ← returned to caller
    ├── MockcatResult.Redirect     → execute redirected call ← returned to caller
    ├── MockcatResult.Error        → buildErrorResponse()   ← returned to caller
    └── MockcatResult.PassThrough  → chain.proceed(request) ← real network call
```

---

## Data flow: logging

```
OkHttp request
    │
    ▼
MockcatHttpLoggingInterceptor.intercept()
    ├── record start time
    ├── chain.proceed(request)       ← executes (real or mock)
    ├── response.peekBody(maxBytes)  ← reads body without consuming
    ├── emit LoggedHttpCall to HttpLogWriter
    └── return original response     ← unchanged
```

---

## Process-wide singletons

Mockcat uses object-style singletons rather than a DI framework:

```kotlin
// MockcatStore singleton
val store: MockcatStore = ProcessMockcatStore.get(context)

// HttpLogStore singleton
val logStore = ProcessHttpLogStore.get(context)

// HttpLogReader registry (used by the UI)
val reader = HttpLogReaderRegistry.requireCurrent()
```

The singleton is created on first access and held for the process lifetime. This pattern avoids requiring Hilt, Koin, or any other DI framework in the library, while still letting apps that use a DI framework inject a `MockcatStore` by calling `ProcessMockcatStore.get(context)` from their module.

---

## Persistence

### Mocks database

- File: `mockcat_db` (Room, version 3)
- Entity: `MockEntity` — all fields from `MockEntry`, with `requiredHeaders` and `requiredQueryParams` stored as JSON strings
- Key operation: `MockDao.importReplace()` — a `@Transaction` that deletes all rows then bulk-inserts. Called on every `mockcatImport` run.

### Logs database

- File: `http_log_db` (Room)
- Entity: `HttpLogCallEntity` — request and response snapshots stored as JSON strings
- Key operation: `HttpLogCallDao.observeAll()` — reactive `Flow<List<>>` used by the log viewer

The two databases are intentionally separate so clearing the log never affects mock rules and vice versa.

---

## Redirect loop prevention

Mockcat adds `X-Mockcat-Redirected: true` to every redirected request. All interceptors check for this header first — if present, the request is passed through immediately without consulting the store. This prevents a redirect mock from creating an infinite loop.

```kotlin
// In every interceptor, this is always the first check:
if (request.headerValue(MockcatHeaders.REDIRECT_MARKER) != null) {
    return MockcatResult.PassThrough
}
```
