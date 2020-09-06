package com.babylon.orbit2.uitest.plugin.captor.interaction.detector

import android.view.MotionEvent
import android.view.View
import com.babylon.orbit2.uitest.plugin.captor.interaction.PendingInteraction
import com.babylon.orbit2.uitest.plugin.captor.interaction.description
import java.lang.reflect.Field
import java.lang.reflect.Method

internal class ViewDetector : CompositeDetector(
    OnClickListenerDetector(),
    OnLongClickListenerDetector(),
    OnTouchListenerDetector(),
    OnFocusChangeListenerDetector()
) {

    private class OnClickListenerDetector : InteractionDetector {

        override fun detect(view: View): Sequence<PendingInteraction> {
            view.takeIf(View::hasOnClickListeners)?.let {
                return sequenceOf(PendingInteraction(
                    source = view.description,
                    event = "click",
                    executor = { view.performClick() }
                ))
            }

            return emptySequence()
        }
    }

    private class OnLongClickListenerDetector : InteractionDetector {

        override fun detect(view: View): Sequence<PendingInteraction> {
            view.getListener<View.OnLongClickListener>()?.let {
                return sequenceOf(PendingInteraction(
                    source = view.description,
                    event = "long click",
                    executor = { view.performLongClick() }
                ))
            }

            return emptySequence()
        }
    }

    private class OnTouchListenerDetector : InteractionDetector {

        override fun detect(view: View): Sequence<PendingInteraction> {
            view.getListener<View.OnTouchListener>()?.let { touchListener ->
                @Suppress("MagicNumber")
                return sequenceOf(PendingInteraction(
                    source = view.description,
                    event = "touch",
                    executor = {
                        val event = MotionEvent.obtain(
                            0,
                            355100400000,
                            MotionEvent.ACTION_MOVE,
                            1f,
                            2f,
                            0
                        )
                        touchListener.onTouch(view, event)
                        event.recycle()
                    }
                ))
            }

            return emptySequence()
        }
    }

    private class OnFocusChangeListenerDetector : InteractionDetector {

        override fun detect(view: View): Sequence<PendingInteraction> {
            view.getListener<View.OnFocusChangeListener>()?.let { focusChangeListener ->
                return sequenceOf(
                    PendingInteraction(
                        source = view.description,
                        event = "focus change - focused",
                        executor = { focusChangeListener.onFocusChange(view, true) }
                    ),
                    PendingInteraction(
                        source = view.description,
                        event = "focus change - unfocused",
                        executor = { focusChangeListener.onFocusChange(view, false) }
                    )
                )
            }

            return emptySequence()
        }
    }

    companion object {
        private val getListenerInfo: Method by lazy {
            // noinspection DiscouragedPrivateApi
            View::class.java.getDeclaredMethod("getListenerInfo").apply {
                isAccessible = true
            }
        }

        private val listenerInfoClass by lazy {
            // noinspection PrivateApi
            Class.forName("android.view.View\$ListenerInfo")
        }

        private val listenerFieldCache = mutableMapOf<String, Field>()

        private inline fun <reified T> View.getListener(): T? {
            val listenerInfo = getListenerInfo.invoke(this)

            return listenerFieldCache.getOrPut(T::class.java.simpleName) {
                listenerInfoClass.getDeclaredField("m${T::class.java.simpleName}").apply {
                    isAccessible = true
                }
            }.get(listenerInfo) as? T
        }
    }
}
