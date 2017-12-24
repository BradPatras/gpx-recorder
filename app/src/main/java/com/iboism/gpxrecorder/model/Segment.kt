package com.iboism.gpxrecorder.model

import com.iboism.gpxrecorder.analysis.Distance
import com.iboism.gpxrecorder.util.UUIDHelper
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

    companion object Keys {
        val primaryKey = "identifier"
    }
}