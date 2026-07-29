package com.parsaplanner.app.util

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File

object AttachmentStorage {

    /**
     * Copies the bytes behind [uri] into app-private storage and returns a stable
     * file:// Uri. This is the single most important reliability fix for attachments:
     * content:// Uris returned by pickers (especially GetMultipleContents / the photo
     * picker) are only guaranteed valid for the current session — after the app process
     * dies and restarts, or after a reboot, those Uris can throw SecurityException or
     * simply fail to resolve. Copying the bytes once, at pick time, means every photo,
     * file, and voice memo stays viewable/playable forever, independent of any grant.
     */
    fun copyToAppStorage(context: Context, uri: Uri, subfolder: String = "attachments"): Uri? {
        return try {
            val dir = File(context.filesDir, subfolder).apply { mkdirs() }
            val originalName = queryDisplayName(context, uri)
            val mimeType = context.contentResolver.getType(uri)
            val safeName = buildSafeFileName(originalName, mimeType)
            val destFile = File(dir, safeName)

            context.contentResolver.openInputStream(uri)?.use { input ->
                destFile.outputStream().use { output -> input.copyTo(output) }
            } ?: return null

            Uri.fromFile(destFile)
        } catch (e: Exception) {
            null
        }
    }

    /** Builds a unique filename that always keeps (or infers from MIME type) a real extension,
     * since file:// Uris can't be MIME-sniffed later the way content:// Uris can. */
    private fun buildSafeFileName(originalName: String?, mimeType: String?): String {
        val prefix = System.currentTimeMillis().toString()
        val hasExtension = originalName != null && originalName.contains(".") &&
            originalName.substringAfterLast(".").length in 2..5
        if (hasExtension) return "${prefix}_$originalName"

        val extension = when {
            mimeType == null -> ""
            mimeType.startsWith("image/") -> "." + mimeType.substringAfter("/").substringBefore("+")
            mimeType == "application/pdf" -> ".pdf"
            mimeType.startsWith("audio/") -> "." + mimeType.substringAfter("/")
            mimeType.startsWith("video/") -> "." + mimeType.substringAfter("/")
            mimeType.startsWith("text/") -> ".txt"
            else -> ""
        }
        val base = originalName ?: "file"
        return "${prefix}_$base$extension"
    }

    private fun queryDisplayName(context: Context, uri: Uri): String? {
        if (uri.scheme == "file") return uri.lastPathSegment
        return try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (idx >= 0 && cursor.moveToFirst()) cursor.getString(idx) else null
            }
        } catch (e: Exception) {
            null
        }
    }
}
