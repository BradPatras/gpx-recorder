package com.iboism.gpxrecorder.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.iboism.gpxrecorder.R
import kotlinx.android.synthetic.main.bottom_navigation_drawer_layout.*

class BottomNavigationDrawer: BottomSheetDialogFragment() {
    private val navigationHelper: NavigationHelper by lazy { NavigationHelper(requireActivity()) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.bottom_navigation_drawer_layout, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        nav_view.setNavigationItemSelectedListener(navigationHelper)
    }
}