package com.kit.jedyapp.data.repository

import com.kit.jedyapp.data.remote.OmdbApi
import com.kit.jedyapp.di.NetworkModule
import com.kit.jedyapp.domain.model.MovieDataException
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class OmdbMovieRepositoryTest {
    private lateinit var server: MockWebServer
    private lateinit var repository: OmdbMovieRepository

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        val api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .client(NetworkModule.provideHttpClient("test-key"))
            .addConverterFactory(
                NetworkModule.provideJson().asConverterFactory("application/json".toMediaType())
            )
            .build()
            .create(OmdbApi::class.java)
        repository = OmdbMovieRepository(api)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `search encodes query supplies key and maps results`() = runBlocking {
        respond("""{"Response":"True","totalResults":"12","Search":[
            {"imdbID":"tt1","Title":"Love & Life","Year":"2020","Type":"movie","Poster":"N/A"}
        ],"unknown":"ignored"}""")

        val page = repository.searchMovies("  love & life  ", 2)
        val request = requireNotNull(server.takeRequest(2, TimeUnit.SECONDS))

        assertEquals("GET", request.method)
        assertEquals("/", request.requestUrl!!.encodedPath)
        assertEquals("love & life", request.requestUrl!!.queryParameter("s"))
        assertEquals("2", request.requestUrl!!.queryParameter("page"))
        assertEquals("test-key", request.requestUrl!!.queryParameter("apikey"))
        assertEquals(12, page.totalResults)
        assertEquals("tt1", page.movies.single().id)
        assertEquals("Love & Life", page.movies.single().title)
        assertNull(page.movies.single().posterUrl)
    }

    @Test
    fun `details looks up IMDb ID and maps poster`() = runBlocking {
        respond("""{"Response":"True","imdbID":"tt1","Title":"Movie",
            "Year":"2020","Type":"movie","Poster":"https://example.com/poster.jpg","Plot":"ignored"}""")

        val movie = repository.getMovie("tt1")
        val request = requireNotNull(server.takeRequest(2, TimeUnit.SECONDS))

        assertEquals("tt1", request.requestUrl!!.queryParameter("i"))
        assertNull(request.requestUrl!!.queryParameter("s"))
        assertEquals("https://example.com/poster.jpg", movie.posterUrl)
    }

    @Test
    fun `no matches is an empty page`() = runBlocking {
        respond("""{"Response":"False","Error":"Movie not found!"}""")
        val page = repository.searchMovies("unknown")
        assertTrue(page.movies.isEmpty())
        assertEquals(0, page.totalResults)
    }

    @Test
    fun `API rejection is not treated as empty results`() {
        respond("""{"Response":"False","Error":"Invalid API key!"}""")
        val error = assertThrows(MovieDataException::class.java) {
            runBlocking { repository.searchMovies("movie") }
        }
        assertEquals("Invalid API key!", error.message)
    }

    @Test
    fun `HTTP failures propagate`() {
        server.enqueue(MockResponse().setResponseCode(503))
        assertThrows(HttpException::class.java) {
            runBlocking { repository.getMovie("tt1") }
        }
    }

    @Test
    fun `missing required movie fields fail explicitly`() {
        respond("""{"Response":"True","Title":"Movie"}""")
        assertThrows(MovieDataException::class.java) {
            runBlocking { repository.getMovie("tt1") }
        }
    }

    @Test
    fun `invalid totals fail explicitly`() {
        respond("""{"Response":"True","totalResults":"invalid","Search":[]}""")
        assertThrows(MovieDataException::class.java) {
            runBlocking { repository.searchMovies("movie") }
        }
    }

    @Test
    fun `blank input is rejected before making a request`() {
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking { repository.searchMovies("  ") }
        }
        assertEquals(0, server.requestCount)
    }

    @Test
    fun `missing configured key fails before network access`() {
        val client = NetworkModule.provideHttpClient("")
        val request = okhttp3.Request.Builder().url(server.url("/")).build()
        assertThrows(IOException::class.java) {
            client.newCall(request).execute().close()
        }
        assertEquals(0, server.requestCount)
    }

    private fun respond(body: String) {
        server.enqueue(
            MockResponse().setHeader("Content-Type", "application/json").setBody(body)
        )
    }
}
