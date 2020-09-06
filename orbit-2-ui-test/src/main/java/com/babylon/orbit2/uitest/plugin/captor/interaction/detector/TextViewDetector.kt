package com.babylon.orbit2.uitest.plugin.captor.interaction.detector

import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import com.babylon.orbit2.uitest.plugin.captor.interaction.PendingInteraction
import com.babylon.orbit2.uitest.plugin.captor.interaction.description

internal class TextViewDetector : CompositeDetector(
    OnEditorActionListenerDetector(),
    OnTextChangedListenerDetector()
) {

    private class OnEditorActionListenerDetector : InteractionDetector {

        val actions = mapOf(
            "done" to EditorInfo.IME_ACTION_DONE,
            "go" to EditorInfo.IME_ACTION_GO,
            "next" to EditorInfo.IME_ACTION_NEXT,
            "previous" to EditorInfo.IME_ACTION_PREVIOUS,
            "search" to EditorInfo.IME_ACTION_SEARCH,
            "send" to EditorInfo.IME_ACTION_SEND
        )

        override fun detect(view: View): Sequence<PendingInteraction> {
            if (view is TextView) {

                val editorAction = view.editorActionListener?.let { listener ->
                    actions.map { action ->
                        PendingInteraction(
                            source = view.description,
                            event = "editor action ${action.key}",
                            executor = {
                                listener.onEditorAction(view, action.value, null)
                            }
                        )
                    }
                }

                val enterEditorAction = view.editorActionListener?.let { listener ->
                    PendingInteraction(
                        source = view.description,
                        event = "editor action enter",
                        executor = {
                            listener.onEditorAction(view, EditorInfo.IME_NULL, KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
                        }
                    )
                }

                return sequence<PendingInteraction> {
                    editorAction?.let { yieldAll(editorAction) }
                    enterEditorAction?.let { yield(enterEditorAction) }
                }
            }

            return emptySequence()
        }
    }

    private class OnTextChangedListenerDetector : InteractionDetector {
        override fun detect(view: View): Sequence<PendingInteraction> {
            if (view is TextView) {
                return view.textChangedListener.asSequence().flatMap { listener ->
                    sequenceOf(
                        PendingInteraction(
                            source = view.description,
                            event = "after text changed",
                            executor = {
                                listener.afterTextChanged(SpannableStringBuilder(SAMPLE_TEXT))
                            }
                        ),
                        PendingInteraction(
                            source = view.description,
                            event = "before text changed",
                            executor = {
                                @Suppress("MagicNumber")
                                listener.beforeTextChanged(SAMPLE_TEXT, 0, 6, 7)
                            }
                        ),
                        PendingInteraction(
                            source = view.description,
                            event = "text changed",
                            executor = {
                                @Suppress("MagicNumber")
                                listener.onTextChanged(SAMPLE_TEXT, 0, 5, 0)
                            }
                        )
                    )
                }
            }

            return emptySequence()
        }

        companion object {
            private const val SAMPLE_TEXT = "abc123"
        }
    }

    companion object {
        private val TextView.textChangedListener: List<TextWatcher>
            @Suppress("UNCHECKED_CAST")
            get() = TextView::class.java.getDeclaredField("mListeners").apply {
                isAccessible = true
            }.get(this) as? List<TextWatcher> ?: emptyList()

        private val TextView.editorActionListener: TextView.OnEditorActionListener?
            get() = TextView::class.java.getDeclaredField("mEditor").apply {
                isAccessible = true
            }.get(this)?.let { editor ->
                editor::class.java.getDeclaredField("mInputContentType").apply {
                    isAccessible = true
                }.get(editor)?.let { inputContentType ->
                    inputContentType::class.java.getDeclaredField("onEditorActionListener").apply {
                        isAccessible = true
                    }.get(inputContentType) as? TextView.OnEditorActionListener
                }
            }
    }
}
