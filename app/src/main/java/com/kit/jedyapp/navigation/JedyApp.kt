package com.kit.jedyapp.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.*
import androidx.navigation3.ui.NavDisplay
import com.kit.jedyapp.R
import com.kit.jedyapp.feature.details.DetailsRoute
import com.kit.jedyapp.feature.favorites.FavoritesRoute
import com.kit.jedyapp.feature.search.SearchRoute
import com.kit.jedyapp.ui.theme.ThemeMode
import com.kit.jedyapp.sdk.api.AdsProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JedyApp(theme: ThemeMode, onThemeChange: (ThemeMode) -> Unit, adsProvider: AdsProvider) {
    val backStack = rememberNavBackStack(Search)
    var themeMenu by remember { mutableStateOf(false) }
    val goBack: () -> Unit = { if (backStack.size > 1) backStack.removeLastOrNull() }
    val openMovie: (String) -> Unit = { id ->
        if (backStack.lastOrNull() is MovieDetails) backStack.removeLastOrNull()
        backStack.add(MovieDetails(id))
    }
    val favoritesSelected = backStack.any { it == Favorites }
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val sceneStrategy = remember(maxWidth >= 900.dp) { TwoPaneSceneStrategy(maxWidth >= 900.dp) }
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("JEDY", style = MaterialTheme.typography.titleLarge, letterSpacing = 4.sp) },
                    navigationIcon = {
                        if (backStack.lastOrNull() is MovieDetails) IconButton(onClick = goBack) {
                            Icon(painterResource(R.drawable.ic_back), stringResource(R.string.back))
                        }
                    },
                    actions = {
                        Box {
                            IconButton(onClick = { themeMenu = true }) {
                                Icon(painterResource(R.drawable.ic_theme), stringResource(R.string.appearance))
                            }
                            DropdownMenu(expanded = themeMenu, onDismissRequest = { themeMenu = false }) {
                                ThemeMode.entries.forEach { mode ->
                                    val label = when (mode) {
                                        ThemeMode.System -> R.string.theme_system
                                        ThemeMode.Light -> R.string.theme_light
                                        ThemeMode.Dark -> R.string.theme_dark
                                    }
                                    DropdownMenuItem(
                                        text = { Text(stringResource(label)) },
                                        leadingIcon = { RadioButton(selected = theme == mode, onClick = null) },
                                        onClick = { onThemeChange(mode); themeMenu = false },
                                    )
                                }
                            }
                        }
                    },
                )
            },
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        selected = !favoritesSelected,
                        onClick = { while (backStack.size > 1) backStack.removeLastOrNull() },
                        icon = { Icon(painterResource(R.drawable.ic_search), null) },
                        label = { Text(stringResource(R.string.search_title)) },
                    )
                    NavigationBarItem(
                        selected = favoritesSelected,
                        onClick = {
                            while (backStack.size > 1 && backStack.last() != Favorites) backStack.removeLastOrNull()
                            if (backStack.last() != Favorites) backStack.add(Favorites)
                        },
                        icon = { Icon(painterResource(R.drawable.ic_favorite), null) },
                        label = { Text(stringResource(R.string.favorites_title)) },
                    )
                }
            },
        ) { padding ->
            NavDisplay(
                modifier = Modifier.fillMaxSize().padding(padding),
                backStack = backStack, onBack = goBack,
                sceneStrategies = listOf(sceneStrategy),
                entryDecorators = listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),
                    rememberViewModelStoreNavEntryDecorator(),
                ),
                entryProvider = entryProvider {
                    entry<Search>(metadata = metadata { put(ListPane, true) }) { SearchRoute(onMovieClick = openMovie, adsProvider = adsProvider) }
                    entry<Favorites>(metadata = metadata { put(ListPane, true) }) { FavoritesRoute(onMovieClick = openMovie) }
                    entry<MovieDetails>(metadata = metadata { put(DetailPane, true) }) { destination -> DetailsRoute(destination.imdbId) }
                },
            )
        }
    }
}
