package com.iboism.gpxrecorder.viewer


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.LatLng
import com.iboism.gpxrecorder.R
import com.iboism.gpxrecorder.model.GpxContent
import com.iboism.gpxrecorder.util.DateTimeFormatHelper
import com.iboism.gpxrecorder.util.Keys
import io.realm.Realm
import kotlinx.android.synthetic.main.fragment_gpx_content_viewer.*


class GpxContentViewer : Fragment() {

    private var gpxId: Long? = null
    private val gpxContent: GpxContent by lazy {
        Realm.getDefaultInstance()
                .where(GpxContent::class.java)
                .equalTo(GpxContent.Keys.primaryKey, gpxId)
                .findFirst()!! // todo fix
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gpxId = arguments?.get(Keys.GpxId) as? Long
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return inflater?.inflate(R.layout.fragment_gpx_content_viewer, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        title_tv.text = gpxContent.title
        distance_tv.text = "${gpxContent.trackList.first()?.segments?.first()?.distance ?: 0}km"
        waypoint_tv.text = "${gpxContent.waypointList.size} waypoints"
        date_tv.text = DateTimeFormatHelper.toReadableString(gpxContent.date)

        map_view?.onCreate(savedInstanceState)
        // this is mostly just a test
        map_view?.getMapAsync {
            MapsInitializer.initialize(view!!.context) // todo fix
            it.uiSettings.isMyLocationButtonEnabled = false
            val testpt = gpxContent.trackList.first()?.segments?.first()?.points?.first()!!
            it.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(testpt.lat, testpt.lon), 10f))
        }
    }

    // todo look into switching to MapFragment so I dont have to do these
    override fun onResume() {
        super.onResume()
        map_view?.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        map_view?.onDestroy()
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

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        map_view?.onSaveInstanceState(outState)
    }

    companion object {
        fun newInstance(gpxId: Long): GpxContentViewer {
            val fragment = GpxContentViewer()
            val args = Bundle()
            args.putLong(Keys.GpxId, gpxId)
            fragment.arguments = args
            return fragment
        }
    }
}

