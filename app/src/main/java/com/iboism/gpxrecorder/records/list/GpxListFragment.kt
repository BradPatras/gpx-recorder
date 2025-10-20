package com.iboism.gpxrecorder.records.list

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.divider.MaterialDividerItemDecoration
import com.iboism.gpxrecorder.Events
import com.iboism.gpxrecorder.R
import com.iboism.gpxrecorder.databinding.FragmentRouteListBinding
import com.iboism.gpxrecorder.export.ExportFragment
import com.iboism.gpxrecorder.model.GpxContent
import com.iboism.gpxrecorder.navigation.NavigationHelper
import com.iboism.gpxrecorder.recording.RecorderFragment
import com.iboism.gpxrecorder.recording.RecorderServiceConnection
import com.iboism.gpxrecorder.recording.configurator.RecordingConfiguratorModal
import com.iboism.gpxrecorder.records.details.GpxDetailsFragment
import com.iboism.gpxrecorder.util.DP
import com.iboism.gpxrecorder.util.PermissionHelper
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class GpxListFragment : Fragment(), RecorderServiceConnection.OnServiceConnectedDelegate {
    private val placeholderViews = listOf(R.id.placeholder_routes_text, R.id.placeholder_routes_icon)
    private val gpxContentList = Realm.getDefaultInstance().where(GpxContent::class.java).findAll().sort("date", Sort.DESCENDING)
    private var adapter: GpxRecyclerViewAdapter? = null
    private var currentlyRecordingRouteId: Long? = null
    private var serviceConnection: RecorderServiceConnection = RecorderServiceConnection(this)
    private lateinit var binding: FragmentRouteListBinding
    private var isSelecting: Boolean = false
    private var didSelectAll: Boolean = false
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
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                updateCurrentRecordingView(null)
            }
        }
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
            binding.fab.hide()
        } else if (!isSelecting) {
            binding.fab.show()
        }
    }

    private fun onFabClicked(view: View) {
        PermissionHelper.checkLocationPermissions(this.requireActivity()) {
            RecordingConfiguratorModal.circularReveal(
                originXY = Pair(view.x.toInt() + (view.width / 2), view.y.toInt() + (view.height / 2)),
                fragmentManager = parentFragmentManager
            )
        }
    }

    private fun showContentViewerFragment(gpxId: Long) {
        parentFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.slide_in_right, R.anim.none, R.anim.none, R.anim.slide_out_right)
            .replace(R.id.content_container, GpxDetailsFragment.newInstance(gpxId))
            .addToBackStack("view")
            .commit()
    }

    private fun showRecordingFragment() {
        val gpxId = currentlyRecordingRouteId ?: return
        parentFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.slide_in_right, R.anim.none, R.anim.none, R.anim.slide_out_right)
            .replace(R.id.content_container, RecorderFragment.newInstance(gpxId))
            .addToBackStack("recorder")
            .commit()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentRouteListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    private fun onExportButtonClicked(button: View) {
        // Enable select mode
        isSelecting = true
        binding.selectAllButton.text = getString(R.string.select_all)
        binding.listNavOverflowButton.visibility = View.GONE
        binding.gpxNavTitle.visibility = View.INVISIBLE
        binding.exportButton.visibility = View.GONE
        binding.cancelButton.visibility = View.VISIBLE
        binding.selectAllButton.visibility = View.VISIBLE
        binding.fab.hide()
        binding.exportFab.show()
        didSelectAll = false
        adapter?.selectModeChanged(isSelecting)
    }

    private fun onCancelButtonClicked(button: View) {
        disableSelectMode()
    }

    private fun disableSelectMode() {
        // disable select mode
        isSelecting = false
        didSelectAll = false
        binding.selectAllButton.text = getString(R.string.select_all)
        binding.listNavOverflowButton.visibility = View.VISIBLE
        binding.gpxNavTitle.visibility = View.VISIBLE
        binding.exportButton.visibility = View.VISIBLE
        binding.cancelButton.visibility = View.GONE
        binding.selectAllButton.visibility = View.GONE
        binding.fab.show()
        binding.exportFab.hide()
        adapter?.selectModeChanged(isSelecting)
    }

    fun onExportFabClicked(button: View) {
        adapter?.let {
            ExportFragment.newInstance(it.selectedIds, actions = listOf(ExportFragment.Action.Download,
                ExportFragment.Action.Share)).show(parentFragmentManager, "export")
        }
    }

    fun onSelectAllButtonClicked(view: View) {
        if (didSelectAll) {
            binding.selectAllButton.text = getString(R.string.select_all)
            adapter?.selectedIds = mutableListOf()
            didSelectAll = false
        } else {
            adapter?.selectedIds = adapter
                ?.contentList
                ?.map { it.identifier }
                ?.toMutableList()
                ?: mutableListOf()
            binding.selectAllButton.text = getString(R.string.deselect_all)
            didSelectAll = true

        }
        adapter?.notifyDataSetChanged()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = (requireActivity() as AppCompatActivity)

        binding.cancelButton.setOnClickListener(this::onCancelButtonClicked)
        binding.exportFab.setOnClickListener(this::onExportFabClicked)
        binding.exportButton.setOnClickListener(this::onExportButtonClicked)
        binding.selectAllButton.setOnClickListener(this::onSelectAllButtonClicked)

        val popup = PopupMenu(requireContext(), binding.listNavOverflowButton)
        popup.menuInflater.inflate(R.menu.activity_main_drawer, popup.menu)
        popup.setOnMenuItemClickListener {
            val navHelper = NavigationHelper(activity)
            navHelper.onNavigationItemSelected(it)
        }

        binding.listNavOverflowButton.setOnClickListener {
            popup.show()
        }

        binding.fab.setOnClickListener(this::onFabClicked)
        val adapter = GpxRecyclerViewAdapter(view.context, gpxContentList)
        adapter.contentViewerOpener = this::showContentViewerFragment
        adapter.currentRecordingOpener = this::showRecordingFragment

        this.adapter = adapter
        adapter.registerAdapterDataObserver(object: RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                binding.gpxListView.scrollToPosition(0)
            }
        })

        ItemTouchHelper(GpxListSwipeHandler(adapter::rowDismissed)).attachToRecyclerView(binding.gpxListView)
        binding.gpxListView.layoutManager = LinearLayoutManager(view.context)
        binding.gpxListView.adapter = adapter
        binding.gpxListView.setHasFixedSize(true)
        (binding.gpxListView.itemAnimator as DefaultItemAnimator).supportsChangeAnimations = false
        val divider = MaterialDividerItemDecoration(view.context, DividerItemDecoration.VERTICAL)
        divider.isLastItemDecorated = false
        divider.dividerInsetStart = DP(20f, view.context).pxValue
        binding.gpxListView.addItemDecoration(divider)

        setPlaceholdersHidden(gpxContentList.isNotEmpty())
        gpxContentList.addChangeListener(gpxChangeListener)

        binding.gpxListView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0 && binding.fab.visibility == View.VISIBLE) {
                    if (isSelecting) {
                        binding.exportFab.hide()
                    } else {
                        binding.fab.hide()
                    }
                } else if (dy < 0 && binding.fab.visibility != View.VISIBLE && currentlyRecordingRouteId == null) {
                    if (isSelecting) {
                        binding.exportFab.show()
                    } else {
                        binding.fab.show()
                    }
                }
            }
        })
    }

    private fun setPlaceholdersHidden(hidden: Boolean) {
        binding.fragmentGpxList.let { root ->
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
                        this.animate().alpha(1.0f).start()
                    }
                }
            }
        }
    }

    private fun launchExternalIntent(intent: Intent) {
        try {
            requireActivity().startActivity(intent)
        } catch (e: Exception) {
            // fail silently
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        gpxContentList.removeChangeListener(gpxChangeListener)
        binding.gpxListView.adapter = null
    }

    companion object {
        fun newInstance() = GpxListFragment()
    }
}
