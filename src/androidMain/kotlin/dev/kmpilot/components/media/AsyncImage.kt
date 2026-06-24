package dev.kmpilot.components.media

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import kotlinx.coroutines.CoroutineScope

/**
 * Android `actual` — real remote-image loading via Coil3. [SubcomposeAsyncImage] is used (not the plain
 * `AsyncImage`) because it is the only Coil composable that exposes `@Composable` loading/error slots, so the
 * existing [fallback] monogram tile shows while loading and on failure.
 */
@Composable
actual fun AsyncImage(url: String, modifier: Modifier, fallback: @Composable () -> Unit) {
    coil3.compose.SubcomposeAsyncImage(
        model = url,
        contentDescription = null,
        modifier = modifier,
        contentScale = ContentScale.Crop,
        loading = { fallback() },
        error = { fallback() },
    )
}

/** Android `actual` — Coil self-caches, so warming is a no-op here. */
actual fun warmImages(scope: CoroutineScope, urls: List<String>) { /* no-op: Coil self-caches */ }
