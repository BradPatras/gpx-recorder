package com.iboism.gpxrecorder.util

import android.content.Context
import android.util.TypedValue

class DP(val dpValue: Float, context: Context) {
    val pxValue: Int = pxToDP(dpValue, context)

    private fun pxToDP(dp: Float, context: Context): Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics()).toInt()
    }
}