package com.iboism.gpxrecorder.util

import android.R.attr.bitmap
import android.R.attr.data
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.core.net.toUri
import com.iboism.gpxrecorder.R
import com.iboism.gpxrecorder.model.GpxContent
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.realm.Realm
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.net.URI
import java.nio.charset.StandardCharsets


/**
 * Created by Brad on 11/18/2017.
 */

private const val SHARE_PATH = "shared"
private const val REPLACE_CONTENT_TAG = "replacemewithcontent"
private const val FILE_EXTENSION = ".gpx"

private enum class FileDestinationType {
    CACHE,
}

class FileHelper {
    private var exporting: Long? = null

    fun isExporting() = exporting

    fun shareGpxFile(context: Context, gpxContentId: Long): Completable {
        val sharedFilesDir = File(context.cacheDir, SHARE_PATH).apply { this.mkdirs() }
        val filename = getGpxFilename(context, gpxContentId)
        val sharedFile = File(sharedFilesDir, filename)

        return createGpxFile(context, gpxContentId, sharedFile)
                .observeOn(AndroidSchedulers.mainThread())
                .map { file ->
                    ShareHelper(context).shareFile(file)
                }.ignoreElement().onErrorComplete {
                    Alerts(context).genericError(R.string.file_share_failed).show()
                    return@onErrorComplete true
                }
    }

    fun saveGpxFile(context: Context, gpxContentId: Long, destinationFileURI: Uri): Completable {
        return createGpxFile(context, gpxContentId, destinationFileURI)
                .observeOn(AndroidSchedulers.mainThread())
                .ignoreElement().onErrorComplete {
                    Alerts(context).genericError(R.string.file_save_failed).show()
                    return@onErrorComplete true
                }
    }

    fun getGpxFilename(context: Context, gpxContentId: Long): String {
        val realm = Realm.getDefaultInstance()
        val gpx = GpxContent.withId(gpxContentId, realm) ?: throw Exception()
        return gpx.title.getLegalFilename().withGpxExt()
    }

    private fun createGpxFile(context: Context, gpxContentId: Long, destFile: File): Single<File> {
        exporting = gpxContentId
        return Single.just(gpxContentId)
                .observeOn(Schedulers.io())
                .map {
                    val realm = Realm.getDefaultInstance()
                    val gpx = GpxContent.withId(it, realm) ?: throw Exception()
                    writeGpxToFile(context, gpx, destFile)
                    realm.close()
                    return@map destFile
                }
                .doFinally { exporting = null }
    }

    private fun createGpxFile(context: Context, gpxContentId: Long, uri: Uri): Single<Unit> {
        exporting = gpxContentId
        return Single.just(gpxContentId)
                .observeOn(Schedulers.io())
                .map {
                    val realm = Realm.getDefaultInstance()
                    val gpx = GpxContent.withId(it, realm) ?: throw Exception()
                    writeGpxToStream(context, gpx, uri)
                    realm.close()
                }
                .doFinally { exporting = null }
    }

    private fun getGpxStub(context: Context): String {
        val inputStream = context.resources.openRawResource(R.raw.gpx_stub)
        val gpxStub = IOUtils.toString(inputStream, StandardCharsets.UTF_8)
        inputStream.close()
        return gpxStub
    }

    private fun writeGpxToFile(context: Context, gpx: GpxContent, file: File): File {
        val gpxFull = getGpxStub(context).replaceFirst(REPLACE_CONTENT_TAG, gpx.getXmlString())
        FileUtils.writeStringToFile(file, gpxFull, StandardCharsets.UTF_8)

        return file
    }

    private fun writeGpxToStream(context: Context, gpx: GpxContent, uri: Uri) {
        val gpxFull = getGpxStub(context).replaceFirst(REPLACE_CONTENT_TAG, gpx.getXmlString())
        val fileOutputStream = context.contentResolver.openOutputStream(uri) ?: return
        try {
            fileOutputStream.write(gpxFull.encodeToByteArray())
            fileOutputStream.flush()
            fileOutputStream.close()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun String.getLegalFilename(): String {
        return this.replace(Regex.fromLiteral("[^a-zA-Z0-9\\.\\-]"), "_")
                .replace(" ", "_")
    }

    private fun String.withGpxExt(): String {
        return "$this$FILE_EXTENSION"
    }

    companion object : SingletonHolder<FileHelper>(::FileHelper)
}