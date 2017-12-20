package com.iboism.gpxrecorder.primary

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListAdapter
import android.widget.TextView
import com.iboism.gpxrecorder.R
import com.iboism.gpxrecorder.model.GpxContent
import com.iboism.gpxrecorder.util.FileHelper
import com.iboism.gpxrecorder.util.ShareHelper
import io.realm.RealmBaseAdapter
import io.realm.RealmResults

/**
 * Created by bradpatras on 12/8/17.
 */
class GpxContentAdapter(val realmResults: RealmResults<GpxContent>?) : RealmBaseAdapter<GpxContent>(realmResults), ListAdapter {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view = convertView
        if (view == null) {
            view = LayoutInflater.from(parent!!.getContext())
                    .inflate(R.layout.list_row_gpx_content, parent, false);
        }

        realmResults?.get(position)?.let { gpxContent ->
            val titleView: TextView = view!!.findViewById(R.id.gpx_content_title)
            val dateView: TextView = view.findViewById(R.id.gpx_content_date)
            val exportButton: TextView = view.findViewById(R.id.gpx_content_export_button)
            val pointCountView: TextView = view.findViewById(R.id.gpx_content_point_count)
            val waypointCountView: TextView = view.findViewById(R.id.gpx_content_waypoint_count)

            titleView.text = gpxContent.title
            dateView.text = gpxContent.date
            pointCountView.text = (gpxContent.trackList.get(0)?.segments?.get(0)?.points?.count() ?: 0).toString()
            waypointCountView.text = (gpxContent?.waypointList?.size).toString()

            exportButton.setOnClickListener {
                parent?.let {
                    val file = FileHelper(it.context).gpxFileWith(gpxContent)
                    ShareHelper(it.context).shareFile(file);
                }

            }
        }

        return view ?: View(parent!!.context)
    }
}