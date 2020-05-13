package com.babylon.orbit.state.generator

import com.babylon.orbit.state.generator.contentWritter.ClassContentWritter
import com.babylon.orbit.state.generator.contentWritter.ContentWritter
import com.babylon.orbit.state.generator.valueGenerator.InnerPropertyClassValueGenerator
import com.babylon.orbit.state.generator.valueGenerator.ListValueGenerator
import com.babylon.orbit.state.generator.valueGenerator.WrappedValueGenerator
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.jvm.jvmErasure

internal class ClassGenerator private constructor(
    private val clazz: KClass<*>,
    private val instance: Any,
    private val contentWritter: ContentWritter
) {

    private val wrappedValueGenerator =
        WrappedValueGenerator()
    private val innerPropertyClassValueGenerator =
        InnerPropertyClassValueGenerator()

    private val listValueGenerator = ListValueGenerator()

    fun generateClass(): String {

        if (clazz.isAbstract || clazz.isCompanion) {
            throw IllegalArgumentException(
                "Abstract classes or companion objects are not supported" +
                        "for class with type ${clazz.java.canonicalName}"
            )
        }

        val fixtureAnalyser = FixtureAnalyser.of(instance)

        val kotlinClassDescriptor = fixtureAnalyser.analiseClass()

        val stringBuilder = StringBuilder()

        stringBuilder.appendln(
            contentWritter.before(clazz)
        )

        val requiredProperties = generateProperties(kotlinClassDescriptor.requiredProperties)
        if (requiredProperties.isNotEmpty()) {
            stringBuilder.append(
                contentWritter.mainContent(requiredProperties)
            )
        }

        stringBuilder.append(
            contentWritter.after(clazz)
        )

        return stringBuilder.toString()
    }

    private fun generateProperties(properties: Collection<KParameter>): List<String> {
        return properties.mapNotNull { property ->
            if (property.type.jvmErasure.isWrappedValue) {
                wrappedValueGenerator.generate(property, property.propertyValue(clazz, instance)!!)
            } else if (property.type.jvmErasure == List::class) {
                listValueGenerator.generate(property, property.propertyValue(clazz, instance)!!)
            }
            else {
                innerPropertyClassValueGenerator.generate(property, property.propertyValue(clazz, instance)!!)
            }
        }
    }

    companion object {

        fun of(instance: Any, contentWritter: ContentWritter = ClassContentWritter()): ClassGenerator {
            val kotlinClass = instance.javaClass.kotlin
            return ClassGenerator(clazz = kotlinClass, instance = instance, contentWritter = contentWritter)
        }
    }
}