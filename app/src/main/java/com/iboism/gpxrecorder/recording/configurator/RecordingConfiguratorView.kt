package com.iboism.gpxrecorder.recording.configurator

import android.content.Context
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.NumberPicker
import com.iboism.gpxrecorder.R

/**
 * Created by brad on 5/8/18.
 */

class RecordingConfiguratorView(root: View,
                                initialIntervalMillis: Float,
                                val titleEditText: EditText = root.findViewById(R.id.config_title_editText),
                                val doneButton: Button = root.findViewById(R.id.start_button),
                                val hoursPicker: NumberPicker = root.findViewById(R.id.interval_hours_picker),
                                val minutesPicker: NumberPicker = root.findViewById(R.id.interval_minutes_picker),
                                val secondsPicker: NumberPicker = root.findViewById(R.id.interval_seconds_picker)) {
    private val context: Context = root.context

    init {
        hoursPicker.minValue = 0
        hoursPicker.maxValue = 24
        hoursPicker.wrapSelectorWheel = false
        minutesPicker.minValue = 0
        minutesPicker.maxValue = 60
        minutesPicker.wrapSelectorWheel = false
        secondsPicker.minValue = 1
        secondsPicker.maxValue = 60
        secondsPicker.wrapSelectorWheel = false
    }

    fun getIntervalMillis(): Long {
        return (hoursPicker.value * 3600000L) + (minutesPicker.value * 60000L) + (secondsPicker.value * 1000L)
    }
}