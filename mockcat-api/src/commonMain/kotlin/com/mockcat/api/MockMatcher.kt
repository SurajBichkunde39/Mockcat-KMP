package com.mockcat.api

object MockMatcher {
    /**
     * Picks the best [MockEntry] for this request. Stricter required header + query sets win
     * (larger total constraint count), same as the original header-only rule.
     */
    fun findBestMatch(
        request: HttpRequestMetadata,
        candidates: List<MockEntry>,
    ): MockEntry? {
        if (candidates.isEmpty()) {
            return null
        }
        val sorted =
            candidates
                .filter { headerMatches(request, it) && queryMatches(request, it) }
                .sortedByDescending { (it.requiredHeaders?.size ?: 0) + (it.requiredQueryParams?.size ?: 0) }
        return sorted.firstOrNull()
    }

    private fun headerMatches(
        request: HttpRequestMetadata,
        mock: MockEntry,
    ): Boolean {
        if (mock.requiredHeaders.isNullOrEmpty()) {
            return true
        }
        return mock.requiredHeaders.all { (key, requiredValue) -> request.headerValue(key) == requiredValue }
    }

    private fun queryMatches(
        request: HttpRequestMetadata,
        mock: MockEntry,
    ): Boolean {
        if (mock.requiredQueryParams.isNullOrEmpty()) {
            return true
        }
        val reqQ = request.queryParameters
        return mock.requiredQueryParams.all { (key, v) -> reqQ[key] == v }
    }

    /**
     * Resolves a request to a [MockcatResult] when a [MockEntry] is already chosen.
     */
    fun toResult(
        request: HttpRequestMetadata,
        mock: MockEntry,
    ): MockcatResult = when (mock.mockType) {
        MockType.STATIC ->
            MockcatResult.ApplyStatic(
                statusCode = mock.responseCode ?: 200,
                body = mock.responseBody.orEmpty(),
                contentType = "application/json",
                delayMs = mock.delayMs ?: 0L,
            )
        MockType.REDIRECT -> {
            val u = mock.redirectUrl
            if (u.isNullOrBlank()) {
                MockcatResult.Error(
                    statusCode = 598,
                    message = "Mockcat Redirect Failed: The redirect URL for this mock is empty or null.",
                )
            } else {
                MockcatResult.Redirect(u)
            }
        }
    }
}
