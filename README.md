# Brainvita

Brainvita (English peg solitaire), as a web game and a native Android app.
Jump a marble over a neighbour into an empty hole to remove it; win by
leaving a single marble, ideally in the centre. Press **Watch perfect game**
to see the computer solve it in 31 moves.

## Download for Android

**[Download Brainvita.apk](https://github.com/infinityat0/Brainvita/releases/latest/download/Brainvita.apk)** (Android 8.0+).
Open it on your phone and allow installing from your browser or file manager when asked.
Every change to `android/` on `master` publishes a new build to
[Releases](https://github.com/infinityat0/Brainvita/releases) via `.github/workflows/android-release.yml`.

## Layout

- `web/` – the browser game (static site, no build step)
- `android/` – native Kotlin + Jetpack Compose app (`com.infinityat0.brainvita`)

## Web

Run these from `web/`. No build step: serve the `public/` folder over HTTP (ES modules don't load from `file://`):

```sh
cd web
npm start          # or: python3 -m http.server -d public
npm test           # Node 18+, uses the built-in test runner
```

- `web/public/engine.js` – pure game rules (board, move validation, win/stuck detection) and the perfect-game solver
- `web/public/main.js` – UI: rendering, selection, undo, keyboard controls, solution playback
- `web/public/index.html`, `web/public/style.css` – page and styling (light and dark)
- `web/test/` – engine tests, including a search proving the start position is solvable

## Android

Needs JDK 17+ and the Android SDK (compileSdk 36, minSdk 26).

```sh
cd android
./gradlew installDebug          # build and install on a connected device/emulator
./gradlew testDebugUnitTest     # engine tests + Robolectric UI test
```

Or open `android/` in Android Studio and press Run. The UI test writes
screenshots (light and dark) to `android/app/build/screenshots/`.

- `game/` – pure-Kotlin port of `engine.js` (no Android imports)
- `ui/` – ViewModel (selection, undo, perfect-game playback), theme, Compose screen
- `src/test/` – JUnit port of the web engine tests, plus a Robolectric Compose test

## Deploy

The web game is a static site; `vercel.json` serves `web/public/` with no build.
Deploy with `vercel --prod`, or import the repository in the Vercel dashboard.
