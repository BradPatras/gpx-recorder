package com.iboism.gpxrecorder.recording

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.iboism.gpxrecorder.R
import com.iboism.gpxrecorder.model.RecordingConfiguration


private const val INTERVAL_MIN = 1
private const val DISPLACEMENT_MIN = 2

class RecordingConfiguratorModal : Fragment() {
    private lateinit var configuratorView: RecordingConfiguratorView
    private var listener: Listener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.config_dialog, container, false)?.apply {
            configuratorView = RecordingConfiguratorView(this)
            configuratorView.doneButton.setOnClickListener {
                listener?.configurationCreated (
                        RecordingConfiguration(
                                title = configuratorView.titleEditText.text.toString().takeIf { it.isNotEmpty() } ?: "Untitled",
                                interval = configuratorView.intervalSlider.progress.toLong().plus(INTERVAL_MIN).times(60 * 1000),
                                minDisplacement = configuratorView.displacementSlider.progress.plus(DISPLACEMENT_MIN).toFloat()
                        ))
                this@RecordingConfiguratorModal.fragmentManager.popBackStack()
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
