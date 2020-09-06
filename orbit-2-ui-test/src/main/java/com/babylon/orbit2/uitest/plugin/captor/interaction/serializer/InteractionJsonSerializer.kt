package com.babylon.orbit2.uitest.plugin.captor.interaction.serializer

import com.babylon.orbit2.uitest.plugin.captor.interaction.Interaction
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type

internal class InteractionJsonSerializer : JsonSerializer<Interaction> {

    override fun serialize(src: Interaction, typeOfSrc: Type, context: JsonSerializationContext) = JsonObject().apply {
        add("flow", context.serialize(src.flow))
        add("action", context.serialize(src.action))
    }
}
