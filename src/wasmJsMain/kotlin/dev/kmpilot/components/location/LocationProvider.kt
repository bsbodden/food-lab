package dev.kmpilot.components.location

import dev.kmpilot.components.map.LatLng
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

/**
 * wasm `actual` — no‑op. The in‑browser preview is the emulator; it doesn't need a real device fix (the studio
 * feeds positions over the sensor bridge). Returns no location. (A `navigator.geolocation` adapter could be added
 * later if a wasm app needs the browser's real location.)
 */
actual class LocationProvider actual constructor() {
    actual suspend fun current(): LatLng? = null
    actual fun updates(): Flow<LatLng> = emptyFlow()
}
