package com.iboism.gpxrecorder.primary

import android.content.Context
import android.graphics.*
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.util.Size
import android.view.View
import com.google.android.gms.maps.model.LatLng
import com.iboism.gpxrecorder.R
import kotlin.math.max

/**
 * Created by bradpatras on 4/13/18.
 */
private const val PADDING_RATIO = .2f

class PathPreviewView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var points: List<LatLng> = emptyList()
    private val linePaint = Paint()
    private val dotPaint = Paint()
    private val capDotPaint = Paint()
    private var scaledPoints: List<PointF>? = null
    private var scaledPath = Path()
    private var size: Size? = null

    fun loadPoints(points: List<LatLng>? = emptyList()) {
        this.points = points ?: emptyList()
        setupPaints()

        size?.let {
            onSizeChanged(it.width, it.height, it.width, it.width)
        }

        this.invalidate()
    }
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas?.drawColor(ContextCompat.getColor(context, R.color.gLightGreen))

        if (scaledPoints == null) return
        if (size == null) return
        if (canvas == null) return

        setupPaints()
        canvas.drawPath(scaledPath, linePaint)

        scaledPoints?.forEach {
            canvas.drawCircle(it.x, height - it.y, width.toFloat() * 0.02f, dotPaint)
        }

        scaledPoints?.firstOrNull()?.let {
            capDotPaint.color = Color.BLACK
            canvas.drawCircle(it.x, height - it.y, width.toFloat() * 0.035f, capDotPaint)
            capDotPaint.color = Color.GREEN
            canvas.drawCircle(it.x, height - it.y, width.toFloat() * 0.03f, capDotPaint)
        }

        scaledPoints?.lastOrNull()?.let {
            capDotPaint.color = Color.BLACK
            canvas.drawCircle(it.x, height - it.y, width.toFloat() * 0.035f, capDotPaint)
            capDotPaint.color = Color.RED
            canvas.drawCircle(it.x, height - it.y, width.toFloat() * 0.03f, capDotPaint)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        size = Size(w, h)

        if (points.isEmpty()) return

        val scaledPoints: List<PointF>

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
            val y = (((point.latitude - pointsXMin) / boundingLength) * viewBoundWidth) + xOffset
            return@map PointF(x.toFloat(), y.toFloat())
        }
        this.scaledPoints = scaledPoints

        scaledPath.reset()
        scaledPath.moveTo(scaledPoints[0].x, viewBoundHeight - scaledPoints[0].y)
        scaledPoints.forEach {
            scaledPath.lineTo(it.x, h - it.y)
        }

        this.scaledPoints = scaledPoints
    }

    private fun setupPaints() {
        val size = this.size ?: return

        linePaint.color = ContextCompat.getColor(context, R.color.gLightBlue)
        linePaint.strokeWidth = size.height * .05f
        linePaint.style = Paint.Style.STROKE
        linePaint.strokeJoin = Paint.Join.ROUND
        linePaint.strokeCap = Paint.Cap.ROUND
        linePaint.flags = Paint.ANTI_ALIAS_FLAG

        dotPaint.color = Color.WHITE
        dotPaint.style = Paint.Style.FILL
        dotPaint.strokeWidth = size.height * .01f
        dotPaint.flags = Paint.ANTI_ALIAS_FLAG

        capDotPaint.set(dotPaint)
    }
}