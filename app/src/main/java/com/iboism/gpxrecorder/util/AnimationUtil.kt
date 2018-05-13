package com.iboism.gpxrecorder.util

import android.view.View
import android.view.ViewAnimationUtils

/**
 * Created by bradpatras on 5/12/18.
 */

fun View.circularRevealOnNextLayout(originX: Int, originY: Int) {
    this.addOnLayoutChangeListener(object : View.OnLayoutChangeListener {
        override fun onLayoutChange(v: View, left: Int, top: Int, right: Int, bottom: Int, oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int) {
            v.removeOnLayoutChangeListener(this)
            v.translationZ = 25f
            val finalRadius = Math.hypot(right.toDouble(), bottom.toDouble()).toInt().toFloat() * 1.25f
            val anim = ViewAnimationUtils.createCircularReveal(v, originX, originY, 0f, finalRadius)
            anim.duration = 400
            anim.start()
        }
    })
}