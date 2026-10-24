package com.kit.jedyapp.feature.search

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kit.jedyapp.sdk.api.AdsProvider
import com.kit.jedyapp.sdk.ui.NativeAdSlot

@Composable
fun SearchRoute(onMovieClick: (String) -> Unit, adsProvider: AdsProvider) {
    val viewModel: SearchViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    SearchScreen(state, viewModel::onQueryChanged, viewModel::onSearch, onMovieClick,
        nativeAd = { slot -> NativeAdSlot(adsProvider, "${state.query.trim()}:$slot") },
        onLoadMore = viewModel::loadMore)
}
