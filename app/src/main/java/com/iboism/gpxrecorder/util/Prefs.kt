package com.iboism.gpxrecorder.util

import android.content.Context
import android.content.SharedPreferences

class Prefs {
    companion object {
        fun getDefault(context: Context): SharedPreferences {
            return context.getSharedPreferences("com.iboism.gpxrecorder", Context.MODE_PRIVATE)
        }
    }
}