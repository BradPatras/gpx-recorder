package com.iboism.gpxrecorder.records.list

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.google.android.gms.maps.model.LatLng
import com.iboism.gpxrecorder.R
import com.iboism.gpxrecorder.model.Segment
import com.iboism.gpxrecorder.extensions.takeGist
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class GpxViewHolder(view: View): RecyclerView.ViewHolder(view) {
    private val pointLoadingThread = Schedulers.single()

    val rootView = view
    val contentView = view.findViewById(R.id.main_content_layout) as View
    val deletedView = view.findViewById(R.id.deleted_layout) as View
    val titleView = view.findViewById(R.id.gpx_content_title) as TextView
    val dateView = view.findViewById(R.id.gpx_content_date) as TextView
    val exportButton = view.findViewById(R.id.gpx_content_export_button) as Button
    val distanceView = view.findViewById(R.id.gpx_content_distance) as TextView
    val waypointCountView = view.findViewById(R.id.gpx_content_waypoint_count) as TextView
    private val exportProgressBar = view.findViewById(R.id.gpx_content_export_progress_bar) as ProgressBar
    var previewView = view.findViewById(R.id.preview_view) as PathPreviewView
    var previewImageView = view.findViewById(R.id.preview_image) as ImageView

    init {
        deletedView.visibility = View.GONE
        exportProgressBar.isIndeterminate = true
        exportProgressBar.visibility = View.GONE
    }

    fun startPreviewPointsLoader(segment: Segment?, identifier: Long): Single<List<LatLng>>? {
        return segment?.getLatLngPoints(pointLoadingThread)
                ?.observeOn(Schedulers.computation())
                ?.map { lst ->
                    lst.takeGist(50)
                }?.observeOn(AndroidSchedulers.mainThread())

    }

    fun setExportLoading(loading: Boolean) {
        exportButton.visibility = if (loading) View.INVISIBLE else View.VISIBLE
        exportButton.invalidate()

        exportProgressBar.visibility = if (loading) View.VISIBLE else View.GONE
        exportProgressBar.invalidate()
    }
}