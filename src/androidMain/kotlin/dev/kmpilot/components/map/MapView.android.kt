package dev.kmpilot.components.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.kmpilot.food.domain.LatLng
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.layers.CircleLayer
import org.maplibre.compose.layers.LineLayer
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.style.BaseStyle
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.LineString
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Position

/**
 * Android `actual` — a REAL MapLibre vector map (MapLibre Native, via MapLibre Compose). Renders the
 * OpenFreeMap basemap from [styleUrl], the OSRM route [polyline] as a LineLayer (white casing + accent line),
 * and the pickup/dropoff/courier [markers] as CircleLayers. The courier marker MOVES as its [MapMarker.at]
 * updates — `rememberGeoJsonSource` re-sends the feature data on recomposition. iOS keeps the Canvas
 * stand-in until the SPM link path is proven green; wasm uses maplibre-gl-js. Same surface, per-platform adapter.
 */
@Composable
actual fun MapView(
    center: LatLng,
    zoom: Double,
    polyline: List<LatLng>,
    markers: List<MapMarker>,
    styleUrl: String,
    modifier: Modifier,
) {
    val camera = rememberCameraState(
        firstPosition = CameraPosition(target = Position(center.lng, center.lat), zoom = zoom),
    )
    MaplibreMap(modifier = modifier, baseStyle = BaseStyle.Uri(styleUrl), cameraState = camera) {
        if (polyline.size >= 2) {
            val routeColor = markers.firstOrNull { it.kind == MarkerKind.Courier }?.color ?: 0xFF2F9E44L
            val routeSource = rememberGeoJsonSource(
                GeoJsonData.Features(
                    FeatureCollection(
                        listOf(
                            Feature(
                                geometry = LineString(polyline.map { Position(it.lng, it.lat) }),
                                properties = null,
                            ),
                        ),
                    ),
                ),
            )
            LineLayer(id = "route-casing", source = routeSource, color = const(Color.White), width = const(8.dp))
            LineLayer(id = "route", source = routeSource, color = const(Color(routeColor)), width = const(5.dp))
        }
        markers.forEach { m ->
            key(m.kind) {
                val src = rememberGeoJsonSource(
                    GeoJsonData.Features(
                        FeatureCollection(
                            listOf(Feature(geometry = Point(Position(m.at.lng, m.at.lat)), properties = null)),
                        ),
                    ),
                )
                CircleLayer(
                    id = "marker-${m.kind}",
                    source = src,
                    radius = const(if (m.kind == MarkerKind.Courier) 9.dp else 7.dp),
                    color = const(Color(m.color)),
                    strokeColor = const(Color.White),
                    strokeWidth = const(3.dp),
                )
            }
        }
    }
}
