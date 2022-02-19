package com.iboism.gpxrecorder.records.details

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import androidx.appcompat.app.AlertDialog
import com.iboism.gpxrecorder.R
import com.iboism.gpxrecorder.databinding.FragmentGpxContentViewerBinding
import io.reactivex.subjects.PublishSubject

const private val DRAFT_TITLE_KEY: String = "GpxDetailsView_titleDraft"

class GpxDetailsView(
    val binding: FragmentGpxContentViewerBinding,
    val titleText: String,
    val distanceText: String,
    val waypointsText: String,
    val dateText: String
        ) {

    private var savedText = ""
    private val moreMenu: PopupMenu = PopupMenu(binding.root.context, binding.root)
    private val exportMenuItem: MenuItem = moreMenu.menu.add("Export")
    private val mapToggleMenuItem: MenuItem = moreMenu.menu.add("Toggle map type")
    private val deleteMenuItem: MenuItem = moreMenu.menu.add("Delete route")
    var exportTouchObservable: PublishSubject<Unit> = PublishSubject.create()
    var gpxTitleObservable: PublishSubject<String> = PublishSubject.create()
    var mapTypeToggleObservable: PublishSubject<Unit> = PublishSubject.create()
    var deleteRouteObservable: PublishSubject<Unit> = PublishSubject.create()

    init {
        binding.titleEt.isEnabled = false
        binding.titleEt.append(titleText)
        binding.distanceTv.text = distanceText
        binding.waypointTv.text = waypointsText
        binding.dateTv.text = dateText

        binding.titleEditBtn.setOnClickListener { editPressed() }
        binding.moreBtn.setOnClickListener { morePressed() }

        moreMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem) {
                exportMenuItem -> exportPressed()
                mapToggleMenuItem -> mapTypeToggleObservable.onNext(Unit)
                deleteMenuItem -> deletePressed()
                else -> return@setOnMenuItemClickListener false
            }

            return@setOnMenuItemClickListener true
        }
    }

    fun restoreInstanceState(outState: Bundle?) {
        val titleDraft = outState?.getString(DRAFT_TITLE_KEY) ?: return

        editPressed()
        binding.titleEt.text.clear()
        binding.titleEt.text.append(titleDraft)
        outState.remove(DRAFT_TITLE_KEY)
    }

    fun onSaveInstanceState(outState: Bundle) {
        if (binding.titleEt.isEnabled) {
            outState.putString(DRAFT_TITLE_KEY, binding.titleEt.text.toString())
        }
    }

    private fun editPressed() {
        binding.titleEt.isEnabled = true
        binding.titleEt.isFocusableInTouchMode = true
        binding.titleEt.requestFocusFromTouch()
        binding.titleEt.setBackgroundResource(R.drawable.rect_rounded_light_accent)
        savedText = binding.titleEt.text.toString()
        binding.titleEditBtn.setOnClickListener { applyPressed() }
        binding.titleEditBtn.setImageResource(R.drawable.ic_check)
        binding.moreBtn.setOnClickListener { cancelPressed() }
        binding.moreBtn.setImageResource(R.drawable.ic_close)
    }

    private fun deletePressed() {
        AlertDialog.Builder(binding.root.context)
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
        binding.titleEt.isEnabled = false
        binding.titleEt.clearFocus()
        binding.titleEt.setBackgroundResource(R.color.colorAccent)
        binding.titleEditBtn.setOnClickListener { editPressed() }
        binding.titleEditBtn.setImageResource(R.drawable.ic_edit)
        binding.moreBtn.setOnClickListener { morePressed() }
        binding.moreBtn.setImageResource(R.drawable.ic_more)
        gpxTitleObservable.onNext(binding.titleEt.text.toString())
    }

    private fun cancelPressed() {
        binding.titleEt.isEnabled = false
        binding.titleEt.clearFocus()
        binding.titleEt.setBackgroundResource(R.color.colorAccent)
        binding.titleEt.setText("")
        binding.titleEt.append(savedText)
        binding.titleEditBtn.setOnClickListener { editPressed() }
        binding.titleEditBtn.setImageResource(R.drawable.ic_edit)
        binding.moreBtn.setOnClickListener { morePressed() }
        binding.moreBtn.setImageResource(R.drawable.ic_more)
    }

    fun setButtonsExporting(isExporting: Boolean) {
        binding.titleEditBtn.isEnabled = !isExporting
        binding.moreBtn.isEnabled = !isExporting
        binding.moreBtn.visibility = if (isExporting) View.INVISIBLE else View.VISIBLE
        binding.exportProgressBar.visibility = if (isExporting) View.VISIBLE else View.GONE
    }
}

