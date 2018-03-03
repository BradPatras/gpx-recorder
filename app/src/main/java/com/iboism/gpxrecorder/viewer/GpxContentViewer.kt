package com.iboism.gpxrecorder.viewer


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.iboism.gpxrecorder.R
import com.iboism.gpxrecorder.model.GpxContent
import com.iboism.gpxrecorder.util.DateTimeFormatHelper
import com.iboism.gpxrecorder.util.Keys
import kotlinx.android.synthetic.main.fragment_gpx_content_viewer.*


class GpxContentViewer : Fragment() {

    private var gpxId: Long? = null

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
        // Can't do anything if we don't have an Id and corresponding gpxContent //TODO handle invalid state
        val gpxId = gpxId ?: return
        val gpxContent = GpxContent.withId(gpxId) ?: return

        title_tv.text = gpxContent.title
        distance_tv.text = "${gpxContent.trackList.first()?.segments?.first()?.distance ?: 0} km"
        waypoint_tv.text = "${gpxContent.waypointList.size} waypoints"
        date_tv.text = DateTimeFormatHelper.toReadableString(gpxContent.date)

        map_view?.onCreate(savedInstanceState)
        map_view?.getMapAsync(MapController(context, gpxId))
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
        map_view?.onSaveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    companion object {
        fun newInstance(gpxId: Long): GpxContentViewer {
            val args = Bundle()
            args.putLong(Keys.GpxId, gpxId)

            val fragment = GpxContentViewer()
            fragment.arguments = args

            return fragment
        }
    }
}

