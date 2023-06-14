package com.iboism.gpxrecorder.recording.configurator

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.NumberPicker
import android.widget.TextView
import com.iboism.gpxrecorder.R

/**
 * Created by brad on 5/8/18.
 */

private const val DRAFT_INTERVAL_KEY = "RecordingConfiguratorView_draftInterval"
private const val DRAFT_TITLE_KEY = "RecordingConfiguratorView_draftTitle"

class RecordingConfiguratorView(
    root: View,
    initialIntervalMillis: Long,
    val titleEditText: EditText = root.findViewById(R.id.config_title_editText),
    val doneButton: TextView = root.findViewById(R.id.start_button),
    private val screenTitle: TextView = root.findViewById(R.id.title),
    private val hoursPicker: NumberPicker = root.findViewById(R.id.interval_hours_picker),
    private val minutesPicker: NumberPicker = root.findViewById(R.id.interval_minutes_picker),
    private val secondsPicker: NumberPicker = root.findViewById(R.id.interval_seconds_picker),
    val routeTitle: String? = null,
    val isTitleEditable: Boolean = true,
    val isResumingRoute: Boolean = false
) {

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

        routeTitle?.let {
            titleEditText.setText(it)
        }

        if (isResumingRoute) {
            screenTitle.text = "Resume Recording"
            doneButton.text = root.context.getText(R.string.resume_recording)
        } else {
            doneButton.text = root.context.getText(R.string.start)
            screenTitle.text = root.context.getText(R.string.new_recording)
        }

        titleEditText.isEnabled = isTitleEditable
        setInterval(initialIntervalMillis)
    }

    fun onSaveInstanceState(outState: Bundle) {
        outState.putLong(DRAFT_INTERVAL_KEY, getIntervalMillis())
        outState.putString(DRAFT_TITLE_KEY, titleEditText.text.toString())
    }

    fun restoreInstanceState(outState: Bundle?) {
        outState?.getLong(DRAFT_INTERVAL_KEY)?.let { draftInterval ->
            setInterval(draftInterval)
            outState.remove(DRAFT_INTERVAL_KEY)
        }

        outState?.getString(DRAFT_TITLE_KEY)?.let { draftTitle ->
            titleEditText.text.clear()
            titleEditText.text.append(draftTitle)
            outState.remove(DRAFT_TITLE_KEY)
        }
    }

    fun getIntervalMillis(): Long {
        return (hoursPicker.value * 3600000L) + (minutesPicker.value * 60000L) + (secondsPicker.value * 1000L)
    }

    private fun setInterval(intervalMillis: Long) {
        val hours = intervalMillis / 3600000L
        val minutes = (intervalMillis - (hours * 3600000L)) / 60000L
        val seconds = (intervalMillis - (hours * 3600000L) - (minutes * 60000L)) / 1000L

        hoursPicker.value = hours.toInt()
        minutesPicker.value = minutes.toInt()
        secondsPicker.value = seconds.toInt()
    }
}