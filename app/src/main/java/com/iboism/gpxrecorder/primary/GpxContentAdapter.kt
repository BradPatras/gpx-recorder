package com.iboism.gpxrecorder.primary

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ListAdapter
import android.widget.ProgressBar
import android.widget.TextView
import com.google.android.gms.maps.model.LatLng
import com.iboism.gpxrecorder.R
import com.iboism.gpxrecorder.model.GpxContent
import com.iboism.gpxrecorder.util.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.realm.RealmBaseAdapter
import io.realm.RealmResults

/**
 * Created by bradpatras on 12/8/17.
 */
class GpxContentAdapter(private val realmResults: RealmResults<GpxContent>?) : RealmBaseAdapter<GpxContent>(realmResults), ListAdapter {
    private var fileHelper: FileHelper? = null
    private val pointLoadingThread = Schedulers.single()
    private var cachedPoints = Array<List<LatLng>?>(realmResults?.size ?: 0, { null })

    var contentViewerOpener: ((gpxId: Long) -> Unit)? = null

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        val view: View
        val viewHolder: ViewHolder

        if (convertView == null) {
            view = LayoutInflater.from(parent?.context).inflate(R.layout.list_row_gpx_content, parent, false)
            viewHolder = ViewHolder(view, position)
            viewHolder.exportProgressBar.isIndeterminate = true
            viewHolder.exportProgressBar.visibility = View.GONE
            view.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as ViewHolder
            viewHolder.position = position
        }

        if (fileHelper == null) {
            fileHelper = FileHelper.getInstance(view.context)
        }

        val gpx = realmResults?.getOrNull(position) ?: return view

        viewHolder.dateView.text = DateTimeFormatHelper.toReadableString(gpx.date)
        viewHolder.titleView.text = gpx.title
        viewHolder.waypointCountView.text = view.resources.getQuantityString(R.plurals.waypoint_count, gpx.waypointList.size, gpx.waypointList.size)
        val segment = gpx.trackList.firstOrNull()?.segments?.firstOrNull()
        val distance = segment?.distance ?: 0f
        viewHolder.distanceView.text = view.resources.getString(R.string.distance_km, distance)

        viewHolder.previewView.setLoading()
        val previewPoints = cachedPoints[position]
        if (previewPoints!= null) {
            viewHolder.previewView.loadPoints(previewPoints)
        } else {
            segment?.getLatLngPoints(pointLoadingThread)
                    ?.observeOn(AndroidSchedulers.mainThread())
                    ?.subscribe { lst ->
                        val gist = lst.takeGist(50)
                        cachedPoints[position] = gist
                        if (position == viewHolder.position)
                            viewHolder.previewView.loadPoints(gist)
                    }
        }

        viewHolder.setExportLoading(fileHelper?.isExporting() == gpx.identifier)

        viewHolder.exportButton.setOnClickListener {
            fileHelper?.let {
                viewHolder.setExportLoading(true)
                it.gpxFileWith(gpx.identifier)
                        .subscribeOn(AndroidSchedulers.mainThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe { file, error ->
                            if (error != null) {
                                viewHolder.setExportLoading(false)
                                Alerts(view.context).genericError(R.string.file_share_failed).show()
                            } else {
                                ShareHelper(it.context).shareFile(file)
                                viewHolder.setExportLoading(false)
                            }
                        }
            }
        }

        view.setOnClickListener {
            contentViewerOpener?.invoke(gpx.identifier)
        }

        return view
    }

    private inner class ViewHolder(view: View?, var position: Int) {
        val titleView = view?.findViewById(R.id.gpx_content_title) as TextView
        val dateView = view?.findViewById(R.id.gpx_content_date) as TextView
        val exportButton = view?.findViewById(R.id.gpx_content_export_button) as Button
        val distanceView = view?.findViewById(R.id.gpx_content_distance) as TextView
        val waypointCountView = view?.findViewById(R.id.gpx_content_waypoint_count) as TextView
        val exportProgressBar = view?.findViewById(R.id.gpx_content_export_progress_bar) as ProgressBar
        var previewView = view?.findViewById(R.id.preview_view) as PathPreviewView

        fun setExportLoading(loading: Boolean) {
            exportButton.visibility = if (loading) View.INVISIBLE else View.VISIBLE
            exportButton.invalidate()

            exportProgressBar.visibility = if (loading) View.VISIBLE else View.GONE
            exportProgressBar.invalidate()
        }
    }
}
