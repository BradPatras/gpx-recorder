package com.iboism.gpxrecorder.primary

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListAdapter
import android.widget.TextView
import com.iboism.gpxrecorder.R
import com.iboism.gpxrecorder.analysis.Distance
import com.iboism.gpxrecorder.model.GpxContent
import com.iboism.gpxrecorder.util.FileHelper
import com.iboism.gpxrecorder.util.ShareHelper
import io.realm.Realm
import io.realm.RealmBaseAdapter
import io.realm.RealmResults
import java.text.DecimalFormat

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
            val distanceView: TextView = view.findViewById(R.id.gpx_content_distance)
            val waypointCountView: TextView = view.findViewById(R.id.gpx_content_waypoint_count)

            val distance = Distance(Realm.getDefaultInstance())
            val kmDistance = distance.ofSegment(gpxContent.trackList
                    .getOrNull(0)?.segments?.firstOrNull()?.identifier ?: 0)

            titleView.text = gpxContent.title
            dateView.text = gpxContent.date
            distanceView.text = String.format("%.2f", kmDistance)
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