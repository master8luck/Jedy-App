package com.kit.jedyapp.feature.search

import com.kit.jedyapp.domain.model.Movie

data class SearchUiState(val query: String = "", val content: SearchContent = SearchContent.Idle)

sealed interface SearchContent {
    data object Idle : SearchContent
    data object Loading : SearchContent
    data class Results(
        val movies: List<Movie>, val totalResults: Int,
        val page: Int = 1, val loadingMore: Boolean = false, val appendError: Boolean = false,
    ) : SearchContent
    data class Error(val reason: SearchError) : SearchContent
}

enum class SearchError { Network, Service, Unexpected }
