package dev.kmpilot.food.data

import dev.kmpilot.food.domain.LatLng
import dev.kmpilot.food.domain.MenuItem
import dev.kmpilot.food.domain.Restaurant

// Real dish/restaurant photos from TheMealDB (CC, CORS-open); /medium keeps the preload light (~40KB each).
private fun img(id: String) = "https://www.themealdb.com/images/media/meals/$id.jpg/medium"

/** The delivery destination — a real San Francisco address. Routes (Routes.kt) run from each restaurant here. */
internal const val DESTINATION_ADDRESS = "1100 Market St, San Francisco"
internal val DESTINATION = LatLng(37.7806, -122.4109)

internal val RESTAURANTS: List<Restaurant> = listOf(
    Restaurant("r_burg", "Patty Wagon", "Burgers · American", 4.7, 22, 199, 0xFFE8590C, "🍔",
        "451 Hayes St, San Francisco", LatLng(37.7766, -122.4244), img("13fg4j1764441982")),
    Restaurant("r_sushi", "Nori & Co.", "Sushi · Japanese", 4.8, 34, 299, 0xFF1971C2, "🍣",
        "1737 Buchanan St, San Francisco", LatLng(37.7855, -122.4298), img("1548772327")),
    Restaurant("r_pizza", "Slice House", "Pizza · Italian", 4.6, 28, 0, 0xFFE03131, "🍕",
        "532 Columbus Ave, San Francisco", LatLng(37.8003, -122.4090), img("usywpp1511189717")),
    Restaurant("r_taco", "El Fuego", "Tacos · Mexican", 4.5, 18, 149, 0xFF2F9E44, "🌮",
        "2889 Mission St, San Francisco", LatLng(37.7599, -122.4148), img("jgl9qq1764437635")),
    Restaurant("r_ramen", "Steam", "Ramen · Noodles", 4.9, 31, 249, 0xFF7048E8, "🍜",
        "5812 Geary Blvd, San Francisco", LatLng(37.7820, -122.4640), img("se5vhk1764114880")),
)

internal val MENUS: Map<String, List<MenuItem>> = mapOf(
    "r_burg" to listOf(
        MenuItem("b1", "Classic Smash", "Double smashed patty, cheese, house sauce", 1095, "🍔", img("lgmnff1763789847")),
        MenuItem("b2", "Bacon Stack", "Triple bacon, cheddar, crispy onions", 1395, "🥓", img("khpso71779732715")),
        MenuItem("b3", "Shroom Melt", "Swiss, sautéed mushrooms, garlic aioli", 1245, "🍔", img("uuuspp1511297945")),
        MenuItem("b4", "Loaded Fries", "Cheese, bacon bits, scallions", 695, "🍟", img("1550441882")),
        MenuItem("b5", "Chocolate Shake", "Hand-spun, real malt", 545, "🥤", img("tqtywx1468317395")),
    ),
    "r_sushi" to listOf(
        MenuItem("s1", "Salmon Nigiri (4)", "Wild-caught, sushi rice", 1200, "🍣", img("ikizdm1763760862")),
        MenuItem("s2", "Dragon Roll", "Eel, avocado, cucumber, unagi glaze", 1650, "🍱", img("g046bb1663960946")),
        MenuItem("s3", "Spicy Tuna Roll", "Tuna, sriracha mayo, tempura crunch", 1395, "🍣", img("yypwwq1511304979")),
        MenuItem("s4", "Edamame", "Sea salt, lightly steamed", 595, "🫛", img("9tddhg1764443699")),
        MenuItem("s5", "Miso Soup", "Tofu, wakame, scallion", 445, "🥣", img("x2fw9e1560460636")),
    ),
    "r_pizza" to listOf(
        MenuItem("p1", "Margherita", "San Marzano, fresh mozzarella, basil", 1495, "🍕", img("x0lk931587671540")),
        MenuItem("p2", "Pepperoni", "Cup-and-char pepperoni, aged mozz", 1695, "🍕", img("lrfdwz1764438393")),
        MenuItem("p3", "Garlic Knots (6)", "Butter, parmesan, parsley", 695, "🥖", img("lmc6r51764365554")),
        MenuItem("p4", "Caesar Salad", "Romaine, croutons, shaved parm", 895, "🥗", img("zry07j1763779321")),
        MenuItem("p5", "Tiramisu", "Espresso-soaked, mascarpone", 745, "🍰", img("wkhg581762773124")),
    ),
    "r_taco" to listOf(
        MenuItem("t1", "Al Pastor (3)", "Pork, pineapple, onion, cilantro", 1095, "🌮", img("lwsnkl1604181187")),
        MenuItem("t2", "Carne Asada Burrito", "Grilled steak, rice, beans, salsa", 1295, "🌯", img("8rfd4q1764112993")),
        MenuItem("t3", "Chips & Guac", "House guacamole, warm chips", 695, "🥑", img("rvtvuw1511190488")),
        MenuItem("t4", "Elote", "Grilled corn, cotija, chili-lime", 545, "🌽", img("vz94r81760534692")),
        MenuItem("t5", "Horchata", "Cinnamon rice milk", 395, "🥤", img("a4kgf21763075288")),
    ),
    "r_ramen" to listOf(
        MenuItem("n1", "Tonkotsu", "18-hr pork broth, chashu, egg", 1595, "🍜", img("ip5xtp1769779958")),
        MenuItem("n2", "Spicy Miso", "Ground pork, corn, scallion, chili oil", 1495, "🍜", img("prrirc1763781360")),
        MenuItem("n3", "Veg Shoyu", "Shiitake dashi, tofu, greens", 1395, "🍲", img("sqpqtp1515365614")),
        MenuItem("n4", "Gyoza (5)", "Pan-fried pork dumplings", 795, "🥟", img("uyqrrv1511553350")),
        MenuItem("n5", "Matcha Soft-serve", "Stone-ground matcha", 545, "🍦", img("1xscby1764790242")),
    ),
)
