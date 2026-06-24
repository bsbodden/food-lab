package dev.kmpilot.components.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.HtmlElementView
import org.w3c.dom.HTMLElement

/**
 * wasm `actual` — hosts **maplibre-gl-js** (loaded in app.html) inside Compose via `HtmlElementView`. The thin
 * `window.KMap` glue (also in app.html) owns the map instance, the route line, the pins, and the courier marker;
 * Kotlin drives it through one-line `js(...)` calls (each the function's sole statement, per the Kotlin/Wasm rule).
 *
 * Layout note (CMP-8521): the DOM map renders above the Compose canvas and captures pointer events in its
 * rectangle — so keep all Compose chrome OUTSIDE the map region (TrackingScreen puts the sheet below the map).
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
actual fun MapView(
    center: LatLng,
    zoom: Double,
    polyline: List<LatLng>,
    markers: List<MapMarker>,
    styleUrl: String,
    modifier: Modifier,
) {
    val coords = remember(polyline) { polyline.joinToString(",", "[", "]") { "[${it.lng},${it.lat}]" } }
    val pickup = markers.firstOrNull { it.kind == MarkerKind.Pickup }
    val drop = markers.firstOrNull { it.kind == MarkerKind.Dropoff }
    val courier = markers.firstOrNull { it.kind == MarkerKind.Courier }
    HtmlElementView(
        factory = { mapDiv() },
        modifier = modifier,
        // NOTE: HtmlElementView.update fires on EVERY recomposition (each telemetry tick). The window.KMap
        // guards are LOAD-BEARING + must stay idempotent: mount(el._map), route(getSource), pins(el._pins) are
        // one-shot; only courier.setLngLat re-runs to move the marker. Keep those guards if editing app.html.
        update = { el ->
            kmapMount(el, styleUrl, center.lng, center.lat, zoom)
            if (coords != "[]") kmapRoute(el, coords, hex(courier?.color ?: 0xFF2F9E44L))
            if (pickup != null && drop != null) kmapPins(el, pickup.at.lng, pickup.at.lat, hex(pickup.color), drop.at.lng, drop.at.lat, hex(drop.color))
            if (courier != null) kmapCourier(el, courier.at.lng, courier.at.lat, hex(courier.color))
        },
        onRelease = { el -> kmapDestroy(el) },
    )
}

private fun hex(c: Long): String = "#" + (c and 0xFFFFFF).toString(16).padStart(6, '0')

private fun mapDiv(): HTMLElement = js("(function(){var d=document.createElement('div');d.style.width='100%';d.style.height='100%';return d;})()")
private fun kmapMount(el: HTMLElement, style: String, lng: Double, lat: Double, zoom: Double) { js("window.KMap.mount(el, style, lng, lat, zoom)") }
private fun kmapRoute(el: HTMLElement, coordsJson: String, color: String) { js("window.KMap.route(el, coordsJson, color)") }
private fun kmapPins(el: HTMLElement, plng: Double, plat: Double, pcolor: String, dlng: Double, dlat: Double, dcolor: String) { js("window.KMap.pins(el, plng, plat, pcolor, dlng, dlat, dcolor)") }
private fun kmapCourier(el: HTMLElement, lng: Double, lat: Double, color: String) { js("window.KMap.courier(el, lng, lat, color)") }
private fun kmapDestroy(el: HTMLElement) { js("window.KMap.destroy(el)") }
