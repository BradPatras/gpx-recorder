package com.iboism.gpxrecorder.util

import android.content.Context
import com.iboism.gpxrecorder.R
import com.iboism.gpxrecorder.model.GpxContent
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import io.realm.Realm
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import java.io.File
import java.nio.charset.StandardCharsets


/**
 * Created by Brad on 11/18/2017.
 */

private const val SHARE_PATH = "shared"
private const val REPLACE_CONTENT_TAG = "replacemewithcontent"
private const val FILE_EXTENSION = ".gpx"

class FileHelper(var context: Context) {
    private var exporting: Long? = null

    fun isExporting() = exporting

    fun gpxFileWith(gpxContentId: Long): Single<File> {
        exporting = gpxContentId
        return Single.just(gpxContentId)
                .observeOn(Schedulers.io())
                .map { GpxContent.withId(Realm.getDefaultInstance(), it) }
                .map { writeGpxToFile(it) }
                .doFinally { exporting = null }
    }

    private fun getGpxStub(): String {
        val inputStream = context.resources.openRawResource(R.raw.gpx_stub)
        val gpxStub = IOUtils.toString(inputStream, StandardCharsets.UTF_8)
        inputStream.close()
        return gpxStub
    }
    
    private fun writeGpxToFile(gpx: GpxContent): File {
        val sharedFilesPath = File(context.cacheDir, SHARE_PATH).apply { this.mkdirs() }
        val gpxFull = getGpxStub().replaceFirst(REPLACE_CONTENT_TAG, gpx.getXmlString())
        val gpxSharedFile = File(sharedFilesPath, gpx.title.getLegalFilename().withGpxExt())

        FileUtils.writeStringToFile(gpxSharedFile, gpxFull, StandardCharsets.UTF_8)

        return gpxSharedFile
    }

    private fun String.getLegalFilename(): String {
        return this.replace(Regex.fromLiteral("[^a-zA-Z0-9\\.\\-]"), "_")
                .replace(" ", "_")
    }

    private fun String.withGpxExt(): String {
        return "$this$FILE_EXTENSION"
    }

    companion object : SingletonHolder<FileHelper, Context>(::FileHelper)
}