package dev.kmpilot.food.data

import dev.kmpilot.food.domain.LatLng
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Parses an OSRM `route/v1/driving` response (geometries=geojson) into a list of [LatLng]. GeoJSON coordinates
 * are `[lon, lat]` — we flip them. Pure (no networking) so it's unit-testable; the wasm layer does the fetch.
 */
object OsrmRoute {
    private val json = Json { ignoreUnknownKeys = true }

    @Serializable private data class Resp(val routes: List<Route> = emptyList())
    @Serializable private data class Route(val geometry: Geometry = Geometry())
    @Serializable private data class Geometry(val coordinates: List<List<Double>> = emptyList())

    /** Decode the first route's polyline; empty list if the response has no usable route. */
    fun parse(body: String): List<LatLng> = runCatching {
        json.decodeFromString<Resp>(body).routes.firstOrNull()?.geometry?.coordinates
            ?.mapNotNull { c -> if (c.size >= 2) LatLng(lat = c[1], lng = c[0]) else null }
            ?: emptyList()
    }.getOrElse { emptyList() }

    /** Build the OSRM request URL for a driving route from [from] to [to]. */
    fun url(from: LatLng, to: LatLng): String =
        "https://router.project-osrm.org/route/v1/driving/" +
            "${from.lng},${from.lat};${to.lng},${to.lat}?overview=full&geometries=geojson"
}
