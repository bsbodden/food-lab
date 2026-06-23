package dev.kmpilot.food.runtime

/** Editor bridge — publishes the live state + structure to the host. Real on wasm; no-op on the jvm test harness. */
expect fun publishScreenState(label: String, state: String)
expect fun publishChartSpec(json: String)
expect fun publishAppGraph(json: String)
expect fun publishCurrentScreen(name: String)
