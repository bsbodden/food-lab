package dev.kmpilot.food.runtime

import kotlinx.serialization.Serializable

/** The owned, serializable statechart IR — published by the running app so the editor renders the REAL machine. */
@Serializable
data class ChartSpec(
    val id: String,
    val initial: String,
    val states: List<StateSpec>,
    val transitions: List<TransitionSpec>,
)

@Serializable
data class StateSpec(val id: String, val kind: String)

@Serializable
data class TransitionSpec(val from: String, val to: String, val event: String)
