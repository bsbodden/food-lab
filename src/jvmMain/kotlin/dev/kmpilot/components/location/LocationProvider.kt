package dev.kmpilot.components.location

import dev.kmpilot.components.map.LatLng
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

/** jvm `actual` — no‑op (the jvm target is the acceptance-test harness; there is no device GPS). */
actual class LocationProvider actual constructor() {
    actual suspend fun current(): LatLng? = null
    actual fun updates(): Flow<LatLng> = emptyFlow()
}
