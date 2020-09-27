package com.iboism.gpxrecorder.records.details

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.iboism.gpxrecorder.Keys
import com.iboism.gpxrecorder.R
import com.iboism.gpxrecorder.model.GpxContent
import com.iboism.gpxrecorder.util.DateTimeFormatHelper
import com.iboism.gpxrecorder.util.FileHelper
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Consumer
import io.realm.Realm
import kotlinx.android.synthetic.main.fragment_gpx_content_viewer.*

const val CREATE_FILE_INTENT_ID = 1

class GpxDetailsFragment : Fragment() {
    private lateinit var detailsView: GpxDetailsView
    private var gpxId: Long? = null
    private var fileHelper: FileHelper? = null
    private val compositeDisposable = CompositeDisposable()
    private var mapController: MapController? = null

    private var gpxTitleConsumer: Consumer<in String> = Consumer {
        updateGpxTitle(it)
    }

    private val exportTouchConsumer = Consumer<Unit> {
        exportPressed()
    }

    private val saveToDeviceConsumer = Consumer<Unit> {
        saveToDevicePressed()
    }

    private val mapLayerTouchConsumer = Consumer<Unit> {
        mapController?.toggleMapType()
    }

    private val deleteRouteTouchConsumer = Consumer<Unit> {
        deleteRouteAndPopFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gpxId = arguments?.get(Keys.GpxId) as? Long
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_gpx_content_viewer, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Can't do anything if we don't have an Id and corresponding gpxContent //TODO handle invalid state
        val gpxId = gpxId ?: return
        val realm = Realm.getDefaultInstance()
        val gpxContent = GpxContent.withId(gpxId, realm) ?: return
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
        compositeDisposable.add(detailsView.saveTouchObservable.subscribe(saveToDeviceConsumer))

        realm.close()

        map_view?.let {
            it.onCreate(savedInstanceState)
            mapController = MapController(it, gpxId)
            it.getMapAsync(mapController)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode) {
            CREATE_FILE_INTENT_ID -> onSaveLocationSelected(data?.data)
        }
    }

    private fun deleteRouteAndPopFragment() {
        val realm = Realm.getDefaultInstance()
        realm.executeTransaction { itRealm ->
            gpxId?.let {
                GpxContent.withId(it, itRealm)?.deleteFromRealm()
            }
        }
        realm.close()

        parentFragmentManager.popBackStack()
    }

    private fun exportPressed() {
        val gpxId = gpxId ?: return
        val context = context ?: return

        detailsView.setButtonsExporting(true)
        fileHelper?.apply {
            shareGpxFile(context, gpxId).subscribe {
                detailsView.setButtonsExporting(false)
            }
        }
    }

    private fun saveToDevicePressed() {
        showSystemFolderPicker()
    }

    private fun showSystemFolderPicker() {
        val context = context ?: return
        val gpxId = gpxId ?: return
        val filename = fileHelper?.getGpxFilename(context, gpxId) ?: return

        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/xml"

            putExtra(Intent.EXTRA_TITLE, filename)
        }

        startActivityForResult(intent, CREATE_FILE_INTENT_ID)
    }

    private fun onSaveLocationSelected(location: Uri?) {
        val destination = location ?: return
        val gpxId = gpxId ?: return
        val context = context ?: return

        detailsView.setButtonsExporting(true)
        fileHelper?.apply {
            saveGpxFile(context, gpxId, destination).subscribe {
                detailsView.setButtonsExporting(false)
            }
        }
    }

    private fun updateGpxTitle(newTitle: String) {
        val realm = Realm.getDefaultInstance()
        realm.executeTransaction { itRealm ->
            gpxId?.let {
                GpxContent.withId(it, itRealm)?.title = newTitle
            }
        }
        realm.close()
    }

    // todo look into switching to MapFragment so I don't have to do these
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

