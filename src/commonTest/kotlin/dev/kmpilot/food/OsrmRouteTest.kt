package dev.kmpilot.food

import dev.kmpilot.food.data.OsrmRoute
import dev.kmpilot.food.domain.LatLng
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Acceptance criteria for decoding the OSRM road route (GeoJSON [lon,lat] → LatLng). */
class OsrmRouteTest {

    private val sample = """
        {"code":"Ok","routes":[{"distance":210.4,"geometry":{"type":"LineString",
        "coordinates":[[-122.4244,37.7766],[-122.4200,37.7780],[-122.4109,37.7806]]}}]}
    """.trimIndent()

    @Test fun parses_and_flips_lon_lat_to_latlng() {
        val pts = OsrmRoute.parse(sample)
        assertEquals(3, pts.size)
        assertEquals(LatLng(37.7766, -122.4244), pts.first())
        assertEquals(LatLng(37.7806, -122.4109), pts.last())
    }

    @Test fun empty_or_garbage_yields_empty_list() {
        assertTrue(OsrmRoute.parse("").isEmpty())
        assertTrue(OsrmRoute.parse("not json").isEmpty())
        assertTrue(OsrmRoute.parse("""{"code":"NoRoute","routes":[]}""").isEmpty())
    }

    @Test fun builds_a_driving_request_url() {
        val url = OsrmRoute.url(LatLng(37.7766, -122.4244), LatLng(37.7806, -122.4109))
        assertTrue(url.startsWith("https://router.project-osrm.org/route/v1/driving/"))
        assertTrue(url.contains("-122.4244,37.7766;-122.4109,37.7806"))
        assertTrue(url.contains("geometries=geojson"))
    }
}
