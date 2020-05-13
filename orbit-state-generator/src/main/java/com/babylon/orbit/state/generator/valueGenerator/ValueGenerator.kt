package com.babylon.orbit.state.generator.valueGenerator

import kotlin.reflect.KParameter

internal interface ValueGenerator {

    fun generate(parameter: KParameter, instance: Any): String

}