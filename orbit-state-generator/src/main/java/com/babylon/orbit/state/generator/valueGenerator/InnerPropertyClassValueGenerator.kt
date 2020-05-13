package com.babylon.orbit.state.generator.valueGenerator

import com.babylon.orbit.state.generator.ClassGenerator
import com.babylon.orbit.state.generator.contentWritter.ClassContentWritter
import com.babylon.orbit.state.generator.contentWritter.ContentWritter
import com.babylon.orbit.state.generator.isNestedSealedClass
import com.babylon.orbit.state.generator.parentSealedClass
import kotlin.reflect.KClass
import kotlin.reflect.KParameter

internal class InnerPropertyClassValueGenerator : ValueGenerator {

    override fun generate(parameter: KParameter, instance: Any): String {
        val clazz = instance.javaClass.kotlin

        return if (clazz.objectInstance != null) {
            val parentSealedClass = clazz.parentSealedClass
            val isNestedSealedClass = parentSealedClass != null && clazz.isNestedSealedClass(parentSealedClass)

            if (isNestedSealedClass) {
                "${parameter.name} = ${parentSealedClass!!.java.simpleName}.${clazz.java.simpleName}"
            } else {
                "${parameter.name} = ${clazz.java.simpleName}"
            }
        } else {
            val innerPropertyClass = ClassGenerator.of(instance, ClassContentWritterNoNewEndLine()).generateClass()
            "${parameter.name} = $innerPropertyClass"
        }
    }

    private class ClassContentWritterNoNewEndLine : ContentWritter by ClassContentWritter() {

        override fun after(clazz: KClass<*>): String {
            return ")"
        }

    }
}