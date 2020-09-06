package com.babylon.orbit2.uitest.plugin.captor.interaction.serializer

import com.babylon.orbit2.uitest.plugin.captor.interaction.InteractionRecord
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type

internal class InteractionRecordJsonSerializer : JsonSerializer<InteractionRecord> {

    override fun serialize(src: InteractionRecord, typeOfSrc: Type, context: JsonSerializationContext) = JsonObject().apply {
        add("source", context.serialize(src.source))
        add("event", context.serialize(src.event))
        add("interactions", context.serialize(src.interactions))
    }
}
