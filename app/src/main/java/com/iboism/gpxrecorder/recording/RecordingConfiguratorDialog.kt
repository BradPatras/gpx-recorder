package com.iboism.gpxrecorder.recording

import android.app.DialogFragment
import android.graphics.PorterDuff
import android.os.Bundle
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


/**
 * Created by Brad on 12/5/2017.
 */
class RecordingConfiguratorDialog: DialogFragment() {

    var intervalSlider: SeekBar? = null
    var intervalValue: TextView? = null
    var displacementSlider: SeekBar? = null
    var displacementValue: TextView? = null
    var titleEditText: EditText? = null
    var doneButton: Button? = null

    var onComplete: ((RecordingConfiguration) -> Unit)? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater?.inflate(R.layout.config_dialog, container)
        view?.let {
            intervalSlider = it.findViewById(R.id.interval_seekBar)
            displacementSlider = it.findViewById(R.id.displacement_seekBar)
            titleEditText = it.findViewById(R.id.config_title_editText)
            intervalValue = it.findViewById(R.id.interval_value)
            displacementValue = it.findViewById(R.id.displacement_value)
            doneButton = it.findViewById(R.id.start_button)

            displacementSlider?.getProgressDrawable()?.setColorFilter(ContextCompat.getColor(activity, R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
            displacementSlider?.getThumb()?.setColorFilter(ContextCompat.getColor(activity, R.color.colorPrimary), PorterDuff.Mode.SRC_IN);

            intervalSlider?.getProgressDrawable()?.setColorFilter(ContextCompat.getColor(activity, R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
            intervalSlider?.getThumb()?.setColorFilter(ContextCompat.getColor(activity, R.color.colorPrimary), PorterDuff.Mode.SRC_IN);

            intervalValue?.text = intervalSlider?.progress.toString()
            displacementValue?.text = displacementSlider?.progress.toString()

            intervalSlider?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onStartTrackingTouch(p0: SeekBar?) {}

                override fun onStopTrackingTouch(p0: SeekBar?) {}

                override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                    intervalValue?.text = intervalSlider?.progress.toString()
                }
            })

            displacementSlider?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onStartTrackingTouch(p0: SeekBar?) {}

                override fun onStopTrackingTouch(p0: SeekBar?) {}

                override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                    displacementValue?.text = displacementSlider?.progress.toString()
                }
            })

            doneButton?.setOnClickListener {
                onComplete?.invoke(
                        RecordingConfiguration(
                                title = titleEditText?.text.toString() ?: "Untitled",
                                interval = intervalSlider?.progress?.toLong()?.times(60) ?: 15 * 60,
                                minDisplacement = displacementSlider?.progress?.toFloat() ?: 5f
                        ))
                this.dismiss()
            }
        }

        return view ?: View(activity)
    }

    companion object {
        fun instance(onComplete :((RecordingConfiguration) -> Unit)) : RecordingConfiguratorDialog {
            var dialog = RecordingConfiguratorDialog()
            dialog.onComplete = onComplete
            return dialog
        }
    }
}