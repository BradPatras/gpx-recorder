package com.iboism.gpxrecorder.model

import com.iboism.gpxrecorder.util.UUIDHelper
import io.realm.RealmList
import io.realm.RealmObject

/**
 * Created by Brad on 11/18/2017.
 */
open class Track(
        var identifier: Long = UUIDHelper.random(),
        var name: String = "",
        var description: String = "",
        var segments: RealmList<Segment> = RealmList()

) : RealmObject(), XmlSerializable {
    override fun getXmlString(): String {
        val segmentsXml = segments.map { it.getXmlString() }.reduce { segmentList, segmentString -> segmentList + segmentString }
        val nameXml = "<name>$name</name>"
        return "<trk>$nameXml$segmentsXml</trk>"
    }

}