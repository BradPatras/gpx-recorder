package com.iboism.gpxrecorder.model

import com.iboism.gpxrecorder.util.DateTimeFormatHelper
import com.iboism.gpxrecorder.util.UUIDHelper
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

/**
 * Created by Brad on 11/19/2017.
 */
open class GpxContent(
        @PrimaryKey var identifier: Long = UUIDHelper.random(),
        var trackList: RealmList<Track> = RealmList(),
        var waypointList: RealmList<Waypoint> = RealmList(),
        var title: String = "",
        var date: String = DateTimeFormatHelper.formatDate()
        ) : XmlSerializable, RealmObject() {

    override fun getXmlString(): String {
        val titleXml = "<name>$title</name>"
        val descXml = "<desc>Recorded with GPX Recorder for Android</desc>"
        val metaDataXml = "<metadata>$titleXml$descXml</metadata>"
        val contentXml = trackList // in the future, do flatmap of all entity types first
                .map { it.getXmlString() }
                .reduce { content, entity -> content + entity }

        return "$metaDataXml$contentXml"
    }
}