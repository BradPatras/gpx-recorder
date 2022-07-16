package com.iboism.gpxrecorder.records.details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.iboism.gpxrecorder.Keys
import com.iboism.gpxrecorder.R
import com.iboism.gpxrecorder.databinding.FragmentRouteDetailsBinding
import com.iboism.gpxrecorder.export.ExportFragment
import com.iboism.gpxrecorder.model.GpxContent
import com.iboism.gpxrecorder.recording.configurator.GPX_ID_KEY
import com.iboism.gpxrecorder.recording.configurator.READ_ONLY_TITLE_KEY
import com.iboism.gpxrecorder.recording.configurator.RecordingConfiguratorModal
import com.iboism.gpxrecorder.util.DateTimeFormatHelper
import com.iboism.gpxrecorder.util.FileHelper
import com.iboism.gpxrecorder.util.Holder
import com.iboism.gpxrecorder.util.PermissionHelper
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Consumer
import io.realm.Realm

const val CREATE_FILE_INTENT_ID = 1

class GpxDetailsFragment : Fragment() {
    private lateinit var detailsView: GpxDetailsView
    private lateinit var gpxId: Holder<Long>
    private var fileHelper: FileHelper? = null
    private val compositeDisposable = CompositeDisposable()
    private var mapController: MapController? = null
    private lateinit var binding: FragmentRouteDetailsBinding

    private var gpxTitleConsumer: Consumer<in String> = Consumer {
        updateGpxTitle(it)
    }

    private val exportTouchConsumer = Consumer<Unit> {
        exportPressed()
    }

    private val mapLayerTouchConsumer = Consumer<Unit> {
        mapController?.toggleMapType()
    }

    private val deleteRouteTouchConsumer = Consumer<Unit> {
        deleteRouteAndPopFragment()
    }

    private val resumeRecordingTouchConsumer = Consumer<Unit> {
        resumeRecording(binding.resumeFab)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gpxId = Holder(requireArguments().get(Keys.GpxId) as Long)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        binding = FragmentRouteDetailsBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val realm = Realm.getDefaultInstance()
        val gpxContent = GpxContent.withId(gpxId.value, realm) ?: return
        fileHelper = FileHelper()

        val distance = gpxContent.trackList.first()?.segments?.first()?.distance ?: 0f

        detailsView = GpxDetailsView(
                binding = binding,
                titleText = gpxContent.title,
                distanceText = resources.getString(R.string.distance_km, distance),
                dateText = DateTimeFormatHelper.toReadableString(gpxContent.date),
                waypointsText = resources.getQuantityString(R.plurals.waypoint_count, gpxContent.waypointList.size, gpxContent.waypointList.size)
        )
        detailsView.restoreInstanceState(savedInstanceState)

        compositeDisposable.add(detailsView.gpxTitleObservable.subscribe(gpxTitleConsumer))
        compositeDisposable.add(detailsView.exportTouchObservable.subscribe(exportTouchConsumer))
        compositeDisposable.add(detailsView.mapTypeToggleObservable.subscribe(mapLayerTouchConsumer))
        compositeDisposable.add(detailsView.deleteRouteObservable.subscribe(deleteRouteTouchConsumer))
        compositeDisposable.add(detailsView.resumeRecordingObservable.subscribe(resumeRecordingTouchConsumer))
        realm.close()

        binding.mapView.let {
            it.onCreate(savedInstanceState)
            val controller = MapController(it, gpxId.value)
            mapController = controller
            it.getMapAsync(controller)
        }
    }

    private fun deleteRouteAndPopFragment() {
        val realm = Realm.getDefaultInstance()
        realm.executeTransaction { itRealm ->
            GpxContent.withId(gpxId.value, itRealm)?.deleteFromRealm()
        }
        realm.close()

        parentFragmentManager.popBackStack()
    }

    private fun exportPressed() {
        ExportFragment.newInstance(gpxId.value).show(parentFragmentManager, "export")
    }

    private fun resumeRecording(view: View) {
        PermissionHelper.getInstance(this.requireActivity()).checkLocationPermissions(onAllowed = {
           showConfiguratorModal()
        })
    }

    private fun showConfiguratorModal() {
        val args = Bundle()

        args.putString(READ_ONLY_TITLE_KEY, binding.titleEt.text.toString())
        args.putLong(GPX_ID_KEY, gpxId.value)
        val frag = RecordingConfiguratorModal.instance()
        frag.arguments = args

        parentFragmentManager.popBackStackImmediate()
        RecordingConfiguratorModal.show(parentFragmentManager, frag)
    }

    private fun updateGpxTitle(newTitle: String) {
        val realm = Realm.getDefaultInstance()
        realm.executeTransaction { itRealm ->
            GpxContent.withId(gpxId.value, itRealm)?.title = newTitle
        }
        realm.close()
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }

    override fun onDestroyView() {
        binding.detailRoot.removeAllViews()
        binding.mapView.onDestroy()
        mapController?.onDestroy()
        super.onDestroyView()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
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
        detailsView.onSaveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    companion object {
        fun newInstance(gpxId: Long): GpxDetailsFragment {
            val args = Bundle()
            args.putLong(Keys.GpxId, gpxId)

            val fragment = GpxDetailsFragment()
            fragment.arguments = args

            return fragment
        }
    }
}

