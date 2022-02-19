package com.iboism.gpxrecorder.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.iboism.gpxrecorder.R
import com.iboism.gpxrecorder.databinding.BottomNavigationDrawerLayoutBinding

class BottomNavigationDrawer: BottomSheetDialogFragment() {
    private val navigationHelper: NavigationHelper by lazy { NavigationHelper(requireActivity()) }
    private lateinit var binding: BottomNavigationDrawerLayoutBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = BottomNavigationDrawerLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.navView.setNavigationItemSelectedListener(navigationHelper)
    }
}