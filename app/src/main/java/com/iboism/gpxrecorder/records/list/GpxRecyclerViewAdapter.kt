package com.iboism.gpxrecorder.records.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.model.LatLng
import com.iboism.gpxrecorder.R
import com.iboism.gpxrecorder.model.GpxContent
import com.iboism.gpxrecorder.util.*
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.realm.RealmRecyclerViewAdapter
import io.realm.OrderedRealmCollection
import io.realm.Realm
import java.util.concurrent.TimeUnit

/**
 * Created by bradpatras on 6/15/18.
 */
private const val VIEW_TYPE_DELETED = 1

class GpxRecyclerViewAdapter(contentList: OrderedRealmCollection<GpxContent>) : RealmRecyclerViewAdapter<GpxContent, GpxViewHolder>(contentList, true) {
    private var fileHelper: FileHelper? = null

    private var hiddenRowIdentifiers: MutableList<Long> = mutableListOf()
    var contentViewerOpener: ((gpxId: Long) -> Unit)? = null

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
            holder.exportButton.setOnClickListener { _ ->
                fileHelper?.apply {
                    holder.setExportLoading(true)
                    shareGpxFile(holder.itemId).subscribe {
                        holder.setExportLoading(false)
                    }
                }
            }
        }

        return holder
    }

    override fun onViewDetachedFromWindow(holder: GpxViewHolder) {
        previewLoaders[holder.itemId]?.dispose()
        previewLoaders[holder.itemId] = null

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
                    ?.subscribe { gist ->
                        cachedPoints[gpx.identifier] = gist

                        if (viewHolder.itemId == gpx.identifier)
                            viewHolder.previewView.loadPoints(gist)
                    }
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

        val delayedDelete = Single.just(identifier)
                .delay(3, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { id: Long ->
                    if (hiddenRowIdentifiers.contains(id)) deleteRow(id)
                }
    }

    private fun deleteRow(identifier: Long) {
        val realm = Realm.getDefaultInstance()
        realm.executeTransaction { _ ->
            data?.where()?.equalTo(GpxContent.primaryKey, identifier)?.findFirst()?.deleteFromRealm()
            previewLoaders.remove(identifier)?.dispose()
            hiddenRowIdentifiers.remove(identifier)
        }
        realm.close()
    }
}