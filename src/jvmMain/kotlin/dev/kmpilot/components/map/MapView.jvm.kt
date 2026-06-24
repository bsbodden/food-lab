package dev.kmpilot.components.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** jvm test `actual` — no map in the unit-test harness (the Geo/OSRM logic in commonMain is what's tested). */
@Composable
actual fun MapView(
    center: LatLng,
    zoom: Double,
    polyline: List<LatLng>,
    markers: List<MapMarker>,
    styleUrl: String,
    modifier: Modifier,
) { /* no-op */ }
