package com.mockcat.api

object MockMatcher {
    /**
     * Picks the best [MockEntry] for this request. Stricter required header sets win
     * (more header keys first), same as the original Android app.
     */
    fun findBestMatch(
        request: HttpRequestMetadata,
        candidates: List<MockEntry>,
    ): MockEntry? {
        if (candidates.isEmpty()) {
            return null
        }
        val sorted = candidates.sortedByDescending { it.requiredHeaders?.size ?: 0 }
        return sorted.firstOrNull { mock ->
            if (mock.requiredHeaders.isNullOrEmpty()) {
                true
            } else {
                mock.requiredHeaders.all { (key, requiredValue) ->
                    request.headerValue(key) == requiredValue
                }
            }
        }
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
