package com.iboism.gpxrecorder.records.list

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.iboism.gpxrecorder.Events
import com.iboism.gpxrecorder.Keys
import com.iboism.gpxrecorder.R
import com.iboism.gpxrecorder.databinding.ListRowActiveRouteBinding
import com.iboism.gpxrecorder.databinding.ListRowDeletedRouteBinding
import com.iboism.gpxrecorder.databinding.ListRowRouteBinding
import com.iboism.gpxrecorder.model.GpxContent
import com.iboism.gpxrecorder.recording.LocationRecorderService
import com.iboism.gpxrecorder.recording.RecorderServiceConnection
import com.iboism.gpxrecorder.recording.waypoint.CreateWaypointDialogActivity
import com.iboism.gpxrecorder.util.DateTimeFormatHelper
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.realm.OrderedRealmCollection
import io.realm.Realm
import io.realm.RealmRecyclerViewAdapter
import org.greenrobot.eventbus.Subscribe
import java.util.concurrent.TimeUnit

/**
 * Created by bradpatras on 6/15/18.
 */
private const val VIEW_TYPE_DELETED = 1
private const val VIEW_TYPE_CURRENT = 2

class GpxRecyclerViewAdapter(
        val context: Context,
        contentList: OrderedRealmCollection<GpxContent>,
) : RealmRecyclerViewAdapter<GpxContent, RecyclerView.ViewHolder>(contentList, true),
        RecorderServiceConnection.OnServiceConnectedDelegate {
    private var hiddenRowIdentifiers: MutableList<Long> = mutableListOf()
    private var currentlyRecordingRouteId: Long? = null
    private var serviceConnection: RecorderServiceConnection = RecorderServiceConnection(this)
    var contentViewerOpener: ((gpxId: Long) -> Unit)? = null
    var currentRecordingOpener: (() -> Unit)? = null

    init {
        setHasStableIds(true)
    }

    override fun onServiceConnected(serviceConnection: RecorderServiceConnection) {
        currentlyRecordingRouteId = serviceConnection.service?.gpxId
        currentlyRecordingRouteId?.let { notifyItemChanged(it) }
    }

    override fun onServiceDisconnected() {
        val changedId = currentlyRecordingRouteId
        currentlyRecordingRouteId = null
        changedId?.let { notifyItemChanged(it) }
    }

    @Subscribe(sticky = true)
    fun onServiceStartedEvent(event: Events.RecordingStartedEvent) {
        serviceConnection.requestConnection(context)
    }

    @Subscribe
    fun onServiceStoppedEvent(event: Events.RecordingStoppedEvent) {
        val changedId = currentlyRecordingRouteId
        currentlyRecordingRouteId = null
        changedId?.let { notifyItemChanged(it) }
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        serviceConnection.disconnect(context)
    }

    override fun getItemId(position: Int): Long {
        return data?.get(position)?.identifier ?: -1
    }

    override fun getItemViewType(position: Int): Int {
        val identifier: Long = getItem(position)?.identifier ?: return super.getItemViewType(position)
        return when {
            hiddenRowIdentifiers.contains(identifier) -> VIEW_TYPE_DELETED
            currentlyRecordingRouteId == identifier -> VIEW_TYPE_CURRENT
            else -> super.getItemViewType(position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType) {
            VIEW_TYPE_DELETED -> onCreateDeletedViewHolder(parent)
            VIEW_TYPE_CURRENT -> onCreateCurrentRecordingViewHolder(parent)
            else -> onCreateContentViewHolder(parent)
        }
    }

    private fun onCreateContentViewHolder(parent: ViewGroup): RouteRowViewHolder {
        val binding = ListRowRouteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val holder = RouteRowViewHolder(binding)

        holder.rootView.setOnClickListener {
            contentViewerOpener?.invoke(holder.itemId)
        }

        return holder
    }

    private fun onCreateDeletedViewHolder(parent: ViewGroup): DeletedRouteRowViewHolder {
        val binding = ListRowDeletedRouteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DeletedRouteRowViewHolder(binding)
    }

    private fun onCreateCurrentRecordingViewHolder(parent: ViewGroup): ActiveRouteRowViewHolder {
        val binding = ListRowActiveRouteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val holder = ActiveRouteRowViewHolder(binding)
        holder.rootView.setOnClickListener {
            currentRecordingOpener?.invoke()
        }
        holder.addWaypointButton.setOnClickListener(this::addWaypointButtonClicked)
        holder.playPauseButton.setOnClickListener(this::playPauseButtonClicked)
        holder.stopButton.setOnClickListener(this::stopButtonClicked)

        return  holder
    }

    private fun notifyItemChanged(id: Long) {
        val itemIndex = data?.indexOfFirst { it.identifier == id } ?: return

        notifyItemChanged(itemIndex)
    }

    private fun addWaypointButtonClicked(view: View) {
        currentlyRecordingRouteId?.let {
            context.startActivity(Intent(context, CreateWaypointDialogActivity::class.java).putExtra(Keys.GpxId, it))
        }
    }

    private fun playPauseButtonClicked(view: View) {
        serviceConnection.service?.let { recordingService ->
            currentlyRecordingRouteId?.let { notifyItemChanged(it) }
            if (recordingService.isPaused) {
                recordingService.resumeRecording()
            } else {
                recordingService.pauseRecording()
            }
        }
    }

    private fun stopButtonClicked(view: View) {
        LocationRecorderService.requestStopRecording(context)
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        when (viewHolder) {
            is RouteRowViewHolder -> bindContentViewHolder(viewHolder, position)
            is DeletedRouteRowViewHolder -> bindDeletedViewHolder(viewHolder, position)
            is ActiveRouteRowViewHolder -> bindCurrentViewHolder(viewHolder, position)
        }
    }

    private fun bindCurrentViewHolder(viewHolder: ActiveRouteRowViewHolder, position: Int) {
        val gpx = getItem(position) ?: return
        viewHolder.routeTitle.text = gpx.title
        val playPauseText = if(serviceConnection.service?.isPaused == true) R.string.resume_recording else R.string.pause_recording
        viewHolder.playPauseButton.text = context.getString(playPauseText)
    }

    private fun bindContentViewHolder(viewHolder: RouteRowViewHolder, position: Int) {
        val gpx = getItem(position) ?: return
        val context = viewHolder.rootView.context

        viewHolder.rootView.x = 0f
        viewHolder.dateView.text = DateTimeFormatHelper.toReadableString(gpx.date)
        viewHolder.titleView.text = gpx.title
        viewHolder.waypointCountView.text = context.resources.getQuantityString(R.plurals.waypoint_count, gpx.waypointList.size, gpx.waypointList.size)

        val segment = gpx.trackList.firstOrNull()?.segments?.firstOrNull()
        val distance = segment?.distance ?: 0f
        viewHolder.distanceView.text = context.resources.getString(R.string.distance_km, distance)
    }

    private fun bindDeletedViewHolder(viewHolder: DeletedRouteRowViewHolder, position: Int) {
        viewHolder.rootView.setOnClickListener {
            unDismissRow(viewHolder.itemId, position)
        }
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
        realm.executeTransaction {
            data?.where()?.equalTo(GpxContent.primaryKey, identifier)?.findFirst()?.deleteFromRealm()
            hiddenRowIdentifiers.remove(identifier)
        }
        realm.close()
    }
}