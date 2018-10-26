package com.iboism.gpxrecorder.viewer


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import com.iboism.gpxrecorder.R
import com.iboism.gpxrecorder.model.GpxContent
import com.iboism.gpxrecorder.util.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.realm.Realm
import kotlinx.android.synthetic.main.fragment_gpx_content_viewer.*


class GpxContentViewerFragment : Fragment() {

    private var gpxId: Long? = null
    private var savedText: String = ""
    private var fileHelper: FileHelper? = null
    private val compositeDisposable = CompositeDisposable()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gpxId = arguments?.get(Keys.GpxId) as? Long
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_gpx_content_viewer, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Can't do anything if we don't have an Id and corresponding gpxContent //TODO handle invalid state
        val gpxId = gpxId ?: return
        val realm = Realm.getDefaultInstance()
        val gpxContent = GpxContent.withId(gpxId, realm) ?: return
        fileHelper = FileHelper(view.context)

        title_et.append(gpxContent.title)
        title_et.isEnabled = false
        title_edit_btn.setOnClickListener { editPressed() }
        export_btn.setOnClickListener { exportPressed() }
        val distance = gpxContent.trackList.first()?.segments?.first()?.distance ?: 0f
        distance_tv.text = resources.getString(R.string.distance_km, distance)
        waypoint_tv.text = resources.getQuantityString(R.plurals.waypoint_count, gpxContent.waypointList.size, gpxContent.waypointList.size)
        date_tv.text = DateTimeFormatHelper.toReadableString(gpxContent.date)

        realm.close()

        map_view?.let {
            it.onCreate(savedInstanceState)
            val mapController = MapController(it.context, gpxId)
            it.viewTreeObserver.addOnGlobalLayoutListener(mapController)
            it.getMapAsync(mapController)
        }
    }

    private fun editPressed() {
        title_et.isEnabled = true
        title_et.isFocusableInTouchMode = true
        title_et.requestFocusFromTouch()
        title_et.setBackgroundResource(R.drawable.rect_rounded_grey)
        savedText = title_et.text.toString()
        title_edit_btn.setOnClickListener { applyPressed() }
        title_edit_btn.setImageResource(R.drawable.ic_check)
        export_btn.setOnClickListener { cancelPressed() }
        export_btn.setImageResource(R.drawable.ic_close)
    }

    private fun applyPressed() {
        title_et.isEnabled = false
        title_et.clearFocus()
        title_et.setBackgroundResource(R.color.transparent)
        title_edit_btn.setOnClickListener { editPressed() }
        title_edit_btn.setImageResource(R.drawable.ic_edit)
        export_btn.setOnClickListener { exportPressed() }
        export_btn.setImageResource(R.drawable.ic_open_in)
        updateGpxTitle(title_et.text.toString())
    }

    private fun cancelPressed() {
        title_et.isEnabled = false
        title_et.clearFocus()
        title_et.setBackgroundResource(R.color.transparent)
        title_et.setText("")
        title_et.append(savedText)
        title_edit_btn.setOnClickListener { editPressed() }
        title_edit_btn.setImageResource(R.drawable.ic_edit)
        export_btn.setOnClickListener { exportPressed() }
        export_btn.setImageResource(R.drawable.ic_open_in)
    }

    private fun exportPressed() {
        val gpxId = gpxId ?: return

       setButtonsExporting(true)
        fileHelper?.apply {
            shareGpxFile(gpxId).subscribe {
                setButtonsExporting(false)
            }
        }
    }

    private fun setButtonsExporting(isExporting: Boolean) {
        title_edit_btn.isEnabled = !isExporting
        export_btn.isEnabled = !isExporting
        export_btn.visibility = if (isExporting) View.INVISIBLE else View.VISIBLE
        export_progress_bar.visibility = if (isExporting) View.VISIBLE else View.GONE
    }

    private fun updateGpxTitle(newTitle: String) {
        val realm = Realm.getDefaultInstance()
        realm.executeTransaction {itRealm ->
            gpxId?.let {
                GpxContent.withId(it, itRealm)?.title = newTitle
            }
        }
        realm.close()
    }

    // todo look into switching to MapFragment so I don't have to do these
    override fun onResume() {
        super.onResume()
        map_view?.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        map_view?.onDestroy()
        compositeDisposable.dispose()
    }

    override fun onPause() {
        super.onPause()
        map_view?.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        map_view?.onLowMemory()
    }

    override fun onStart() {
        super.onStart()
        map_view?.onStart()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        map_view?.onSaveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    companion object {
        fun newInstance(gpxId: Long): GpxContentViewerFragment {
            val args = Bundle()
            args.putLong(Keys.GpxId, gpxId)

            val fragment = GpxContentViewerFragment()
            fragment.arguments = args

            return fragment
        }
    }
}

