package com.iboism.gpxrecorder.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

/**
 * Created by bradpatras on 12/19/17.
 */
class ShareHelper(val context: Context) {
    fun shareFiles(gpxSharedFiles: List<File>) {
        val contentUris = gpxSharedFiles.map { FileProvider.getUriForFile(context, "com.iboism.gpxrecorder.fileprovider", it) }

        if (contentUris.isEmpty()) return

        val shareIntent = Intent()
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        shareIntent.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("application/octet-stream", "text/plain"))
        shareIntent.type = "application/octet-stream"

        if (contentUris.size > 1) {
            shareIntent.action = Intent.ACTION_SEND_MULTIPLE
            shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(contentUris))
        } else {
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.putExtra(Intent.EXTRA_STREAM, contentUris.first())
        }
        context.startActivity(Intent.createChooser(shareIntent, "Choose an app"))
    }
}