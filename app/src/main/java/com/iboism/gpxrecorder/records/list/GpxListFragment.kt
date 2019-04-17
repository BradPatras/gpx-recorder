package com.iboism.gpxrecorder.records.list

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.iboism.gpxrecorder.R
import com.iboism.gpxrecorder.model.GpxContent
import com.iboism.gpxrecorder.recording.configurator.RecordingConfiguratorModal
import com.iboism.gpxrecorder.util.PermissionHelper
import com.iboism.gpxrecorder.records.details.GpxDetailsFragment
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import kotlinx.android.synthetic.main.fragment_gpx_list.*
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.helper.ItemTouchHelper
import android.support.v7.widget.RecyclerView
import com.iboism.gpxrecorder.Events
import com.iboism.gpxrecorder.recording.RecorderServiceConnection
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class GpxListFragment : Fragment(), RecorderServiceConnection.OnServiceConnectedDelegate {
    private val placeholderViews = listOf(R.id.placeholder_menu_icon, R.id.placeholder_menu_text, R.id.placeholder_routes_text, R.id.placeholder_routes_icon)
    private val gpxContentList = Realm.getDefaultInstance().where(GpxContent::class.java).findAll().sort("date", Sort.DESCENDING)
    private var adapter: GpxRecyclerViewAdapter? = null
    private var isTransitioning = false
    private var currentlyRecordingRouteId: Long? = null
    private var serviceConnection: RecorderServiceConnection = RecorderServiceConnection(this)
    private val gpxChangeListener = { gpxContent: RealmResults<GpxContent> ->
        setPlaceholdersHidden(gpxContent.isNotEmpty())
    }

    override fun onStop() {
        super.onStop()

        EventBus.getDefault().apply {
            unregister(adapter)
            unregister(this@GpxListFragment)
        }
    }

    override fun onServiceConnected(serviceConnection: RecorderServiceConnection) {
        currentlyRecordingRouteId = serviceConnection.service?.gpxId
        updateCurrentRecordingView(currentlyRecordingRouteId)
    }

    override fun onServiceDisconnected() {
        currentlyRecordingRouteId = null
        updateCurrentRecordingView(null)
    }

    @Subscribe(sticky = true)
    fun onServiceStartedEvent(event: Events.RecordingStartedEvent) {
        serviceConnection.requestConnection(requireContext())
    }

    @Subscribe()
    fun onServiceStoppedEvent(event: Events.RecordingStoppedEvent) {
        currentlyRecordingRouteId = null
        updateCurrentRecordingView(null)
    }

    private fun updateCurrentRecordingView(gpxId: Long?) {
        val realm = Realm.getDefaultInstance()
        val gpx = GpxContent.withId(gpxId, realm)
        realm.close()

        if (gpx == null) {
            current_recording_view.visibility = View.GONE
            return
        }

        current_recording_view.visibility = View.VISIBLE
        current_recording_view.apply {
            routeTitle.text = gpx.title
        }
    }

    private fun hideRecordingView() {

    }

    private fun showRecordingView() {

    }

    private fun onFabClicked(view: View) {
        if (isTransitioning) return

        PermissionHelper.getInstance(this.activity!!).checkLocationPermissions(onAllowed = {
            RecordingConfiguratorModal.circularReveal(
                    originXY = Pair(view.x.toInt() + (view.width / 2), view.y.toInt() + (view.height / 2)),
                    fragmentManager = fragmentManager
            )
        })
    }

    private fun openContentViewer(gpxId: Long) {
        if (isTransitioning) return
        isTransitioning = true
        fragmentManager?.beginTransaction()
                ?.setCustomAnimations(R.anim.slide_in_right, android.R.anim.fade_out, R.anim.none, android.R.anim.slide_out_right)
                ?.replace(R.id.content_container, GpxDetailsFragment.newInstance(gpxId))
                ?.addToBackStack("view")
                ?.commit()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_gpx_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fab.setOnClickListener(this::onFabClicked)
        val adapter = GpxRecyclerViewAdapter(view.context, gpxContentList)
        adapter.contentViewerOpener = this::openContentViewer

        EventBus.getDefault().apply {
            register(adapter)
            register(this@GpxListFragment)
        }

        this.adapter = adapter

        ItemTouchHelper(GpxListSwipeHandler(adapter::rowDismissed)).attachToRecyclerView(gpx_listView)
        gpx_listView.layoutManager = LinearLayoutManager(view.context)
        gpx_listView.adapter = adapter
        gpx_listView.setHasFixedSize(true)

        setPlaceholdersHidden(gpxContentList.isNotEmpty())
        gpxContentList.addChangeListener(gpxChangeListener)

        isTransitioning = false

        gpx_listView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0 && fab.visibility == View.VISIBLE) {
                    fab.hide()
                } else if (dy < 0 && fab.visibility != View.VISIBLE) {
                    fab.show()
                }
            }
        })
    }

    private fun setPlaceholdersHidden(hidden: Boolean) {
        fragment_gpx_list?.let { root ->
            if (hidden) {
                placeholderViews.forEach {
                    root.findViewById<View>(it).apply {
                        this.visibility = View.GONE
                        this.alpha = 0f
                    }
                }
            } else {
                placeholderViews.forEach {
                    root.findViewById<View>(it).apply {
                        this.visibility = View.VISIBLE
                        this.animate().alpha(2.0f).start()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        gpxContentList.removeChangeListener(gpxChangeListener)
        gpx_listView.adapter = null
    }

    companion object {
        fun newInstance() = GpxListFragment()
    }
}