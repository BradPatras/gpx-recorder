package com.iboism.gpxrecorder.primary

import android.content.Context
import android.graphics.*
import android.util.Size
import android.view.View

/**
 * Created by bradpatras on 4/13/18.
 */
private const val PADDING_RATIO = .1f

class PathPreviewView : View {

    val points: List<PointF>
    val linePaint = Paint()
    val dotPaint = Paint()
    var scaledPoints: List<PointF>
    var scaledPath = Path()

    constructor(context: Context, points: List<PointF>) : super(context) {
        this.points = points
        this.scaledPoints = points
        setupPaints()
    }

    var size: Size? = null

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (size == null) return
        if (canvas == null) return

        setupPaints()

        scaledPoints.forEach { point ->
            canvas.drawPath(scaledPath, linePaint)
        }

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

        scaledPath.reset()
        scaledPath.moveTo(scaledPoints[0].x, scaledPoints[0].y)
        scaledPoints.forEach {
            scaledPath.lineTo(scaledPoints[0].x, scaledPoints[0].y)
        }
    }

    private fun setupPaints() {
        val size = this.size ?: return

        linePaint.color = Color.RED
        linePaint.strokeWidth = size.height * .05f

        dotPaint.color = Color.DKGRAY
        dotPaint.strokeWidth = size.height * .05f

    }
}