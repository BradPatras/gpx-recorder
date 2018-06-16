package com.iboism.gpxrecorder.primary


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.iboism.gpxrecorder.R
import com.iboism.gpxrecorder.model.GpxContent
import com.iboism.gpxrecorder.recording.REVEAL_ORIGIN_X_KEY
import com.iboism.gpxrecorder.recording.REVEAL_ORIGIN_Y_KEY
import com.iboism.gpxrecorder.recording.RecordingConfiguratorModal
import com.iboism.gpxrecorder.util.PermissionHelper
import com.iboism.gpxrecorder.viewer.GpxContentViewerFragment
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import kotlinx.android.synthetic.main.fragment_gpx_list.*
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager



class GpxListFragment : Fragment() {
    private val permissionHelper: PermissionHelper by lazy { PermissionHelper.getInstance(this.activity) }
    private val placeholderViews = listOf(R.id.placeholder_menu_icon, R.id.placeholder_menu_text, R.id.placeholder_routes_text, R.id.placeholder_routes_icon)
    private val gpxContentList = Realm.getDefaultInstance().where(GpxContent::class.java).findAll().sort("date", Sort.DESCENDING)

    private val gpxChangeListener = { gpxContent: RealmResults<GpxContent> ->
        setPlaceholdersHidden(gpxContent.isNotEmpty())
    }

    private fun onFabClicked (view: View) {
        permissionHelper.checkLocationPermissions(
                onAllowed = {
                    val configFragment = RecordingConfiguratorModal.instance()

                    val args = Bundle()
                    args.putInt(REVEAL_ORIGIN_X_KEY, view.x.toInt() + (view.width / 2))
                    args.putInt(REVEAL_ORIGIN_Y_KEY, view.y.toInt() + (view.height / 2))
                    configFragment.arguments = args

                    fragmentManager.beginTransaction()
                            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out)
                            .replace(R.id.content_container, configFragment)
                            .addToBackStack(null)
                            .commit()
                })
    }

    private fun openContentViewer(gpxId: Long) {
        fragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, android.R.anim.fade_out, R.anim.none, android.R.anim.slide_out_right)
                .replace(R.id.content_container, GpxContentViewerFragment.newInstance(gpxId))
                .addToBackStack("view")
                .commit()
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_gpx_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fab.setOnClickListener(this::onFabClicked)

        val adapter = GpxRecyclerViewAdapter(gpxContentList)
        adapter.contentViewerOpener = this::openContentViewer
        gpx_listView.layoutManager = LinearLayoutManager(view.context)
        gpx_listView.adapter = adapter
        gpx_listView.setHasFixedSize(true)

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