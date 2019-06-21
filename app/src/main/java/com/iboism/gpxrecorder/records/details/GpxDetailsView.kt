package com.iboism.gpxrecorder.records.details

import androidx.appcompat.app.AlertDialog
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import com.iboism.gpxrecorder.R
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
    private val moreMenu: PopupMenu = PopupMenu(root.context, root.more_btn)
    private val shareMenuItem: MenuItem = moreMenu.menu.add("Share")
    private val mapToggleMenuItem: MenuItem = moreMenu.menu.add("Toggle map type")
    private val deleteMenuItem: MenuItem = moreMenu.menu.add("Delete route")

    var exportTouchObservable: PublishSubject<Unit> = PublishSubject.create()
    var gpxTitleObservable: PublishSubject<String> = PublishSubject.create()
    var mapTypeToggleObservable: PublishSubject<Unit> = PublishSubject.create()
    var deleteRouteObservable: PublishSubject<Unit> = PublishSubject.create()

    init {
        root.title_et.isEnabled = false
        root.title_et.append(titleText)
        root.distance_tv.text = distanceText
        root.waypoint_tv.text = waypointsText
        root.date_tv.text = dateText

        root.title_edit_btn.setOnClickListener { editPressed() }
        root.more_btn.setOnClickListener { morePressed() }

        moreMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem) {
                shareMenuItem -> exportPressed()
                mapToggleMenuItem -> mapTypeToggleObservable.onNext(Unit)
                deleteMenuItem -> deletePressed()
                else -> return@setOnMenuItemClickListener false
            }

            return@setOnMenuItemClickListener true
        }
    }

    private fun editPressed() {
        root.title_et.isEnabled = true
        root.title_et.isFocusableInTouchMode = true
        root.title_et.requestFocusFromTouch()
        root.title_et.setBackgroundResource(R.drawable.rect_rounded_grey)
        savedText = root.title_et.text.toString()
        root.title_edit_btn.setOnClickListener { applyPressed() }
        root.title_edit_btn.setImageResource(R.drawable.ic_check)
        root.more_btn.setOnClickListener { cancelPressed() }
        root.more_btn.setImageResource(R.drawable.ic_close)
    }

    private fun deletePressed() {
        AlertDialog.Builder(root.context)
                .setTitle(R.string.delete_recording_alert_title)
                .setMessage(R.string.delete_recording_alert_message)
                .setCancelable(true)
                .setPositiveButton(R.string.delete) { _, _ ->
                    deleteRouteObservable.onNext(Unit)
                }.create().show()
    }

    private fun exportPressed() {
        exportTouchObservable.onNext(Unit)
    }

    private fun morePressed() {
        moreMenu.show()
    }

    private fun applyPressed() {
        root.title_et.isEnabled = false
        root.title_et.clearFocus()
        root.title_et.setBackgroundResource(R.color.white)
        root.title_edit_btn.setOnClickListener { editPressed() }
        root.title_edit_btn.setImageResource(R.drawable.ic_edit)
        root.more_btn.setOnClickListener { morePressed() }
        root.more_btn.setImageResource(R.drawable.ic_more)
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
        root.more_btn.setOnClickListener { morePressed() }
        root.more_btn.setImageResource(R.drawable.ic_more)
    }

    fun setButtonsExporting(isExporting: Boolean) {
        root.title_edit_btn.isEnabled = !isExporting
        root.more_btn.isEnabled = !isExporting
        root.more_btn.visibility = if (isExporting) View.INVISIBLE else View.VISIBLE
        root.export_progress_bar.visibility = if (isExporting) View.VISIBLE else View.GONE
    }
}

