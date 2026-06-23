package dev.kmpilot.food.domain

/**
 * The cart/pricing engine — pure functions over integer cents (no float drift). This is the Tier-1 money
 * "meat": line total = price × qty, subtotal = Σ lines, order total = subtotal + delivery fee. Every rule
 * here is a RED/GREEN acceptance criterion (see CartLogicTest).
 */
object CartLogic {

    /** Add [qty] of [item] to the cart; merges with an existing line. */
    fun add(lines: List<CartLine>, item: MenuItem, qty: Int = 1): List<CartLine> {
        if (qty <= 0) return lines
        return if (lines.any { it.item.id == item.id })
            lines.map { if (it.item.id == item.id) it.copy(qty = it.qty + qty) else it }
        else lines + CartLine(item, qty)
    }

    /** Set a line's quantity; qty ≤ 0 removes the line. */
    fun setQty(lines: List<CartLine>, itemId: String, qty: Int): List<CartLine> =
        if (qty <= 0) lines.filterNot { it.item.id == itemId }
        else lines.map { if (it.item.id == itemId) it.copy(qty = qty) else it }

    fun itemCount(lines: List<CartLine>): Int = lines.sumOf { it.qty }

    fun subtotalCents(lines: List<CartLine>): Int = lines.sumOf { it.totalCents }

    /** Order total = subtotal + delivery fee, to the cent. An empty cart costs nothing (no phantom fee). */
    fun totalCents(lines: List<CartLine>, deliveryFeeCents: Int): Int =
        if (lines.isEmpty()) 0 else subtotalCents(lines) + deliveryFeeCents
}
