package com.babylon.orbit2.uitest.plugin.captor.screenshot

import android.graphics.Bitmap
import android.view.View
import com.babylon.orbit2.uitest.engine.UiMetadata
import com.babylon.orbit2.uitest.engine.createFile
import com.babylon.orbit2.uitest.plugin.captor.UiCaptor
import com.babylon.orbit2.uitest.plugin.captor.UiCaptureResult
import com.facebook.testing.screenshot.Screenshot
import java.io.File
import kotlin.reflect.KClass

internal class ScreenshotUiCaptor : UiCaptor {

    override fun capture(screenUnderTest: KClass<out Any>, rootView: View, metadata: UiMetadata): UiCaptureResult {
        return Screenshot.snap(rootView).bitmap.use { bitmap ->
            val file = metadata.createFile("png")
            file.writeBitmap(bitmap)

            UiCaptureResult.Image("screenshot", file, bitmap.height, bitmap.width)
        }
    }

    private inline fun <T> Bitmap.use(block: (Bitmap) -> T): T {
        return try {
            block(this)
        } finally {
            recycle()
        }
    }

    private fun File.writeBitmap(bitmap: Bitmap) = outputStream().use {
        bitmap.compress(
            Bitmap.CompressFormat.PNG,
            FULL_QUALITY, it
        )
        it.flush()
    }

    companion object {
        private const val FULL_QUALITY = 100
    }
}
