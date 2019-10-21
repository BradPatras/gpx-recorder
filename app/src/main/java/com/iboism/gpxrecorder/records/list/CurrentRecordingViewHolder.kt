package com.iboism.gpxrecorder.records.list

import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.iboism.gpxrecorder.R
import kotlinx.android.synthetic.main.list_row_current_route.view.*

class CurrentRecordingViewHolder(view: View): RecyclerView.ViewHolder(view) {
    val rootView = view
    val routeTitle: TextView? = view.route_title_tv
    val addWaypointButton: Button? = view.add_wpt_btn
    val playPauseButton: Button? = view.playpause_btn
    val stopButton: Button? = view.stop_btn

    fun setPaused(isPaused: Boolean) {
        playPauseButton?.text = if (isPaused) rootView.context.getText(R.string.resume_recording) else rootView.context.getText(R.string.pause_recording)
    }
}