package com.iboism.gpxrecorder.primary


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView

import com.iboism.gpxrecorder.R
import com.iboism.gpxrecorder.model.GpxContent
import io.realm.Realm

class GpxList : Fragment() {

    private var listView: ListView? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val root = checkNotNull(inflater?.inflate(R.layout.fragment_gpx_list, container, false)) { return null }

        listView = root.findViewById<ListView>(R.id.gpx_listView)
        val gpxContentList = Realm.getDefaultInstance().where(GpxContent::class.java).findAll()
        listView?.adapter = GpxContentAdapter(gpxContentList)

        return root
    }

    companion object {
        fun newInstance() = GpxList()
    }
}