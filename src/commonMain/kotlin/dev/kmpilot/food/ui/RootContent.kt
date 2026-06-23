package dev.kmpilot.food.ui

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.extensions.compose.stack.Children
import dev.kmpilot.food.presentation.RootComponent

@Composable
fun RootContent(root: RootComponent) {
    Children(stack = root.stack) { child ->
        when (val i = child.instance) {
            is RootComponent.Child.Restaurants -> RestaurantsScreen(i.component)
            is RootComponent.Child.Menu -> MenuScreen(i.component)
            is RootComponent.Child.Cart -> CartScreen(i.component)
            is RootComponent.Child.Tracking -> TrackingScreen(i.component)
        }
    }
}
