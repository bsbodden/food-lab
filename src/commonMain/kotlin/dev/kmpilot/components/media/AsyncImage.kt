package dev.kmpilot.components.media

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.coroutines.CoroutineScope

/**
 * KMPilot component — **AsyncImage**: one shared interface, a per-platform adapter behind it.
 *
 * Loads a remote image and renders it (cover-cropped); shows [fallback] until ready, or if it fails.
 *  - **wasm preview** → browser `fetch` + Skia decode (CORS-open hosts only); decoded bitmaps cached by URL.
 *  - **Android / iOS / jvm** → renders [fallback] (the monogram tile). Real Coil/AVFoundation loading is a
 *    FOLLOW-UP; the placeholder keeps the funnel compiling + visually intact on the real targets.
 */
@Composable
expect fun AsyncImage(url: String, modifier: Modifier = Modifier, fallback: @Composable () -> Unit = {})

/** Warm the cache for a set of images (call at startup) so cards show real photos immediately. No-op off-wasm. */
expect fun warmImages(scope: CoroutineScope, urls: List<String>)
