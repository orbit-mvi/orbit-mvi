package com.babylon.orbit2.uitest.plugin.captor.interaction.detector

import android.view.View
import android.widget.Adapter
import android.widget.AdapterView
import com.babylon.orbit2.uitest.plugin.captor.interaction.PendingInteraction
import com.babylon.orbit2.uitest.plugin.captor.interaction.description

internal class AdapterViewDetector : CompositeDetector(
    OnItemSelectedListenerDetector(),
    OnItemClickListenerDetector(),
    OnItemLongClickListenerDetector()
) {

    private class OnItemSelectedListenerDetector : InteractionDetector {

        override fun detect(view: View): Sequence<PendingInteraction> {
            if (view is AdapterView<*>) {
                view.onItemSelectedListener?.let { listener ->
                    return view.adapter.adapterItems.map { adapterItem ->
                        PendingInteraction(
                            source = adapterItem.view.description,
                            event = "item selected",
                            executor = {
                                listener.onItemSelected(view, adapterItem.view, adapterItem.position, adapterItem.id)
                            }
                        )
                    } + PendingInteraction(
                        source = view.description,
                        event = "nothing selected",
                        executor = {
                            listener.onNothingSelected(view)
                        }
                    )
                }
            }

            return emptySequence()
        }
    }

    private class OnItemClickListenerDetector : InteractionDetector {

        override fun detect(view: View): Sequence<PendingInteraction> {
            if (view is AdapterView<*>) {
                view.onItemClickListener?.let { listener ->
                    return view.adapter.adapterItems.map { adapterItem ->
                        PendingInteraction(
                            source = adapterItem.view.description,
                            event = "item click",
                            executor = {
                                listener.onItemClick(view, adapterItem.view, adapterItem.position, adapterItem.id)
                            }
                        )
                    }
                }
            }

            return emptySequence()
        }
    }

    private class OnItemLongClickListenerDetector : InteractionDetector {

        override fun detect(view: View): Sequence<PendingInteraction> {
            if (view is AdapterView<*>) {
                view.onItemLongClickListener?.let { listener ->
                    return view.adapter.adapterItems.map { adapterItem ->
                        PendingInteraction(
                            source = adapterItem.view.description,
                            event = "item long click",
                            executor = {
                                listener.onItemLongClick(view, adapterItem.view, adapterItem.position, adapterItem.id)
                            }
                        )
                    }
                }
            }

            return emptySequence()
        }
    }

    companion object {
        private class AdapterItem(val position: Int, val id: Long, val view: View)

        private val Adapter.adapterItems: Sequence<AdapterItem>
            get() {
                var index = 0
                return generateSequence {
                    if (index < count) {
                        AdapterItem(index, getItemId(index), getView(index, null, null)).also { index++ }
                    } else {
                        null
                    }
                }
            }
    }
}
