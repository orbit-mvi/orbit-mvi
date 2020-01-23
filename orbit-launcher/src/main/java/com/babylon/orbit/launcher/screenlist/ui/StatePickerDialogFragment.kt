package com.babylon.orbit.launcher.screenlist.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.babylon.orbit.launcher.R
import com.babylon.orbit.launcher.screenlist.ui.item.HeaderItem
import com.babylon.orbit.launcher.screenlist.ui.item.StateItem
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Section

internal class StatePickerDialogFragment : DialogFragment() {

    private val hardcodedMocks = Section()
    private val recordedMocks = Section()

    var onStateChanged: OnStateChanged? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val listView = inflater.inflate(
            R.layout.orbit_state_picker_dialog_fragment,
            container,
            false
        ) as RecyclerView

        hardcodedMocks.setHeader(HeaderItem(HARDCODED_STATE))
        recordedMocks.setHeader(HeaderItem(RECORDED_STATE))

        listView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = GroupAdapter<GroupieViewHolder>().apply {
                add(hardcodedMocks)
                add(recordedMocks)
            }
        }

        unzipToSection(HARDCODED_STATE_LIST_KEY, hardcodedMocks)
        unzipToSection(RECORDED_STATE_LIST_KEY, recordedMocks)

        return listView
    }

    private fun unzipToSection(key: String, section: Section) =
        arguments
            ?.getStringArray(key)
            ?.map {
                StateItem(it) { stateName ->
                    onStateChanged?.onChange(stateName)
                    dismiss()
                }
            }
            ?.let(section::update)

    override fun dismiss() {
        super.dismiss()

        onStateChanged = null
    }

    companion object {

        fun newInstance(
            hardcodedStates: List<String>,
            recordedStates: List<String>
        ): StatePickerDialogFragment =
            StatePickerDialogFragment()
                .apply {
                    arguments = Bundle()
                        .apply {
                            putStringArray(HARDCODED_STATE_LIST_KEY, hardcodedStates.toTypedArray())
                            putStringArray(RECORDED_STATE_LIST_KEY, recordedStates.toTypedArray())
                        }
                }

        private const val HARDCODED_STATE_LIST_KEY =
            "com.babylon.orbit.launcher.StatePickerFragment_HARDCODED_STATE_LIST_KEY"

        private const val RECORDED_STATE_LIST_KEY =
            "com.babylon.orbit.launcher.StatePickerFragment_RECORDED_STATE_LIST_KEY"

        private const val HARDCODED_STATE = "HARDCODED MOCKS"

        private const val RECORDED_STATE = "RECORDED MOCKS"
    }

    internal interface OnStateChanged {

        fun onChange(stateName: String)
    }
}
