package com.iboism.gpxrecorder.recording

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.iboism.gpxrecorder.Events
import com.iboism.gpxrecorder.Keys
import com.iboism.gpxrecorder.R
import com.iboism.gpxrecorder.databinding.FragmentActiveRouteDetailsBinding
import com.iboism.gpxrecorder.model.GpxContent
import com.iboism.gpxrecorder.recording.waypoint.CreateWaypointDialogActivity
import com.iboism.gpxrecorder.records.details.MapController
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.realm.Realm
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.util.concurrent.TimeUnit

class RecorderFragment : Fragment(), RecorderServiceConnection.OnServiceConnectedDelegate {
    private var gpxId: Long? = null
    private var mapController: MapController? = null
    private var serviceConnection: RecorderServiceConnection = RecorderServiceConnection(this)
    private var observableInterval: Observable<Long> = Observable.interval(5, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread())
    private var intervalObserver: Disposable? = null
    private lateinit var binding: FragmentActiveRouteDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gpxId = arguments?.get(Keys.GpxId) as? Long
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = FragmentActiveRouteDetailsBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val gpxId = gpxId ?: return

        binding.addWptBtn.setOnClickListener(this::addWaypointButtonClicked)
        binding.playpauseBtn.setOnClickListener(this::playPauseButtonClicked)
        binding.stopBtn.setOnClickListener(this::stopButtonClicked)

        updateUI(gpxId)

        binding.mapView.let {
            it.onCreate(savedInstanceState)
            val controller = MapController(it, gpxId)
            mapController?.shouldDrawEnd = false
            it.getMapAsync(controller)
            mapController = controller
        }
    }

    private fun addWaypointButtonClicked(view: View) {
        context?.startActivity(Intent(context, CreateWaypointDialogActivity::class.java).putExtra(Keys.GpxId, gpxId))
    }

    private fun playPauseButtonClicked(view: View) {
        serviceConnection.service?.let {
            if (it.isPaused) {
                it.resumeRecording()
            } else {
                it.pauseRecording()
            }
        }
    }

    private fun stopButtonClicked(view: View) {
        context?.let {
            LocationRecorderService.requestStopRecording(it)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        EventBus.getDefault().register(this)
    }

    override fun onDetach() {
        super.onDetach()
        EventBus.getDefault().unregister(this)
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()

        intervalObserver = observableInterval.subscribe {
            mapController?.redraw()
        }
    }

    override fun onDestroyView() {
        binding.mapView.onDestroy()
        mapController?.onDestroy()
        super.onDestroyView()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
        intervalObserver?.dispose()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }

    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        binding.mapView.onSaveInstanceState(outState)
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

        val update = Single.just(Pair(gpxContent.title, isPaused))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { pair ->
                    binding.routeTitleTv.text = pair.first
                    val pauseResumeString = if (pair.second) R.string.resume_recording else R.string.pause_recording
                    binding.playpauseBtn.setText(pauseResumeString)
                    mapController?.redraw()
                }
        realm.close()
    }

    override fun onServiceConnected(serviceConnection: RecorderServiceConnection) {
        updateUI(gpxId)
    }

    override fun onServiceDisconnected() {
        dismiss()
    }

    @Subscribe(sticky = true)
    fun onServiceStartedEvent(event: Events.RecordingStartedEvent) {
        requestServiceConnectionIfNeeded()
    }

    @Subscribe
    fun onServiceStoppedEvent(event: Events.RecordingStoppedEvent) {
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
        parentFragmentManager?.popBackStack()
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
