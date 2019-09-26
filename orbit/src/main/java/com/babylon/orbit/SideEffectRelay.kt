package com.babylon.orbit

interface SideEffectRelay<T : Any> {
    fun post(event: T)
}
