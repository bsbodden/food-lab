package dev.kmpilot.food.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import kotlin.math.round
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.testTag
import dev.kmpilot.components.media.AsyncImage
import dev.kmpilot.food.domain.Restaurant
import dev.kmpilot.food.domain.money
import dev.kmpilot.food.presentation.RestaurantsComponent

/** Round to 4 decimals (~11 m) for displaying a device GPS fix from commonMain (no String.format on KMP). */
private fun round4(d: Double): Double = round(d * 1e4) / 1e4

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantsScreen(c: RestaurantsComponent) {
    val located by c.locatedAt.collectAsState()
    Scaffold(
        containerColor = Bg,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(if (located == null) "Delivery to" else "Your location", style = MaterialTheme.typography.labelSmall, color = Dim)
                        Text(
                            located?.let { "📍 ${round4(it.lat)}, ${round4(it.lng)}" } ?: "Home · 1100 Market St",
                            fontWeight = FontWeight.Bold, color = Ink,
                        )
                    }
                },
                actions = {
                    IconButton(onClick = c.onLocate) {
                        Icon(Icons.Filled.MyLocation, contentDescription = "Use my location", tint = Ink)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Bg),
            )
        },
    ) { pad ->
        LazyColumn(Modifier.fillMaxSize().padding(pad).padding(horizontal = 16.dp).testTag("restaurants")) {
            items(c.restaurants, key = { it.id }) { r -> RestaurantCard(r) { c.onOpen(r.id) } }
        }
    }
}

@Composable
private fun RestaurantCard(r: Restaurant, onClick: () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(vertical = 8.dp).clip(RoundedCornerShape(14.dp)).background(Card).clickable(onClick = onClick).testTag("rest_${r.id}")) {
        AsyncImage(r.imageUrl, Modifier.fillMaxWidth().height(118.dp)) {
            FoodTile(r.accent, r.name, Modifier.fillMaxWidth().height(118.dp), corner = 0, emojiSize = 52)
        }
        Column(Modifier.padding(12.dp)) {
            Text(r.name, fontWeight = FontWeight.Bold, color = Ink, style = MaterialTheme.typography.titleMedium)
            Text(r.cuisine, color = Dim, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(4.dp))
            Row {
                Text("★ ${r.rating}", color = Ink, style = MaterialTheme.typography.bodySmall)
                Text("  ·  ${r.etaMin} min  ·  ${if (r.deliveryFeeCents == 0) "Free delivery" else money(r.deliveryFeeCents) + " delivery"}", color = Dim, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
