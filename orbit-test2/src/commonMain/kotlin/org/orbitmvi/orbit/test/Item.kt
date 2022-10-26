package org.orbitmvi.orbit.test

public sealed class Item<STATE : Any, SIDE_EFFECT : Any> {
    public data class StateItem<STATE : Any, SIDE_EFFECT : Any>(val value: STATE) : Item<STATE, SIDE_EFFECT>()
    public data class SideEffectItem<STATE : Any, SIDE_EFFECT : Any>(val value: SIDE_EFFECT) : Item<STATE, SIDE_EFFECT>()
}
