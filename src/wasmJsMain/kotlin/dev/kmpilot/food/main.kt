package dev.kmpilot.food

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import dev.kmpilot.food.domain.DeliveryStatus
import dev.kmpilot.food.domain.Telemetry
import dev.kmpilot.food.presentation.RootComponent
import dev.kmpilot.food.ui.App
import dev.kmpilot.food.ui.buildRoot
import kotlinx.browser.document
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Morsel — the food-delivery preview (the catalog's `preview` entry; renders like the Android/iOS app would).
 * The order/cart engine + delivery statechart are shared, tested commonMain; the live-track screen consumes
 * COURIER TELEMETRY streamed from the emulator over the sensor bridge.
 */
@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val scope = CoroutineScope(Dispatchers.Main)
    val root = buildRoot(scope)
    startBridgePosting() // stream the screen graph + live state to the editor (wasm preview only)
    startBridges(scope, root) // accept navigate commands + courier telemetry from the editor
    ComposeViewport(document.body!!) { App(root) }
}

@Serializable
private data class TelemetryDto(val progress: Float = 0f, val status: String = "Confirmed", val etaMin: Int = 0, val driverName: String = "", val vehicle: String = "")

private fun startBridges(scope: CoroutineScope, root: RootComponent) {
    installListener()
    scope.launch {
        var lastTel = ""
        while (true) {
            delay(150)
            val cmd = readPendingNav(); if (cmd.isNotEmpty()) { clearPendingNav(); root.navigateTo(cmd) }
            val tel = readTelemetry()
            if (tel.isNotEmpty() && tel != lastTel) {
                lastTel = tel
                runCatching {
                    val d = Json.decodeFromString<TelemetryDto>(tel)
                    Telemetry(d.progress, DeliveryStatus.valueOf(d.status), d.etaMin, d.driverName, d.vehicle)
                }.getOrNull()?.let { root.onTelemetry(it) }
            }
        }
    }
}

private fun startBridgePosting() {
    js("setInterval(function(){ try { if (window.parent && window.parent !== window) { window.parent.postMessage({ type: 'kmpilot', appGraph: globalThis.__appGraph, currentScreen: globalThis.__currentScreen, chartSpec: globalThis.__chartSpec, screen: globalThis.__screen }, '*'); } } catch (e) {} }, 400)")
}

private fun installListener() {
    js("window.addEventListener('message', function(e){ var d=e.data; if(!d) return; if (d.type==='kmpilot-cmd' && d.navigate) globalThis.__pendingNav = d.navigate; if (d.type==='kmpilot-sensor' && d.sensor==='telemetry') globalThis.__telemetry = JSON.stringify(d.value); })")
}

private fun readPendingNav(): String = js("(globalThis.__pendingNav || '')")
private fun clearPendingNav() { js("globalThis.__pendingNav = null") }
private fun readTelemetry(): String = js("(globalThis.__telemetry || '')")
