package com.babylon.orbit.state.generator.contentWritter

import com.babylon.orbit.state.generator.isNestedSealedClass
import com.babylon.orbit.state.generator.parentSealedClass
import kotlin.reflect.KClass

internal class ClassContentWritter : ContentWritter {

    override fun before(clazz: KClass<*>): String {
        val parentSealedClass = clazz.parentSealedClass
        return if (parentSealedClass != null && clazz.isNestedSealedClass(parentSealedClass)) {
            "${parentSealedClass.java.simpleName}.${clazz.java.simpleName}("
        } else {
            "${clazz.java.simpleName}("
        }
    }

    override fun mainContent(properties: List<String>): String {
        return properties.joinToString(",\n")
    }

    override fun after(clazz: KClass<*>): String {
        return "\n)"
    }

}