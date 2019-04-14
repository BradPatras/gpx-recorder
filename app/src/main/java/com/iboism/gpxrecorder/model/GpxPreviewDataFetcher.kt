package com.iboism.gpxrecorder.model

import android.graphics.*
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.data.DataFetcher
import com.google.android.gms.maps.model.LatLng
import io.realm.Realm
import java.lang.Exception
import kotlin.math.max

private const val PADDING_RATIO = .2f

class GpxPreviewDataFetcher(val gpxId: Long, val width: Int,val height: Int): DataFetcher<Bitmap> {
    private val linePaint = Paint()
    private val dotPaint = Paint()
    private val capDotPaint = Paint()

    override fun getDataClass(): Class<Bitmap> {
        return Bitmap::class.java
    }

    override fun cleanup() {
        // todo figure out if i need to do something here
    }

    override fun getDataSource(): DataSource {
        return DataSource.LOCAL // todo investigate the other source options
    }

    override fun cancel() {
    }

    override fun loadData(priority: Priority, callback: DataFetcher.DataCallback<in Bitmap>) {
        val points = getPoints(gpxId) ?: return callback.onLoadFailed(Exception()) // todo add exceptions

        val config = Bitmap.Config.RGB_565
        val bitmap = Bitmap.createBitmap(width, height, config)
        val canvas = Canvas(bitmap)

        if (points.isEmpty()) {
            val scaledPoints = getScaledPoints(points, width, height)
            val scaledPath = getScaledPath(scaledPoints, width)

            drawPreview(scaledPoints, scaledPath, canvas, height, width)
        }

        callback.onDataReady(bitmap)
    }

    private fun getPoints(gpxId: Long): List<LatLng>? {
        val realm = Realm.getDefaultInstance()
        val gpx: GpxContent = GpxContent.withId(gpxId, realm) ?: return null
        val segment = gpx.trackList.firstOrNull()?.segments?.firstOrNull() ?: return null
        val points = segment.getLatLngPointsSync()
        realm.close()
        return points
    }

    private fun getScaledPoints(points: List<LatLng>, w: Int, h: Int): List<PointF> {
        val pointsXMax = points.fold(points.first().latitude.toFloat()) { max, pointF -> Math.max(max, pointF.latitude.toFloat()) }
        val pointsXMin = points.fold(points.first().latitude.toFloat()) { min, pointF -> Math.min(min, pointF.latitude.toFloat()) }

        val pointsYMax = points.fold(points.first().longitude.toFloat()) { max, pointF -> Math.max(max, pointF.longitude.toFloat()) }
        val pointsYMin = points.fold(points.first().longitude.toFloat()) { min, pointF -> Math.min(min, pointF.longitude.toFloat()) }

        val pointsHeight = Math.abs(pointsYMax - pointsYMin)
        val pointsWidth = Math.abs(pointsXMax - pointsXMin)

        val viewBoundWidth = w - (w * PADDING_RATIO)
        val viewBoundHeight = h - (h * PADDING_RATIO)

        var boundingLength = max(pointsHeight, pointsWidth)
        if (boundingLength == 0f) {
            boundingLength = viewBoundWidth
        }

        val newPointsHeight = (pointsHeight / boundingLength) * viewBoundHeight
        val newPointsWidth = (pointsWidth / boundingLength) * viewBoundWidth

        val yOffset = (h - newPointsHeight)/2f
        val xOffset = (w - newPointsWidth)/2f

        return points.map { point ->
            val x = (((point.longitude - pointsYMin) / boundingLength) * viewBoundHeight) + yOffset
            val y = (((point.latitude - pointsXMin) / boundingLength) * viewBoundWidth) + xOffset
            return@map PointF(x.toFloat(), y.toFloat())
        }
    }

    private fun getScaledPath(points: List<PointF>, h: Int): Path {
        val scaledPath = Path()
        val viewBoundHeight = h - (h * PADDING_RATIO)

        if (points.size > 1) {
            scaledPath.reset()
            scaledPath.moveTo(points[0].x, viewBoundHeight - points[0].y)
            points.forEach {
                scaledPath.lineTo(it.x, h - it.y)
            }
        }

        return scaledPath
    }

    private fun drawPreview(scaledPoints: List<PointF>, scaledPath: Path, canvas: Canvas, height: Int, width: Int) {
        canvas.drawColor(Color.parseColor("#c1ecb0"))

        setupPaints(height)
        canvas.drawPath(scaledPath, linePaint)

        scaledPoints.forEach {
            canvas.drawCircle(it.x, height - it.y, width.toFloat() * 0.02f, dotPaint)
        }

        scaledPoints.firstOrNull()?.let {
            capDotPaint.color = Color.BLACK
            canvas.drawCircle(it.x, height - it.y, width.toFloat() * 0.035f, capDotPaint)
            capDotPaint.color = Color.GREEN
            canvas.drawCircle(it.x, height - it.y, width.toFloat() * 0.03f, capDotPaint)
        }

        scaledPoints.lastOrNull()?.let {
            capDotPaint.color = Color.BLACK
            canvas.drawCircle(it.x, height - it.y, width.toFloat() * 0.035f, capDotPaint)
            capDotPaint.color = Color.RED
            canvas.drawCircle(it.x, height - it.y, width.toFloat() * 0.03f, capDotPaint)
        }
    }

    private fun setupPaints(heightForScaling: Int) {
        linePaint.color = Color.parseColor("#46b4fb")
        linePaint.strokeWidth = heightForScaling * .05f
        linePaint.style = Paint.Style.STROKE
        linePaint.strokeJoin = Paint.Join.ROUND
        linePaint.strokeCap = Paint.Cap.ROUND
        linePaint.flags = Paint.ANTI_ALIAS_FLAG

        dotPaint.color = Color.WHITE
        dotPaint.style = Paint.Style.FILL
        dotPaint.strokeWidth = heightForScaling * .01f
        dotPaint.flags = Paint.ANTI_ALIAS_FLAG

        capDotPaint.set(dotPaint)
    }
}