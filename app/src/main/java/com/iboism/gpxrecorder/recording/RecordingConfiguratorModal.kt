package com.iboism.gpxrecorder.recording

import android.app.Dialog
import android.content.Context
import android.graphics.PorterDuff
import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.BottomSheetDialogFragment
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import com.iboism.gpxrecorder.R
import com.iboism.gpxrecorder.model.RecordingConfiguration
import android.support.design.widget.CoordinatorLayout


private const val INTERVAL_MIN = 1
private const val DISPLACEMENT_MIN = 2

class RecordingConfiguratorModal : BottomSheetDialogFragment() {

    var intervalSlider: SeekBar? = null
    var intervalValue: TextView? = null
    var displacementSlider: SeekBar? = null
    var displacementValue: TextView? = null
    var titleEditText: EditText? = null
    var doneButton: Button? = null

    var listener: Listener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.config_dialog, container)?.apply {
            initializeViews(this)
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

    private fun initializeViews(root: View) {
        intervalSlider = root.findViewById(R.id.interval_seekBar)
        displacementSlider = root.findViewById(R.id.displacement_seekBar)
        titleEditText = root.findViewById(R.id.config_title_editText)
        intervalValue = root.findViewById(R.id.interval_value)
        displacementValue = root.findViewById(R.id.displacement_value)
        doneButton = root.findViewById(R.id.start_button)

        displacementSlider?.let {
            it.progressDrawable
                    ?.setColorFilter(ContextCompat.getColor(activity, R.color.colorPrimary), PorterDuff.Mode.SRC_IN)
            it.thumb
                    ?.setColorFilter(ContextCompat.getColor(activity, R.color.colorPrimary), PorterDuff.Mode.SRC_IN)
            it.max -= DISPLACEMENT_MIN
        }

        intervalSlider?.let {
            it.progressDrawable
                    ?.setColorFilter(ContextCompat.getColor(activity, R.color.colorPrimary), PorterDuff.Mode.SRC_IN)
            it.thumb
                    ?.setColorFilter(ContextCompat.getColor(activity, R.color.colorPrimary), PorterDuff.Mode.SRC_IN)
            it.max -= INTERVAL_MIN
        }

        intervalValue?.text = intervalSlider?.progress.toString()
        displacementValue?.text = displacementSlider?.progress.toString()

        intervalSlider?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(p0: SeekBar?) {}

            override fun onStopTrackingTouch(p0: SeekBar?) {}

            override fun onProgressChanged(seekBar: SeekBar?, p1: Int, p2: Boolean) {
                seekBar?.let {
                    intervalValue?.text = (seekBar.progress + INTERVAL_MIN).toString()
                }
            }
        })

        displacementSlider?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(p0: SeekBar?) {}

            override fun onStopTrackingTouch(p0: SeekBar?) {}

            override fun onProgressChanged(seekBar: SeekBar?, p1: Int, p2: Boolean) {
                seekBar?.let {
                    displacementValue?.text = (seekBar.progress + DISPLACEMENT_MIN).toString()
                }
            }
        })

        doneButton?.setOnClickListener {
            listener?.configurationCreated (
                    RecordingConfiguration(
                            title = titleEditText?.text.toString().takeIf { it.isNotEmpty() } ?: "Untitled",
                            interval = intervalSlider?.progress?.toLong()?.plus(INTERVAL_MIN)?.times(60 * 1000) ?: 900000,
                            minDisplacement = displacementSlider?.progress?.plus(DISPLACEMENT_MIN)?.toFloat() ?: 5f
                    ))
            this.dismiss()
        }
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
