package com.babylon.orbit.state.generator.contentWritter

import kotlin.reflect.KClass

interface ContentWritter {

    fun before(clazz: KClass<*>): String

    fun mainContent(properties: List<String>): String

    fun after(clazz: KClass<*>): String
}