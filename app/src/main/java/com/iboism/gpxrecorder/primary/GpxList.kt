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
import io.realm.RealmResults

class GpxList : Fragment() {

    private val placeholderViews = listOf(R.id.placeholder_menu_icon, R.id.placeholder_menu_text, R.id.placeholder_routes_text, R.id.placeholder_routes_icon)
    private val gpxContentList = Realm.getDefaultInstance().where(GpxContent::class.java).findAll()
    private var rootView: View? = null

    private val gpxChangeListener = { gpxContent: RealmResults<GpxContent> ->
        setPlaceholdersHidden(gpxContent.isNotEmpty())
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        rootView = checkNotNull(inflater?.inflate(R.layout.fragment_gpx_list, container, false)) { return null }
        val listView: ListView? = rootView?.findViewById(R.id.gpx_listView)
        listView?.adapter = GpxContentAdapter(gpxContentList)

        gpxContentList.addChangeListener(gpxChangeListener)

        setPlaceholdersHidden(gpxContentList.isNotEmpty())

        return rootView
    }

    private fun setPlaceholdersHidden(hidden: Boolean) {
        rootView?.let { root ->
            placeholderViews.forEach { root.findViewById<View>(it).visibility = if (hidden) View.GONE else View.VISIBLE }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        gpxContentList.removeChangeListener(gpxChangeListener)
    }

    companion object {
        fun newInstance() = GpxList()
    }
}