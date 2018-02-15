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
    private val placeholderViews = listOf(R.id.placeholder_menu_icon, R.id.placeholder_menu_text, R.id.placeholder_routes_text, R.id.placeholder_routes_icon)

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val root = checkNotNull(inflater?.inflate(R.layout.fragment_gpx_list, container, false)) { return null }

        listView = root.findViewById(R.id.gpx_listView)
        val gpxContentList = Realm.getDefaultInstance().where(GpxContent::class.java).findAll()
        listView?.adapter = GpxContentAdapter(gpxContentList)

        if (gpxContentList.isNotEmpty()) hidePlaceholders(root)

        return root
    }

    private fun hidePlaceholders(root: View) {
        placeholderViews.forEach { root.findViewById<View>(it).visibility = View.GONE }
    }

    companion object {
        fun newInstance() = GpxList()
    }
}