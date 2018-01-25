package com.iboism.gpxrecorder.details

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.iboism.gpxrecorder.R
import com.iboism.gpxrecorder.model.GpxContent
import com.iboism.gpxrecorder.util.Keys
import io.realm.Realm
import kotlinx.android.synthetic.main.fragment_edit_gpxroute.*

class GpxRouteDetailsFragment : Fragment() {

    private var mListener: OnGpxEditedListener? = null
    private var gpxId: Long? = null
    private var gpxContent: GpxContent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            gpxId = arguments.getLong(Keys.GpxId)
        }


    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_edit_gpxroute, container, false)

        Realm.getDefaultInstance().where(GpxContent::class.java)
                .equalTo(GpxContent.primaryKey, gpxId)
                .findFirst()?.let {
            waypoint_listView.adapter = WaypointAdapter(gpxContent?.waypointList?.where()?.findAll())
            title_editText.setText(it.title)
        }


        return rootView
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
         * @return A new instance of fragment GpxRouteDetailsFragment.
         */
        fun newInstance(gpxId: Long): GpxRouteDetailsFragment {
            val fragment = GpxRouteDetailsFragment()
            val args = Bundle()
            args.putLong(Keys.GpxId, gpxId)
            fragment.arguments = args
            return fragment
        }
    }
}
