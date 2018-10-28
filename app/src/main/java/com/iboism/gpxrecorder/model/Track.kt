package com.iboism.gpxrecorder.model

import com.iboism.gpxrecorder.util.UUIDHelper
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

/**
 * Created by Brad on 11/18/2017.
 */
open class Track(
        @PrimaryKey var identifier: Long = UUIDHelper.random(),
        var name: String = "",
        var description: String = "",
        var segments: RealmList<Segment> = RealmList()

) : RealmObject(), XmlSerializable {
    override fun getXmlString(): String {
        val segmentsXml = segments.asSequence().map { it.getXmlString() }
                .fold("") { segmentList, segmentString -> segmentList + segmentString }
        val nameXml = "<name>$name</name>"
        return "<trk>$nameXml$segmentsXml</trk>"
    }
}