package com.iboism.gpxrecorder.records.list

import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.iboism.gpxrecorder.R
import com.iboism.gpxrecorder.databinding.ListRowActiveRouteBinding

class ActiveRouteRowViewHolder(binding: ListRowActiveRouteBinding): RecyclerView.ViewHolder(binding.root) {
    val rootView = binding.root
    val routeTitle: TextView = binding.routeTitleTv
    val addWaypointButton: Button = binding.addWptBtn
    val playPauseButton: Button = binding.playpauseBtn
    val stopButton: Button = binding.stopBtn

    fun setPaused(isPaused: Boolean) {
        playPauseButton.text = if (isPaused) rootView.context.getText(R.string.resume_recording) else rootView.context.getText(R.string.pause_recording)
    }
}