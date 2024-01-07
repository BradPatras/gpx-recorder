package com.iboism.gpxrecorder.recording.configurator

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.preference.PreferenceManager
import com.google.android.gms.location.LocationServices
import com.iboism.gpxrecorder.R
import com.iboism.gpxrecorder.extensions.hideSoftKeyBoard
import com.iboism.gpxrecorder.model.LastLocation
import com.iboism.gpxrecorder.model.RecordingConfiguration
import com.iboism.gpxrecorder.util.PermissionHelper
import com.iboism.gpxrecorder.util.circularRevealOnNextLayout

const val REVEAL_ORIGIN_X_KEY = "kRevealXOrigin"
const val REVEAL_ORIGIN_Y_KEY = "kRevealYOrigin"
const val READ_ONLY_TITLE_KEY = "kReadOnlyTitle"
const val GPX_ID_KEY = "kGpxId"

class RecordingConfiguratorModal : Fragment() {
    private lateinit var configuratorView: RecordingConfiguratorView
    private var listener: Listener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.config_dialog, container, false)?.apply {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            val initialInterval = prefs.getLong(RecordingConfiguration.intervalKey, RecordingConfiguration.REQUEST_INTERVAL)
            val readOnlyTitle = arguments?.getString(READ_ONLY_TITLE_KEY)
            val gpxId = if (arguments?.containsKey(GPX_ID_KEY) == true) arguments?.getLong(GPX_ID_KEY) else null

            configuratorView = RecordingConfiguratorView(
                this,
                initialInterval,
                routeTitle = readOnlyTitle,
                isTitleEditable = readOnlyTitle.isNullOrEmpty(),
                isResumingRoute = gpxId != null
            )

            configuratorView.restoreInstanceState(savedInstanceState)
            configuratorView.doneButton.setOnClickListener { clickedView ->
                PreferenceManager.getDefaultSharedPreferences(context).edit()
                        .putLong(RecordingConfiguration.intervalKey, configuratorView.getIntervalMillis())
                        .apply()
                context.hideSoftKeyBoard(clickedView)
                listener?.configurationCreated (
                        gpxId = gpxId,
                        configuration = RecordingConfiguration(
                                title = configuratorView.titleEditText.text.toString().takeIf { it.isNotEmpty() } ?: "Untitled",
                                interval = configuratorView.getIntervalMillis()
                        ))
                this@RecordingConfiguratorModal.parentFragmentManager.popBackStack()
            }

            val originX = arguments?.getInt(REVEAL_ORIGIN_X_KEY) ?: return@apply
            val originY = arguments?.getInt(REVEAL_ORIGIN_Y_KEY) ?: return@apply

            this.circularRevealOnNextLayout(originX, originY)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as Listener
        cacheLastLocation()
    }

    override fun onDetach() {
        listener = null
        super.onDetach()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        configuratorView.onSaveInstanceState(outState)
    }

    @SuppressLint("MissingPermission")
    private fun cacheLastLocation() {
        PermissionHelper.getInstance(requireActivity()).checkLocationPermissions {
            LocationServices
                .getFusedLocationProviderClient(requireActivity())
                .lastLocation
                .addOnSuccessListener { loc ->
                    loc?.let {
                        LastLocation.put(lat = it.latitude, lon = it.longitude)
                    }
                }
        }
    }
    interface Listener {
        fun configurationCreated(gpxId: Long?, configuration: RecordingConfiguration)
    }

    companion object {
        fun instance() = RecordingConfiguratorModal()

        fun circularReveal(
            originXY: Pair<Int,Int>,
            fragmentManager: FragmentManager?
        ) {
            val configFragment = instance()

            val args = Bundle()
            args.putInt(REVEAL_ORIGIN_X_KEY, originXY.first)
            args.putInt(REVEAL_ORIGIN_Y_KEY, originXY.second)
            configFragment.arguments = args

            show(fragmentManager, configFragment)
        }

        fun show(fragmentManager: FragmentManager?, configFragment: RecordingConfiguratorModal = instance()) {
            fragmentManager?.beginTransaction()
                    ?.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out)
                    ?.add(R.id.content_container, configFragment)
                    ?.addToBackStack(null)
                    ?.commit()
        }
    }
}
