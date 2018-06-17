package com.iboism.gpxrecorder.primary

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import com.google.android.gms.maps.model.LatLng
import com.iboism.gpxrecorder.R
import com.iboism.gpxrecorder.model.GpxContent
import com.iboism.gpxrecorder.util.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.realm.RealmRecyclerViewAdapter
import io.realm.OrderedRealmCollection
import io.realm.Realm
import kotlin.math.PI
import kotlin.math.absoluteValue
import kotlin.math.atan2

/**
 * Created by bradpatras on 6/15/18.
 */
class GpxRecyclerViewAdapter(contentList: OrderedRealmCollection<GpxContent>) : RealmRecyclerViewAdapter<GpxContent, GpxRecyclerViewAdapter.GpxViewHolder>(contentList, true) {
    private var fileHelper: FileHelper? = null
    private val pointLoadingThread = Schedulers.single()
    private var cachedPoints = Array<List<LatLng>?>(contentList.size, { null })
    var contentViewerOpener: ((gpxId: Long) -> Unit)? = null

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long {
        return data?.get(position)?.identifier ?: -1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GpxViewHolder {
        val gpxRow = LayoutInflater.from(parent.context).inflate(R.layout.list_row_gpx_content, parent, false)

        if (fileHelper == null) {
            fileHelper = FileHelper.getInstance(parent.context)
        }

        return GpxViewHolder(gpxRow)
    }

    override fun onBindViewHolder(viewHolder: GpxViewHolder, position: Int) {
        val gpx = getItem(position) ?: return
        viewHolder.position = position
        val context = viewHolder.rootView.context

        viewHolder.dateView.text = DateTimeFormatHelper.toReadableString(gpx.date)
        viewHolder.titleView.text = gpx.title
        viewHolder.waypointCountView.text = context.resources.getQuantityString(R.plurals.waypoint_count, gpx.waypointList.size, gpx.waypointList.size)
        val segment = gpx.trackList.firstOrNull()?.segments?.firstOrNull()
        val distance = segment?.distance ?: 0f
        viewHolder.distanceView.text = context.resources.getString(R.string.distance_km, distance)

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

        viewHolder.rootView.setOnTouchListener(getSwipeHandler(position))

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
        var position: Int? = null

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

    private fun getSwipeHandler(position: Int): View.OnTouchListener {
        return object: View.OnTouchListener {
            private var startX = 0f
            private var startY = 0f
            private var deltaX = 0f
            private var deltaY = 0f
            private var currentTouch = false
            private val position = position

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when(event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        startX = event.rawX
                        startY = event.rawY
                        return false
                    }
                    MotionEvent.ACTION_UP -> {
                        if (currentTouch && (deltaX.absoluteValue < 6 || deltaY.absoluteValue < 6)) {
                            cancelSwipe(v)
                            return false
                        }

                        if (currentTouch && deltaX.absoluteValue > v.width / 3) {
                            v.animate()
                                    .translationXBy(deltaX * 3f)
                                    .withEndAction {
                                        Realm.getDefaultInstance().executeTransaction {
                                            getItem(position)?.deleteFromRealm()
                                            val mutableCache = cachedPoints.toMutableList()
                                            mutableCache.removeAt(position)
                                            cachedPoints = mutableCache.toTypedArray()
                                        }
                                        notifyItemRemoved(this.position)
                                        notifyItemRangeChanged(this.position, data?.size ?: 0)
                                    }.start()

                            return true
                        }

                        return cancelSwipe(v)
                    }
                    MotionEvent.ACTION_MOVE -> {
                        deltaX = (event.rawX - startX)
                        deltaY = (event.rawY - startY)
                        val touchAngle = atan2(deltaY.absoluteValue, deltaX.absoluteValue) * (180 / PI)
                        val horizontalSwipe = touchAngle in -1f..30f

                        if (!currentTouch && horizontalSwipe) {
                            currentTouch = true
                            startX = event.rawX
                            return true
                        }

                        if (currentTouch) {
                            v.parent.requestDisallowInterceptTouchEvent(true)

                            v.x = deltaX
                            v.alpha = (v.width - (deltaX.absoluteValue * 1.3f)) / v.width
                            return true
                        }

                        return false
                    }
                    MotionEvent.ACTION_CANCEL -> {
                        return cancelSwipe(v)
                    }
                    MotionEvent.ACTION_SCROLL -> {
                        return cancelSwipe(v)
                    }
                    else -> {
                        return false
                    }
                }
            }

            private fun cancelSwipe(v: View): Boolean{
                if (currentTouch) {
                    v.parent.requestDisallowInterceptTouchEvent(false)
                    currentTouch = false
                    deltaX = 0f
                    deltaY = 0f
                    v.animate()
                            .x(0f)
                            .alpha(1f)
                    return true
                }

                return false
            }
        }
    }
}