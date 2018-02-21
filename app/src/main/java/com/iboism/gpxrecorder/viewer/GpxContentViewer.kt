package com.iboism.gpxrecorder.viewer


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.iboism.gpxrecorder.R
import com.iboism.gpxrecorder.util.Keys


class GpxContentViewer : Fragment() {

    private var gpxId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {

        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.fragment_gpx_content_viewer, container, false)
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

}// Required empty public constructor
