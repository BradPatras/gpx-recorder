package com.iboism.gpxrecorder.model

import com.iboism.gpxrecorder.util.UUIDHelper
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

/**
 * Created by Brad on 11/18/2017.
 */

open class Segment(
        @PrimaryKey var identifier: Long = UUIDHelper.random(),
        var points: RealmList<TrackPoint> = RealmList()
) : RealmObject(), XmlSerializable {

    override fun getXmlString(): String {
        val pointsList = points
                .map { it.getXmlString() }
                .reduce { xmlString: String, pointString: String -> xmlString + pointString }

        return "<trkseg>$pointsList</trkseg>"
    }
}