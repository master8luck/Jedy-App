package com.kit.jedyapp.feature.search

import androidx.lifecycle.SavedStateHandle
import com.kit.jedyapp.domain.model.Movie
import com.kit.jedyapp.domain.model.MovieDataException
import com.kit.jedyapp.domain.model.SearchPage
import com.kit.jedyapp.domain.repository.MovieRepository
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.*
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class SearchViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeMovieRepository
    private lateinit var viewModel: SearchViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        repository = FakeMovieRepository()
        viewModel = SearchViewModel(repository, SavedStateHandle())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `typing is debounced and sends only the latest trimmed query`() = runTest(dispatcher) {
        viewModel.onQueryChanged("lo")
        runCurrent()
        advanceTimeBy(200)
        viewModel.onQueryChanged(" love ")
        runCurrent()
        advanceTimeBy(349)
        assertTrue(repository.queries.isEmpty())
        advanceTimeBy(1)
        runCurrent()
        assertEquals(listOf("love"), repository.queries)
        assertTrue(viewModel.state.value.content is SearchContent.Results)
    }

    @Test
    fun `keyboard search bypasses debounce without a duplicate request`() = runTest(dispatcher) {
        viewModel.onQueryChanged("movie")
        viewModel.onSearch()
        runCurrent()
        assertEquals(listOf("movie"), repository.queries)
        advanceUntilIdle()
        assertEquals(1, repository.queries.size)
    }

    @Test
    fun `clearing cancels pending work and restores idle`() = runTest(dispatcher) {
        viewModel.onQueryChanged("movie")
        runCurrent()
        viewModel.onQueryChanged(" ")
        advanceUntilIdle()
        assertTrue(repository.queries.isEmpty())
        assertEquals(SearchContent.Idle, viewModel.state.value.content)
    }

    @Test
    fun `old response cannot overwrite a newer search`() = runTest(dispatcher) {
        repository.search = { query ->
            if (query == "old") withContext(NonCancellable) { delay(1000) }
            SearchPage(listOf(Movie(query, query, "", "", null)), 1)
        }
        viewModel.onQueryChanged("old")
        viewModel.onSearch()
        runCurrent()
        viewModel.onQueryChanged("new")
        viewModel.onSearch()
        runCurrent()
        advanceUntilIdle()
        val result = viewModel.state.value.content as SearchContent.Results
        assertEquals("new", result.movies.single().id)
    }

    @Test
    fun `network error can be retried successfully`() = runTest(dispatcher) {
        repository.search = { throw IOException("offline") }
        viewModel.onQueryChanged("movie")
        viewModel.onSearch()
        runCurrent()
        assertEquals(SearchContent.Error(SearchError.Network), viewModel.state.value.content)
        repository.search = { SearchPage(emptyList(), 0) }
        viewModel.onSearch()
        runCurrent()
        assertEquals(SearchContent.Results(emptyList(), 0), viewModel.state.value.content)
        assertEquals(listOf("movie", "movie"), repository.queries)
    }

    @Test
    fun `service rejection becomes service error state`() = runTest(dispatcher) {
        repository.search = { throw MovieDataException("Invalid API key!") }
        viewModel.onQueryChanged("movie")
        viewModel.onSearch()
        runCurrent()
        assertEquals(SearchContent.Error(SearchError.Service), viewModel.state.value.content)
    }

    @Test
    fun `restored query is searched and edits are saved`() = runTest(dispatcher) {
        val handle = SavedStateHandle(mapOf("searchQuery" to "saved"))
        val restored = SearchViewModel(repository, handle)
        advanceUntilIdle()
        assertEquals(listOf("saved"), repository.queries)
        restored.onQueryChanged("changed")
        assertEquals("changed", handle.get<String>("searchQuery"))
        advanceUntilIdle()
    }

    @Test
    fun `whitespace edits do not repeat identical search`() = runTest(dispatcher) {
        viewModel.onQueryChanged("movie")
        advanceUntilIdle()
        viewModel.onQueryChanged(" movie ")
        advanceUntilIdle()
        assertEquals(listOf("movie"), repository.queries)
        assertEquals(" movie ", viewModel.state.value.query)
    }


    @Test
    fun `pagination appends unique movies and advances the page`() = runTest(dispatcher) {
        val first = Movie("tt1", "One", "", "", null)
        val second = Movie("tt2", "Two", "", "", null)
        repository.search = {
            if (repository.queries.size == 1) SearchPage(listOf(first), 3)
            else SearchPage(listOf(first, second), 3)
        }
        viewModel.onQueryChanged("movie"); viewModel.onSearch(); runCurrent()
        viewModel.loadMore(); runCurrent()
        val result = viewModel.state.value.content as SearchContent.Results
        assertEquals(listOf(first, second), result.movies)
        assertEquals(2, result.page)
        assertEquals(listOf(1, 2), repository.pages)
    }

    @Test
    fun `pagination failure keeps results and retries the same page`() = runTest(dispatcher) {
        val first = Movie("tt1", "One", "", "", null)
        repository.search = {
            if (repository.queries.size == 2) throw IOException("offline")
            SearchPage(listOf(first), 2)
        }
        viewModel.onQueryChanged("movie"); viewModel.onSearch(); runCurrent()
        viewModel.loadMore(); runCurrent()
        val failed = viewModel.state.value.content as SearchContent.Results
        assertTrue(failed.appendError)
        assertEquals(listOf(first), failed.movies)
        viewModel.loadMore(); runCurrent()
        assertFalse((viewModel.state.value.content as SearchContent.Results).appendError)
        assertEquals(listOf(1, 2, 2), repository.pages)
    }

    @Test
    fun `repeated pagination triggers share one in flight request`() = runTest(dispatcher) {
        repository.search = {
            if (repository.pages.last() == 2) delay(1000)
            SearchPage(listOf(Movie("tt1", "One", "", "", null)), 20)
        }
        viewModel.onQueryChanged("movie"); viewModel.onSearch(); runCurrent()
        repeat(5) { viewModel.loadMore() }
        runCurrent()
        repeat(5) { viewModel.loadMore() }
        advanceUntilIdle()
        assertEquals(listOf(1, 2), repository.pages)
    }

    @Test
    fun `cancelled append cannot pollute a different search`() = runTest(dispatcher) {
        repository.search = { query ->
            if (repository.pages.last() == 2) withContext(NonCancellable) { delay(1000) }
            SearchPage(listOf(Movie(query, query, "", "", null)), 20)
        }
        viewModel.onQueryChanged("old"); viewModel.onSearch(); runCurrent()
        viewModel.loadMore(); runCurrent()
        viewModel.onQueryChanged("new"); viewModel.onSearch(); runCurrent()
        advanceUntilIdle()
        val result = viewModel.state.value.content as SearchContent.Results
        assertEquals("new", result.movies.single().id)
        assertEquals(1, result.page)
        assertFalse(result.loadingMore)
    }

    private class FakeMovieRepository : MovieRepository {
        val queries = mutableListOf<String>()
        val pages = mutableListOf<Int>()
        var search: suspend (String) -> SearchPage = { SearchPage(emptyList(), 0) }

        override suspend fun searchMovies(query: String, page: Int): SearchPage {
            queries += query
            pages += page
            return search(query)
        }

        override suspend fun getMovie(id: String): Movie = error("Not used in search tests")
    }
}
