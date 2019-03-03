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
                                initialIntervalMillis: Long,
                                val titleEditText: EditText = root.findViewById(R.id.config_title_editText),
                                val doneButton: Button = root.findViewById(R.id.start_button),
                                val hoursPicker: NumberPicker = root.findViewById(R.id.interval_hours_picker),
                                val minutesPicker: NumberPicker = root.findViewById(R.id.interval_minutes_picker),
                                val secondsPicker: NumberPicker = root.findViewById(R.id.interval_seconds_picker)) {

    init {
        hoursPicker.minValue = 0
        hoursPicker.maxValue = 12
        hoursPicker.wrapSelectorWheel = true
        minutesPicker.minValue = 0
        minutesPicker.maxValue = 59
        minutesPicker.wrapSelectorWheel = true
        secondsPicker.minValue = 0
        secondsPicker.maxValue = 59
        secondsPicker.wrapSelectorWheel = true

        val hours = initialIntervalMillis / 3600000L
        val minutes = (initialIntervalMillis - (hours * 3600000L)) / 60000L
        val seconds = (initialIntervalMillis - (hours * 3600000L) - (minutes * 60000L)) / 1000L

        hoursPicker.value = hours.toInt()
        minutesPicker.value = minutes.toInt()
        secondsPicker.value = seconds.toInt()
    }

    fun getIntervalMillis(): Long {
        return (hoursPicker.value * 3600000L) + (minutesPicker.value * 60000L) + (secondsPicker.value * 1000L)
    }
}