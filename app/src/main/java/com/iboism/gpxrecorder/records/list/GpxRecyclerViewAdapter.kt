package com.iboism.gpxrecorder.records.list

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.iboism.gpxrecorder.Events
import com.iboism.gpxrecorder.R
import com.iboism.gpxrecorder.glide.GpxModelKey
import com.iboism.gpxrecorder.model.GpxContent
import com.iboism.gpxrecorder.recording.RecorderServiceConnection
import com.iboism.gpxrecorder.util.*
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.realm.RealmRecyclerViewAdapter
import io.realm.OrderedRealmCollection
import io.realm.Realm
import org.greenrobot.eventbus.Subscribe
import java.util.concurrent.TimeUnit

/**
 * Created by bradpatras on 6/15/18.
 */
private const val VIEW_TYPE_DELETED = 1
private const val VIEW_TYPE_CURRENT = 2

class GpxRecyclerViewAdapter(val context: Context, contentList: OrderedRealmCollection<GpxContent>) : RealmRecyclerViewAdapter<GpxContent, RecyclerView.ViewHolder>(contentList, true), RecorderServiceConnection.OnServiceConnectedDelegate {
    private var fileHelper: FileHelper? = null
    private var hiddenRowIdentifiers: MutableList<Long> = mutableListOf()
    private var currentlyRecordingRouteId: Long? = null
    private var serviceConnection: RecorderServiceConnection = RecorderServiceConnection(this)
    var contentViewerOpener: ((gpxId: Long) -> Unit)? = null

    init {
        setHasStableIds(true)
    }

    override fun onServiceConnected(serviceConnection: RecorderServiceConnection) {
        currentlyRecordingRouteId = serviceConnection.service?.gpxId
        notifyDataSetChanged()
    }

    override fun onServiceDisconnected() {
        currentlyRecordingRouteId = null
        notifyDataSetChanged()
    }

    @Subscribe(sticky = true)
    fun onServiceStartedEvent(event: Events.RecordingStartedEvent) {
        serviceConnection.requestConnection(context)
    }

    @Subscribe()
    fun onServiceStoppedEvent(event: Events.RecordingStoppedEvent) {
        currentlyRecordingRouteId = null

    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        fileHelper = null
    }

    override fun getItemId(position: Int): Long {
        return data?.get(position)?.identifier ?: -1
    }

    override fun getItemViewType(position: Int): Int {
        val identifier: Long = getItem(position)?.identifier ?: return super.getItemViewType(position)
        return if (hiddenRowIdentifiers.contains(identifier)) {
            VIEW_TYPE_DELETED
        } else if (currentlyRecordingRouteId == identifier) {
            VIEW_TYPE_CURRENT
        } else {
            super.getItemViewType(position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (fileHelper == null) {
            fileHelper = FileHelper.getInstance()
        }

        return when(viewType) {
            VIEW_TYPE_DELETED -> onCreateDeletedViewHolder(parent)
            VIEW_TYPE_CURRENT -> onCreateCurrentRecordingViewHolder(parent)
            else -> onCreateContentViewHolder(parent)
        }
    }

    private fun onCreateContentViewHolder(parent: ViewGroup): GpxContentViewHolder {
        val rowView = LayoutInflater.from(parent.context).inflate(R.layout.list_row_gpx_content, parent, false)
        val holder = GpxContentViewHolder(rowView)

        holder.rootView.setOnClickListener {
            contentViewerOpener?.invoke(holder.itemId)
        }

        holder.exportButton.setOnClickListener {
            fileHelper?.apply {
                holder.setExportLoading(true)
                shareGpxFile(it.context, holder.itemId).subscribe {
                    holder.setExportLoading(false)
                }
            }
        }

        return holder
    }

    private fun onCreateDeletedViewHolder(parent: ViewGroup): DeletedViewHolder {
        val rowView = LayoutInflater.from(parent.context).inflate(R.layout.list_row_deleted, parent, false)
        val holder = DeletedViewHolder(rowView)

        holder.rootView.setOnClickListener {
            unDismissRow(holder.itemId, holder.adapterPosition)
        }

        return holder
    }

    private fun onCreateCurrentRecordingViewHolder(parent: ViewGroup): CurrentRecordingViewHolder {
        val rowView = LayoutInflater.from(parent.context).inflate(R.layout.list_row_current_route, parent, false)
        val holder = CurrentRecordingViewHolder(rowView)
        holder.rootView.visibility = View.GONE
        return holder
    }

        override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        when (viewHolder) {
            is GpxContentViewHolder -> bindContentViewHolder(viewHolder, position)
            is DeletedViewHolder -> bindDeletedViewHolder(viewHolder, position)
            is CurrentRecordingViewHolder -> bindCurrentRecordingiewHolder(viewHolder, position)
        }
    }

    private fun bindContentViewHolder(viewHolder: GpxContentViewHolder, position: Int) {
        val gpx = getItem(position) ?: return
        val context = viewHolder.rootView.context

        viewHolder.rootView.x = 0f
        viewHolder.dateView.text = DateTimeFormatHelper.toReadableString(gpx.date)
        viewHolder.titleView.text = gpx.title
        viewHolder.waypointCountView.text = context.resources.getQuantityString(R.plurals.waypoint_count, gpx.waypointList.size, gpx.waypointList.size)

        val segment = gpx.trackList.firstOrNull()?.segments?.firstOrNull()
        val distance = segment?.distance ?: 0f
        viewHolder.distanceView.text = context.resources.getString(R.string.distance_km, distance)
        Glide.with(context)
                .load(GpxModelKey(gpx.identifier))
                .placeholder(R.drawable.preview_placeholder)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .into(viewHolder.previewImageView)

        viewHolder.setExportLoading(fileHelper?.isExporting() == gpx.identifier)
    }

    private fun bindDeletedViewHolder(viewHolder: DeletedViewHolder, position: Int) {
        // todo no op?
    }

    private fun bindCurrentRecordingiewHolder(viewHolder: CurrentRecordingViewHolder, position: Int) {
        //todo
    }

    private fun unDismissRow(identifier: Long, viewPosition: Int) {
        hiddenRowIdentifiers.remove(identifier)
        notifyItemChanged(viewPosition)
    }

    fun rowDismissed(position: Int) {
        val identifier: Long = getItem(position)?.identifier ?: return

        if (identifier == currentlyRecordingRouteId || hiddenRowIdentifiers.contains(identifier)) {
            notifyItemChanged(position)
            return
        }

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
            hiddenRowIdentifiers.remove(identifier)
        }
        realm.close()
    }
}