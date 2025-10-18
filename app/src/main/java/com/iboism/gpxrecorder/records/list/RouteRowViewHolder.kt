package com.iboism.gpxrecorder.records.list

import androidx.recyclerview.widget.RecyclerView
import com.iboism.gpxrecorder.databinding.ListRowRouteBinding

class RouteRowViewHolder(binding: ListRowRouteBinding): RecyclerView.ViewHolder(binding.root) {
    val rootView = binding.root
    val contentView = binding.mainContentLayout
    val titleView = binding.gpxContentTitle
    val dateView = binding.gpxContentDate
    val distanceView = binding.gpxContentDistance
    val waypointCountView = binding.gpxContentWaypointCount
    val checkbox = binding.checkBox
}