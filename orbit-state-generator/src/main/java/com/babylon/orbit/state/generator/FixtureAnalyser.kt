package com.babylon.orbit.state.generator

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.primaryConstructor

internal class FixtureAnalyser private constructor(
    private val clazz: KClass<*>,
    private val instance: Any
) {

    //private val gson = Gson()

    private val moshi = Moshi.Builder()
//        .add(PolymorphicJsonAdapterFactory())
        .build()

    fun analiseClass(): KotlinClassDescriptor {
        val primaryConstructor = clazz.primaryConstructor
            ?: throw IllegalArgumentException("No Primary ctor found for class $clazz")

        val optionalProperties = primaryConstructor.parameters.filter { it.isOptional }
        val mutatedProperties = findMutatedProperties2(optionalProperties, primaryConstructor)
        val requiredProperties = primaryConstructor.parameters
            .filterNot { it.isOptional } + mutatedProperties

        return KotlinClassDescriptor(
            primaryConstructor = primaryConstructor,
            requiredProperties = requiredProperties.sortedBy { it.index }
        )
    }


    private fun findMutatedProperties(optionalProperties: Collection<KParameter>): Collection<KParameter> {
        val jsonAdapter = moshi.adapter<Any>(clazz.java)
        val jsonObject = jsonAdapter.toJson(instance).let {
            JsonParser.parseString(it).asJsonObject
        }

        val propertyNameAndValue = mutableMapOf<KParameter, Any?>()
        optionalProperties.forEach { parameter ->
            // Remove all optional parameters
            jsonObject.remove(parameter.name)
            val propertyValue = parameter.propertyValue(clazz, instance)

            propertyNameAndValue[parameter] = propertyValue
        }

        val instanceWithoutMutatedProperties = jsonAdapter.fromJson(jsonObject.asString)!!

        return optionalProperties.filter { parameter ->
            val propertyValue = parameter.propertyValue(clazz, instanceWithoutMutatedProperties)
            propertyNameAndValue[parameter] != propertyValue
        }
    }

    private fun findMutatedProperties2(optionalProperties: Collection<KParameter>, primaryConstructor: KFunction<*>): Collection<KParameter> {

        val propertyNameAndValue = mutableMapOf<KParameter, Any?>()
        val arguments: Map<KParameter, Any?> = primaryConstructor.parameters.map {
            val propertyValue = it.propertyValue(clazz, instance)
            propertyNameAndValue[it] = propertyValue

            if (it.isOptional) {
                Pair(it!!, null)
            } else {
                Pair(it!!, propertyValue)
            }
        }.toMap()


        val instanceCopy = primaryConstructor.callBy(arguments)!!

//        val jsonAdapter = moshi.adapter<Any>(clazz.java)
//        val jsonObject = jsonAdapter.toJson(instance).let {
//            JsonParser.parseString(it).asJsonObject
//        }



       // val propertyNameAndValue = mutableMapOf<KParameter, Any?>()
//        optionalProperties.forEach { parameter ->
//            // Remove all optional parameters
//            jsonObject.remove(parameter.name)
//            val propertyValue = parameter.propertyValue(clazz, instance)
//
//            propertyNameAndValue[parameter] = propertyValue
//        }

       // val instanceWithoutMutatedProperties = jsonAdapter.fromJson(jsonObject.asString)!!

        return optionalProperties.filter { parameter ->
            val propertyValue = parameter.propertyValue(clazz, instanceCopy)
            propertyNameAndValue[parameter] != propertyValue
        }
    }

    companion object {

        fun of(instance: Any): FixtureAnalyser {
            val kotlinClass = instance.javaClass.kotlin
            return FixtureAnalyser(clazz = kotlinClass, instance = instance)
        }
    }
}

private object KotlinReflectiveFactoryCreator {

    fun create(clazz: KClass<*>) {

    }

}