package com.iboism.gpxrecorder.records.details

import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import androidx.appcompat.app.AlertDialog
import com.iboism.gpxrecorder.R
import com.iboism.gpxrecorder.databinding.FragmentRouteDetailsBinding
import io.reactivex.subjects.PublishSubject

private const val DRAFT_TITLE_KEY: String = "GpxDetailsView_titleDraft"

class GpxDetailsView(
    val binding: FragmentRouteDetailsBinding,
    val titleText: String,
    val distanceText: String,
    val waypointsText: String,
    val dateText: String
) {

    private var savedText = ""
    private val moreMenu: PopupMenu = PopupMenu(binding.root.context, binding.moreBtn, R.menu.details_overlfow_menu)

    var saveTouchObservable: PublishSubject<Unit> = PublishSubject.create()
    var shareTouchObservable: PublishSubject<Unit> = PublishSubject.create()
    var gpxTitleObservable: PublishSubject<String> = PublishSubject.create()
    var mapTypeToggleObservable: PublishSubject<Unit> = PublishSubject.create()
    var deleteRouteObservable: PublishSubject<Unit> = PublishSubject.create()
    var resumeRecordingObservable: PublishSubject<Unit> = PublishSubject.create()
    var addWaypointObservable: PublishSubject<Unit> = PublishSubject.create()

    init {
        binding.titleEt.isEnabled = false
        binding.titleEt.append(titleText)
        binding.distanceTv.text = distanceText
        binding.waypointTv.text = waypointsText
        binding.dateTv.text = dateText

        binding.resumeBtn.setOnClickListener { resumePressed() }
        binding.moreBtn.setOnClickListener { morePressed() }
        binding.addWptBtn.setOnClickListener { addWaypointObservable.onNext(Unit) }
        moreMenu.menuInflater.inflate(R.menu.details_overlfow_menu, moreMenu.menu)

        moreMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.save -> savePressed()
                R.id.share -> sharePressed()
                R.id.toggle_map_type -> mapTypeToggleObservable.onNext(Unit)
                R.id.delete_route -> deletePressed()
                R.id.rename_route -> editPressed()
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
        binding.resumeBtn.setOnClickListener { applyPressed() }
        binding.resumeBtn.setImageResource(R.drawable.ic_check)
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

    private fun sharePressed() {
        shareTouchObservable.onNext(Unit)
    }

    private fun savePressed() {
        saveTouchObservable.onNext(Unit)
    }

    private fun morePressed() {
        moreMenu.show()
    }

    private fun applyPressed() {
        binding.titleEt.isEnabled = false
        binding.titleEt.clearFocus()
        binding.titleEt.setBackgroundResource(R.color.nav_bar_surface)
        binding.resumeBtn.setOnClickListener { resumePressed() }
        binding.resumeBtn.setImageResource(R.drawable.ic_near_me)
        binding.moreBtn.setOnClickListener { morePressed() }
        binding.moreBtn.setImageResource(R.drawable.ic_more)
        gpxTitleObservable.onNext(binding.titleEt.text.toString())
    }

    private fun cancelPressed() {
        binding.titleEt.isEnabled = false
        binding.titleEt.clearFocus()
        binding.titleEt.setBackgroundResource(R.color.nav_bar_surface)
        binding.titleEt.setText("")
        binding.titleEt.append(savedText)
        binding.resumeBtn.setOnClickListener { resumePressed() }
        binding.resumeBtn.setImageResource(R.drawable.ic_near_me)
        binding.moreBtn.setOnClickListener { morePressed() }
        binding.moreBtn.setImageResource(R.drawable.ic_more)
    }

    private fun resumePressed() {
        AlertDialog.Builder(binding.root.context)
            .setTitle("Resume recording")
            .setMessage("If a route recording is progress, it will be stopped.  Would you like to continue recording this route?")
            .setCancelable(true)
            .setPositiveButton("Continue") { _, _ ->
                resumeRecordingObservable.onNext(Unit)
            }.create().show()
    }

    fun setButtonsExporting(isExporting: Boolean) {
        binding.resumeBtn.isEnabled = !isExporting
        binding.moreBtn.isEnabled = !isExporting
        binding.moreBtn.visibility = if (isExporting) View.INVISIBLE else View.VISIBLE
        binding.exportProgressBar.visibility = if (isExporting) View.VISIBLE else View.GONE
    }
}

