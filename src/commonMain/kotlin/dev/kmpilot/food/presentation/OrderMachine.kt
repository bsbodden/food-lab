package dev.kmpilot.food.presentation

import dev.kmpilot.food.domain.DeliveryStatus
import dev.kmpilot.food.runtime.ChartSpec
import dev.kmpilot.food.runtime.StateSpec
import dev.kmpilot.food.runtime.TransitionSpec
import dev.kmpilot.food.runtime.publishChartSpec
import dev.kmpilot.food.runtime.publishScreenState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ru.nsk.kstatemachine.event.*
import ru.nsk.kstatemachine.state.*
import ru.nsk.kstatemachine.statemachine.*
import ru.nsk.kstatemachine.transition.*

/**
 * The order/delivery lifecycle as a STATECHART (the live-track screen's machine). The courier telemetry is the
 * source of truth: `advanceTo(status)` walks the machine FORWARD to the reported status; backward reports are
 * no-ops (an order never un-delivers). Confirmed → Preparing → PickedUp → OnTheWay → Arriving → Delivered.
 */
class OrderMachine(private val scope: CoroutineScope) {

    private val _status = MutableStateFlow(DeliveryStatus.Confirmed)
    val status: StateFlow<DeliveryStatus> = _status.asStateFlow()

    private lateinit var machine: StateMachine

    private object Prepare : Event
    private object Pickup : Event
    private object Depart : Event
    private object Approach : Event
    private object Deliver : Event

    companion object {
        val CHART = ChartSpec(
            id = "Tracking", initial = "Confirmed",
            states = listOf(
                StateSpec("Confirmed", "content"), StateSpec("Preparing", "loading"), StateSpec("PickedUp", "content"),
                StateSpec("OnTheWay", "content"), StateSpec("Arriving", "content"), StateSpec("Delivered", "empty"),
            ),
            transitions = listOf(
                TransitionSpec("Confirmed", "Preparing", "Prepare"),
                TransitionSpec("Preparing", "PickedUp", "Pickup"),
                TransitionSpec("PickedUp", "OnTheWay", "Depart"),
                TransitionSpec("OnTheWay", "Arriving", "Approach"),
                TransitionSpec("Arriving", "Delivered", "Deliver"),
            ),
        )
    }

    suspend fun start() {
        machine = createStateMachine(scope, name = "Tracking") {
            val confirmed = initialState("Confirmed")
            val preparing = state("Preparing")
            val pickedUp = state("PickedUp")
            val onTheWay = state("OnTheWay")
            val arriving = state("Arriving")
            val delivered = state("Delivered")
            confirmed { transition<Prepare> { targetState = preparing; onTriggered { set(DeliveryStatus.Preparing) } } }
            preparing { transition<Pickup> { targetState = pickedUp; onTriggered { set(DeliveryStatus.PickedUp) } } }
            pickedUp { transition<Depart> { targetState = onTheWay; onTriggered { set(DeliveryStatus.OnTheWay) } } }
            onTheWay { transition<Approach> { targetState = arriving; onTriggered { set(DeliveryStatus.Arriving) } } }
            arriving { transition<Deliver> { targetState = delivered; onTriggered { set(DeliveryStatus.Delivered) } } }
        }
        publishChartSpec(Json.encodeToString(CHART))
        publishScreenState("Tracking", "Confirmed")
    }

    private fun set(s: DeliveryStatus) { _status.value = s; publishScreenState("Tracking", s.name) }

    /** Walk the machine forward to [target]; a backward target is a no-op. */
    suspend fun advanceTo(target: DeliveryStatus) {
        while (_status.value.ordinal < target.ordinal) {
            when (_status.value) {
                DeliveryStatus.Confirmed -> machine.processEvent(Prepare)
                DeliveryStatus.Preparing -> machine.processEvent(Pickup)
                DeliveryStatus.PickedUp -> machine.processEvent(Depart)
                DeliveryStatus.OnTheWay -> machine.processEvent(Approach)
                DeliveryStatus.Arriving -> machine.processEvent(Deliver)
                DeliveryStatus.Delivered -> return
            }
        }
    }
}
