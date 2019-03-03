package com.iboism.gpxrecorder.extensions

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View


/**
 * Created by Brad Patras on 3/3/19.
 */

fun View.generateBitmap(): Bitmap {
    //Define a bitmap with the same size as the view
    val returnedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(returnedBitmap)

    background.draw(canvas)
    this.draw(canvas)

    return returnedBitmap
}