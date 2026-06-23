package dev.kmpilot.food

import dev.kmpilot.food.domain.CartLine
import dev.kmpilot.food.domain.CartLogic
import dev.kmpilot.food.domain.MenuItem
import dev.kmpilot.food.domain.money
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Acceptance criteria for the cart/pricing engine — the Tier-1 money invariants, RED/GREEN. */
class CartLogicTest {
    private val burger = MenuItem("b1", "Classic Smash", "", 1095, "🍔")
    private val fries = MenuItem("b4", "Loaded Fries", "", 695, "🍟")

    @Test fun line_total_is_price_times_quantity() {                 // AC5
        assertEquals(1095 * 3, CartLine(burger, 3).totalCents)
    }

    @Test fun add_to_bag_increments_count_and_subtotal() {           // AC2
        val cart = CartLogic.add(emptyList(), burger)
        assertEquals(1, CartLogic.itemCount(cart))
        assertEquals(1095, CartLogic.subtotalCents(cart))
    }

    @Test fun adding_the_same_item_merges_quantity() {
        val cart = CartLogic.add(CartLogic.add(emptyList(), burger), burger)
        assertEquals(1, cart.size)
        assertEquals(2, CartLogic.itemCount(cart))
    }

    @Test fun quantity_multiplies_the_line() {                       // AC5
        val cart = CartLogic.setQty(CartLogic.add(emptyList(), fries), "b4", 4)
        assertEquals(695 * 4, CartLogic.subtotalCents(cart))
    }

    @Test fun setting_quantity_to_zero_removes_the_line() {
        val cart = CartLogic.setQty(CartLogic.add(emptyList(), fries), "b4", 0)
        assertTrue(cart.isEmpty())
    }

    @Test fun order_total_is_subtotal_plus_fee_to_the_cent() {       // AC6 (integer cents → no float drift)
        val cart = CartLogic.add(CartLogic.add(emptyList(), burger), fries)   // 1095 + 695 = 1790
        assertEquals(1790 + 199, CartLogic.totalCents(cart, 199))
    }

    @Test fun empty_cart_costs_nothing() {                           // no phantom delivery fee
        assertEquals(0, CartLogic.totalCents(emptyList(), 199))
    }

    @Test fun money_formats_to_the_cent() {
        assertEquals("$17.90", money(1790))
        assertEquals("$5.00", money(500))
        assertEquals("$0.05", money(5))
    }
}
