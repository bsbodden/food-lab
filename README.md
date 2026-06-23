# Morsel — KMPilot demo (Kotlin Multiplatform)

A food-delivery app — browse restaurants → menu → cart → a **live map TRACK** screen fed by emulated courier
telemetry — built **TEST-FIRST** as a **Kotlin Multiplatform + Compose Multiplatform** app by
[KMPilot](https://kmpilot.dev). One shared `commonMain` domain/statechart + Compose UI runs natively on
**Android** and **iOS**; the **wasm** build is the in-browser preview/emulator.

## Build each target
- **Android** — `./gradlew assembleDebug` → `build/outputs/apk/debug/*.apk` (needs the Android SDK).
- **iOS** — on a Mac via `iosApp/`, or **free on CI** (below).
- **Web (wasm preview)** — `./gradlew wasmJsBrowserDevelopmentExecutableDistribution`.
- **Tests** — `./gradlew jvmTest` (the acceptance criteria run on the JVM).

## iOS without a Mac (free)
The **Actions → "iOS (free simulator build)"** workflow compiles an iOS **Simulator** `.app` on a GitHub-hosted
macOS runner (no signing, no Apple account) and uploads it as an artifact. Add an `APPETIZE_TOKEN` repo secret
to also stream it to [Appetize.io](https://appetize.io) — the run summary prints a browser link you open on your
iPhone. macOS runners are **unlimited-free on public repos**.

## Layout
- `src/commonMain` — shared domain (money/Geo/OSRM), statecharts (KStateMachine), and the Compose UI + `App()`.
- `src/{androidMain,iosMain,wasmJsMain,jvmMain}` — thin per-platform entrypoints + `expect/actual` shims.
- `iosApp/` — the SwiftUI host (an XcodeGen `project.yml`; the `.xcodeproj` is generated in CI).

## Platform notes (follow-ups)
- **Map** — Android/iOS render a **stylized Compose-Canvas** stand-in (route line + pickup/dropoff/courier
  pins). wasm uses real MapLibre GL JS. Real **MapLibre Native** on Android/iOS is a follow-up.
- **Images** — Android/iOS/jvm render the monogram **placeholder** tile; wasm fetches+decodes real photos.
  Real Coil/AVFoundation loading is a follow-up.
