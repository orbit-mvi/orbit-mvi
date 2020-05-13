package com.babylon.orbit.state.generator.valueGenerator

import kotlin.reflect.KParameter

internal class WrappedValueGenerator : ValueGenerator {

    private val plainValueGenerator = PlainValueGenerator()

    override fun generate(parameter: KParameter, instance: Any): String {
        val parameterName = parameter.name!!
        return "$parameterName = ${plainValueGenerator.generate(instance)}"
    }
}