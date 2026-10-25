package com.kit.jedyapp.feature.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kit.jedyapp.domain.model.MovieDataException
import com.kit.jedyapp.domain.repository.MovieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val movieRepository: MovieRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val mutableState = MutableStateFlow(
        SearchUiState(query = savedStateHandle[QUERY_KEY] ?: "")
    )
    val state = mutableState.asStateFlow()
    private var searchJob: Job? = null

    init {
        if (state.value.query.isNotBlank()) startSearch(debounce = true)
    }

    fun onQueryChanged(query: String) {
        val previous = state.value.query
        if (query == previous) return
        savedStateHandle[QUERY_KEY] = query
        mutableState.update { it.copy(query = query) }
        // Whitespace-only edits should not repeat an identical search.
        if (query.trim() != previous.trim()) startSearch(debounce = true)
    }

    /** Keyboard Search and retry bypass the typing debounce. */
    fun onSearch() = startSearch(debounce = false)


    fun loadMore() {
        val current = state.value.content as? SearchContent.Results ?: return
        if (current.loadingMore || current.movies.size >= current.totalResults || current.page >= 100) return
        val query = state.value.query.trim()
        mutableState.update { it.copy(content = current.copy(loadingMore = true, appendError = false)) }
        searchJob = viewModelScope.launch {
            val content = try {
                val next = movieRepository.searchMovies(query, current.page + 1)
                current.copy(
                    movies = (current.movies + next.movies).distinctBy { it.id },
                    totalResults = next.totalResults,
                    page = current.page + 1,
                    loadingMore = false,
                    appendError = false,
                )
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                current.copy(loadingMore = false, appendError = true)
            }
            ensureActive()
            mutableState.update { it.copy(content = content) }
        }
    }

    private fun startSearch(debounce: Boolean) {
        searchJob?.cancel()
        val query = state.value.query.trim()
        if (query.isEmpty()) {
            mutableState.update { it.copy(content = SearchContent.Idle) }
            return
        }

        mutableState.update { it.copy(content = SearchContent.Loading) }
        searchJob = viewModelScope.launch {
            if (debounce) delay(350)
            val content = try {
                val page = movieRepository.searchMovies(query)
                SearchContent.Results(page.movies, page.totalResults)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: IOException) {
                SearchContent.Error(SearchError.Network)
            } catch (_: MovieDataException) {
                SearchContent.Error(SearchError.Service)
            } catch (_: Exception) {
                SearchContent.Error(SearchError.Unexpected)
            }
            // Protect against a dependency that finishes despite cancellation.
            ensureActive()
            mutableState.update { it.copy(content = content) }
        }
    }

    private companion object {
        const val QUERY_KEY = "searchQuery"
    }
}
