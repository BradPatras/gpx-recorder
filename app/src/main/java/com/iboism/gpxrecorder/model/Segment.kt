package com.iboism.gpxrecorder.model

import com.google.android.gms.maps.model.LatLng
import com.iboism.gpxrecorder.analysis.Distance
import com.iboism.gpxrecorder.util.UUIDHelper
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import io.realm.Realm
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

/**
 * Created by Brad on 11/18/2017.
 */

open class Segment(
        @PrimaryKey var identifier: Long = UUIDHelper.random(),
        var points: RealmList<TrackPoint> = RealmList(),
        var distance: Float = 0f
) : RealmObject(), XmlSerializable {

    override fun getXmlString(): String {
        val pointsList = points
                .map { it.getXmlString() }
                .fold("") { xmlString: String, pointString: String -> xmlString + pointString }

        return "<trkseg>$pointsList</trkseg>"
    }

    fun addPoint(point: TrackPoint) {
        points.lastOrNull()?.let {
            distance += Distance.haversineKm(it, point)
        }

        points.add(point)
    }

    fun getLatLngPoints(thread: Scheduler = Schedulers.computation()): Single<List<LatLng>> {
        val identifier = this.identifier
        return Single.just(identifier)
                .subscribeOn(thread)
                .map {
                    return@map Realm.getDefaultInstance()
                            .where(Segment::class.java)
                            .equalTo(primaryKey, identifier)
                            .findFirst()
                            ?.points ?: emptyList<TrackPoint>()
                }.map {
                    return@map it.map { LatLng(it.lat,it.lon) }
                }
    }

    companion object Keys {
        const val primaryKey = "identifier"
    }
}