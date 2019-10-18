package com.iboism.gpxrecorder.recording.configurator

import android.content.Context
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import com.iboism.gpxrecorder.R
import com.iboism.gpxrecorder.model.RecordingConfiguration
import com.iboism.gpxrecorder.util.circularRevealOnNextLayout
import androidx.core.content.ContextCompat.getSystemService
import com.iboism.gpxrecorder.extensions.hideSoftKeyBoard


const val REVEAL_ORIGIN_X_KEY = "kRevealXOrigin"
const val REVEAL_ORIGIN_Y_KEY = "kRevealYOrigin"

class RecordingConfiguratorModal : Fragment() {
    private lateinit var configuratorView: RecordingConfiguratorView
    private var listener: Listener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.config_dialog, container, false)?.apply {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            val initialInterval = prefs.getLong(RecordingConfiguration.intervalKey, RecordingConfiguration.defaultInterval)
            configuratorView = RecordingConfiguratorView(this, initialInterval)
            configuratorView.restoreInstanceState(savedInstanceState)
            configuratorView.doneButton.setOnClickListener {
                PreferenceManager.getDefaultSharedPreferences(context).edit()
                        .putLong(RecordingConfiguration.intervalKey, configuratorView.getIntervalMillis())
                        .apply()
                context.hideSoftKeyBoard(it)
                listener?.configurationCreated (
                        RecordingConfiguration(
                                title = configuratorView.titleEditText.text.toString().takeIf { it.isNotEmpty() } ?: "Untitled",
                                interval = configuratorView.getIntervalMillis()
                        ))
                this@RecordingConfiguratorModal.fragmentManager?.popBackStack()
            }

            val originX = arguments?.getInt(REVEAL_ORIGIN_X_KEY) ?: return@apply
            val originY = arguments?.getInt(REVEAL_ORIGIN_Y_KEY) ?: return@apply
            this.circularRevealOnNextLayout(originX, originY)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as Listener
    }

    override fun onDetach() {
        listener = null
        super.onDetach()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        configuratorView.onSaveInstanceState(outState)
    }

    interface Listener {
        fun configurationCreated(configuration: RecordingConfiguration)
    }

    companion object {
        fun instance() = RecordingConfiguratorModal()

        fun circularReveal(originXY: Pair<Int,Int>, fragmentManager: FragmentManager?) {
            val configFragment = RecordingConfiguratorModal.instance()

            val args = Bundle()
            args.putInt(REVEAL_ORIGIN_X_KEY, originXY.first)
            args.putInt(REVEAL_ORIGIN_Y_KEY, originXY.second)
            configFragment.arguments = args

            show(fragmentManager, configFragment)
        }

        fun show(fragmentManager: FragmentManager?, configFragment: RecordingConfiguratorModal = instance()) {

            // marshmallow bug workaround https://issuetracker.google.com/issues/37067655
            if (android.os.Build.VERSION.SDK_INT == android.os.Build.VERSION_CODES.M) {
                fragmentManager?.beginTransaction()
                        ?.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out)
                        ?.add(R.id.content_container, configFragment)
                        ?.addToBackStack(null)
                        ?.commitAllowingStateLoss()
            } else {
                fragmentManager?.beginTransaction()
                        ?.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out)
                        ?.add(R.id.content_container, configFragment)
                        ?.addToBackStack(null)
                        ?.commit()
            }
        }
    }
}
