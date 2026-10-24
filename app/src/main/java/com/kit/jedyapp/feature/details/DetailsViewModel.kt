package com.kit.jedyapp.feature.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kit.jedyapp.domain.model.Movie
import com.kit.jedyapp.domain.repository.FavoritesRepository
import com.kit.jedyapp.domain.repository.MovieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DetailsUiState(
    val movie: Movie? = null,
    val loading: Boolean = true,
    val loadError: Boolean = false,
    val isFavorite: Boolean = false,
    val saving: Boolean = false,
    val saveError: Boolean = false,
)

@HiltViewModel
class DetailsViewModel @Inject constructor(
    private val movies: MovieRepository,
    private val favorites: FavoritesRepository,
) : ViewModel() {
    private val mutableState = MutableStateFlow(DetailsUiState())
    val state = mutableState.asStateFlow()
    private var currentId: String? = null
    private var loadJob: Job? = null

    init {
        viewModelScope.launch {
            try {
                favorites.observeFavorites().collect { saved ->
                    mutableState.update { it.copy(isFavorite = saved.any { movie -> movie.id == currentId }) }
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                mutableState.update { it.copy(saveError = true) }
            }
        }
    }

    fun load(id: String) {
        if (currentId == id && state.value.movie != null) return
        currentId = id
        loadJob?.cancel()
        mutableState.value = DetailsUiState()
        loadJob = viewModelScope.launch {
            try {
                // Saved metadata is sufficient for this assignment and works offline.
                val saved = favorites.observeFavorites().first().firstOrNull { it.id == id }
                val movie = saved ?: movies.getMovie(id)
                ensureActive()
                mutableState.update { it.copy(movie = movie, loading = false, isFavorite = saved != null) }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                ensureActive()
                mutableState.update { it.copy(loading = false, loadError = true) }
            }
        }
    }

    fun toggleFavorite() {
        val snapshot = state.value
        val movie = snapshot.movie ?: return
        if (snapshot.saving) return
        mutableState.update { it.copy(saving = true, saveError = false) }
        viewModelScope.launch {
            try {
                if (snapshot.isFavorite) favorites.removeFavorite(movie.id) else favorites.addFavorite(movie)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                mutableState.update { it.copy(saveError = true) }
            } finally {
                mutableState.update { it.copy(saving = false) }
            }
        }
    }
}
