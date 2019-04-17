package com.iboism.gpxrecorder.model

import com.iboism.gpxrecorder.util.DateTimeFormatHelper
import com.iboism.gpxrecorder.util.UUIDHelper
import io.realm.Realm
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
        val contentXml = listOf(trackList, waypointList) // in the future, do flatmap of all entity types first
                .flatten()
                .asSequence()
                .filterIsInstance(XmlSerializable::class.java)
                .map { it.getXmlString() }
                .fold("") { content, entity -> content + entity }

        return "$metaDataXml$contentXml"
    }

    companion object Keys {
        const val primaryKey = "identifier"

        fun withId(identifier: Long?, realm: Realm): GpxContent? {
            if (identifier == null) return null

            return realm.where(GpxContent::class.java)
                    .equalTo(GpxContent.primaryKey, identifier)
                    .findFirst()
        }
    }
}