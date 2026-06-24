package dev.kmpilot.food.presentation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.pushNew
import com.arkivanov.decompose.router.stack.replaceAll
import com.arkivanov.decompose.value.Value
import dev.kmpilot.components.location.LocationProvider
import dev.kmpilot.food.data.FoodRepository
import dev.kmpilot.food.domain.CartLine
import dev.kmpilot.food.domain.CartLogic
import dev.kmpilot.food.domain.DeliveryStatus
import dev.kmpilot.food.domain.LatLng
import dev.kmpilot.food.domain.MenuItem
import dev.kmpilot.food.domain.Restaurant
import dev.kmpilot.food.domain.Telemetry
import dev.kmpilot.food.runtime.ChartSpec
import dev.kmpilot.food.runtime.StateSpec
import dev.kmpilot.food.runtime.TransitionSpec
import dev.kmpilot.food.runtime.publishAppGraph
import dev.kmpilot.food.runtime.publishCurrentScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class RestaurantsComponent(
    ctx: ComponentContext, repo: FoodRepository,
    val locatedAt: StateFlow<LatLng?>, val onLocate: () -> Unit, val onOpen: (String) -> Unit,
) : ComponentContext by ctx {
    val restaurants: List<Restaurant> = repo.restaurants()
}

class MenuComponent(
    ctx: ComponentContext, repo: FoodRepository, id: String,
    val cart: StateFlow<List<CartLine>>, val onAdd: (MenuItem) -> Unit, val onBack: () -> Unit, val onViewBag: () -> Unit,
) : ComponentContext by ctx {
    val restaurant: Restaurant? = repo.restaurant(id)
    val menu: List<MenuItem> = repo.menu(id)
}

class CartComponent(
    ctx: ComponentContext, val restaurant: Restaurant?,
    val cart: StateFlow<List<CartLine>>, val onSetQty: (String, Int) -> Unit, val onPlaceOrder: () -> Unit, val onBack: () -> Unit,
) : ComponentContext by ctx {
    val deliveryFeeCents: Int = restaurant?.deliveryFeeCents ?: 0
}

class TrackingComponent(
    ctx: ComponentContext, val restaurant: Restaurant?,
    val status: StateFlow<DeliveryStatus>, val telemetry: StateFlow<Telemetry?>,
    val route: List<LatLng>, val destination: LatLng, val onDone: () -> Unit,
) : ComponentContext by ctx

class RootComponent(
    ctx: ComponentContext,
    private val scope: CoroutineScope,
    private val repo: FoodRepository,
) : ComponentContext by ctx {

    private val _cart = MutableStateFlow<List<CartLine>>(emptyList())
    val cart: StateFlow<List<CartLine>> = _cart.asStateFlow()
    private var currentRestaurant: Restaurant? = null

    private val order = OrderMachine(scope)
    val orderStatus: StateFlow<DeliveryStatus> = order.status
    private val _telemetry = MutableStateFlow<Telemetry?>(null)
    val telemetry: StateFlow<Telemetry?> = _telemetry.asStateFlow()

    // device location via the location/LocationProvider component — "locate me" resolves the real GPS fix
    private val location = LocationProvider()
    private val _locatedAt = MutableStateFlow<LatLng?>(null)
    val locatedAt: StateFlow<LatLng?> = _locatedAt.asStateFlow()
    fun locateMe() { scope.launch { _locatedAt.value = location.current() } }

    private val nav = StackNavigation<Config>()
    val stack: Value<ChildStack<Config, Child>> = childStack(
        source = nav, serializer = Config.serializer(), initialConfiguration = Config.Restaurants,
        handleBackButton = true, childFactory = ::child,
    )

    init {
        scope.launch { order.start() }
        publishAppGraph(Json.encodeToString(APP_GRAPH))
        stack.subscribe { childStack -> publishCurrentScreen(childStack.active.configuration.screenName()) }
    }

    /** A telemetry frame from the emulator — drives the order forward (forward-only) + exposes the live frame. */
    fun onTelemetry(t: Telemetry) { _telemetry.value = t; scope.launch { order.advanceTo(t.status) } }

    private fun addToCart(item: MenuItem) { _cart.value = CartLogic.add(_cart.value, item) }
    private fun placeOrder() { _telemetry.value = null; nav.pushNew(Config.Tracking) }
    private fun ensureRestaurant() { if (currentRestaurant == null) currentRestaurant = repo.restaurants().first() }

    companion object {
        val APP_GRAPH = ChartSpec(
            id = "App", initial = "Restaurants",
            states = listOf(
                StateSpec("Restaurants", "list"), StateSpec("Menu", "detail"),
                StateSpec("Cart", "form"), StateSpec("Tracking", "detail"),
            ),
            transitions = listOf(
                TransitionSpec("Restaurants", "Menu", "Open"),
                TransitionSpec("Menu", "Cart", "View bag"),
                TransitionSpec("Menu", "Restaurants", "Back"),
                TransitionSpec("Cart", "Tracking", "Place order"),
                TransitionSpec("Cart", "Menu", "Back"),
                TransitionSpec("Tracking", "Restaurants", "Done"),
            ),
        )
    }

    private fun child(config: Config, childCtx: ComponentContext): Child = when (config) {
        Config.Restaurants -> Child.Restaurants(RestaurantsComponent(childCtx, repo, locatedAt, ::locateMe, onOpen = {
            currentRestaurant = repo.restaurant(it); nav.pushNew(Config.Menu(it))
        }))
        is Config.Menu -> Child.Menu(MenuComponent(childCtx, repo, config.id, cart, onAdd = ::addToCart,
            onBack = { nav.pop() }, onViewBag = { nav.pushNew(Config.Cart) }))
        Config.Cart -> Child.Cart(CartComponent(childCtx, currentRestaurant, cart,
            onSetQty = { id, q -> _cart.value = CartLogic.setQty(_cart.value, id, q) }, onPlaceOrder = ::placeOrder, onBack = { nav.pop() }))
        Config.Tracking -> Child.Tracking(TrackingComponent(childCtx, currentRestaurant, orderStatus, telemetry,
            repo.route(currentRestaurant?.id ?: ""), repo.destination(),
            onDone = { _cart.value = emptyList(); nav.replaceAll(Config.Restaurants) }))
    }

    fun navigateTo(screen: String) {
        when (screen) {
            "Restaurants" -> nav.replaceAll(Config.Restaurants)
            "Menu" -> { val r = repo.restaurants().first(); currentRestaurant = r; nav.replaceAll(Config.Restaurants, Config.Menu(r.id)) }
            "Cart" -> { ensureRestaurant(); nav.replaceAll(Config.Restaurants, Config.Cart) }
            // direct nav (studio click-to-navigate / app-graph) may skip the menu — guarantee a restaurant so
            // the Tracking screen has a real route + accent (else repo.route("") is empty → no route/courier).
            "Tracking" -> { ensureRestaurant(); nav.replaceAll(Config.Restaurants, Config.Tracking) }
        }
    }

    @Serializable
    sealed interface Config {
        @Serializable data object Restaurants : Config
        @Serializable data class Menu(val id: String) : Config
        @Serializable data object Cart : Config
        @Serializable data object Tracking : Config
    }

    sealed interface Child {
        class Restaurants(val component: RestaurantsComponent) : Child
        class Menu(val component: MenuComponent) : Child
        class Cart(val component: CartComponent) : Child
        class Tracking(val component: TrackingComponent) : Child
    }

    private fun Config.screenName(): String = when (this) {
        Config.Restaurants -> "Restaurants"; is Config.Menu -> "Menu"; Config.Cart -> "Cart"; Config.Tracking -> "Tracking"
    }
}
