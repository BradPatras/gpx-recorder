package com.iboism.gpxrecorder.primary

import android.support.v7.widget.RecyclerView
import android.text.Layout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import com.google.android.gms.maps.model.LatLng
import com.iboism.gpxrecorder.R
import com.iboism.gpxrecorder.model.GpxContent
import com.iboism.gpxrecorder.util.FileHelper
import io.reactivex.schedulers.Schedulers
import io.realm.RealmRecyclerViewAdapter
import io.realm.OrderedRealmCollection

/**
 * Created by bradpatras on 6/15/18.
 */
class GpxRecyclerViewAdapter(contentList: OrderedRealmCollection<GpxContent>) : RealmRecyclerViewAdapter<GpxContent, GpxRecyclerViewAdapter.GpxViewHolder>(data, true) {
    private var fileHelper: FileHelper? = null
    private val pointLoadingThread = Schedulers.single()
    private var cachedPoints = Array<List<LatLng>?>(data.size, { null })
    var contentViewerOpener: ((gpxId: Long) -> Unit)? = null

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long {
        return data?.get(position)?.identifier ?: -1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GpxViewHolder {
        val gpxRow = LayoutInflater.from(parent.context).inflate(R.layout.list_row_gpx_content, parent)
        return GpxViewHolder(gpxRow)
    }

    override fun onBindViewHolder(holder: GpxViewHolder?, position: Int) {
        val content = getItem(position) ?: return
    }

    inner class GpxViewHolder(view: View?): RecyclerView.ViewHolder(view) {
        val titleView = view?.findViewById(R.id.gpx_content_title) as TextView
        val dateView = view?.findViewById(R.id.gpx_content_date) as TextView
        val exportButton = view?.findViewById(R.id.gpx_content_export_button) as Button
        val distanceView = view?.findViewById(R.id.gpx_content_distance) as TextView
        val waypointCountView = view?.findViewById(R.id.gpx_content_waypoint_count) as TextView
        val exportProgressBar = view?.findViewById(R.id.gpx_content_export_progress_bar) as ProgressBar
        var previewView = view?.findViewById(R.id.preview_view) as PathPreviewView
        var position: Int? = null

        fun setExportLoading(loading: Boolean) {
            exportButton.visibility = if (loading) View.INVISIBLE else View.VISIBLE
            exportButton.invalidate()

            exportProgressBar.visibility = if (loading) View.VISIBLE else View.GONE
            exportProgressBar.invalidate()
        }
    }
}