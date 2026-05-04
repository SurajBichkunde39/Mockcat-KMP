package com.mockcat.api

import kotlinx.coroutines.flow.Flow

/**
 * Read/write access to mock entries. Implemented by the persistence module (Room) and
 * used by UI and client interceptors.
 */
interface MockcatStore {
    fun observeAllMocks(): Flow<List<MockEntry>>

    suspend fun getAllMocksOnce(): List<MockEntry>

    suspend fun insertOrUpdate(entry: MockEntry)

    suspend fun delete(entry: MockEntry)

    suspend fun deleteAll()

    /**
     * Returns enabled mocks for [urlWithoutQuery] and [httpMethod] (query matching is left to [com.mockcat.api.MockMatcher]).
     */
    suspend fun findMatchingMockCandidates(
        urlWithoutQuery: String,
        httpMethod: String,
    ): List<MockEntry>

    suspend fun importFromJsonReplaceAll(json: String): Int

    suspend fun exportAllToJson(): String
}

/**
 * Resolves a request using the same candidate query + [MockMatcher] rules.
 */
suspend fun MockcatStore.resolveWithMatcher(request: HttpRequestMetadata): MockcatResult {
    if (request.headerValue(MockcatHeaders.REDIRECT_MARKER) != null) {
        return MockcatResult.PassThrough
    }
    val candidates = findMatchingMockCandidates(request.baseUrl, request.method)
    val mock = MockMatcher.findBestMatch(request, candidates) ?: return MockcatResult.PassThrough
    return MockMatcher.toResult(request, mock)
}
