package com.babylon.orbit2.uitest.plugin.captor.interaction

import android.annotation.SuppressLint
import android.content.res.Resources
import android.view.View
import android.view.ViewGroup
import android.widget.ActionMenuView
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.children
import androidx.core.view.isVisible
import com.babylon.orbit2.uitest.plugin.captor.interaction.detector.AdapterViewDetector
import com.babylon.orbit2.uitest.plugin.captor.interaction.detector.BottomNavigationViewDetector
import com.babylon.orbit2.uitest.plugin.captor.interaction.detector.CompositeDetector
import com.babylon.orbit2.uitest.plugin.captor.interaction.detector.CompoundButtonDetector
import com.babylon.orbit2.uitest.plugin.captor.interaction.detector.RecyclerViewDetector
import com.babylon.orbit2.uitest.plugin.captor.interaction.detector.TabLayoutDetector
import com.babylon.orbit2.uitest.plugin.captor.interaction.detector.ToolbarDetector
import com.babylon.orbit2.uitest.plugin.captor.interaction.detector.ViewDetector
import com.babylon.orbit2.uitest.plugin.captor.interaction.detector.ViewPagerDetector

const val UI_COMPONENT_CLASS_KEY: Int = 0xF123456

internal val View.description: String
    @SuppressLint("ResourceType")
    @Suppress("EXPERIMENTAL_API_USAGE")
    get() = this.let {
        val type = (this.getTag(UI_COMPONENT_CLASS_KEY) as? String)
            ?: this.javaClass.simpleName

        val name = when (this) {
            is ImageView -> contentDescription?.takeIf(CharSequence::isNotEmpty) ?: idName
            else -> hierarchySequence()
                .filterIsInstance<TextView>()
                .filter { it.text.isNotBlank() }
                .fold(null as TextView?) { acc, textView ->
                    if (textView.textSize > acc?.textSize ?: 0f) {
                        textView
                    } else {
                        acc
                    }
                }?.text ?: idName
        }

        listOf(name, type).joinToString(separator = " - ")
    }

private val View.idName: String
    get() = try {
        "R.id.${resources.getResourceEntryName(id)}"
    } catch (exception: Resources.NotFoundException) {
        "(id unknown)"
    }

internal val defaultDetector = CompositeDetector(
    AdapterViewDetector(),
    BottomNavigationViewDetector(),
    CompoundButtonDetector(),
    RecyclerViewDetector(),
    TabLayoutDetector(),
    ToolbarDetector(),
    ViewDetector(),
    ViewPagerDetector()
)

internal fun View.hierarchySequence(): Sequence<View> = sequence {
    val view = this@hierarchySequence
    if (view.isVisible) {
        yield(view)
        if (view is ViewGroup) {
            yieldAll(
                view.children
                    .filter { it !is ActionMenuView && it !is androidx.appcompat.widget.ActionMenuView }
                    .flatMap { it.hierarchySequence() }
            )
        }
    }
}
