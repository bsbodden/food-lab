package dev.kmpilot.components.media

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.coroutines.CoroutineScope

/**
 * iOS `actual` — renders the [fallback] monogram tile for now. Real AVFoundation/Coil-Multiplatform loading is
 * a FOLLOW-UP; the placeholder keeps the funnel intact on the real target.
 */
@Composable
actual fun AsyncImage(url: String, modifier: Modifier, fallback: @Composable () -> Unit) { fallback() }

/** iOS `actual` — image warming is a wasm concern; no-op here. */
actual fun warmImages(scope: CoroutineScope, urls: List<String>) { /* no-op */ }
