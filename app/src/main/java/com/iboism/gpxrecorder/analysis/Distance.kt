package com.iboism.gpxrecorder.analysis

import android.graphics.PointF
import com.iboism.gpxrecorder.model.Segment
import com.iboism.gpxrecorder.model.TrackPoint
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.realm.Realm
import io.realm.RealmList

/**
 * Created by bradpatras on 12/20/17.
 */
class Distance {
    // Unused now that distances are calculated and stored in the Segment objects as points are added
//    fun ofSegment(segmentId: Long): Single<Float> {
//        return Single.just(segmentId)
//                .observeOn(Schedulers.computation())
//                .map { Realm.getDefaultInstance().where(Segment::class.java).equalTo(Segment.primaryKey, segmentId)
//                            .findFirst()?.points ?: RealmList<TrackPoint>()
//                }
//                .map { pointsToDistance(it) }
//    }
//
//    private fun pointsToDistance(points: List<TrackPoint>): Float {
//        val initialPoint = points.getOrNull(0) ?: return 0f
//
//        val (_, distance) = points.fold(initialPoint.to(0f), { (previousPoint, distance), trackPoint ->
//            trackPoint.to(distance + haversineKm(previousPoint, trackPoint))
//        })
//
//        return distance
//    }

    companion object {
        fun haversineKm(pointA: TrackPoint, pointB: TrackPoint): Float {
            // Thanks, StackOverflow
            val earthRadiusKM = 6378.1370f
            val toRads = (Math.PI / 180).toFloat()

            val a = (pointA.lat * toRads).to(pointA.lon * toRads)
            val b = (pointB.lat * toRads).to(pointB.lon * toRads)

            val latD = a.first - b.first
            val lonD = a.second - b.second

            val aa = Math.pow(Math.sin(latD / 2.0), 2.0) + Math.cos(a.first) *
                    Math.cos(b.first) *
                    Math.pow(Math.sin(lonD / 2.0), 2.0)
            val cc = 2.0 * Math.atan2(Math.sqrt(aa), Math.sqrt(1.0 - aa))

            return (cc * earthRadiusKM).toFloat()
        }
    }
}