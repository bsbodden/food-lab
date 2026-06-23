package dev.kmpilot.food.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import dev.kmpilot.components.media.warmImages
import dev.kmpilot.food.data.FoodRepository
import dev.kmpilot.food.presentation.RootComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

/** The shared app UI — identical on Android, iOS, and the wasm preview. Each platform entrypoint calls App(root). */
@Composable
fun App(root: RootComponent) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFF2F9E44), background = Color(0xFFF6F7F9), surface = Color.White,
            onPrimary = Color.White, onBackground = Color(0xFF1A1D24), onSurface = Color(0xFF1A1D24),
        ),
    ) { RootContent(root) }
}

/**
 * Builds the root component (resumed lifecycle + a Main-dispatcher scope) and warms the food photos. Used by
 * every platform entrypoint. warmImages is a no-op off-wasm, so it is safe to call on every target.
 */
fun buildRoot(scope: CoroutineScope = CoroutineScope(Dispatchers.Main)): RootComponent {
    val lifecycle = LifecycleRegistry()
    val repo = FoodRepository()
    val root = RootComponent(DefaultComponentContext(lifecycle), scope, repo)
    lifecycle.resume()
    warmImages(scope, repo.imageUrls())
    return root
}
