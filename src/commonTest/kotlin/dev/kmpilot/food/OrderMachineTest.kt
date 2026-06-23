package dev.kmpilot.food

import dev.kmpilot.food.domain.DeliveryStatus
import dev.kmpilot.food.presentation.OrderMachine
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** Acceptance criteria for the order/delivery lifecycle — driven forward-only by courier telemetry. */
class OrderMachineTest {

    @Test fun starts_confirmed() = runTest {
        val m = OrderMachine(backgroundScope); m.start()
        assertEquals(DeliveryStatus.Confirmed, m.status.value)
    }

    @Test fun advances_through_the_full_lifecycle() = runTest {
        val m = OrderMachine(backgroundScope); m.start()
        m.advanceTo(DeliveryStatus.Delivered)
        assertEquals(DeliveryStatus.Delivered, m.status.value)
    }

    @Test fun reaches_each_status_in_order() = runTest {
        val m = OrderMachine(backgroundScope); m.start()
        m.advanceTo(DeliveryStatus.Preparing); assertEquals(DeliveryStatus.Preparing, m.status.value)
        m.advanceTo(DeliveryStatus.PickedUp); assertEquals(DeliveryStatus.PickedUp, m.status.value)
        m.advanceTo(DeliveryStatus.OnTheWay); assertEquals(DeliveryStatus.OnTheWay, m.status.value)
        m.advanceTo(DeliveryStatus.Arriving); assertEquals(DeliveryStatus.Arriving, m.status.value)
    }

    @Test fun telemetry_cannot_move_an_order_backward() = runTest {
        val m = OrderMachine(backgroundScope); m.start()
        m.advanceTo(DeliveryStatus.OnTheWay)
        m.advanceTo(DeliveryStatus.Confirmed)   // a stale/backward report is a no-op
        assertEquals(DeliveryStatus.OnTheWay, m.status.value)
    }
}
