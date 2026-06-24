package dev.kmpilot.food.domain

/** Framework-free POJOs. Money is in integer cents (no float drift — the Tier-1 money invariant). */

/** A geographic point (WGS-84). Used for real addresses, the OSRM route, and the courier position. Now the
 *  component library's shared type (so MapView stays app-agnostic) — a typealias keeps the food domain/OSRM/
 *  telemetry/Geo code unchanged. */
typealias LatLng = dev.kmpilot.components.map.LatLng

data class Restaurant(
    val id: String,
    val name: String,
    val cuisine: String,
    val rating: Double,
    val etaMin: Int,
    val deliveryFeeCents: Int,
    val accent: Long,
    val emoji: String,
    val address: String = "",
    val location: LatLng = LatLng(0.0, 0.0),
    val imageUrl: String = "",
)

data class MenuItem(
    val id: String,
    val name: String,
    val desc: String,
    val priceCents: Int,
    val emoji: String,
    val imageUrl: String = "",
)

data class CartLine(val item: MenuItem, val qty: Int) {
    val totalCents: Int get() = item.priceCents * qty
}

/** The order/delivery lifecycle — the live-track screen's states. */
enum class DeliveryStatus(val label: String) {
    Confirmed("Order confirmed"),
    Preparing("Preparing your order"),
    PickedUp("Courier picked up"),
    OnTheWay("On the way"),
    Arriving("Arriving now"),
    Delivered("Delivered");
}

/** A telemetry frame streamed from the emulator (the courier data source). */
data class Telemetry(
    val progress: Float,           // 0..1 along the route
    val status: DeliveryStatus,
    val etaMin: Int,
    val driverName: String,
    val vehicle: String,
)

fun money(cents: Int): String = "$" + (cents / 100) + "." + (cents % 100).toString().padStart(2, '0')
