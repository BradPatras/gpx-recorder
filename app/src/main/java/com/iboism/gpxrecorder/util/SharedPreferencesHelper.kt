package com.iboism.gpxrecorder.util

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences

class SharedPreferencesHelper {
    companion object {
        private const val PREFS_KEY = "gpx_recorder_prefs"
        fun default(context: Context): SharedPreferences {
            return context.getSharedPreferences(PREFS_KEY, MODE_PRIVATE)
        }
    }
}