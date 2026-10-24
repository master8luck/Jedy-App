# Jedy App

Android movie-search assignment using Compose and Navigation 3.

## Setup

Open the project in Android Studio. Add your OMDb key to the existing
`local.properties` (this file is ignored by Git), then sync and rebuild:

```properties
OMDB_API_KEY=your_key
```

The key is required when making requests, not to build or open the placeholder
screens. It is compiled into the APK; local configuration keeps it out of Git,
but does not make an APK-embedded key secret. Debug builds log HTTP request/response headers and bodies to Logcat
(OkHttp), including API keys and credential headers without redaction. Release builds emit
no HTTP logs.

## Current step

Search is connected to the repository through a Hilt-created ViewModel. Type a
movie title to search after a 350 ms pause, or use the keyboard Search action.
Details and Favorites remain placeholders. Search currently loads only page one.
No automatic request runs at application startup.

- `NetworkModule`: creates the JSON converter, HTTP client and OMDb API.
- `RepositoryModule`: binds the repository interface to its implementation.
- `OmdbMovieRepository`: calls the API and maps responses to domain models.
- `MovieMapper`: validates required fields and handles unavailable posters.
- `SearchRoute`: obtains the entry-scoped ViewModel and collects state with lifecycle awareness.
- `SearchViewModel`: saves the query, cancels superseded work and exposes UI state.
- `SearchScreen`: renders state and emits callbacks; it does not perform requests.

Domain and data are packages inside `:app`. `:SDK` is the separate ads module.
Domain code does not depend on Hilt, Retrofit, Room or Compose.

Search with no matches returns an empty page. Other OMDb failures throw
`MovieDataException`; HTTP/network failures propagate. Cancellation is not caught.
The Search ViewModel translates failures into retryable UI states and preserves cancellation.

## Verification

```shell
./gradlew :app:assembleDebug :app:testDebugUnitTest
```

Local HTTP tests use MockWebServer and a fake API key; they do not contact OMDb.
They cover encoded queries, authentication, JSON mapping, empty results, API
errors, HTTP errors and missing poster/required fields.
Search ViewModel tests cover debounce, immediate submit, clearing, stale responses,
retry, service errors, restored query and whitespace-only edits.
