package dev.kmpilot.food.runtime

// The editor bridge is a wasm-preview concern only; on the real Android target it is a no-op.
actual fun publishScreenState(label: String, state: String) { /* no-op */ }
actual fun publishChartSpec(json: String) { /* no-op */ }
actual fun publishAppGraph(json: String) { /* no-op */ }
actual fun publishCurrentScreen(name: String) { /* no-op */ }
