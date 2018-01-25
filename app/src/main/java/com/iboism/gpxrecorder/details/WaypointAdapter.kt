package com.iboism.gpxrecorder.details

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListAdapter
import android.widget.TextView
import com.iboism.gpxrecorder.R
import com.iboism.gpxrecorder.model.Waypoint
import com.iboism.gpxrecorder.util.DateTimeFormatHelper
import io.realm.RealmBaseAdapter
import io.realm.RealmResults

/**
 * Created by bradpatras on 1/24/18.
 */

class WaypointAdapter(private val realmResults: RealmResults<Waypoint>?) : RealmBaseAdapter<Waypoint>(realmResults), ListAdapter {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val viewHolder: ViewHolder

        if (convertView == null) {
            view = LayoutInflater.from(parent?.context).inflate(R.layout.list_row_waypoint_content, parent)
            viewHolder = ViewHolder(view)
            view.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as ViewHolder
        }

        val wpt = realmResults?.get(position) ?: return view

        viewHolder.dateView.text = DateTimeFormatHelper.toReadableString(wpt.time)
        viewHolder.titleView.text = wpt.title
        viewHolder.descriptionView.text = wpt.desc

        return view
    }

    private inner class ViewHolder(view: View?) {
        val titleView = view?.findViewById(R.id.title) as TextView
        val dateView = view?.findViewById(R.id.date) as TextView
        val descriptionView = view?.findViewById(R.id.subtitle) as TextView
    }

}