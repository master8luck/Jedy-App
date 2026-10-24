# Jedy App

Movie search assignment built with Kotlin, Compose, Navigation 3, Hilt and Room.

## Run

Open in Android Studio. Add your OMDb key to the existing ignored
`local.properties`, sync, then run the `app` configuration:

```properties
OMDB_API_KEY=your_key
```

Keys are not committed. An APK-embedded key is still extractable.
Debug builds log complete OMDb requests, headers and bodies without redaction,
as requested. Release builds emit no HTTP logs.

## Features

- Debounced search with immediate keyboard Search, cancellation, retry and paging.
- Movie details with poster, title, year/type and a favorite action.
- Room-backed favorites. Saved metadata opens offline; posters use Coil caching
  and show a fallback when unavailable.
- Persistent System/Light/Dark appearance, scalable text and scrollable details.
- One Nav3 back stack: single pane on compact windows, list/detail at 900 dp+.
- Native and interstitial AdMob demo ads through a separate `SDK` module.

## Architecture

`:app` contains domain contracts/models, data implementations and UI feature
packages. `:SDK` owns provider integration, ad rendering, lifecycle and policy.
This keeps the assignment lean; features do not each have their own modules.

ViewModels use repository interfaces. Room is the source of truth for favorites.
Retrofit DTOs map to domain models, and provider-specific ad objects stay in SDK.
Hilt's `NetworkModule`, `DatabaseModule`, `RepositoryModule` and `AdsModule`
are the dependency composition points. Screens receive state and callbacks;
routes collect lifecycle-aware state.

## Ad assumptions

The assignment leaves these behaviors open to interpretation:

- Indexes are zero-based in the final feed: movie, ad, movie, ad, movie.
  Both slots are present only when there are at least three movie results.
  Paging does not add further ad slots.
- Initial launch is excluded. The second later process-foreground entry is the
  one interstitial opportunity. The counter is persisted, including relaunches.
  Rotation, resizing and internal navigation do not count.
- If the interstitial is unavailable at that return, the opportunity is consumed
  and skipped. It is never shown later in the session.
- Native no-fill hides the slot. Ad objects are released on disposal, including
  a load that finishes after its owner disappears.
- All variants use Google's demo app/ad unit IDs. No real AdMob credentials are
  needed for evaluation. Real monetization/consent configuration is outside this
  assignment; demo IDs must be replaced before a monetized release.

Another provider implements `AdsProvider` and `NativeAdHandle`; app/domain code
does not need a Google SDK dependency.

## Verification

```shell
./gradlew :app:assembleDebug :app:testDebugUnitTest :SDK:testDebugUnitTest
./gradlew :app:connectedDebugAndroidTest
```

Unit tests cover HTTP mapping/errors, cancellation/debounce/retry/paging,
details/offline favorites, ad positions, return eligibility and logging policy.
Emulator tests cover Room uniqueness/removal and long-title/large-font layouts.
Tests use fake keys/data; no real OMDb calls are required by the test suite.

## Deliberate scope

No accounts, recommendations, ratings, filters, background work or extra use-case
wrappers. Favorites are device-local. OMDb allows pages 1–100; results are loaded
on demand. The project keeps two modules instead of speculative feature modules.
