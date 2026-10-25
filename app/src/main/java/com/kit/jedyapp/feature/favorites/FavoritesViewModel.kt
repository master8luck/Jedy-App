package com.kit.jedyapp.feature.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kit.jedyapp.domain.model.Movie
import com.kit.jedyapp.domain.repository.FavoritesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FavoritesUiState(
    val movies: List<Movie> = emptyList(),
    val loading: Boolean = true,
    val error: Boolean = false,
    val removing: Set<String> = emptySet(),
)

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val repository: FavoritesRepository,
) : ViewModel() {
    private val mutableState = MutableStateFlow(FavoritesUiState())
    val state = mutableState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                repository.observeFavorites().collect { movies ->
                    mutableState.update { it.copy(movies = movies, loading = false) }
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                mutableState.update { it.copy(loading = false, error = true) }
            }
        }
    }

    fun remove(id: String) {
        if (id in state.value.removing) return
        mutableState.update { it.copy(removing = it.removing + id, error = false) }
        viewModelScope.launch {
            try {
                repository.removeFavorite(id)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                mutableState.update { it.copy(error = true) }
            } finally {
                mutableState.update { it.copy(removing = it.removing - id) }
            }
        }
    }
}
