# iOS & URLSession

Mockcat integrates with iOS via a `URLProtocol` subclass that you register on your `URLSessionConfiguration`. The Kotlin side provides the resolution function and the shared data store; the Swift side owns the protocol lifecycle.

---

## Dependencies

The iOS integration is split across two KMP frameworks:

- `MockcatInterceptUrlsession` — provides `runMockcatUrlSessionResolve()` and the iOS mock store
- `MockcatLoggerUI` — provides the HTTP log viewer (`UIViewController`) and log store bootstrap

Link both frameworks as debug-only in your `project.yml` or Xcode project:

```yaml title="project.yml (XcodeGen)"
dependencies:
  - framework: path/to/MockcatInterceptUrlsession.framework
    embed: true
    configurations: [Debug]
  - framework: path/to/MockcatLoggerUI.framework
    embed: true
    configurations: [Debug]
```

!!! note
    A no-op XCFramework that mirrors the same Swift API is planned for a future release. For now, remove the framework references from your production target or restrict them to `Debug` configuration.

---

## Setting up the URLProtocol

Create a `URLProtocol` subclass that calls into Mockcat's resolver:

```swift
import Foundation
import MockcatInterceptUrlsession

class MockcatURLProtocol: URLProtocol {

    override class func canInit(with request: URLRequest) -> Bool {
        // Avoid intercepting requests we already redirected
        return URLProtocol.property(forKey: "MockcatHandled", in: request) == nil
    }

    override class func canonicalRequest(for request: URLRequest) -> URLRequest {
        return request
    }

    override func startLoading() {
        guard let url = request.url?.absoluteString else {
            client?.urlProtocol(self, didFailWithError: URLError(.badURL))
            return
        }

        let meta = HttpRequestMetadata(
            url: url,
            method: request.httpMethod ?? "GET",
            headers: (request.allHTTPHeaderFields ?? [:]).map { Pair(first: $0.key, second: $0.value) }
        )

        let store = ProcessMockcatStoreCompanion().get(context: nil) // iOS process store
        let result = store.resolveWithMatcher(request: meta)

        switch result {
        case let static as MockcatResult.ApplyStatic:
            let data = static.body.data(using: .utf8) ?? Data()
            let response = HTTPURLResponse(
                url: request.url!,
                statusCode: Int(static.statusCode),
                httpVersion: "HTTP/1.1",
                headerFields: ["Content-Type": static.contentType]
            )!
            if static.delayMs > 0 {
                Thread.sleep(forTimeInterval: Double(static.delayMs) / 1000.0)
            }
            client?.urlProtocol(self, didReceive: response, cacheStoragePolicy: .notAllowed)
            client?.urlProtocol(self, didLoad: data)
            client?.urlProtocolDidFinishLoading(self)

        case is MockcatResult.PassThrough:
            // Fall back to a regular request
            let marked = (request as NSURLRequest).mutableCopy() as! NSMutableURLRequest
            URLProtocol.setProperty(true, forKey: "MockcatHandled", in: marked)
            let task = URLSession.shared.dataTask(with: marked as URLRequest) { data, response, error in
                if let error = error {
                    self.client?.urlProtocol(self, didFailWithError: error)
                } else {
                    self.client?.urlProtocol(self, didReceive: response!, cacheStoragePolicy: .notAllowed)
                    if let data = data { self.client?.urlProtocol(self, didLoad: data) }
                    self.client?.urlProtocolDidFinishLoading(self)
                }
            }
            task.resume()

        default:
            client?.urlProtocol(self, didFailWithError: URLError(.unknown))
        }
    }

    override func stopLoading() {}
}
```

---

## Registering the protocol

Register `MockcatURLProtocol` on a `URLSessionConfiguration` before creating your session. Do **not** register it globally on `URLProtocol` class — that affects all sessions including system ones.

```swift
var config = URLSessionConfiguration.default
config.protocolClasses = [MockcatURLProtocol.self] + (config.protocolClasses ?? [])
let session = URLSession(configuration: config)
```

---

## Bootstrapping the log store

Call this once at app startup (e.g., `AppDelegate.application(_:didFinishLaunchingWithOptions:)`):

```swift
import MockcatLoggerUI

installHttpLogReaderForIos()
```

This initializes the Room-backed log store, registers it with `HttpLogReaderRegistry`, and sets up an overlay button that opens the log viewer.

---

## Opening the log viewer

```swift
import MockcatLoggerUI

let logViewController = createHttpLogListViewController()
present(logViewController, animated: true)
```

The log viewer shows all captured HTTP calls with status badges, timing, and a detail view with full headers and body.

---

## Current limitations

| Feature | Status |
|---------|--------|
| URL matching | ✓ |
| Method matching | ✓ (pass from Swift) |
| Header matching | ✓ (pass from Swift) |
| Query param matching | ✓ (included in URL) |
| Mock editor UI | Planned |
| Redirect mocks | Planned |
| No-op XCFramework | Planned |

The `NSURLRequest.toHttpRequestMetadata()` Kotlin helper currently only extracts the URL. For full header and method matching, build the `HttpRequestMetadata` in Swift (as shown above) and pass it directly.
