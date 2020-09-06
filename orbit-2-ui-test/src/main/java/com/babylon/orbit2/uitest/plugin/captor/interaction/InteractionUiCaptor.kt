package com.babylon.orbit2.uitest.plugin.captor.interaction

import android.view.View
import com.babylon.orbit2.uitest.engine.UiMetadata
import com.babylon.orbit2.uitest.engine.createFile
import com.babylon.orbit2.uitest.plugin.captor.UiCaptor
import com.babylon.orbit2.uitest.plugin.captor.UiCaptureResult
import com.babylon.orbit2.uitest.plugin.captor.interaction.serializer.InteractionJsonSerializer
import com.babylon.orbit2.uitest.plugin.captor.interaction.serializer.InteractionRecordJsonSerializer
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlin.reflect.KClass

internal class InteractionUiCaptor : UiCaptor {

    override fun capture(screenUnderTest: KClass<out Any>, rootView: View, metadata: UiMetadata): UiCaptureResult? {
        val interactionRecordExtractor = InteractionRecordExtractor(
            screenUnderTest,
            InteractionRecorderFactory.interactionRecorder,
            InteractionRecorderFactory.lifecycleInteractionRecorder
        )

        val interactionRecords = runBlocking(Dispatchers.Main) {
            captureInteractionRecords(interactionRecordExtractor, rootView).sortedWith(
                compareBy<InteractionRecord> { it.source }
                    .thenBy { it.event }
            ).toList()
        }

        val file = metadata.createFile("json")
        val json = gson.toJson(interactionRecords)
        file.writeText(json)

        return UiCaptureResult.Json("interaction", file)
    }

    private fun captureInteractionRecords(interactionRecordExtractor: InteractionRecordExtractor, rootView: View) = sequence {

        // Middleware created actions
        interactionRecordExtractor.extractMiddleware()?.let {
            yield(it)
        }

        yieldAll(interactionRecordExtractor.extractLifecycle())

        yieldAll(
            rootView.hierarchySequence()
                .flatMap { view ->
                    defaultDetector.detect(view)
                }.mapNotNull { pendingInteraction ->
                    interactionRecordExtractor.extract(pendingInteraction)
                }
        )
    }

    private companion object {
        val gson: Gson = GsonBuilder()
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .registerTypeAdapter(InteractionRecord::class.java, InteractionRecordJsonSerializer())
            .registerTypeAdapter(Interaction::class.java, InteractionJsonSerializer())
            .create()
    }
}
