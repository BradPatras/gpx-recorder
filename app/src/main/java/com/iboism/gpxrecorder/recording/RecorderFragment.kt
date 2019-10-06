package com.iboism.gpxrecorder.recording

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.iboism.gpxrecorder.Events
import com.iboism.gpxrecorder.Keys
import com.iboism.gpxrecorder.R
import com.iboism.gpxrecorder.model.GpxContent
import com.iboism.gpxrecorder.model.Segment
import com.iboism.gpxrecorder.records.details.MapController
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.realm.Realm
import kotlinx.android.synthetic.main.fragment_gpx_content_viewer.*
import kotlinx.android.synthetic.main.fragment_gpx_content_viewer.map_view
import kotlinx.android.synthetic.main.fragment_gpx_list.*
import kotlinx.android.synthetic.main.fragment_recorder.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class RecorderFragment : Fragment(), RecorderServiceConnection.OnServiceConnectedDelegate {
    private var gpxId: Long? = null
    private var mapController: MapController? = null
    private var serviceConnection: RecorderServiceConnection = RecorderServiceConnection(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gpxId = arguments?.get(Keys.GpxId) as? Long
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_recorder, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val gpxId = gpxId ?: return
        val realm = Realm.getDefaultInstance()
        val gpxContent = GpxContent.withId(gpxId, realm) ?: return

        gpxContent.trackList.first()?.segments?.first()?.addChangeListener<Segment> { segment ->
            updateUI(gpxId)
        }

        updateUI(gpxId)

        map_view?.let {
            it.onCreate(savedInstanceState)
            mapController = MapController(it, gpxId)
            mapController?.shouldDrawEnd = false
            it.getMapAsync(mapController)
        }

       realm.close()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        EventBus.getDefault().register(this)
    }

    override fun onDetach() {
        super.onDetach()
        EventBus.getDefault().unregister(this)
    }

    // todo look into switching to MapFragment so I don't have to do these
    override fun onResume() {
        super.onResume()
        map_view?.onResume()
    }

    override fun onDestroyView() {
        map_view?.onDestroy()
        mapController?.onDestroy()
        super.onDestroyView()
    }

    override fun onPause() {
        super.onPause()
        map_view?.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        map_view?.onLowMemory()
    }

    override fun onStart() {
        super.onStart()
        map_view?.onStart()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        map_view?.onSaveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    private fun updateUI(gpxIdOrNull: Long?) {
        //val distance = content.trackList.first()?.segments?.first()?.distance ?: 0f todo: add distance to ui
        val gpxId = gpxIdOrNull ?: return
        val realm = Realm.getDefaultInstance()
        val gpxContent = GpxContent.withId(gpxId, realm) ?: return

        var isPaused = false
        serviceConnection.service?.let {
            isPaused = it.isPaused
        }

        val updateUI = Single.just(Pair(gpxContent.title, isPaused))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { pair ->
                    route_title_tv.text = pair.first
                    val pauseResumeString = if (pair.second) R.string.resume_recording else R.string.pause_recording
                    playpause_btn.setText(pauseResumeString)
                    mapController?.redraw()
                }


    }

    override fun onServiceConnected(serviceConnection: RecorderServiceConnection) {
        updateUI(gpxId)
    }

    override fun onServiceDisconnected() {
        dismiss() // todo: show completed route details page maybe
    }

    @Subscribe(sticky = true)
    fun onServiceStartedEvent(event: Events.RecordingStartedEvent) {
        requestServiceConnectionIfNeeded()
    }

    @Subscribe
    fun onServiceStoppedEvent(event: Events.RecordingStoppedEvent) {
        // todo: show completed route details page
        serviceConnection.service = null
    }

    @Subscribe
    fun onServicePausedEvent(event: Events.RecordingPausedEvent) {
        updateUI(gpxId)
    }

    @Subscribe
    fun onServiceResumedEvent(event: Events.RecordingResumedEvent) {
        updateUI(gpxId)
    }

    private fun requestServiceConnectionIfNeeded() {
        if (serviceConnection.service == null) {
            serviceConnection.requestConnection(requireContext())
        } else {
            updateUI(serviceConnection.service?.gpxId)
        }
    }

    private fun dismiss() {
        fragmentManager?.popBackStack()
    }

    companion object {
        fun newInstance(gpxId: Long): RecorderFragment {
            val args = Bundle()
            args.putLong(Keys.GpxId, gpxId)

            val fragment = RecorderFragment()
            fragment.arguments = args

            return fragment
        }
    }
}
