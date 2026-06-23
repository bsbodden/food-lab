package dev.kmpilot.food.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import dev.kmpilot.components.media.AsyncImage
import dev.kmpilot.food.domain.CartLogic
import dev.kmpilot.food.domain.MenuItem
import dev.kmpilot.food.domain.money
import dev.kmpilot.food.presentation.MenuComponent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(c: MenuComponent) {
    val cart by c.cart.collectAsState()
    val count = CartLogic.itemCount(cart)
    val subtotal = CartLogic.subtotalCents(cart)
    val accent = Color(c.restaurant?.accent ?: 0xFF2F9E44)
    Scaffold(
        containerColor = Bg,
        topBar = {
            TopAppBar(
                title = { Text(c.restaurant?.name ?: "Menu", fontWeight = FontWeight.Bold, color = Ink) },
                navigationIcon = { IconButton(onClick = c.onBack, modifier = Modifier.testTag("back")) { Icon(Icons.Filled.ArrowBack, "Back", tint = Ink) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Bg),
            )
        },
        bottomBar = {
            if (count > 0) Row(
                Modifier.fillMaxWidth().padding(16.dp).clip(RoundedCornerShape(12.dp)).background(accent)
                    .clickable(onClick = c.onViewBag).padding(16.dp).testTag("view_bag"),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("View bag", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text("$count item${if (count == 1) "" else "s"} · ${money(subtotal)}", color = Color.White, fontWeight = FontWeight.SemiBold)
            }
        },
    ) { pad ->
        val accentLong = c.restaurant?.accent ?: 0xFF2F9E44L
        LazyColumn(Modifier.fillMaxSize().padding(pad).padding(horizontal = 16.dp).testTag("menu")) {
            items(c.menu, key = { it.id }) { item -> MenuRow(item, accentLong) { c.onAdd(item) } }
        }
    }
}

@Composable
private fun MenuRow(item: MenuItem, accent: Long, onAdd: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(item.name, fontWeight = FontWeight.SemiBold, color = Ink, style = MaterialTheme.typography.titleSmall)
            Text(item.desc, color = Dim, style = MaterialTheme.typography.bodySmall)
            Text(money(item.priceCents), color = Ink, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
        }
        AsyncImage(item.imageUrl, Modifier.size(64.dp).clip(RoundedCornerShape(12.dp))) {
            FoodTile(accent, item.name, Modifier.size(64.dp), corner = 12, emojiSize = 26)
        }
        FilledTonalButton(onClick = onAdd, modifier = Modifier.padding(start = 8.dp).testTag("add_${item.id}"), contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)) {
            Icon(Icons.Filled.Add, "Add", modifier = Modifier.size(18.dp)); Text("Add")
        }
    }
}
