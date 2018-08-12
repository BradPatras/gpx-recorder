package com.iboism.gpxrecorder.primary

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
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.realm.RealmRecyclerViewAdapter
import io.realm.OrderedRealmCollection
import io.realm.Realm
import java.util.concurrent.TimeUnit

/**
 * Created by bradpatras on 6/15/18.
 */
private const val VIEW_TYPE_DELETED = 1

class GpxRecyclerViewAdapter(contentList: OrderedRealmCollection<GpxContent>) : RealmRecyclerViewAdapter<GpxContent, GpxRecyclerViewAdapter.GpxViewHolder>(contentList, true) {
    private var fileHelper: FileHelper? = null
    private val pointLoadingThread = Schedulers.single()

    private var hiddenRowIdentifiers: MutableList<Long> = mutableListOf()
    var contentViewerOpener: ((gpxId: Long) -> Unit)? = null
    var snackbarProvider: SnackbarProvider? = null

    private var cachedPoints = mutableMapOf<Long, List<LatLng>?>()
    private var previewLoaders = mutableMapOf<Long, Disposable?>()

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long {
        return data?.get(position)?.identifier ?: -1
    }

    override fun getItemViewType(position: Int): Int {
        val identifier: Long = getItem(position)?.identifier ?: return super.getItemViewType(position)
        return if (hiddenRowIdentifiers.contains(identifier)) VIEW_TYPE_DELETED else super.getItemViewType(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GpxViewHolder {
        val gpxRow = LayoutInflater.from(parent.context).inflate(R.layout.list_row_gpx_content, parent, false)

        if (fileHelper == null) {
            fileHelper = FileHelper.getInstance(parent.context)
        }

        val holder = GpxViewHolder(gpxRow)

        if (viewType == VIEW_TYPE_DELETED) {
            holder.deletedView.setOnClickListener {
                unDismissRow(holder.itemId, holder.adapterPosition)
            }
        } else {
            holder.rootView.setOnClickListener {
                contentViewerOpener?.invoke(holder.itemId)
            }

            holder.exportButton.setOnClickListener {
                fileHelper?.let {
                    holder.setExportLoading(true)
                    it.gpxFileWith(holder.itemId)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe { file, error ->
                                if (error != null) {
                                    holder.setExportLoading(false)
                                    Alerts(parent.context).genericError(R.string.file_share_failed).show()
                                } else {
                                    ShareHelper(it.context).shareFile(file)
                                    holder.setExportLoading(false)
                                }
                            }
                }
            }

        }

        return holder
    }

    override fun onViewDetachedFromWindow(holder: GpxViewHolder?) {
        holder?.itemId?.let {
            previewLoaders[it]?.dispose()
            previewLoaders[it] = null
        }

        super.onViewDetachedFromWindow(holder)
    }

    override fun onBindViewHolder(viewHolder: GpxViewHolder, position: Int) {
        val gpx = getItem(position) ?: return
        val context = viewHolder.rootView.context

        if (viewHolder.itemViewType == VIEW_TYPE_DELETED) {
            viewHolder.contentView.visibility = View.GONE
            viewHolder.deletedView.visibility = View.VISIBLE
            return
        }

        viewHolder.contentView.visibility = View.VISIBLE
        viewHolder.deletedView.visibility = View.GONE
        viewHolder.rootView.x = 0f
        viewHolder.dateView.text = DateTimeFormatHelper.toReadableString(gpx.date)
        viewHolder.titleView.text = gpx.title
        viewHolder.waypointCountView.text = context.resources.getQuantityString(R.plurals.waypoint_count, gpx.waypointList.size, gpx.waypointList.size)

        val segment = gpx.trackList.firstOrNull()?.segments?.firstOrNull()
        val distance = segment?.distance ?: 0f
        viewHolder.distanceView.text = context.resources.getString(R.string.distance_km, distance)
        viewHolder.previewView.setLoading()

        val previewPoints = cachedPoints[gpx.identifier]
        if (previewPoints != null) {
            viewHolder.previewView.loadPoints(previewPoints)
        } else {
            previewLoaders[gpx.identifier] = viewHolder.startPreviewPointsLoader(segment, gpx.identifier)
        }

        viewHolder.setExportLoading(fileHelper?.isExporting() == gpx.identifier)

    }

    private fun unDismissRow(identifier: Long, viewPosition: Int) {
        hiddenRowIdentifiers.remove(identifier)
        notifyItemChanged(viewPosition)
    }

    fun rowDismissed(position: Int) {
        val identifier: Long = getItem(position)?.identifier ?: return

        if (hiddenRowIdentifiers.contains(identifier)) return

        hiddenRowIdentifiers.add(identifier)
        notifyItemChanged(position)

        Single.just(identifier)
                .delay(3, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { id: Long ->
                    if (hiddenRowIdentifiers.contains(id)) deleteRow(id)
                }
    }

    private fun deleteRow(identifier: Long) {
        Realm.getDefaultInstance().executeTransaction { _ ->
            data?.where()?.equalTo(GpxContent.primaryKey, identifier)?.findFirst()?.deleteFromRealm()
            previewLoaders.remove(identifier)?.dispose()
            hiddenRowIdentifiers.remove(identifier)
        }
    }

    inner class GpxViewHolder(view: View?): RecyclerView.ViewHolder(view) {
        val rootView = view as View
        val contentView = view?.findViewById(R.id.main_content_layout) as View
        val deletedView = view?.findViewById(R.id.deleted_layout) as View
        val titleView = view?.findViewById(R.id.gpx_content_title) as TextView
        val dateView = view?.findViewById(R.id.gpx_content_date) as TextView
        val exportButton = view?.findViewById(R.id.gpx_content_export_button) as Button
        val distanceView = view?.findViewById(R.id.gpx_content_distance) as TextView
        val waypointCountView = view?.findViewById(R.id.gpx_content_waypoint_count) as TextView
        val exportProgressBar = view?.findViewById(R.id.gpx_content_export_progress_bar) as ProgressBar
        var previewView = view?.findViewById(R.id.preview_view) as PathPreviewView

        init {
            deletedView.visibility = View.GONE
            exportProgressBar.isIndeterminate = true
            exportProgressBar.visibility = View.GONE
        }

        fun startPreviewPointsLoader(segment: Segment?, identifier: Long): Disposable? {
            return segment?.getLatLngPoints(pointLoadingThread)
                    ?.observeOn(Schedulers.computation())
                    ?.map { lst ->
                        lst.takeGist(50)
                    }?.observeOn(AndroidSchedulers.mainThread())
                    ?.subscribe { gist ->
                        cachedPoints[identifier] = gist

                        if (itemId == identifier)
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
}