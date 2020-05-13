package com.babylon.orbit.state.generator.valueGenerator

import com.babylon.orbit.state.generator.simpleNameValue

internal class PlainValueGenerator {

    fun generate(instance: Any): String {
        val clazz = instance::class

        return if (PLAIN_TYPES.contains(instance::class)) {
            instance.toString()
        } else if (clazz == Float::class) {
            "${instance}f"
        } else if (clazz == String::class || clazz == Char::class) {
            "\"${instance}\""
        } else if(instance is Enum<*>) {
            instance.simpleNameValue
        } else {
            throw IllegalArgumentException("Unknown type for $clazz")
        }
    }

    private companion object {
        private val PLAIN_TYPES = listOf(
            Boolean::class,
            Long::class,
            Int::class,
            Byte::class,
            Short::class,
            Double::class
        )
    }
}