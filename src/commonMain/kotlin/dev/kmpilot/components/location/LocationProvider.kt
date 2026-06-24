package dev.kmpilot.components.location

import dev.kmpilot.components.map.LatLng
import kotlinx.coroutines.flow.Flow

/**
 * KMPilot component — **device location**. A one‑shot [current] fix plus a cold [updates] stream of positions
 * (reuses the library's [LatLng]). Android + iOS read the real device GPS; the wasm preview and the jvm test
 * harness are no‑ops (return `null` / an empty stream) — the preview doesn't need a real fix.
 *
 * The **app owns the permission**: declare it (Android `ACCESS_FINE_LOCATION`/`ACCESS_COARSE_LOCATION`; iOS
 * `NSLocationWhenInUseUsageDescription`), request it at runtime, and on Android seed [LocationContext] from your
 * Activity. If permission is absent, [current] returns `null` and [updates] emits nothing (never throws).
 * Same call site on every platform; per‑platform adapter underneath. See COMPONENTS.md.
 */
expect class LocationProvider() {
    suspend fun current(): LatLng?
    fun updates(): Flow<LatLng>
}
