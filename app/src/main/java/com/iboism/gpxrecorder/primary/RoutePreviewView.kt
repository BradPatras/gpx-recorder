package com.iboism.gpxrecorder.primary

import android.content.Context
import android.graphics.Canvas
import android.graphics.PointF
import android.util.Size
import android.view.View
import com.iboism.gpxrecorder.model.Track
import com.iboism.gpxrecorder.model.TrackPoint

/**
 * Created by bradpatras on 4/13/18.
 */
private const val PADDING_RATIO = .1f

class RoutePreviewView : View {

    val points: List<PointF>
    var scaledPoints: List<PointF>

    constructor(context: Context, points: List<PointF>) : super(context) {
        this.points = points
        this.scaledPoints = points
    }

    var size: Size? = null

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        val size = this.size ?: return
        //draw points with line between them

    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        size = Size(w, h)
        // recalculate scaled points
        val pointsXMax = points.fold(0f, { max, pointF -> Math.max(max, pointF.x) })
        val pointsXMin = points.fold(0f, { min, pointF -> Math.min(min, pointF.x) })

        val pointsYMax = points.fold(0f, { max, pointF -> Math.max(max, pointF.y) })
        val pointsYMin = points.fold(0f, { min, pointF -> Math.min(min, pointF.y) })

        val pointsHeight = pointsYMax - pointsYMin
        val pointsWidth = pointsXMax - pointsXMin

        val viewBoundWidth = w - (w * PADDING_RATIO)
        val viewBoundHeight = h - (h * PADDING_RATIO)

        val scale = Math.min((pointsWidth/viewBoundWidth), (pointsHeight/viewBoundHeight))

        val newPointsHeight = scale * pointsHeight
        val newPointsWidth = scale * pointsWidth

        val yOffset = (h - newPointsHeight)/2f
        val xOffset = (w - newPointsWidth)/2f

        scaledPoints = points.map { point ->
            val x = (point.x * scale) + xOffset
            val y = (point.y * scale) + yOffset
            return@map PointF(x, y)
        }
    }
}