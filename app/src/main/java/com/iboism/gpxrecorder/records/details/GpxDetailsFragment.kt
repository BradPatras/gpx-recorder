package com.iboism.gpxrecorder.records.details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.iboism.gpxrecorder.Keys
import com.iboism.gpxrecorder.R
import com.iboism.gpxrecorder.export.ExportFragment
import com.iboism.gpxrecorder.model.GpxContent
import com.iboism.gpxrecorder.util.DateTimeFormatHelper
import com.iboism.gpxrecorder.util.FileHelper
import com.iboism.gpxrecorder.util.Holder
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Consumer
import io.realm.Realm
import kotlinx.android.synthetic.main.fragment_gpx_content_viewer.*

const val CREATE_FILE_INTENT_ID = 1

class GpxDetailsFragment : Fragment() {
    private lateinit var detailsView: GpxDetailsView
    private lateinit var gpxId: Holder<Long>
    private var fileHelper: FileHelper? = null
    private val compositeDisposable = CompositeDisposable()
    private var mapController: MapController? = null

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gpxId = Holder(requireArguments().get(Keys.GpxId) as Long)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_gpx_content_viewer, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val realm = Realm.getDefaultInstance()
        val gpxContent = GpxContent.withId(gpxId.value, realm) ?: return
        fileHelper = FileHelper()

        val distance = gpxContent.trackList.first()?.segments?.first()?.distance ?: 0f

        detailsView = GpxDetailsView(
                root = detail_root,
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

        realm.close()

        map_view?.let {
            it.onCreate(savedInstanceState)
            mapController = MapController(it, gpxId.value)
            it.getMapAsync(mapController)
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
        parentFragmentManager.beginTransaction()
                .add(R.id.content_container, ExportFragment.newInstance(gpxId.value))
                .addToBackStack("export")
                .commit()
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
        map_view?.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }

    override fun onDestroyView() {
        detail_root.removeAllViews()
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

