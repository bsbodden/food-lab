package dev.kmpilot.food.data

import dev.kmpilot.food.domain.LatLng
import dev.kmpilot.food.domain.MenuItem
import dev.kmpilot.food.domain.Restaurant

class FoodRepository {
    fun restaurants(): List<Restaurant> = RESTAURANTS
    fun restaurant(id: String): Restaurant? = RESTAURANTS.find { it.id == id }
    fun menu(restaurantId: String): List<MenuItem> = MENUS[restaurantId].orEmpty()
    fun item(id: String): MenuItem? = MENUS.values.flatten().find { it.id == id }

    /** The real OSRM road route from a restaurant to the delivery destination. */
    fun route(restaurantId: String): List<LatLng> = ROUTES[restaurantId].orEmpty()
    fun destination(): LatLng = DESTINATION
    fun destinationAddress(): String = DESTINATION_ADDRESS

    /** Every photo URL (restaurant banners + menu items) — preloaded so cards show real art immediately. */
    fun imageUrls(): List<String> =
        (RESTAURANTS.map { it.imageUrl } + MENUS.values.flatten().map { it.imageUrl }).filter { it.isNotEmpty() }.distinct()
}
