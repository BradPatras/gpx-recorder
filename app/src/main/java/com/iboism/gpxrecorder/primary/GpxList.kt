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
import io.realm.RealmBaseAdapter


class GpxList : Fragment() {

    var listView: ListView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val root = inflater!!.inflate(R.layout.fragment_gpx_list, container, false)

        listView = root.findViewById(R.id.gpx_listView)
        val gpxContentList = Realm.getDefaultInstance().where(GpxContent::class.java).findAll()
        listView?.adapter = GpxContentAdapter(gpxContentList)

        return root
    }

    companion object {

        fun newInstance(): GpxList {
            val fragment = GpxList()
            return fragment
        }
    }
}