# Jedy App

An Android movie search app built with Kotlin, Jetpack Compose, Navigation 3,
Hilt, Retrofit, and Room.

## Setup and launch

1. Open the project in Android Studio and let Gradle sync finish.
2. Obtain an API key from [OMDb](https://www.omdbapi.com/apikey.aspx).
3. Add the key to `local.properties` in the project root, keeping any existing
   Android SDK configuration:

   ```properties
   OMDB_API_KEY=your_key
   ```

4. Select the `app` run configuration and launch it on an emulator or device
   running Android 9 (API 28) or newer.

`local.properties` is ignored by Git. Rebuild the app after changing the key.
Google test ads are configured already; an AdMob account is not required.

## Features

- Search movies by title, with automatic pagination and manual retry.
- View a movie's poster, title, year, and type.
- Save and remove favorites stored locally on the device. Saved movie metadata
  is available offline; poster availability depends on the image cache.
- Choose System, Light, or Dark appearance.
- Adaptive layouts support landscape, larger screens, and larger text.
  Wide windows display the movie list and details side by side.

## Ads

The app uses Google's test ad units for both native ads and interstitials.

Two native placements appear when the search contains at least three movies:
movie, ad, movie, ad, movie. Loading more results does not add more placements.

An interstitial is shown, if loaded, on the second return from the background,
at most once per app launch. If unavailable at that return, it is skipped.
The return counter resets after a process restart or explicitly closing the app;
rotation and screen navigation do not affect it.

## Project structure

- `app`: screens and ViewModels, domain models and repository interfaces,
  API integration, local storage, and dependency injection.
- `SDK`: ad provider interfaces, AdMob integration, ad rendering, and placement
  and lifecycle rules.

ViewModels depend on repository interfaces. API responses are mapped to domain
models in the data layer, while Room stores favorites. Hilt provides dependencies.
Another ad provider can be added through `AdsProvider` and `NativeAdHandle`.

Screen and component previews are located beside their UI implementations.
Open the relevant Kotlin file in Android Studio's Split or Design view.
