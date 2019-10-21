package com.iboism.gpxrecorder.records.list

import android.view.View
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.iboism.gpxrecorder.R

class GpxContentViewHolder(view: View): RecyclerView.ViewHolder(view) {
    val rootView = view
    val contentView = view.findViewById(R.id.main_content_layout) as View
    val titleView = view.findViewById(R.id.gpx_content_title) as TextView
    val dateView = view.findViewById(R.id.gpx_content_date) as TextView
    val exportButton = view.findViewById(R.id.gpx_content_export_button) as ImageButton
    val distanceView = view.findViewById(R.id.gpx_content_distance) as TextView
    val waypointCountView = view.findViewById(R.id.gpx_content_waypoint_count) as TextView
    private val exportProgressBar = view.findViewById(R.id.gpx_content_export_progress_bar) as ProgressBar

    init {
        exportProgressBar.isIndeterminate = true
        exportProgressBar.visibility = View.GONE
    }

    fun setExportLoading(loading: Boolean) {
        exportButton.visibility = if (loading) View.INVISIBLE else View.VISIBLE
        exportButton.invalidate()

        exportProgressBar.visibility = if (loading) View.VISIBLE else View.GONE
        exportProgressBar.invalidate()
    }
}