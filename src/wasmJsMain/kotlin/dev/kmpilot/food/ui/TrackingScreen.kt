package dev.kmpilot.food.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.kmpilot.components.map.MapMarker
import dev.kmpilot.components.map.MapView
import dev.kmpilot.components.map.MarkerKind
import dev.kmpilot.food.domain.DeliveryStatus
import dev.kmpilot.food.domain.Geo
import dev.kmpilot.food.domain.Telemetry
import dev.kmpilot.food.presentation.TrackingComponent

@Composable
fun TrackingScreen(c: TrackingComponent) {
    val status by c.status.collectAsState()
    val tel by c.telemetry.collectAsState()
    val accent = Color(c.restaurant?.accent ?: 0xFF2F9E44)
    val route = c.route
    val accentLong = c.restaurant?.accent ?: 0xFF2F9E44L
    val courierAt = if (route.size >= 2) Geo.pointAlong(route, (tel?.progress ?: 0f)) else null
    val markers = buildList {
        route.firstOrNull()?.let { add(MapMarker("pickup", it, 0xFF1F2937, MarkerKind.Pickup)) }
        (route.lastOrNull() ?: c.destination).let { add(MapMarker("drop", it, 0xFF16A34A, MarkerKind.Dropoff)) }
        courierAt?.let { add(MapMarker("courier", it, accentLong, MarkerKind.Courier)) }
    }
    val mapCenter = if (route.size >= 2) Geo.pointAlong(route, 0.5f) else c.destination
    Column(Modifier.fillMaxSize().background(Bg).testTag("tracking")) {
        MapView(center = mapCenter, zoom = 12.5, polyline = route, markers = markers,
            modifier = Modifier.fillMaxWidth().weight(1f))
        Column(Modifier.fillMaxWidth().background(Card).padding(20.dp)) {
            val eta = when {
                status == DeliveryStatus.Delivered -> "Delivered"
                tel == null -> "Confirming your order…"
                else -> "Arriving in ${tel!!.etaMin} min"
            }
            Text(eta, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Ink, modifier = Modifier.testTag("eta"))
            Text(status.label, color = Dim, modifier = Modifier.testTag("status"))
            Spacer(Modifier.height(16.dp))
            Timeline(stepOf(status), accent)
            Spacer(Modifier.height(16.dp))
            if (tel != null && status != DeliveryStatus.Delivered) DriverCard(tel!!, accent)
            if (status == DeliveryStatus.Delivered) {
                Button(onClick = c.onDone, colors = ButtonDefaults.buttonColors(containerColor = accent),
                    modifier = Modifier.fillMaxWidth().height(50.dp).testTag("done")) { Text("Order again", fontWeight = FontWeight.Bold) }
            } else if (tel == null) {
                Text("Start the delivery telemetry from the emulator's ⚙ Device panel.", color = Dim, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

private fun stepOf(s: DeliveryStatus) = when (s) {
    DeliveryStatus.Confirmed -> 0
    DeliveryStatus.Preparing -> 1
    DeliveryStatus.PickedUp, DeliveryStatus.OnTheWay, DeliveryStatus.Arriving -> 2
    DeliveryStatus.Delivered -> 3
}

@Composable
private fun Timeline(step: Int, accent: Color) {
    val labels = listOf("Confirmed", "Preparing", "On the way", "Delivered")
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            for (i in 0..3) {
                Box(Modifier.size(14.dp).clip(CircleShape).background(if (i <= step) accent else Hair))
                if (i < 3) Box(Modifier.weight(1f).height(3.dp).background(if (i < step) accent else Hair))
            }
        }
        Spacer(Modifier.height(6.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            labels.forEachIndexed { i, l -> Text(l, color = if (i <= step) Ink else Dim, style = MaterialTheme.typography.labelSmall) }
        }
    }
}

@Composable
private fun DriverCard(t: Telemetry, accent: Color) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        FoodTile(0xFF2B2F3A, t.driverName, Modifier.size(46.dp), corner = 23, emojiSize = 20)
        Column(Modifier.weight(1f).padding(start = 12.dp)) {
            Text(t.driverName, fontWeight = FontWeight.SemiBold, color = Ink)
            Text(t.vehicle, color = Dim, style = MaterialTheme.typography.bodySmall)
        }
        Text("Call · Chat", color = Dim, style = MaterialTheme.typography.labelMedium)
    }
}
