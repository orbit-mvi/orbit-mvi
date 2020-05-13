package com.babylon.orbit.state.generator

import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.memberProperties

fun KParameter.propertyValue(parentClazz: KClass<*>, instance: Any): Any? {
    val memberProperties = parentClazz.memberProperties
    val property = memberProperties.find { it.name == name }
        ?: throw IllegalArgumentException("No property found with name $name")
    return property.getter.call(instance)
}

val KClass<*>.parentSealedClass: KClass<*>?
    get() = supertypes
        .map { it.classifier as? KClass<*> }
        .find {
            it?.isSealed ?: false
        }

fun KClass<*>.isNestedSealedClass(parentSealedClass: KClass<*>): Boolean {
    return parentSealedClass.nestedClasses.contains(this)
}

val Enum<*>.simpleNameValue: String
    get() = {
        val simpleName = this::class.java.simpleName
        "$simpleName.$name"
    }()

