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
private const val SHARE_FILENAME = "recorded"
private const val REPLACE_CONTENT_TAG = "replacemewithcontent"
private const val FILE_EXTENSION = ".gpx"

class FileHelper(var context: Context) {
    private var exporting: Long? = null

    fun isExporting() = exporting

    fun gpxFileWith(gpxContentId: Long): Single<File> {
        exporting = gpxContentId
        return Single.just(gpxContentId)
                .observeOn(Schedulers.io())
                .map {
                    val content = Realm.getDefaultInstance()
                            .where(GpxContent::class.java)
                            .equalTo(GpxContent.primaryKey, gpxContentId)
                            .findFirst()

                    val sharedFilesPath = File(context.cacheDir, SHARE_PATH)
                    sharedFilesPath.mkdirs()

                    val inputStream = context.resources.openRawResource(R.raw.gpx_stub)
                    val gpxStub = IOUtils.toString(inputStream, StandardCharsets.UTF_8)

                    val gpxFull = gpxStub.replaceFirst(REPLACE_CONTENT_TAG, content?.getXmlString() ?: "")
                    val fileName = content?.title?.getLegalFilename()?.withGpxExt() ?: SHARE_FILENAME.withGpxExt()
                    val gpxSharedFile = File(sharedFilesPath, fileName)
                    FileUtils.writeStringToFile(gpxSharedFile, gpxFull, StandardCharsets.UTF_8)
                    exporting = null
                    gpxSharedFile
                }
                .doOnError { exporting = null }
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