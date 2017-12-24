package com.iboism.gpxrecorder.primary

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ListAdapter
import android.widget.TextView
import com.iboism.gpxrecorder.R
import com.iboism.gpxrecorder.analysis.Distance
import com.iboism.gpxrecorder.model.GpxContent
import com.iboism.gpxrecorder.util.FileHelper
import com.iboism.gpxrecorder.util.ShareHelper
import io.reactivex.android.schedulers.AndroidSchedulers
import io.realm.Realm
import io.realm.RealmBaseAdapter
import io.realm.RealmResults
import java.text.DecimalFormat

/**
 * Created by bradpatras on 12/8/17.
 */
class GpxContentAdapter(val realmResults: RealmResults<GpxContent>?) : RealmBaseAdapter<GpxContent>(realmResults), ListAdapter {

    private var cachedDistances: Array<Float?> = Array(realmResults?.size ?: 0, { null })

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        val view: View
        val viewHolder: ViewHolder

        if (convertView == null) {
            view = LayoutInflater.from(parent?.context).inflate(R.layout.list_row_gpx_content, parent, false)
            viewHolder = ViewHolder(position, view)
            view.tag = viewHolder

        } else {
            view = convertView
            viewHolder = view.tag as ViewHolder
        }

        val gpx = realmResults?.get(position) ?: return view

        viewHolder.dateView.text = gpx.date
        viewHolder.titleView.text = gpx.title
        viewHolder.waypointCountView.text = "${gpx.waypointList.count()}"

        viewHolder.exportButton.setOnClickListener {
            parent?.let {
                val file = FileHelper(it.context).gpxFileWith(gpx)
                ShareHelper(it.context).shareFile(file)
            }
        }

        if (cachedDistances[position] == null) {
            viewHolder.distanceView.text = view.context.getString(R.string.calculating_placeholder)
            Distance().ofSegment(gpx.trackList.first()?.segments?.first()?.identifier ?: 0)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { distance ->
                        if (viewHolder.position == position) {
                            viewHolder.distanceView.text = String.format("%.2f", distance)
                        }
                        cachedDistances[position] = distance
                    }
        } else {
            viewHolder.distanceView.text = String.format("%.2f", cachedDistances[position])
        }

        return view
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    private inner class ViewHolder(val position: Int, view: View?) {
        val titleView = view?.findViewById(R.id.gpx_content_title) as TextView
        val dateView = view?.findViewById(R.id.gpx_content_date) as TextView
        val exportButton = view?.findViewById(R.id.gpx_content_export_button) as Button
        val distanceView = view?.findViewById(R.id.gpx_content_distance) as TextView
        val waypointCountView = view?.findViewById(R.id.gpx_content_waypoint_count) as TextView

    }

}