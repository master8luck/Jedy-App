package com.kit.jedyapp.sdk.policy

import org.junit.Assert.*
import org.junit.Test

class AdPoliciesTest {
    @Test fun `native ads occupy final indexes one and three`() {
        val feed = buildAdFeed(listOf("one", "two", "three", "four"))
        assertEquals(FeedEntry.Native(0), feed[1])
        assertEquals(FeedEntry.Native(1), feed[3])
        assertEquals(listOf("one", "two", "three", "four"),
            feed.filterIsInstance<FeedEntry.Content<String>>().map { it.value })
    }
    @Test fun `short lists have no native ads`() {
        for (count in 0..2) {
            val feed = buildAdFeed(List(count) { it })
            assertEquals(count, feed.size)
            assertTrue(feed.none { it is FeedEntry.Native })
        }
        assertEquals(5, buildAdFeed(listOf(1, 2, 3)).size)
    }
    @Test fun `only second return is eligible`() {
        var state = ReturnState()
        val decisions = (0..5).map {
            onForeground(state).also { decision -> state = decision.state }.eligible
        }
        assertEquals(listOf(false, false, true, false, false, false), decisions)
    }
    @Test fun `persisted return count prevents another ad after restart`() {
        assertTrue(onForeground(ReturnState(true, 1)).eligible)
        assertFalse(onForeground(ReturnState(true, 2)).eligible)
        assertFalse(onForeground(ReturnState(true, 3)).eligible)
    }
}
