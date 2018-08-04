package com.iboism.gpxrecorder.primary

import android.support.constraint.ConstraintLayout
import android.support.design.widget.Snackbar

class SnackbarProvider(private val viewContainer: ConstraintLayout) {
    fun getSnackbar(): Snackbar {
        return Snackbar.make(viewContainer, "", Snackbar.LENGTH_LONG)
    }
}