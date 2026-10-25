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
- Each app launch starts at zero. The second background-to-foreground return
  offers at most one interstitial in that launch. Return history is kept only
  in memory and resets after process death/restart or explicitly closing the host.
  Rotation, resizing and internal navigation do not reset or increment the counter.
  There is no one-hour reset in this scope.
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
details/offline favorites, favorite updates during loading, ad positions, return eligibility,
interstitial expiry and logging policy.
Emulator tests cover Room uniqueness/removal, return-ad lifecycle coordination,
long-title/large-font layouts and automatic pagination with manual retry.
Tests use fake keys/data; no real OMDb calls are required by the test suite.

## Deliberate scope

No accounts, recommendations, ratings, filters, background work or extra use-case
wrappers. Favorites are device-local. OMDb allows pages 1–100; results are loaded
on demand. The project keeps two modules instead of speculative feature modules.

## Replaying the return-ad test

The interstitial opportunity is once per app launch, on its second return.
Backgrounding and reopening alone preserves that launch's counter. Restarting
the process or explicitly closing the host gives the next launch a fresh counter.
Debug lifecycle messages use the Logcat tag `JedyAds`.

To replay without deleting favorites, appearance or the API key:

```shell
adb shell am force-stop com.kit.jedyapp
adb shell am start -n com.kit.jedyapp/.MainActivity
```

Wait for `Interstitial loaded` in Logcat. Press Home, wait at least one second,
and reopen: first return, no interstitial. Repeat: second return, show the ad
if available. If unavailable, diagnostics explain why it was skipped; there is
no delayed ad later in the session.

The dismissible Native Validator window is Google test tooling. It is not
part of a native creative and does not appear on live ad requests. It is
disabled by default so it cannot cover Search during normal manual testing.
To enable it when checking native-ad compliance:

```shell
./gradlew :app:assembleDebug -PnativeAdValidator=true
```

List/native view content is clipped to its viewport. This does not attempt to
clip external SDK windows, ad destinations or full-screen ad experiences.

## Screen previews and short windows

Search uses one LazyColumn: introduction, pinned search input, state message or
result feed, and paging footer. The introduction/count can scroll away instead
of consuming all available height in landscape. Favorites also uses one lazy
list, including its heading and state content. No orientation-specific screen
implementation or portrait lock is used.

Open these files in Android Studio's Split/Design view:

- `feature/search/SearchScreen.kt`
- `feature/details/DetailsScreen.kt`
- `feature/favorites/FavoritesScreen.kt`

Named state providers cover initial/loading/empty/error/content states, saved
and saving details, removal in progress, paging/loading/error states, missing
posters and long titles. Search includes one, two, three and many movies.
The original ad policy is unchanged: fewer than three movies means no native
slots; three or more means two native slots in the result feed.

The Layout checks group previews a short landscape content area, tablet pane,
150% font scale and dark theme. These are screen bodies after navigation
chrome, not complete Activity previews. A taller Search ads preview includes
three movies and both native slots.

Previews use offline fixtures and labeled native placeholders, without Hilt,
network requests, storage or Google ad objects. The SDK's fullscreen interstitial
must be checked on an emulator/device; it is not a Compose screen preview.
