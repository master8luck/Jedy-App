package com.kit.jedyapp.sdk.policy

sealed interface FeedEntry<out T> {
    data class Content<T>(val value: T) : FeedEntry<T>
    data class Native(val slot: Int) : FeedEntry<Nothing>
}

/** Zero-based positions in the final feed: movie, ad, movie, ad, movie. */
fun <T> buildAdFeed(items: List<T>): List<FeedEntry<T>> = buildList {
    items.forEachIndexed { index, item ->
        add(FeedEntry.Content(item))
        if (items.size >= 3 && index < 2) add(FeedEntry.Native(index))
    }
}

data class ReturnState(val launched: Boolean = false, val returns: Int = 0)
data class ReturnDecision(val state: ReturnState, val eligible: Boolean)

/** Called only on process foreground, never screen navigation or configuration changes. */
fun onForeground(state: ReturnState): ReturnDecision {
    if (!state.launched) return ReturnDecision(ReturnState(launched = true), false)
    val count = (state.returns + 1).coerceAtMost(3)
    return ReturnDecision(ReturnState(true, count), count == 2)
}
