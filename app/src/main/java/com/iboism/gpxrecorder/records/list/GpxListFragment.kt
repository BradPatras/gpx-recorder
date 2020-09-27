package com.iboism.gpxrecorder.records.list

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.*
import com.iboism.gpxrecorder.Events
import com.iboism.gpxrecorder.R
import com.iboism.gpxrecorder.model.GpxContent
import com.iboism.gpxrecorder.navigation.BottomNavigationDrawer
import com.iboism.gpxrecorder.recording.RecorderFragment
import com.iboism.gpxrecorder.recording.RecorderServiceConnection
import com.iboism.gpxrecorder.recording.configurator.RecordingConfiguratorModal
import com.iboism.gpxrecorder.records.details.CREATE_FILE_INTENT_ID
import com.iboism.gpxrecorder.records.details.GpxDetailsFragment
import com.iboism.gpxrecorder.util.FileHelper
import com.iboism.gpxrecorder.util.PermissionHelper
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import kotlinx.android.synthetic.main.fragment_gpx_list.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

const val CREATE_FILE_INTENT_ID = 1

class GpxListFragment : Fragment(), RecorderServiceConnection.OnServiceConnectedDelegate {
    private val placeholderViews = listOf(R.id.placeholder_routes_text, R.id.placeholder_routes_icon)
    private val gpxContentList = Realm.getDefaultInstance().where(GpxContent::class.java).findAll().sort("date", Sort.DESCENDING)
    private var adapter: GpxRecyclerViewAdapter? = null
    private val fileHelper = FileHelper()
    private var savingId: Long? = null
    private var isTransitioning = false
    private var currentlyRecordingRouteId: Long? = null
    private val compositeDisposable = CompositeDisposable()
    private var serviceConnection: RecorderServiceConnection = RecorderServiceConnection(this)
    private val gpxChangeListener = { gpxContent: RealmResults<GpxContent> ->
        setPlaceholdersHidden(gpxContent.isNotEmpty())
    }

    override fun onStop() {
        EventBus.getDefault().apply {
            unregister(adapter)
            unregister(this@GpxListFragment)
        }
        context?.let {
            serviceConnection.disconnect(it)
        }
        super.onStop()
    }

    override fun onStart() {
        super.onStart()

        EventBus.getDefault().apply {
            register(adapter)
            register(this@GpxListFragment)
        }

        requestServiceConnectionIfNeeded()
    }

    override fun onResume() {
        super.onResume()
        updateCurrentRecordingView(currentlyRecordingRouteId)
    }

    override fun onServiceConnected(serviceConnection: RecorderServiceConnection) {
        currentlyRecordingRouteId = serviceConnection.service?.gpxId
        updateCurrentRecordingView(currentlyRecordingRouteId)
    }

    override fun onServiceDisconnected() {
        currentlyRecordingRouteId = null
        updateCurrentRecordingView(null)
    }

    @Subscribe(sticky = true)
    fun onServiceStartedEvent(event: Events.RecordingStartedEvent) {
        requestServiceConnectionIfNeeded()
    }

    @Subscribe
    fun onServiceStoppedEvent(event: Events.RecordingStoppedEvent) {
        currentlyRecordingRouteId = null
        serviceConnection.service = null
        updateCurrentRecordingView(null)
    }

    private fun requestServiceConnectionIfNeeded() {
        if (serviceConnection.service == null) {
            serviceConnection.requestConnection(requireContext())
        } else {
            updateCurrentRecordingView(serviceConnection.service?.gpxId)
        }
    }

    private fun updateCurrentRecordingView(gpxId: Long?) {
        if (gpxId != null) {
            fab.hide()
        } else {
            fab.show()
        }
    }

    private fun onFabClicked(view: View) {
        if (isTransitioning) return

        PermissionHelper.getInstance(this.requireActivity()).checkLocationPermissions(onAllowed = {
            RecordingConfiguratorModal.circularReveal(
                    originXY = Pair(view.x.toInt() + (view.width / 2), view.y.toInt() + (view.height / 2)),
                    fragmentManager = parentFragmentManager
            )
        })
    }

    private fun showContentViewerFragment(gpxId: Long) {
        if (isTransitioning) return
        isTransitioning = true
        parentFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, android.R.anim.fade_out, R.anim.none, android.R.anim.slide_out_right)
                .replace(R.id.content_container, GpxDetailsFragment.newInstance(gpxId))
                .addToBackStack("view")
                .commit()
    }

    private fun showRecordingFragment() {
        val gpxId = currentlyRecordingRouteId ?: return
        if (isTransitioning) return
        isTransitioning = true
        parentFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, android.R.anim.fade_out, R.anim.none, android.R.anim.slide_out_right)
                .replace(R.id.content_container, RecorderFragment.newInstance(gpxId))
                .addToBackStack("recorder")
                .commit()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_gpx_list, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = (requireActivity() as AppCompatActivity)
        activity.setSupportActionBar(bottomAppBar)
        bottomAppBar.setNavigationOnClickListener {
            val bottomNavDrawerFragment = BottomNavigationDrawer()
            bottomNavDrawerFragment.show(parentFragmentManager, bottomNavDrawerFragment.tag)
        }

        fab.setOnClickListener(this::onFabClicked)
        val adapter = GpxRecyclerViewAdapter(view.context, gpxContentList) { saveRouteToDevice(it) }
        adapter.contentViewerOpener = this::showContentViewerFragment
        adapter.currentRecordingOpener = this::showRecordingFragment

        this.adapter = adapter
        adapter.registerAdapterDataObserver(object: RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                gpx_listView?.scrollToPosition(0)
            }
        })

        ItemTouchHelper(GpxListSwipeHandler(adapter::rowDismissed)).attachToRecyclerView(gpx_listView)
        gpx_listView.layoutManager = LinearLayoutManager(view.context)
        gpx_listView.adapter = adapter
        gpx_listView.setHasFixedSize(true)
        (gpx_listView.itemAnimator as DefaultItemAnimator).supportsChangeAnimations = false
        val dividerItemDecoration = DividerItemDecoration(gpx_listView.context, DividerItemDecoration.VERTICAL)
        gpx_listView.addItemDecoration(dividerItemDecoration)

        setPlaceholdersHidden(gpxContentList.isNotEmpty())
        gpxContentList.addChangeListener(gpxChangeListener)

        isTransitioning = false

        gpx_listView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0 && fab.visibility == View.VISIBLE) {
                    fab.hide()
                } else if (dy < 0 && fab.visibility != View.VISIBLE && currentlyRecordingRouteId == null) {
                    fab.show()
                }
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode) {
            CREATE_FILE_INTENT_ID -> onSaveLocationSelected(data?.data)
        }
    }

    private fun saveRouteToDevice(gpxId: Long) {
        savingId = gpxId
        showSystemFolderPicker()
    }

    private fun showSystemFolderPicker() {
        val context = context ?: return
        val gpxId = savingId ?: return
        val filename = fileHelper.getGpxFilename(context, gpxId) ?: return

        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/xml"

            putExtra(Intent.EXTRA_TITLE, filename)
        }

        startActivityForResult(intent, CREATE_FILE_INTENT_ID)
    }

    private fun onSaveLocationSelected(location: Uri?) {
        val destination = location ?: return
        val gpxId = savingId ?: return
        val context = context ?: return

        compositeDisposable.add(fileHelper.saveGpxFile(context, gpxId, destination).subscribe {
            savingId = null
        })
    }

    private fun setPlaceholdersHidden(hidden: Boolean) {
        fragment_gpx_list?.let { root ->
            if (hidden) {
                placeholderViews.forEach {
                    root.findViewById<View>(it).apply {
                        this.visibility = View.GONE
                        this.alpha = 0f
                    }
                }
            } else {
                placeholderViews.forEach {
                    root.findViewById<View>(it).apply {
                        this.visibility = View.VISIBLE
                        this.animate().alpha(2.0f).start()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        gpxContentList.removeChangeListener(gpxChangeListener)
        gpx_listView.adapter = null
    }

    companion object {
        fun newInstance() = GpxListFragment()
    }
}