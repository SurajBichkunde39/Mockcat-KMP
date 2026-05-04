import Foundation

struct ApiBaseURL {
    let url: URL

    init(_ raw: String) throws {
        guard let url = URL(string: raw) else {
            throw ApiError.invalidBaseUrl(raw)
        }
        self.url = url
    }
}

enum ApiError: LocalizedError {
    case invalidBaseUrl(String)
    case badStatusCode(Int)

    var errorDescription: String? {
        switch self {
        case .invalidBaseUrl(let raw):
            return "Invalid base URL: \(raw)"
        case .badStatusCode(let code):
            return "Request failed (HTTP \(code))."
        }
    }
}

enum URLSessionApi {
    static func fetchJSON<T: Decodable>(from url: URL) async throws -> T {
        var request = URLRequest(url: url)
        request.httpMethod = "GET"
        request.setValue("application/json", forHTTPHeaderField: "Accept")

        let (data, response) = try await URLSession.shared.data(for: request)
        if let http = response as? HTTPURLResponse, !(200...299).contains(http.statusCode) {
            throw ApiError.badStatusCode(http.statusCode)
        }
        let decoder = JSONDecoder()
        return try decoder.decode(T.self, from: data)
    }
}
