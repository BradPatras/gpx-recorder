package com.iboism.gpxrecorder.util

import android.content.Context
import com.iboism.gpxrecorder.R
import com.iboism.gpxrecorder.model.GpxContent
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import java.io.File
import java.nio.charset.StandardCharsets


/**
 * Created by Brad on 11/18/2017.
 */

private const val SHARE_PATH = "shared"
private const val SHARE_FILENAME = "recorded.gpx"
private const val REPLACE_CONTENT_TAG = "replacemewithcontent"

class FileHelper(var context: Context) {
    fun gpxFileWith(content: GpxContent): Single<File> {
        return Single.just(content)
                .observeOn(Schedulers.io())
                .map {
                    val sharedFilesPath = File(context.cacheDir, SHARE_PATH)
                    sharedFilesPath.mkdirs()

                    val inputStream = context.resources.openRawResource(R.raw.gpx_stub)
                    val gpxStub = IOUtils.toString(inputStream, StandardCharsets.UTF_8)

                    val gpxFull = gpxStub.replaceFirst(REPLACE_CONTENT_TAG, content.getXmlString())

                    val gpxSharedFile = File(sharedFilesPath, SHARE_FILENAME)
                    FileUtils.writeStringToFile(gpxSharedFile, gpxFull, StandardCharsets.UTF_8)
                    gpxSharedFile
                }
    }

}