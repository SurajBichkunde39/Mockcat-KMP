package com.mockcat.logger

import com.mockcat.api.http.HttpRequestSnapshot
import com.mockcat.api.http.LoggedHttpCall
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class InMemoryHttpLogStoreTest {

    @Test
    fun emit_assignsIdsAndPrependNewest() = runTest {
        val store = InMemoryHttpLogStore(maxEntries = 10)
        val r = HttpRequestSnapshot("GET", "https://a.test/")
        store.emit(LoggedHttpCall(0L, r))
        store.emit(LoggedHttpCall(0L, r.copy(url = "https://b.test/")))

        assertNotNull(store.getById(1L))
        assertNotNull(store.getById(2L))

        val s = store.observeLogs().first()
        assertEquals(2, s.size)
        assertEquals(2L, s[0].id)
        assertEquals("https://b.test/", s[0].request.url)
    }

    @Test
    fun maxEntries_dropsOldest() = runTest {
        val store = InMemoryHttpLogStore(maxEntries = 2)
        val r = HttpRequestSnapshot("GET", "https://x.test/")
        repeat(3) { i -> store.emit(LoggedHttpCall(0L, r.copy(url = "https://$i.test/"))) }
        val s = store.observeLogs().first()
        assertEquals(2, s.size)
        assertTrue(s[0].request.url.contains("2"))
    }
}
