package com.mockcat.api

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MockMatcherTest {
    @Test
    fun stricterHeaderMatchWins() {
        val r =
            HttpRequestMetadata(
                url = "https://a.test/x",
                method = "GET",
                headers = listOf("A" to "1", "B" to "2"),
            )
        val wide =
            MockEntry(
                id = 1L,
                url = "https://a.test/x",
                httpMethod = "GET",
                requiredHeaders = null,
            )
        val strict =
            MockEntry(
                id = 2L,
                url = "https://a.test/x",
                httpMethod = "GET",
                requiredHeaders = mapOf("A" to "1", "B" to "2"),
            )
        val m = MockMatcher.findBestMatch(r, listOf(wide, strict))
        assertEquals(2L, m?.id)
    }

    @Test
    fun noMatchWhenHeaderMismatch() {
        val r = HttpRequestMetadata("https://a/b", "POST", listOf("X" to "wrong"))
        val m = MockEntry(1, "https://a/b", "", "POST", true, requiredHeaders = mapOf("X" to "y"))
        assertNull(MockMatcher.findBestMatch(r, listOf(m)))
    }

    @Test
    fun toResultStatic() {
        val m =
            MockEntry(
                id = 0,
                url = "u",
                httpMethod = "GET",
                mockType = MockType.STATIC,
                responseCode = 201,
                responseBody = "{ }",
                delayMs = 0,
            )
        val res = MockMatcher.toResult(HttpRequestMetadata("u", "GET"), m)
        assertTrue(res is MockcatResult.ApplyStatic)
        assertEquals(201, res.statusCode)
    }

    @Test
    fun toResultInvalidRedirect() {
        val m =
            MockEntry(
                id = 0,
                url = "u",
                httpMethod = "GET",
                mockType = MockType.REDIRECT,
                redirectUrl = " ",
            )
        val res = MockMatcher.toResult(HttpRequestMetadata("u", "GET"), m)
        assertTrue(res is MockcatResult.Error)
    }
}
