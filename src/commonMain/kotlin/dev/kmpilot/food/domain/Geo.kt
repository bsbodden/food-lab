package dev.kmpilot.food.domain

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Pure geographic helpers (no platform deps → unit-testable). The courier's position is interpolated along the
 * REAL OSRM road route by the telemetry `progress` (0..1), measured by cumulative great-circle distance so the
 * marker moves at a constant ground speed regardless of how the route's vertices are spaced.
 */
object Geo {
    private const val EARTH_M = 6_371_000.0
    private const val DEG = 0.017453292519943295 // π/180

    /** Great-circle distance in metres (haversine). */
    fun haversineMeters(a: LatLng, b: LatLng): Double {
        val dLat = (b.lat - a.lat) * DEG
        val dLng = (b.lng - a.lng) * DEG
        val s = sin(dLat / 2) * sin(dLat / 2) +
            cos(a.lat * DEG) * cos(b.lat * DEG) * sin(dLng / 2) * sin(dLng / 2)
        return 2 * EARTH_M * atan2(sqrt(s), sqrt(1 - s))
    }

    /** The point at fraction [t] (0..1) of the route's total length. */
    fun pointAlong(route: List<LatLng>, t: Float): LatLng {
        if (route.isEmpty()) return LatLng(0.0, 0.0)
        if (route.size == 1 || t.isNaN() || t <= 0f) return route.first()
        if (t >= 1f) return route.last()
        val segs = route.zipWithNext { a, b -> haversineMeters(a, b) }
        val total = segs.sum()
        if (total == 0.0) return route.first()
        var remaining = t * total
        for (i in segs.indices) {
            if (remaining <= segs[i]) {
                val f = if (segs[i] == 0.0) 0.0 else remaining / segs[i]
                return lerp(route[i], route[i + 1], f)
            }
            remaining -= segs[i]
        }
        return route.last()
    }

    /** Axis-aligned bounds (sw, ne) of a set of points — for fitting the map viewport to the route. */
    fun bounds(points: List<LatLng>): Pair<LatLng, LatLng> {
        require(points.isNotEmpty()) { "no points" }
        var minLat = points[0].lat; var maxLat = points[0].lat
        var minLng = points[0].lng; var maxLng = points[0].lng
        for (p in points) {
            if (p.lat < minLat) minLat = p.lat; if (p.lat > maxLat) maxLat = p.lat
            if (p.lng < minLng) minLng = p.lng; if (p.lng > maxLng) maxLng = p.lng
        }
        return LatLng(minLat, minLng) to LatLng(maxLat, maxLng)
    }

    private fun lerp(a: LatLng, b: LatLng, f: Double) =
        LatLng(a.lat + (b.lat - a.lat) * f, a.lng + (b.lng - a.lng) * f)
}
