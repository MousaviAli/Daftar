package com.parsaplanner.app.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.parsaplanner.app.R
import com.parsaplanner.app.data.NoteEntity
import com.parsaplanner.app.ui.components.AttachmentsPreview
import com.parsaplanner.app.ui.components.DrawingScreen
import com.parsaplanner.app.ui.components.parseAttachmentUris
import com.parsaplanner.app.util.AttachmentStorage
import com.parsaplanner.app.util.VoiceRecorder

private val noteColorOptions = listOf(
    "#FFF6E5", "#F1E8FF", "#E8F5E9", "#FFE9E3", "#E3F2FD", "#FFFDF8"
)
private val stickerOptions = listOf("✨", "🌿", "☕", "📌", "❤️", "🔥", "🌙", "⭐")
private const val DEFAULT_NOTEBOOK = "عمومی"

/**
 * Full-page note editor combining Evernote's clean single-canvas writing area with
 * OneNote's notebook grouping and lightweight text formatting (bold / italic / checklist).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    existing: NoteEntity?,
    existingNotebooks: List<String> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (NoteEntity) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf(existing?.title ?: "") }
    var content by remember { mutableStateOf(TextFieldValue(existing?.content ?: "")) }
    var color by remember { mutableStateOf(existing?.colorHex ?: noteColorOptions.first()) }
    var sticker by remember { mutableStateOf(existing?.stickerId) }
    var pinned by remember { mutableStateOf(existing?.pinned ?: false) }
    var notebook by remember { mutableStateOf(existing?.notebook ?: DEFAULT_NOTEBOOK) }
    var attachments by remember {
        mutableStateOf(existing?.attachmentUris?.let { parseAttachmentUris(it) } ?: emptyList())
    }
    var voiceNoteUri by remember { mutableStateOf(existing?.voiceNoteUri) }
    var isRecording by remember { mutableStateOf(false) }
    var showColorPicker by remember { mutableStateOf(false) }
    var showStickerPicker by remember { mutableStateOf(false) }
    var showNotebookPicker by remember { mutableStateOf(false) }
    var showDrawing by remember { mutableStateOf(false) }

    val recorder = remember { VoiceRecorder(context) }

    val photoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        attachments = attachments + uris.mapNotNull { AttachmentStorage.copyToAppStorage(context, it) }
    }
    val fileLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        attachments = attachments + uris.mapNotNull { AttachmentStorage.copyToAppStorage(context, it) }
    }

    /** Wraps the current selection with [marker] on both sides (or inserts it at the cursor if nothing is selected). */
    fun wrapSelection(marker: String) {
        val sel = content.selection
        val text = content.text
        if (sel.collapsed) {
            val newText = text.substring(0, sel.start) + marker + marker + text.substring(sel.start)
            content = TextFieldValue(newText, TextRange(sel.start + marker.length))
        } else {
            val selected = text.substring(sel.min, sel.max)
            val newText = text.substring(0, sel.min) + marker + selected + marker + text.substring(sel.max)
            content = TextFieldValue(newText, TextRange(sel.min, sel.max + marker.length * 2))
        }
    }

    /** Inserts a checklist marker at the start of the current line. */
    fun insertChecklist() {
        val text = content.text
        val cursor = content.selection.start
        val lineStart = text.lastIndexOf('\n', (cursor - 1).coerceAtLeast(0)).let { if (it == -1) 0 else it + 1 }
        val newText = text.substring(0, lineStart) + "☐ " + text.substring(lineStart)
        content = TextFieldValue(newText, TextRange(cursor + 2))
    }

    fun save() {
        val now = System.currentTimeMillis()
        onSave(
            NoteEntity(
                id = existing?.id ?: 0,
                title = title.ifBlank { content.text.take(30).ifBlank { "—" } },
                content = content.text,
                colorHex = color,
                stickerId = sticker,
                pinned = pinned,
                notebook = notebook,
                attachmentUris = attachments.joinToString(",") { it.toString() },
                voiceNoteUri = voiceNoteUri,
                createdAtEpochMillis = existing?.createdAtEpochMillis ?: now,
                updatedAtEpochMillis = now
            )
        )
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {},
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_cancel))
                        }
                    },
                    actions = {
                        IconButton(onClick = { pinned = !pinned }) {
                            Icon(
                                Icons.Filled.PushPin,
                                contentDescription = null,
                                tint = if (pinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (onDelete != null) {
                            IconButton(onClick = onDelete) {
                                Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.action_delete))
                            }
                        }
                        IconButton(onClick = { save() }) {
                            Icon(Icons.Filled.Check, contentDescription = stringResource(R.string.action_save))
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(android.graphics.Color.parseColor(color)).copy(alpha = 0.5f)
                    )
                )
                // OneNote-style notebook chip
                Row(Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                    AssistChip(
                        onClick = { showNotebookPicker = true },
                        label = { Text(notebook) }
                    )
                }
                // Lightweight formatting toolbar (bold / italic / checklist)
                Row(Modifier.padding(horizontal = 8.dp, vertical = 2.dp)) {
                    IconButton(onClick = { wrapSelection("**") }) {
                        Icon(Icons.Filled.FormatBold, contentDescription = stringResource(R.string.format_bold))
                    }
                    IconButton(onClick = { wrapSelection("_") }) {
                        Icon(Icons.Filled.FormatItalic, contentDescription = stringResource(R.string.format_italic))
                    }
                    IconButton(onClick = { insertChecklist() }) {
                        Icon(Icons.Filled.CheckBox, contentDescription = stringResource(R.string.format_checklist))
                    }
                }
            }
        },
        bottomBar = {
            Column {
                if (attachments.isNotEmpty() || voiceNoteUri != null) {
                    Box(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        AttachmentsPreview(attachments.joinToString(",") { it.toString() }, voiceNoteUri)
                    }
                }
                HorizontalDivider()
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ToolbarIcon(icon = { Box(Modifier.size(20.dp).background(Color(android.graphics.Color.parseColor(color)), CircleShape)) }) {
                        showColorPicker = true
                    }
                    ToolbarIcon(icon = { Text(sticker ?: "😊", style = MaterialTheme.typography.titleMedium) }) {
                        showStickerPicker = true
                    }
                    ToolbarIcon(icon = { Icon(Icons.Filled.Image, contentDescription = null) }) {
                        photoLauncher.launch("image/*")
                    }
                    ToolbarIcon(icon = { Icon(Icons.Filled.AttachFile, contentDescription = null) }) {
                        fileLauncher.launch(arrayOf("*/*"))
                    }
                    ToolbarIcon(icon = {
                        Icon(if (isRecording) Icons.Filled.Stop else Icons.Filled.Mic, contentDescription = null,
                            tint = if (isRecording) MaterialTheme.colorScheme.error else LocalContentColor.current)
                    }) {
                        if (isRecording) {
                            voiceNoteUri = recorder.stop()?.let { Uri.fromFile(it) }?.toString()
                            isRecording = false
                        } else {
                            recorder.start()
                            isRecording = true
                        }
                    }
                    ToolbarIcon(icon = { Icon(Icons.Filled.Brush, contentDescription = null) }) {
                        showDrawing = true
                    }
                }
            }
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(android.graphics.Color.parseColor(color)).copy(alpha = 0.12f))
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            androidx.compose.foundation.text.BasicTextField(
                value = title,
                onValueChange = { title = it },
                textStyle = MaterialTheme.typography.headlineMedium.copy(color = MaterialTheme.colorScheme.onBackground),
                decorationBox = { inner ->
                    if (title.isEmpty()) Text(stringResource(R.string.label_title), style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    inner()
                },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(6.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            Spacer(Modifier.height(12.dp))
            androidx.compose.foundation.text.BasicTextField(
                value = content,
                onValueChange = { content = it },
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onBackground, lineHeight = 26.sp),
                decorationBox = { inner ->
                    if (content.text.isEmpty()) Text(stringResource(R.string.label_content), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    inner()
                },
                modifier = Modifier.fillMaxWidth().weight(1f)
            )
        }
    }

    if (showColorPicker) {
        AlertDialog(
            onDismissRequest = { showColorPicker = false },
            confirmButton = { TextButton(onClick = { showColorPicker = false }) { Text(stringResource(R.string.action_confirm)) } },
            title = { Text(stringResource(R.string.note_color_label)) },
            text = {
                Row {
                    noteColorOptions.forEach { c ->
                        Box(
                            Modifier
                                .padding(6.dp)
                                .size(36.dp)
                                .background(Color(android.graphics.Color.parseColor(c)), CircleShape)
                                .clickable { color = c }
                        )
                    }
                }
            }
        )
    }

    if (showStickerPicker) {
        AlertDialog(
            onDismissRequest = { showStickerPicker = false },
            confirmButton = { TextButton(onClick = { showStickerPicker = false }) { Text(stringResource(R.string.action_confirm)) } },
            title = { Text(stringResource(R.string.note_sticker_label)) },
            text = {
                Row {
                    stickerOptions.forEach { s ->
                        Text(
                            s,
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier
                                .padding(6.dp)
                                .clickable { sticker = if (sticker == s) null else s; showStickerPicker = false }
                        )
                    }
                }
            }
        )
    }

    if (showNotebookPicker) {
        var newNotebookText by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showNotebookPicker = false },
            confirmButton = { TextButton(onClick = { showNotebookPicker = false }) { Text(stringResource(R.string.action_confirm)) } },
            title = { Text(stringResource(R.string.note_notebook_label)) },
            text = {
                Column {
                    (existingNotebooks + DEFAULT_NOTEBOOK).distinct().forEach { nb ->
                        AssistChip(
                            onClick = { notebook = nb; showNotebookPicker = false },
                            label = { Text(nb) },
                            modifier = Modifier.padding(vertical = 3.dp)
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newNotebookText,
                        onValueChange = { newNotebookText = it },
                        label = { Text(stringResource(R.string.note_notebook_new)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    TextButton(onClick = {
                        if (newNotebookText.isNotBlank()) { notebook = newNotebookText.trim(); showNotebookPicker = false }
                    }) { Text(stringResource(R.string.action_confirm)) }
                }
            }
        )
    }

    if (showDrawing) {
        Dialog(onDismissRequest = { showDrawing = false }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            DrawingScreen(
                onCancel = { showDrawing = false },
                onSave = { uri -> attachments = attachments + uri; showDrawing = false }
            )
        }
    }
}

@Composable
private fun ToolbarIcon(icon: @Composable () -> Unit, onClick: () -> Unit) {
    Box(
        Modifier
            .size(40.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) { icon() }
}
