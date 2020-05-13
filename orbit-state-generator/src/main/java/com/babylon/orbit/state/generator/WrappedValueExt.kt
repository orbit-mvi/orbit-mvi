package com.babylon.orbit.state.generator

import kotlin.reflect.KClass

internal val KClass<*>.isWrappedValue: Boolean
    get() {
        return this == Boolean::class ||
        this == Int::class ||
        this == Char::class ||
        this == Byte::class ||
        this == Short::class ||
        this == Double::class ||
        this == Long::class ||
        this == Float::class ||
        this == String::class ||
        java.isEnum
    }