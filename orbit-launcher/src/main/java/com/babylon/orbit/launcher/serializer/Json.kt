package com.babylon.orbit.launcher.serializer

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import kotlin.jvm.internal.Reflection
import kotlin.reflect.KClass

internal object Json {

    private val gson =
        GsonBuilder().registerTypeAdapterFactory(
            object : TypeAdapterFactory {
                override fun <T : Any> create(gson: Gson, type: TypeToken<T>): TypeAdapter<T> {
                    val clazz = Reflection.getOrCreateKotlinClass(type.rawType)

                    return if (clazz.sealedSubclasses.any()) {
                        SealedClassTypeAdapter(clazz, gson)
                    } else {
                        gson.getDelegateAdapter(this, type)
                    }
                }
            }).create()

    fun <T> fromJson(json: String, clazz: Class<out T>): T = gson.fromJson(json, clazz)

    fun <T> toJson(item: T): String = this.gson.toJson(item)
}

private class SealedClassTypeAdapter<T : Any>(
    val clazz: KClass<Any>,
    val gson: Gson
) : TypeAdapter<T>() {

    @Suppress("UNCHECKED_CAST")
    override fun read(jsonReader: JsonReader): T? {
        jsonReader.beginObject()
        val nextName = jsonReader.nextName()
        val innerClass = clazz.sealedSubclasses.firstOrNull {
            it.simpleName!!.contains(nextName)
        }
            ?: throw IllegalStateException("$nextName is not found to be a data class of the sealed class ${clazz.qualifiedName}")
        val x = gson.fromJson<T>(jsonReader, innerClass.javaObjectType)
        jsonReader.endObject()

        return innerClass.objectInstance as T? ?: x
    }

    override fun write(out: JsonWriter, value: T) {
        val jsonString = gson.toJson(value)
        out.beginObject()
        out.name(value.javaClass.canonicalName?.splitToSequence(".")?.last()).jsonValue(jsonString)
        out.endObject()
    }
}
