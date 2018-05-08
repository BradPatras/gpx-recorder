package com.iboism.gpxrecorder.recording

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.BottomSheetDialogFragment
import android.support.design.widget.CoordinatorLayout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.iboism.gpxrecorder.R
import com.iboism.gpxrecorder.model.RecordingConfiguration


private const val INTERVAL_MIN = 1
private const val DISPLACEMENT_MIN = 2

class RecordingConfiguratorModal : BottomSheetDialogFragment() {

    lateinit var configuratorView: RecordingConfiguratorView
    var listener: Listener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.config_dialog, container)?.apply {
            configuratorView = RecordingConfiguratorView(this)
            configuratorView.doneButton.setOnClickListener {
                listener?.configurationCreated (
                        RecordingConfiguration(
                                title = configuratorView.titleEditText.text.toString().takeIf { it.isNotEmpty() } ?: "Untitled",
                                interval = configuratorView.intervalSlider.progress.toLong().plus(INTERVAL_MIN).times(60 * 1000),
                                minDisplacement = configuratorView.displacementSlider.progress.plus(DISPLACEMENT_MIN).toFloat()
                        ))
                this@RecordingConfiguratorModal.dismiss()
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)

        dialog.setOnShowListener {
            val behavior = ((dialog.window.findViewById<View>(R.id.design_bottom_sheet).layoutParams as CoordinatorLayout.LayoutParams).behavior as BottomSheetBehavior)
            behavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onSlide(bottomSheet: View, slideOffset: Float) {}

                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    if (newState == BottomSheetBehavior.STATE_DRAGGING) {
                        behavior.state = BottomSheetBehavior.STATE_EXPANDED
                    }
                }
            })
        }

        return dialog
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as Listener
    }

    override fun onDetach() {
        listener = null
        super.onDetach()
    }

    interface Listener {
        fun configurationCreated(configuration: RecordingConfiguration)
    }

    companion object {
        fun instance() = RecordingConfiguratorModal()
    }
}
