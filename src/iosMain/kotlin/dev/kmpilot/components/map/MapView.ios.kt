package dev.kmpilot.components.map

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import dev.kmpilot.food.domain.Geo
import dev.kmpilot.food.domain.LatLng

/**
 * iOS `actual` — a STYLIZED stand-in map drawn with a Compose [Canvas]: a light background, the route
 * [polyline] as a colored line, and the [markers] (pickup/dropoff/courier) as circles. lat/lng are normalized
 * to the canvas bounds (computed from the polyline + markers). Real MapLibre Native iOS is a FOLLOW-UP — this
 * keeps the Tracking screen functional + compiling on the real target. Mirrors the Android `actual`.
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
    val all = polyline + markers.map { it.at }
    Canvas(modifier.background(Color(0xFFE9EDF1))) {
        if (all.isEmpty()) return@Canvas
        val (sw, ne) = Geo.bounds(all)
        val pad = 0.12f
        val w = size.width
        val h = size.height
        fun toOffset(p: LatLng): Offset {
            val latSpan = (ne.lat - sw.lat).takeIf { it != 0.0 } ?: 1.0
            val lngSpan = (ne.lng - sw.lng).takeIf { it != 0.0 } ?: 1.0
            val nx = ((p.lng - sw.lng) / lngSpan).toFloat()
            val ny = ((p.lat - sw.lat) / latSpan).toFloat()
            // lat grows upward → flip y so north is up; pad keeps pins off the edges
            val x = (pad + nx * (1 - 2 * pad)) * w
            val y = (pad + (1 - ny) * (1 - 2 * pad)) * h
            return Offset(x, y)
        }
        if (polyline.size >= 2) {
            val courierColor = markers.firstOrNull { it.kind == MarkerKind.Courier }?.color ?: 0xFF2F9E44L
            val path = Path()
            polyline.forEachIndexed { i, p ->
                val o = toOffset(p)
                if (i == 0) path.moveTo(o.x, o.y) else path.lineTo(o.x, o.y)
            }
            drawPath(path, color = Color(courierColor), style = Stroke(width = 6f))
        }
        markers.forEach { m ->
            val o = toOffset(m.at)
            val r = if (m.kind == MarkerKind.Courier) 11f else 9f
            drawCircle(Color.White, radius = r + 3f, center = o)
            drawCircle(Color(m.color), radius = r, center = o)
        }
    }
}
