package dev.kmpilot.food.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.kmpilot.food.domain.CartLine
import dev.kmpilot.food.domain.CartLogic
import dev.kmpilot.food.domain.money
import dev.kmpilot.food.presentation.CartComponent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(c: CartComponent) {
    val cart by c.cart.collectAsState()
    val subtotal = CartLogic.subtotalCents(cart)
    val total = CartLogic.totalCents(cart, c.deliveryFeeCents)
    val accent = Color(c.restaurant?.accent ?: 0xFF2F9E44)
    Scaffold(
        containerColor = Bg,
        topBar = {
            TopAppBar(
                title = { Text("Your bag", fontWeight = FontWeight.Bold, color = Ink) },
                navigationIcon = { IconButton(onClick = c.onBack, modifier = Modifier.testTag("back")) { Icon(Icons.Filled.ArrowBack, "Back", tint = Ink) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Bg),
            )
        },
        bottomBar = {
            Button(
                onClick = c.onPlaceOrder, enabled = cart.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = accent),
                modifier = Modifier.fillMaxWidth().padding(16.dp).height(52.dp).testTag("place_order"),
            ) { Text("Place order  ·  ${money(total)}", fontWeight = FontWeight.Bold) }
        },
    ) { pad ->
        Column(Modifier.fillMaxSize().padding(pad).padding(horizontal = 16.dp).verticalScroll(rememberScrollState()).testTag("cart")) {
            c.restaurant?.let { Text(it.name, fontWeight = FontWeight.Bold, color = Ink, modifier = Modifier.padding(vertical = 8.dp)) }
            cart.forEach { line -> CartLineRow(line) { q -> c.onSetQty(line.item.id, q) } }
            Spacer(Modifier.height(12.dp)); HorizontalDivider(color = Hair); Spacer(Modifier.height(12.dp))
            SummaryRow("Subtotal", money(subtotal))
            SummaryRow("Delivery", if (c.deliveryFeeCents == 0) "Free" else money(c.deliveryFeeCents))
            Spacer(Modifier.height(6.dp))
            SummaryRow("Total", money(total), bold = true)
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun CartLineRow(line: CartLine, onSetQty: (Int) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp).testTag("line_${line.item.id}"), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(line.item.name, fontWeight = FontWeight.SemiBold, color = Ink)
            Text(money(line.item.priceCents) + " each", color = Dim, style = MaterialTheme.typography.bodySmall)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { onSetQty(line.qty - 1) }, modifier = Modifier.size(32.dp).testTag("dec_${line.item.id}")) { Icon(Icons.Filled.Remove, "−", tint = Ink) }
            Text("${line.qty}", color = Ink, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 8.dp))
            IconButton(onClick = { onSetQty(line.qty + 1) }, modifier = Modifier.size(32.dp).testTag("inc_${line.item.id}")) { Icon(Icons.Filled.Add, "+", tint = Ink) }
        }
        Text(money(line.totalCents), color = Ink, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 10.dp))
    }
}

@Composable
private fun SummaryRow(label: String, value: String, bold: Boolean = false) {
    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = if (bold) Ink else Dim, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal)
        Text(value, color = Ink, fontWeight = if (bold) FontWeight.Bold else FontWeight.SemiBold)
    }
}
