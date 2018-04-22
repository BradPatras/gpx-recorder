package com.iboism.gpxrecorder.primary

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Size
import android.view.View
import com.google.android.gms.maps.model.LatLng
import kotlin.math.max

/**
 * Created by bradpatras on 4/13/18.
 */
private const val PADDING_RATIO = .1f

class PathPreviewView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    var points: List<LatLng> = emptyList()
    val linePaint = Paint()
    val dotPaint = Paint()
    var scaledPoints: List<PointF> = emptyList()
    var scaledPath = Path()

    fun loadPoints(points: List<LatLng>? = emptyList()) {
        this.points = points ?: emptyList()
        this.scaledPoints = emptyList()
        setupPaints()
    }

    var size: Size? = null

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (size == null) return
        if (canvas == null) return

        setupPaints()
        //canvas.drawRect(0f,0f,50f,50f, dotPaint)
        canvas.drawColor(Color.LTGRAY)
        canvas.drawPath(scaledPath, linePaint)
//        if (scaledPoints.isNotEmpty()) {
//            var lastpt = scaledPoints[0]
//            scaledPoints.forEach {
//                canvas.drawLine(lastpt.x, lastpt.y, it.x, it.y, linePaint)
//                lastpt = it
//            }
//        }

    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        size = Size(w, h)

        if (points.isEmpty()) return

        // recalculate scaled points
        val pointsXMax = points.fold(points.first().latitude.toFloat(), { max, pointF -> Math.max(max, pointF.latitude.toFloat()) })
        val pointsXMin = points.fold(points.first().latitude.toFloat(), { min, pointF -> Math.min(min, pointF.latitude.toFloat()) })

        val pointsYMax = points.fold(points.first().longitude.toFloat(), { max, pointF -> Math.max(max, pointF.longitude.toFloat()) })
        val pointsYMin = points.fold(points.first().longitude.toFloat(), { min, pointF -> Math.min(min, pointF.longitude.toFloat()) })

        val pointsHeight = Math.abs(pointsYMax - pointsYMin)
        val pointsWidth = Math.abs(pointsXMax - pointsXMin)

        val viewBoundWidth = w - (w * PADDING_RATIO)
        val viewBoundHeight = h - (h * PADDING_RATIO)

        val boundingLength = max(pointsHeight, pointsWidth)

        val newPointsHeight = (pointsHeight / boundingLength) * viewBoundHeight
        val newPointsWidth = (pointsWidth / boundingLength) * viewBoundWidth

        val yOffset = (h - newPointsHeight)/2f
        val xOffset = (w - newPointsWidth)/2f
        scaledPoints = points.map { point ->

            val x = (((point.longitude - pointsYMin) / boundingLength) * viewBoundHeight) + yOffset
            val y = (((point.latitude - pointsXMin) / boundingLength) * viewBoundWidth) - xOffset
            return@map PointF(x.toFloat(), y.toFloat())
        }

        scaledPath.reset()
        scaledPath.moveTo(scaledPoints[0].x, viewBoundHeight - scaledPoints[0].y)
        scaledPoints.forEach {
            scaledPath.lineTo(it.x, viewBoundHeight - it.y)
        }
    }

    private fun setupPaints() {
        val size = this.size ?: return

        linePaint.color = Color.RED
        linePaint.strokeWidth = 5f//size.height * .05f
        linePaint.style = Paint.Style.STROKE

        dotPaint.color = Color.DKGRAY
        dotPaint.strokeWidth = size.height * .05f
    }
}