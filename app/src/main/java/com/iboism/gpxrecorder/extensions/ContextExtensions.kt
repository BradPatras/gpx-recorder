package com.iboism.gpxrecorder.extensions

import android.content.Context
import com.iboism.gpxrecorder.model.REALM_INIT_FAILED_KEY
import com.iboism.gpxrecorder.model.REALM_SHARED_PREFERENCES_NAME

fun Context.setRealmInitFailure(failed: Boolean) {
    this.getSharedPreferences(REALM_SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(REALM_INIT_FAILED_KEY, failed)
            .apply()
}

fun Context.getRealmInitFailure(): Boolean {
    return this.getSharedPreferences(REALM_SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
            .getBoolean(REALM_INIT_FAILED_KEY, false)
}