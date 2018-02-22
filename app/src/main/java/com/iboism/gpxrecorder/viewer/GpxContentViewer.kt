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
import com.iboism.gpxrecorder.util.Keys
import io.realm.Realm
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

        Realm.getDefaultInstance()
                .where(GpxContent::class.java)
                .equalTo(GpxContent.Keys.primaryKey, gpxId)
                .findFirst()
                ?.let {
                    title_tv.text = it.title
                }

        map_view.getMapAsync {
            MapsInitializer.initialize(view!!.context)
            it.uiSettings.isMyLocationButtonEnabled = false
            it.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(43.1, -87.9), 10f))
        }
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
