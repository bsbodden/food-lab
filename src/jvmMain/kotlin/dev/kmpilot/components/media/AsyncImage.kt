package dev.kmpilot.components.media

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.coroutines.CoroutineScope

/** jvm test `actual` — no network in the unit-test harness; render the [fallback] tile. */
@Composable
actual fun AsyncImage(url: String, modifier: Modifier, fallback: @Composable () -> Unit) { fallback() }

/** jvm test `actual` — image warming is a wasm concern; no-op here. */
actual fun warmImages(scope: CoroutineScope, urls: List<String>) { /* no-op */ }
