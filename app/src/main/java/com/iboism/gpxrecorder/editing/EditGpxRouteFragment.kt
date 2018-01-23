package com.iboism.gpxrecorder.editing

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.iboism.gpxrecorder.R
import com.iboism.gpxrecorder.util.Keys

class EditGpxRouteFragment : Fragment() {

    private var mListener: OnGpxEditedListener? = null
    private var gpxId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            gpxId = arguments.getLong(Keys.GpxId)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_edit_gpxroute, container, false)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is OnGpxEditedListener) {
            mListener = context
        } else {
            throw RuntimeException(context!!.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    interface OnGpxEditedListener {
        fun doneEditing(gpxId: Long)
    }

    companion object {
        /**
         * @param gpxId gpxId of route to be edited.
         * @return A new instance of fragment EditGpxRouteFragment.
         */
        fun newInstance(gpxId: Long): EditGpxRouteFragment {
            val fragment = EditGpxRouteFragment()
            val args = Bundle()
            args.putLong(Keys.GpxId, gpxId)
            fragment.arguments = args
            return fragment
        }
    }
}
