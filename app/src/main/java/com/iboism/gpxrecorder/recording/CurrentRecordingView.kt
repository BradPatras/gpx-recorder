package com.iboism.gpxrecorder.recording

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.LayoutInflater
import com.iboism.gpxrecorder.R
import kotlinx.android.synthetic.main.current_recording_view.view.*

class CurrentRecordingView(context: Context, attrs: AttributeSet) : ConstraintLayout(context, attrs) {
    private val rootView = LayoutInflater.from(context).inflate(R.layout.current_recording_view, this) as ConstraintLayout
    val routeTitle = route_title_tv
    val addWaypointButton = add_wpt_view
    val playPauseButton = playpause_view
    val stopButton = stop_view
}