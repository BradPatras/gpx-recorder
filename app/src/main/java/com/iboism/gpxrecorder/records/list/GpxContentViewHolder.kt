package com.iboism.gpxrecorder.records.list

import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.widget.*
import com.google.android.gms.maps.model.LatLng
import com.iboism.gpxrecorder.R
import com.iboism.gpxrecorder.model.Segment
import com.iboism.gpxrecorder.extensions.takeGist
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class GpxContentViewHolder(view: View): RecyclerView.ViewHolder(view) {
    val rootView = view
    val contentView = view.findViewById(R.id.main_content_layout) as View
    val titleView = view.findViewById(R.id.gpx_content_title) as TextView
    val dateView = view.findViewById(R.id.gpx_content_date) as TextView
    val exportButton = view.findViewById(R.id.gpx_content_export_button) as ImageButton
    val distanceView = view.findViewById(R.id.gpx_content_distance) as TextView
    val waypointCountView = view.findViewById(R.id.gpx_content_waypoint_count) as TextView
    private val exportProgressBar = view.findViewById(R.id.gpx_content_export_progress_bar) as ProgressBar
    var previewImageView = view.findViewById(R.id.preview_image) as ImageView

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