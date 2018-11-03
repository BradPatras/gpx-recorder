package com.iboism.gpxrecorder.recording.configurator

import android.content.Context
import android.graphics.Point
import android.graphics.PointF
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.iboism.gpxrecorder.R
import com.iboism.gpxrecorder.model.RecordingConfiguration
import com.iboism.gpxrecorder.util.circularRevealOnNextLayout


const val REVEAL_ORIGIN_X_KEY = "kRevealXOrigin"
const val REVEAL_ORIGIN_Y_KEY = "kRevealYOrigin"

class RecordingConfiguratorModal : Fragment() {
    private lateinit var configuratorView: RecordingConfiguratorView
    private var listener: Listener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.config_dialog, container, false)?.apply {
            configuratorView = RecordingConfiguratorView(this)
            configuratorView.doneButton.setOnClickListener { _ ->
                listener?.configurationCreated (
                        RecordingConfiguration(
                                title = configuratorView.titleEditText.text.toString().takeIf { it.isNotEmpty() } ?: "Untitled",
                                interval = configuratorView.actualInterval().toLong()
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
