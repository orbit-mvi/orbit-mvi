package com.babylon.orbit.state.generator.valueGenerator

import com.babylon.orbit.state.generator.ClassGenerator
import com.babylon.orbit.state.generator.contentWritter.ClassContentWritter
import com.babylon.orbit.state.generator.contentWritter.ContentWritter
import com.babylon.orbit.state.generator.isWrappedValue
import kotlin.reflect.KClass
import kotlin.reflect.KParameter

internal class ListValueGenerator : ValueGenerator {

    private val plainValueGenerator = PlainValueGenerator()

    override fun generate(parameter: KParameter, instance: Any): String {
        val listInstance = instance as List<*>

        val result = when {
            listInstance.isEmpty() -> EMPTY_LIST
            listInstance.first()!!::class.isWrappedValue -> String.format(LIST_OF, listInstance.toListOfString)
            else -> generateClassInstance(listInstance)
        }

        return "${parameter.name} = $result"
    }

    private fun generateClassInstance(listInstance: List<*>): String {
        return listInstance.filterNotNull().joinToString(",") {
            ClassGenerator.of(it, ListContentWritter()).generateClass()
        }
        .let { String.format(LIST_OF, it) }
    }

    private val List<*>.toListOfString: String
        get() = this.filterNotNull().joinToString(",") { plainValueGenerator.generate(it) }

    private companion object {
        const val EMPTY_LIST = "emptyListOf()"
        const val LIST_OF = "listOf(%s)"

    }

    private class ListContentWritter : ContentWritter by ClassContentWritter() {
        override fun before(clazz: KClass<*>): String {
            return "\n${clazz.java.simpleName}("
        }

        override fun after(clazz: KClass<*>): String {
            return ")"
        }
    }
}