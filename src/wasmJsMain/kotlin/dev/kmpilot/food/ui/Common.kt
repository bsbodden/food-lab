package dev.kmpilot.food.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// light food-delivery palette
val Bg = Color(0xFFF6F7F9)
val Card = Color(0xFFFFFFFF)
val Ink = Color(0xFF1A1D24)
val Dim = Color(0xFF7A8190)
val Hair = Color(0xFFEDEFF3)

/** A food "image" stand-in — a monogram on the accent gradient (Skiko/wasm has no emoji font, so no emoji). */
@Composable
fun FoodTile(accent: Long, label: String, modifier: Modifier = Modifier, corner: Int = 12, emojiSize: Int = 30) {
    val c = Color(accent)
    Box(
        modifier.clip(RoundedCornerShape(corner.dp)).background(
            Brush.linearGradient(listOf(c, c.copy(alpha = 0.65f).compositeOver(Color.White))),
        ),
        contentAlignment = Alignment.Center,
    ) { Text(label.trim().take(1).uppercase(), color = Color.White, style = TextStyle(fontSize = emojiSize.sp, fontWeight = FontWeight.Bold)) }
}
