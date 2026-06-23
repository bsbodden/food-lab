package dev.kmpilot.components.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.kmpilot.food.domain.LatLng

/** The keyless OpenFreeMap (OpenStreetMap) vector style — same URL on every platform, no API key. */
const val OPENFREEMAP_LIBERTY = "https://tiles.openfreemap.org/styles/liberty"

enum class MarkerKind { Pickup, Dropoff, Courier }

/** A pin on the map; [color] is ARGB (0xFFrrggbb). */
data class MapMarker(val id: String, val at: LatLng, val color: Long, val kind: MarkerKind)

/**
 * KMPilot component — **MapView**: one shared interface, a per-platform adapter behind it (portable across
 * Android/iOS, separate adapters allowed). All adapters render **MapLibre**:
 *  - **Android / iOS** → MapLibre Compose (`org.maplibre.compose:maplibre-compose`, MapLibre Native). Same
 *    `MaplibreMap` composable on both — see docs/COMPONENTS.md for the `actual`.
 *  - **wasm preview** → maplibre-gl-js hosted in `HtmlElementView` (MapLibre Compose has no wasm target yet).
 *  - **jvm** (test harness) → no-op.
 *
 * Style is a MapLibre style URL (keyless [OPENFREEMAP_LIBERTY] by default). [polyline] draws the route;
 * [markers] are the pins, including the moving courier (update its [MapMarker.at] each telemetry tick).
 */
@Composable
expect fun MapView(
    center: LatLng,
    zoom: Double,
    polyline: List<LatLng> = emptyList(),
    markers: List<MapMarker> = emptyList(),
    styleUrl: String = OPENFREEMAP_LIBERTY,
    modifier: Modifier = Modifier,
)
