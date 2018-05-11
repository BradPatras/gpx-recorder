package com.iboism.gpxrecorder.recording

import android.content.Context
import android.graphics.PorterDuff
import android.support.design.widget.FloatingActionButton
import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import android.widget.EditText
import com.iboism.gpxrecorder.R

/**
 * Created by brad on 5/8/18.
 */

private const val INTERVAL_MIN = 1
private const val DISPLACEMENT_MIN = 2

class RecordingConfiguratorView(root: View,
                                val intervalSlider: SeekBar = root.findViewById(R.id.interval_seekBar),
                                private val intervalValue: TextView = root.findViewById(R.id.interval_value),
                                val displacementSlider: SeekBar = root.findViewById(R.id.displacement_seekBar),
                                private val displacementValue: TextView = root.findViewById(R.id.displacement_value),
                                val titleEditText: EditText = root.findViewById(R.id.config_title_editText),
                                val doneButton: FloatingActionButton = root.findViewById(R.id.start_fab)) {
    private val context: Context = root.context

    init {
        val primaryColor = ContextCompat.getColor(context, R.color.colorPrimary)

        intervalSlider.progressDrawable.setColorFilter(primaryColor, PorterDuff.Mode.SRC_IN)
        intervalSlider.thumb.setColorFilter(primaryColor, PorterDuff.Mode.SRC_IN)
        intervalSlider.max -= DISPLACEMENT_MIN

        displacementSlider.progressDrawable.setColorFilter(primaryColor, PorterDuff.Mode.SRC_IN)
        displacementSlider.thumb.setColorFilter(primaryColor, PorterDuff.Mode.SRC_IN)
        displacementSlider.max -= DISPLACEMENT_MIN

        intervalValue.text = intervalSlider.progress.toString()
        displacementValue.text = displacementSlider.progress.toString()

        intervalSlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(p0: SeekBar?) {}

            override fun onStopTrackingTouch(p0: SeekBar?) {}

            override fun onProgressChanged(seekBar: SeekBar?, p1: Int, p2: Boolean) {
                seekBar?.let {
                    intervalValue.text = (seekBar.progress + INTERVAL_MIN).toString()
                }
            }
        })
    }
}