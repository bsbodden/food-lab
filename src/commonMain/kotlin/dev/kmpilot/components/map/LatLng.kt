package dev.kmpilot.components.map

/**
 * A geographic point (WGS-84): [lat]itude + [lng]ngitude. The component library's own coordinate type, so
 * [MapView] is app-agnostic. An app with its own LatLng can `typealias LatLng = dev.kmpilot.components.map.LatLng`
 * to interop without converting at the call site.
 */
data class LatLng(val lat: Double, val lng: Double)
