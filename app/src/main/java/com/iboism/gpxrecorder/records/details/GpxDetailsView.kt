package com.iboism.gpxrecorder.records.details

import android.view.View
import com.iboism.gpxrecorder.R
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.fragment_gpx_content_viewer.view.*

class GpxDetailsView(
        val root: View,
        val titleText: String,
        val distanceText: String,
        val waypointsText: String,
        val dateText: String
        ) {
    private var savedText = ""

    var exportTouchObservable: PublishSubject<Unit> = PublishSubject.create()
    var gpxTitleObservable: PublishSubject<String> = PublishSubject.create()
    var mapTypeToggleObservable: PublishSubject<Unit> = PublishSubject.create()

    init {
        root.title_et.isEnabled = false
        root.title_et.append(titleText)
        root.distance_tv.text = distanceText
        root.waypoint_tv.text = waypointsText
        root.date_tv.text = dateText

        root.title_edit_btn.setOnClickListener { editPressed() }
        root.export_btn.setOnClickListener { exportPressed() }
        root.map_type_btn.setOnClickListener { mapTypeToggleObservable.onNext(Unit) }
    }

    private fun editPressed() {
        root.title_et.isEnabled = true
        root.title_et.isFocusableInTouchMode = true
        root.title_et.requestFocusFromTouch()
        root.title_et.setBackgroundResource(R.drawable.rect_rounded_grey)
        savedText = root.title_et.text.toString()
        root.title_edit_btn.setOnClickListener { applyPressed() }
        root.title_edit_btn.setImageResource(R.drawable.ic_check)
        root.export_btn.setOnClickListener { cancelPressed() }
        root.export_btn.setImageResource(R.drawable.ic_close)
    }

    private fun exportPressed() {
        exportTouchObservable.onNext(Unit)
    }

    private fun applyPressed() {
        root.title_et.isEnabled = false
        root.title_et.clearFocus()
        root.title_et.setBackgroundResource(R.color.white)
        root.title_edit_btn.setOnClickListener { editPressed() }
        root.title_edit_btn.setImageResource(R.drawable.ic_edit)
        root.export_btn.setOnClickListener { exportPressed() }
        root.export_btn.setImageResource(R.drawable.ic_open_in)
        gpxTitleObservable.onNext(root.title_et.text.toString())
    }

    private fun cancelPressed() {
        root.title_et.isEnabled = false
        root.title_et.clearFocus()
        root.title_et.setBackgroundResource(R.color.white)
        root.title_et.setText("")
        root.title_et.append(savedText)
        root.title_edit_btn.setOnClickListener { editPressed() }
        root.title_edit_btn.setImageResource(R.drawable.ic_edit)
        root.export_btn.setOnClickListener { exportPressed() }
        root.export_btn.setImageResource(R.drawable.ic_open_in)
    }

    fun setButtonsExporting(isExporting: Boolean) {
        root.title_edit_btn.isEnabled = !isExporting
        root.export_btn.isEnabled = !isExporting
        root.export_btn.visibility = if (isExporting) View.INVISIBLE else View.VISIBLE
        root.export_progress_bar.visibility = if (isExporting) View.VISIBLE else View.GONE
    }
}

