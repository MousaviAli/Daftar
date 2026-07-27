package com.parsaplanner.app.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.parsaplanner.app.R
import com.parsaplanner.app.data.JournalEntity
import com.parsaplanner.app.data.JournalViewModel
import com.parsaplanner.app.ui.components.GradientFab
import com.parsaplanner.app.ui.components.luxuryCardElevation
import com.parsaplanner.app.util.JalaliCalendar
import com.parsaplanner.app.util.VoiceRecorder
import java.time.LocalDate

private val moodResIds = listOf(
    R.string.mood_happy, R.string.mood_calm, R.string.mood_tired,
    R.string.mood_energetic, R.string.mood_sad, R.string.mood_busy
)

@Composable
fun JournalScreen(vm: JournalViewModel = viewModel()) {
    val entries by vm.entries.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            GradientFab(icon = Icons.Filled.Add, contentDescription = stringResource(R.string.action_add)) {
                showDialog = true
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            if (entries.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.journal_empty))
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(entries, key = { it.id }) { entry -> JournalCard(entry, onDelete = { vm.delete(entry) }) }
                }
            }
        }
    }

    if (showDialog) {
        AddJournalDialog(onDismiss = { showDialog = false }, onConfirm = {
            vm.add(it)
            showDialog = false
        })
    }
}

@Composable
private fun JournalCard(entry: JournalEntity, onDelete: () -> Unit) {
    val jalaliDate = JalaliCalendar.gregorianToJalali(LocalDate.ofEpochDay(entry.entryDateEpochDay))
    Card(shape = RoundedCornerShape(16.dp), elevation = luxuryCardElevation()) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(jalaliDate.formatted(), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
                Spacer(Modifier.weight(1f))
                Text(entry.mood, style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(Modifier.height(6.dp))
            Text(entry.title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(entry.content, style = MaterialTheme.typography.bodyMedium, maxLines = 6)
            val attachmentCount = entry.attachmentUris.split(",").count { it.isNotBlank() }
            if (attachmentCount > 0 || entry.voiceNoteUri != null) {
                Spacer(Modifier.height(4.dp))
                Row {
                    if (attachmentCount > 0) {
                        Text(stringResource(R.string.attach_count_short, attachmentCount), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
                        Spacer(Modifier.width(8.dp))
                    }
                    if (entry.voiceNoteUri != null) {
                        Text(stringResource(R.string.voice_tag), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
            TextButton(onClick = onDelete) { Text(stringResource(R.string.action_delete)) }
        }
    }
}

@Composable
private fun AddJournalDialog(onDismiss: () -> Unit, onConfirm: (JournalEntity) -> Unit) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var mood by remember { mutableStateOf<String?>(null) }
    var attachments by remember { mutableStateOf(listOf<Uri>()) }
    var voiceNoteUri by remember { mutableStateOf<Uri?>(null) }
    var isRecording by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val recorder = remember { VoiceRecorder(context) }
    val defaultMood = stringResource(moodResIds.first())
    val noTitle = stringResource(R.string.journal_no_title)

    val photoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        attachments = attachments + uris
    }
    val fileLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        uris.forEach { uri ->
            try {
                context.contentResolver.takePersistableUriPermission(uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (_: Exception) { }
        }
        attachments = attachments + uris
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.journal_dialog_title)) },
        text = {
            Column {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text(stringResource(R.string.label_title)) }, singleLine = true)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = content, onValueChange = { content = it }, label = { Text(stringResource(R.string.journal_content_hint)) }, minLines = 4)
                Spacer(Modifier.height(10.dp))
                Text(stringResource(R.string.mood_label), style = MaterialTheme.typography.labelLarge)
                Column {
                    moodResIds.forEach { resId ->
                        val label = stringResource(resId)
                        val selected = (mood ?: defaultMood) == label
                        FilterChip(
                            selected = selected,
                            onClick = { mood = label },
                            label = { Text(label) },
                            modifier = Modifier.padding(vertical = 2.dp)
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
                    }) {
                        Text(stringResource(if (isRecording) R.string.record_stop else R.string.record_start))
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
                if (title.isNotBlank() || content.isNotBlank()) {
                    onConfirm(
                        JournalEntity(
                            entryDateEpochDay = LocalDate.now().toEpochDay(),
                            title = title.ifBlank { noTitle },
                            content = content,
                            mood = mood ?: defaultMood,
                            attachmentUris = attachments.joinToString(",") { it.toString() },
                            voiceNoteUri = voiceNoteUri?.toString(),
                            createdAtEpochMillis = System.currentTimeMillis()
                        )
                    )
                }
            }) { Text(stringResource(R.string.action_save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } }
    )
}
