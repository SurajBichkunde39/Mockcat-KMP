package com.mockcat.api

import kotlin.test.Test
import kotlin.test.assertEquals

class HttpUrlParserTest {
    @Test
    fun noQuery() {
        val (b, q) = splitHttpUrl("https://host/p/x")
        assertEquals("https://host/p/x", b)
        assertEquals(emptyMap(), q)
    }

    @Test
    fun queryOrderIndependent() {
        val a = parseQueryString("b=2&a=1")
        val b = parseQueryString("a=1&b=2")
        assertEquals("1", a["a"])
        assertEquals("1", b["a"])
    }

    @Test
    fun lastDuplicateWins() {
        val p = parseQueryString("a=1&a=2")
        assertEquals("2", p["a"])
    }

    @Test
    fun buildRoundTrip() {
        val u = "https://api/movies"
        val q = mapOf("b" to "2", "a" to "1")
        val full = buildHttpUrl(u, q)
        val (b, m) = splitHttpUrl(full)
        assertEquals(u, b)
        assertEquals("1", m["a"])
        assertEquals("2", m["b"])
    }
}
