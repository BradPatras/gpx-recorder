package com.iboism.gpxrecorder.recording

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
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
            val originX = arguments?.getInt(REVEAL_ORIGIN_X_KEY) ?: return@apply
            val originY = arguments?.getInt(REVEAL_ORIGIN_Y_KEY) ?: return@apply
            this.circularRevealOnNextLayout(originX, originY)

            configuratorView = RecordingConfiguratorView(this)
            configuratorView.doneButton.setOnClickListener { _ ->
                listener?.configurationCreated (
                        RecordingConfiguration(
                                title = configuratorView.titleEditText.text.toString().takeIf { it.isNotEmpty() } ?: "Untitled",
                                interval = configuratorView.actualInterval().toLong()
                        ))
                this@RecordingConfiguratorModal.fragmentManager?.popBackStack()
            }
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
    }
}
