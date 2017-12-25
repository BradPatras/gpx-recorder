package com.iboism.gpxrecorder.primary

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ListAdapter
import android.widget.ProgressBar
import android.widget.TextView
import com.iboism.gpxrecorder.R
import com.iboism.gpxrecorder.model.GpxContent
import com.iboism.gpxrecorder.util.Alerts
import com.iboism.gpxrecorder.util.FileHelper
import com.iboism.gpxrecorder.util.ShareHelper
import io.reactivex.android.schedulers.AndroidSchedulers
import io.realm.RealmBaseAdapter
import io.realm.RealmResults
import android.widget.RelativeLayout



/**
 * Created by bradpatras on 12/8/17.
 */
class GpxContentAdapter(val realmResults: RealmResults<GpxContent>?) : RealmBaseAdapter<GpxContent>(realmResults), ListAdapter {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        val view: View
        val viewHolder: ViewHolder

        if (convertView == null) {
            view = LayoutInflater.from(parent?.context).inflate(R.layout.list_row_gpx_content, parent, false)
            viewHolder = ViewHolder(view)
            view.tag = viewHolder

        } else {
            view = convertView
            viewHolder = view.tag as ViewHolder
        }

        val gpx = realmResults?.get(position) ?: return view

        viewHolder.dateView.text = gpx.date
        viewHolder.titleView.text = gpx.title
        viewHolder.waypointCountView.text = "${gpx.waypointList.count()}"
        viewHolder.distanceView.text = String.format("%.2f km", gpx.trackList.get(0)?.segments?.get(0)?.distance ?: 0f)

        viewHolder.exportButton.setOnClickListener {
            parent?.let {
                // show indicator
                FileHelper(it.context).gpxFileWith(gpx)
                        .subscribeOn(AndroidSchedulers.mainThread())
                        .subscribe { file, error ->
                            if (error != null) {
                                // hide indicator
                                Alerts(view.context).genericError(R.string.file_share_failed)
                            } else {
                                ShareHelper(it.context).shareFile(file)
                                // hide indicator
                            }
                        }

            }
        }

        return view
    }

    private inner class ViewHolder(view: View?) {
        val titleView = view?.findViewById(R.id.gpx_content_title) as TextView
        val dateView = view?.findViewById(R.id.gpx_content_date) as TextView
        val exportButton = view?.findViewById(R.id.gpx_content_export_button) as Button
        val distanceView = view?.findViewById(R.id.gpx_content_distance) as TextView
        val waypointCountView = view?.findViewById(R.id.gpx_content_waypoint_count) as TextView
    }
}