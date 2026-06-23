package dev.kmpilot.food

import dev.kmpilot.food.domain.Geo
import dev.kmpilot.food.domain.LatLng
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Acceptance criteria for the courier-position geometry (interpolating along the real route). */
class GeoTest {

    @Test fun haversine_one_degree_of_latitude_is_about_111km() {
        val d = Geo.haversineMeters(LatLng(0.0, 0.0), LatLng(1.0, 0.0))
        assertTrue(abs(d - 111_195.0) < 2_000.0, "got $d")
    }

    @Test fun haversine_same_point_is_zero() {
        assertEquals(0.0, Geo.haversineMeters(LatLng(37.77, -122.41), LatLng(37.77, -122.41)))
    }

    @Test fun point_along_clamps_to_endpoints() {
        val route = listOf(LatLng(0.0, 0.0), LatLng(0.0, 1.0), LatLng(0.0, 2.0))
        assertEquals(route.first(), Geo.pointAlong(route, 0f))
        assertEquals(route.first(), Geo.pointAlong(route, -1f))
        assertEquals(route.last(), Geo.pointAlong(route, 1f))
        assertEquals(route.last(), Geo.pointAlong(route, 2f))
    }

    @Test fun point_along_midpoint_of_equal_segments_is_the_middle_vertex() {
        val route = listOf(LatLng(0.0, 0.0), LatLng(0.0, 1.0), LatLng(0.0, 2.0))
        val mid = Geo.pointAlong(route, 0.5f)
        assertEquals(0.0, mid.lat, 1e-6)
        assertTrue(abs(mid.lng - 1.0) < 1e-3, "got ${mid.lng}")
    }

    @Test fun point_along_quarter_is_halfway_through_first_segment() {
        val route = listOf(LatLng(0.0, 0.0), LatLng(0.0, 2.0)) // single segment
        val q = Geo.pointAlong(route, 0.25f)
        assertTrue(abs(q.lng - 0.5) < 1e-3, "got ${q.lng}")
    }

    @Test fun point_along_handles_degenerate_routes() {
        assertEquals(LatLng(0.0, 0.0), Geo.pointAlong(emptyList(), 0.5f))
        val one = LatLng(37.0, -122.0)
        assertEquals(one, Geo.pointAlong(listOf(one), 0.5f))
    }

    @Test fun point_along_nan_returns_start_not_a_silent_snap_to_end() {
        val route = listOf(LatLng(0.0, 0.0), LatLng(0.0, 2.0))
        assertEquals(route.first(), Geo.pointAlong(route, Float.NaN))
    }

    @Test fun bounds_covers_all_points() {
        val (sw, ne) = Geo.bounds(listOf(LatLng(1.0, 5.0), LatLng(3.0, 2.0), LatLng(2.0, 8.0)))
        assertEquals(LatLng(1.0, 2.0), sw)
        assertEquals(LatLng(3.0, 8.0), ne)
    }
}
