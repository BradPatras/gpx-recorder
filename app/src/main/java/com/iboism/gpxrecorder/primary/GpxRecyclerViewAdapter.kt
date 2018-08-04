package com.iboism.gpxrecorder.primary

import android.support.design.internal.BaselineLayout
import android.support.design.widget.BaseTransientBottomBar
import android.support.design.widget.Snackbar
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import com.google.android.gms.maps.model.LatLng
import com.iboism.gpxrecorder.R
import com.iboism.gpxrecorder.model.GpxContent
import com.iboism.gpxrecorder.model.Segment
import com.iboism.gpxrecorder.util.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.realm.RealmRecyclerViewAdapter
import io.realm.OrderedRealmCollection
import io.realm.Realm

/**
 * Created by bradpatras on 6/15/18.
 */
class GpxRecyclerViewAdapter(contentList: OrderedRealmCollection<GpxContent>) : RealmRecyclerViewAdapter<GpxContent, GpxRecyclerViewAdapter.GpxViewHolder>(contentList, true) {
    private val VIEW_TYPE_DELETED = 1
    private var fileHelper: FileHelper? = null
    private val pointLoadingThread = Schedulers.single()
    private var deleted: Triple<Int, GpxContent?, List<LatLng>?>? = null
    private var cachedPoints = MutableList<List<LatLng>?>(contentList.size, { null })
    private var hiddenRowIndicies: MutableList<Int> = mutableListOf()
    var contentViewerOpener: ((gpxId: Long) -> Unit)? = null
    var snackbarProvider: SnackbarProvider? = null

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long {
        return data?.get(position)?.identifier ?: -1
    }

    override fun getItemViewType(position: Int): Int {
        return if (hiddenRowIndicies.contains(position)) VIEW_TYPE_DELETED else super.getItemViewType(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GpxViewHolder {
        val gpxRow = LayoutInflater.from(parent.context).inflate(R.layout.list_row_gpx_content, parent, false)

        if (fileHelper == null) {
            fileHelper = FileHelper.getInstance(parent.context)
        }

        return GpxViewHolder(gpxRow)
    }

    override fun onViewDetachedFromWindow(holder: GpxViewHolder?) {
        super.onViewDetachedFromWindow(holder)
        holder?.previewPointsLoader?.dispose()
    }

    override fun onBindViewHolder(viewHolder: GpxViewHolder, position: Int) {
        val gpx = getItem(position) ?: return
        val context = viewHolder.rootView.context
        if (viewHolder.itemViewType == VIEW_TYPE_DELETED) {
            val params = viewHolder.rootView.layoutParams
            params.height = 0
            viewHolder.rootView.layoutParams = params
            return
        }

        viewHolder.rootView.x = 0f
        viewHolder.dateView.text = DateTimeFormatHelper.toReadableString(gpx.date)
        viewHolder.titleView.text = gpx.title
        viewHolder.waypointCountView.text = context.resources.getQuantityString(R.plurals.waypoint_count, gpx.waypointList.size, gpx.waypointList.size)
        val segment = gpx.trackList.firstOrNull()?.segments?.firstOrNull()
        val distance = segment?.distance ?: 0f
        viewHolder.distanceView.text = context.resources.getString(R.string.distance_km, distance)

        viewHolder.previewView.setLoading()
        val previewPoints = cachedPoints[position]
        if (previewPoints != null) {
            viewHolder.previewView.loadPoints(previewPoints)
        } else {
            viewHolder.startPreviewPointsLoader(segment, position)
        }

        viewHolder.setExportLoading(fileHelper?.isExporting() == gpx.identifier)

        viewHolder.exportButton.setOnClickListener {
            fileHelper?.let {
                viewHolder.setExportLoading(true)
                it.gpxFileWith(gpx.identifier)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe { file, error ->
                            if (error != null) {
                                viewHolder.setExportLoading(false)
                                Alerts(context).genericError(R.string.file_share_failed).show()
                            } else {
                                ShareHelper(it.context).shareFile(file)
                                viewHolder.setExportLoading(false)
                            }
                        }
            }
        }

        viewHolder.rootView.setOnClickListener {
            contentViewerOpener?.invoke(gpx.identifier)
        }
    }

    fun unDeleteRow() {
        val position = hiddenRowIndicies.removeAt(hiddenRowIndicies.lastIndex)
        notifyItemChanged(position)
    }

    fun rowDismissed(position: Int) {
        hiddenRowIndicies.add(position)
        notifyItemChanged(position)
        showUndoSnackbar()
    }

    private fun showUndoSnackbar() {
        val snackbar = snackbarProvider?.getSnackbar() ?: return
        snackbar.setAction("UNDO", { _ -> unDeleteRow() })
        snackbar.addCallback(UndoDeleteSnackbarCallback())
        snackbar.show()
    }

    fun deleteRow() {
        val position = hiddenRowIndicies[hiddenRowIndicies.lastIndex]
        Realm.getDefaultInstance().executeTransaction { realm ->
            val item = getItem(position) ?: return@executeTransaction
            deleted = Triple(position, realm.copyFromRealm(item), cachedPoints.removeAt(position))
            item.deleteFromRealm()
            notifyItemRemoved(position)
            hiddenRowIndicies.removeAt(hiddenRowIndicies.lastIndex)
                //notifyItemRangeChanged(position, data?.size ?: 0)
        }
    }

    inner class GpxViewHolder(view: View?): RecyclerView.ViewHolder(view) {
        val rootView = view as View
        val titleView = view?.findViewById(R.id.gpx_content_title) as TextView
        val dateView = view?.findViewById(R.id.gpx_content_date) as TextView
        val exportButton = view?.findViewById(R.id.gpx_content_export_button) as Button
        val distanceView = view?.findViewById(R.id.gpx_content_distance) as TextView
        val waypointCountView = view?.findViewById(R.id.gpx_content_waypoint_count) as TextView
        val exportProgressBar = view?.findViewById(R.id.gpx_content_export_progress_bar) as ProgressBar
        var previewView = view?.findViewById(R.id.preview_view) as PathPreviewView

        var previewPointsLoader: Disposable? = null

        init {
            exportProgressBar.isIndeterminate = true
            exportProgressBar.visibility = View.GONE
        }

        fun startPreviewPointsLoader(segment: Segment?, position: Int) {
            previewPointsLoader = segment?.getLatLngPoints(pointLoadingThread)
                    ?.observeOn(Schedulers.computation())
                    ?.map { lst ->
                        lst.takeGist(50)
                    }?.observeOn(AndroidSchedulers.mainThread())
                    ?.subscribe { gist ->
                        cachedPoints[position] = gist
                        if (position == adapterPosition)
                            previewView.loadPoints(gist)
                    }
        }

        fun setExportLoading(loading: Boolean) {
            exportButton.visibility = if (loading) View.INVISIBLE else View.VISIBLE
            exportButton.invalidate()

            exportProgressBar.visibility = if (loading) View.VISIBLE else View.GONE
            exportProgressBar.invalidate()
        }
    }

    inner class UndoDeleteSnackbarCallback: Snackbar.Callback() {
        override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
            super.onDismissed(transientBottomBar, event)
            if (event == BaseTransientBottomBar.BaseCallback.DISMISS_EVENT_TIMEOUT) {
                deleteRow()
            }
        }
    }
}