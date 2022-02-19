package com.iboism.gpxrecorder.recording

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.Button
import androidx.constraintlayout.widget.ConstraintLayout
import com.iboism.gpxrecorder.R
import com.iboism.gpxrecorder.databinding.CurrentRecordingViewBinding

class CurrentRecordingView(context: Context, attrs: AttributeSet) : ConstraintLayout(context, attrs) {
    private val binding = CurrentRecordingViewBinding.inflate(LayoutInflater.from(context), this, true)
    val routeTitle = binding.routeTitleTv
    val addWaypointButton = binding.addWptBtn
    val playPauseButton = binding.playpauseBtn
    val stopButton = binding.stopBtn

    fun setPaused(isPaused: Boolean) {
        playPauseButton.text = if (isPaused) context.getText(R.string.resume_recording) else context.getText(R.string.pause_recording)
    }
}