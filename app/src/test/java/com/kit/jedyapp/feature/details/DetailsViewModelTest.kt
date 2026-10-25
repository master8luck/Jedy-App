package com.kit.jedyapp.feature.details

import com.kit.jedyapp.domain.model.Movie
import com.kit.jedyapp.domain.model.SearchPage
import com.kit.jedyapp.domain.repository.FavoritesRepository
import com.kit.jedyapp.domain.repository.MovieRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class DetailsViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val movie = Movie("tt1", "A very long movie title", "2020", "movie", null)
    private val saved = MutableStateFlow<List<Movie>>(emptyList())
    private var fail = false
    private var calls = 0
    private var networkResponse: CompletableDeferred<Movie>? = null
    private val favorites = object : FavoritesRepository {
        override fun observeFavorites() = saved
        override suspend fun addFavorite(movie: Movie) { saved.value = listOf(movie) }
        override suspend fun removeFavorite(id: String) { saved.value = emptyList() }
    }
    private val movies = object : MovieRepository {
        override suspend fun getMovie(id: String): Movie { calls++; check(!fail); return networkResponse?.await() ?: movie }
        override suspend fun searchMovies(query: String, page: Int) = SearchPage(emptyList(), 0)
    }
    private lateinit var viewModel: DetailsViewModel
    @Before fun setUp() {
        Dispatchers.setMain(dispatcher)
        viewModel = DetailsViewModel(movies, favorites)
    }
    @After fun tearDown() { Dispatchers.resetMain() }

    @Test fun `details loads and toggles persisted favorite state`() = runTest(dispatcher) {
        viewModel.load("tt1"); runCurrent()
        assertEquals(movie, viewModel.state.value.movie)
        viewModel.toggleFavorite(); runCurrent()
        assertTrue(viewModel.state.value.isFavorite)
        viewModel.toggleFavorite(); runCurrent()
        assertFalse(viewModel.state.value.isFavorite)
        assertTrue(saved.value.isEmpty())
    }
    @Test fun `saved movie opens without network`() = runTest(dispatcher) {
        saved.value = listOf(movie); fail = true
        viewModel.load("tt1"); runCurrent()
        assertEquals(movie, viewModel.state.value.movie)
        assertTrue(viewModel.state.value.isFavorite)
        assertEquals(0, calls)
    }
    @Test fun `failed details can be retried`() = runTest(dispatcher) {
        fail = true; viewModel.load("tt1"); runCurrent()
        assertTrue(viewModel.state.value.loadError)
        fail = false; viewModel.load("tt1"); runCurrent()
        assertFalse(viewModel.state.value.loadError)
        assertEquals(movie, viewModel.state.value.movie)
    }

    @Test fun `favorite added during network load remains saved when response arrives`() = runTest(dispatcher) {
        networkResponse = CompletableDeferred()
        viewModel.load("tt1"); runCurrent()
        saved.value = listOf(movie); runCurrent()
        assertTrue(viewModel.state.value.isFavorite)
        networkResponse!!.complete(movie); runCurrent()
        assertEquals(movie, viewModel.state.value.movie)
        assertTrue(viewModel.state.value.isFavorite)
        viewModel.toggleFavorite(); runCurrent()
        assertTrue(saved.value.isEmpty())
    }

    @Test fun `switching details updates status even without a database emission`() = runTest(dispatcher) {
        saved.value = listOf(movie); runCurrent()
        viewModel.load("other"); runCurrent()
        assertFalse(viewModel.state.value.isFavorite)
        viewModel.load("tt1"); runCurrent()
        assertTrue(viewModel.state.value.isFavorite)
    }
}
