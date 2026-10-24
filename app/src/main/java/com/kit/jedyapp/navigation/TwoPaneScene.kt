package com.kit.jedyapp.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.NavMetadataKey
import androidx.navigation3.runtime.contains
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope

/** One back stack and entry ownership for both compact and wide layouts. */
class TwoPaneSceneStrategy(private val enabled: Boolean) : SceneStrategy<NavKey> {
    override fun SceneStrategyScope<NavKey>.calculateScene(entries: List<NavEntry<NavKey>>): Scene<NavKey>? {
        if (!enabled || entries.lastOrNull()?.metadata?.contains(DetailPane) != true) return null
        val list = entries.dropLast(1).lastOrNull { it.metadata.contains(ListPane) } ?: return null
        return TwoPaneScene(list, entries.last(), entries.dropLast(1))
    }
}

private data class TwoPaneScene(
    val list: NavEntry<NavKey>,
    val detail: NavEntry<NavKey>,
    override val previousEntries: List<NavEntry<NavKey>>,
) : Scene<NavKey> {
    override val key: Any = list.contentKey
    override val entries = listOf(list, detail)
    override val content: @Composable () -> Unit = {
        Row(Modifier.fillMaxSize()) {
            Box(Modifier.weight(0.42f)) { list.Content() }
            VerticalDivider()
            Box(Modifier.weight(0.58f)) { detail.Content() }
        }
    }
}

object ListPane : NavMetadataKey<Boolean>
object DetailPane : NavMetadataKey<Boolean>
