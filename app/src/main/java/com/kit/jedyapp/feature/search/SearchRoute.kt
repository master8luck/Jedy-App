package com.kit.jedyapp.feature.search

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun SearchRoute(onFavoritesClick: () -> Unit, onMovieClick: (String) -> Unit) {
    val viewModel: SearchViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    SearchScreen(
        state = state,
        onQueryChanged = viewModel::onQueryChanged,
        onSearch = viewModel::onSearch,
        onFavoritesClick = onFavoritesClick,
        onMovieClick = onMovieClick,
    )
}
