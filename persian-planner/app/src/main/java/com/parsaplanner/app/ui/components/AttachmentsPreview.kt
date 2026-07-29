package com.parsaplanner.app.ui.components

import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import java.io.File

/** Splits the comma-joined URI string stored in the DB back into a list. */
fun parseAttachmentUris(stored: String): List<Uri> =
    stored.split(",").mapNotNull { it.trim().takeIf { s -> s.isNotBlank() }?.let(Uri::parse) }

private val imageExtensions = setOf("jpg", "jpeg", "png", "gif", "webp", "bmp", "heic", "heif")

private fun extensionOf(uri: Uri): String =
    uri.lastPathSegment?.substringAfterLast(".", "")?.lowercase() ?: ""

private fun isImageUri(context: android.content.Context, uri: Uri): Boolean {
    if (uri.scheme == "file") return extensionOf(uri) in imageExtensions
    val type = context.contentResolver.getType(uri)
    return type?.startsWith("image/") == true
}

private fun mimeTypeFor(context: android.content.Context, uri: Uri): String {
    if (uri.scheme == "file") {
        val ext = extensionOf(uri)
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext) ?: "*/*"
    }
    return context.contentResolver.getType(uri) ?: "*/*"
}

/**
 * A raw file:// Uri can't be handed to another app directly (throws FileUriExposedException
 * on modern Android) — it must go through FileProvider to become a shareable content:// Uri
 * with a temporary read grant. content:// Uris (already shareable) pass through untouched.
 */
private fun shareableUri(context: android.content.Context, uri: Uri): Uri {
    if (uri.scheme != "file") return uri
    val file = File(uri.path ?: return uri)
    return try {
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    } catch (e: Exception) {
        uri
    }
}

private fun openWithExternalApp(context: android.content.Context, uri: Uri) {
    try {
        val contentUri = shareableUri(context, uri)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(contentUri, mimeTypeFor(context, uri))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(intent, null).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    } catch (e: Exception) {
        // No app installed that can open this file type — silently ignore rather than crash.
    }
}

/**
 * Renders every attachment so it can actually be opened/viewed, plus a play/pause
 * control for the voice memo if one was recorded.
 */
@Composable
fun AttachmentsPreview(attachmentUris: String, voiceNoteUri: String?, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val uris = remember(attachmentUris) { parseAttachmentUris(attachmentUris) }

    if (uris.isEmpty() && voiceNoteUri == null) return

    Column(modifier) {
        if (uris.isNotEmpty()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(uris) { uri ->
                    val isImage = remember(uri) { isImageUri(context, uri) }
                    if (isImage) {
                        AsyncImage(
                            model = uri,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { openWithExternalApp(context, uri) }
                        )
                    } else {
                        val fileName = remember(uri) { fileNameFor(context, uri) }
                        Row(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp))
                                .clickable { openWithExternalApp(context, uri) }
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.InsertDriveFile, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(fileName, style = MaterialTheme.typography.labelLarge, maxLines = 1)
                        }
                    }
                }
            }
        }

        if (voiceNoteUri != null) {
            Spacer(Modifier.height(6.dp))
            VoicePlayer(voiceNoteUri)
        }
    }
}

private fun fileNameFor(context: android.content.Context, uri: Uri): String {
    if (uri.scheme == "file") return uri.lastPathSegment ?: "file"
    var name = "file"
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val idx = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
        if (idx >= 0 && cursor.moveToFirst()) name = cursor.getString(idx) ?: name
    }
    return name
}

@Composable
private fun VoicePlayer(uriString: String) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(false) }
    val mediaPlayer = remember { MediaPlayer() }

    DisposableEffect(Unit) {
        onDispose { mediaPlayer.release() }
    }

    Row(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clickable {
                if (isPlaying) {
                    mediaPlayer.pause()
                    isPlaying = false
                } else {
                    try {
                        mediaPlayer.reset()
                        mediaPlayer.setDataSource(context, Uri.parse(uriString))
                        mediaPlayer.setOnCompletionListener { isPlaying = false }
                        mediaPlayer.prepare()
                        mediaPlayer.start()
                        isPlaying = true
                    } catch (_: Exception) { isPlaying = false }
                }
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Text("یادداشت صوتی", style = MaterialTheme.typography.labelLarge)
    }
}
