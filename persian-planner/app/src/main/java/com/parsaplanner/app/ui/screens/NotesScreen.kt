package com.parsaplanner.app.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.parsaplanner.app.R
import com.parsaplanner.app.data.NoteEntity
import com.parsaplanner.app.data.NotesViewModel
import com.parsaplanner.app.ui.components.AttachmentsPreview
import com.parsaplanner.app.ui.components.GradientFab
import com.parsaplanner.app.ui.components.glassSurface
import com.parsaplanner.app.ui.components.luxuryCardElevation
import com.parsaplanner.app.util.VoiceRecorder

// A small starter sticker set — emoji-based placeholders.
// Swap for real vector sticker assets (see README) once art is ready.
private val stickerOptions = listOf("✨", "🌿", "☕", "📌", "❤️", "🔥", "🌙", "⭐")

private val noteColorOptions = listOf(
    "#FFF6E5", "#F1E8FF", "#E8F5E9", "#FFE9E3", "#E3F2FD"
)

@Composable
fun NotesScreen(vm: NotesViewModel = viewModel()) {
    val notes by vm.notes.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            GradientFab(icon = Icons.Filled.Add, contentDescription = stringResource(R.string.action_add)) {
                showDialog = true
            }
        }
    ) { padding ->
        if (notes.isEmpty()) {
            Box(Modifier.padding(padding).fillMaxSize()) {
                Text(stringResource(R.string.notes_empty),
                    modifier = Modifier.align(androidx.compose.ui.Alignment.Center))
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.padding(padding).fillMaxSize().padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(notes, key = { it.id }) { note ->
                    NoteCard(note, onPin = { vm.togglePin(note) }, onDelete = { vm.delete(note) })
                }
            }
        }
    }

    if (showDialog) {
        AddNoteDialog(onDismiss = { showDialog = false }, onConfirm = {
            vm.add(it)
            showDialog = false
        })
    }
}

@Composable
private fun NoteCard(note: NoteEntity, onPin: () -> Unit, onDelete: () -> Unit) {
    val shape = RoundedCornerShape(18.dp)
    val noteColor = Color(android.graphics.Color.parseColor(note.colorHex))
    Box(
        modifier = Modifier.glassSurface(shape = shape, tint = noteColor)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                if (note.stickerId != null) {
                    Text(note.stickerId, style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.width(6.dp))
                }
                Text(note.title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                IconButton(onClick = onPin, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Filled.PushPin, contentDescription = null, tint = if (note.pinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(note.content, style = MaterialTheme.typography.bodyMedium, maxLines = 5)
            Spacer(Modifier.height(6.dp))
            AttachmentsPreview(note.attachmentUris, note.voiceNoteUri)
            Spacer(Modifier.height(4.dp))
            TextButton(onClick = onDelete) { Text(stringResource(R.string.action_delete)) }
        }
    }
}

@Composable
private fun AddNoteDialog(onDismiss: () -> Unit, onConfirm: (NoteEntity) -> Unit) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var color by remember { mutableStateOf(noteColorOptions.first()) }
    var sticker by remember { mutableStateOf<String?>(null) }
    var attachments by remember { mutableStateOf(listOf<Uri>()) }
    var voiceNoteUri by remember { mutableStateOf<Uri?>(null) }
    var isRecording by remember { mutableStateOf(false) }
    var showDrawing by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val recorder = remember { VoiceRecorder(context) }

    // Pick photos (multiple)
    val photoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        attachments = attachments + uris
    }
    // Pick any document/file type (multiple)
    val fileLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        uris.forEach { uri ->
            try {
                context.contentResolver.takePersistableUriPermission(uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (_: Exception) { /* some providers don't support persistable permissions */ }
        }
        attachments = attachments + uris
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.note_dialog_title)) },
        text = {
            Column {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text(stringResource(R.string.label_title)) }, singleLine = true)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = content, onValueChange = { content = it }, label = { Text(stringResource(R.string.label_content)) })
                Spacer(Modifier.height(10.dp))
                Text(stringResource(R.string.note_color_label), style = MaterialTheme.typography.labelLarge)
                Row {
                    noteColorOptions.forEach { c ->
                        val borderModifier = if (color == c)
                            Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(50))
                        else Modifier
                        Box(
                            Modifier
                                .padding(4.dp)
                                .size(28.dp)
                                .background(Color(android.graphics.Color.parseColor(c)), RoundedCornerShape(50))
                                .then(borderModifier)
                                .clickable { color = c }
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                Text(stringResource(R.string.note_sticker_label), style = MaterialTheme.typography.labelLarge)
                Row {
                    stickerOptions.forEach { s ->
                        Text(
                            s,
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier
                                .padding(4.dp)
                                .clickable { sticker = s }
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                Text(stringResource(R.string.attach_label), style = MaterialTheme.typography.labelLarge)
                Row {
                    OutlinedButton(onClick = { photoLauncher.launch("image/*") }, modifier = Modifier.padding(end = 6.dp)) {
                        Text(stringResource(R.string.attach_photo))
                    }
                    OutlinedButton(onClick = { fileLauncher.launch(arrayOf("*/*")) }, modifier = Modifier.padding(end = 6.dp)) {
                        Text(stringResource(R.string.attach_file))
                    }
                    OutlinedButton(onClick = {
                        if (isRecording) {
                            voiceNoteUri = recorder.stop()?.let { Uri.fromFile(it) }
                            isRecording = false
                        } else {
                            recorder.start()
                            isRecording = true
                        }
                    }, modifier = Modifier.padding(end = 6.dp)) {
                        Text(stringResource(if (isRecording) R.string.record_stop else R.string.record_start))
                    }
                    OutlinedButton(onClick = { showDrawing = true }) {
                        Text("✏️ رسم با قلم")
                    }
                }
                if (attachments.isNotEmpty()) {
                    Text(stringResource(R.string.attach_count, attachments.size), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
                }
                if (voiceNoteUri != null) {
                    Text(stringResource(R.string.voice_recorded), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (title.isNotBlank()) {
                    val now = System.currentTimeMillis()
                    onConfirm(
                        NoteEntity(
                            title = title,
                            content = content,
                            colorHex = color,
                            stickerId = sticker,
                            attachmentUris = attachments.joinToString(",") { it.toString() },
                            voiceNoteUri = voiceNoteUri?.toString(),
                            createdAtEpochMillis = now,
                            updatedAtEpochMillis = now
                        )
                    )
                }
            }) { Text(stringResource(R.string.action_add)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } }
    )

    if (showDrawing) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { showDrawing = false },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            com.parsaplanner.app.ui.components.DrawingScreen(
                onCancel = { showDrawing = false },
                onSave = { uri ->
                    attachments = attachments + uri
                    showDrawing = false
                }
            )
        }
    }
}
