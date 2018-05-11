package com.iboism.gpxrecorder.primary


import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.transition.AutoTransition
import android.transition.Fade
import android.transition.Slide
import android.transition.Transition
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.iboism.gpxrecorder.R
import com.iboism.gpxrecorder.model.GpxContent
import com.iboism.gpxrecorder.recording.RecordingConfiguratorModal
import com.iboism.gpxrecorder.util.PermissionHelper
import com.iboism.gpxrecorder.viewer.GpxContentViewerFragment
import io.realm.Realm
import io.realm.RealmResults
import kotlinx.android.synthetic.main.fragment_gpx_list.*

class GpxListFragment : Fragment() {
    private val permissionHelper: PermissionHelper by lazy { PermissionHelper.getInstance(this.activity) }
    private val placeholderViews = listOf(R.id.placeholder_menu_icon, R.id.placeholder_menu_text, R.id.placeholder_routes_text, R.id.placeholder_routes_icon)
    private val gpxContentList = Realm.getDefaultInstance().where(GpxContent::class.java).findAll()

    private val gpxChangeListener = { gpxContent: RealmResults<GpxContent> ->
        setPlaceholdersHidden(gpxContent.isNotEmpty())
    }

    private fun onFabClicked (view: View) {
        permissionHelper.checkLocationPermissions(
                onAllowed = {
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                        kittenDetails.setSharedElementEnterTransition(DetailsTransition())
//                        kittenDetails.setEnterTransition(Fade())
//                        exitTransition = Fade()
//                        kittenDetails.setSharedElementReturnTransition(DetailsTransition())
//                    }
//
//                    activity.supportFragmentManager
//                            .beginTransaction()
//                            .addSharedElement(holder.image, "kittenImage")
//                            .replace(R.id.container, kittenDetails)
//                            .addToBackStack(null)
//                            .commit()

                    val configFragment = RecordingConfiguratorModal.instance()

                    configFragment.sharedElementEnterTransition = AutoTransition()
                    configFragment.enterTransition = Slide()
                    configFragment.sharedElementReturnTransition = Slide()

                    fragmentManager.beginTransaction()
                            .addSharedElement(fab, "shared_start_fab")
                            .replace(R.id.content_container, configFragment)
                            .addToBackStack(null)
                            .commit()
                })
    }

    private fun openContentViewer(gpxId: Long) {
        fragmentManager.beginTransaction()
                .replace(R.id.content_container, GpxContentViewerFragment.newInstance(gpxId))
                .addToBackStack("view")
                .commit()
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_gpx_list, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fab.setOnClickListener(this::onFabClicked)

        val adapter = GpxContentAdapter(gpxContentList)
        adapter.contentViewerOpener = this::openContentViewer
        gpx_listView.adapter = adapter

        gpxContentList.addChangeListener(gpxChangeListener)

        setPlaceholdersHidden(gpxContentList.isNotEmpty())
    }

    private fun setPlaceholdersHidden(hidden: Boolean) {
        fragment_gpx_list?.let { root ->
            placeholderViews.forEach { root.findViewById<View>(it).visibility = if (hidden) View.GONE else View.VISIBLE }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        gpxContentList.removeChangeListener(gpxChangeListener)
    }

    companion object {
        fun newInstance() = GpxListFragment()
    }
}