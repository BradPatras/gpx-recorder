package com.iboism.gpxrecorder.recording.configurator

import android.content.Context
import android.graphics.PorterDuff
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

class RecordingConfiguratorView(root: View,
                                intervalValue: TextView = root.findViewById(R.id.interval_value),
                                val intervalSlider: SeekBar = root.findViewById(R.id.interval_seekBar),
                                val titleEditText: EditText = root.findViewById(R.id.config_title_editText),
                                val doneButton: Button = root.findViewById(R.id.start_button)) {
    private val context: Context = root.context

    init {
        val primaryColor = ContextCompat.getColor(context, R.color.colorPrimary)

        intervalSlider.progressDrawable.setColorFilter(primaryColor, PorterDuff.Mode.SRC_IN)
        intervalSlider.thumb.setColorFilter(primaryColor, PorterDuff.Mode.SRC_IN)
        intervalSlider.max -= INTERVAL_MIN

        intervalSlider.bindProgress(intervalValue, INTERVAL_MIN)
    }

    fun actualInterval(): Float {
        return (intervalSlider.progress + INTERVAL_MIN) / 2f
    }

    private fun SeekBar.bindProgress(textView: TextView, offset: Int = 0) {
        textView.text = (this.progress + offset).toString()
        this.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(p0: SeekBar?) {}

            override fun onStopTrackingTouch(p0: SeekBar?) {}

            override fun onProgressChanged(seekBar: SeekBar?, p1: Int, p2: Boolean) {
                seekBar?.let {
                    textView.text = actualInterval().toString()
                }
            }
        })
    }
}