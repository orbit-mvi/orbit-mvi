package com.babylon.orbit.state.generator

import kotlin.reflect.KFunction
import kotlin.reflect.KParameter

data class KotlinClassDescriptor(
    val primaryConstructor: KFunction<*>,
    val requiredProperties: Collection<KParameter>
)