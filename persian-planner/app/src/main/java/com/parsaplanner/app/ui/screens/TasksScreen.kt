package com.parsaplanner.app.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.parsaplanner.app.R
import com.parsaplanner.app.data.Priority
import com.parsaplanner.app.data.TaskEntity
import com.parsaplanner.app.data.TasksViewModel
import com.parsaplanner.app.ui.components.AttachmentsPreview
import com.parsaplanner.app.ui.components.GradientFab
import com.parsaplanner.app.ui.components.luxuryCardElevation
import com.parsaplanner.app.ui.theme.PriorityHigh
import com.parsaplanner.app.ui.theme.PriorityLow
import com.parsaplanner.app.ui.theme.PriorityMed
import com.parsaplanner.app.util.JalaliCalendar
import com.parsaplanner.app.util.VoiceRecorder
import java.time.LocalDate

@Composable
fun TasksScreen(vm: TasksViewModel = viewModel()) {
    val tasks by vm.tasks.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            GradientFab(icon = Icons.Filled.Add, contentDescription = stringResource(R.string.action_add)) {
                showDialog = true
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            Text(stringResource(R.string.tasks_today, JalaliCalendar.gregorianToJalali(LocalDate.now()).formatted()),
                style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(14.dp))

            if (tasks.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.tasks_empty), style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(tasks, key = { it.id }) { task ->
                        TaskCard(task, onToggle = { vm.toggleDone(task) }, onDelete = { vm.delete(task) })
                    }
                }
            }
        }
    }

    if (showDialog) {
        AddTaskDialog(onDismiss = { showDialog = false }, onConfirm = {
            vm.add(it)
            showDialog = false
        })
    }
}

@Composable
private fun TaskCard(task: TaskEntity, onToggle: () -> Unit, onDelete: () -> Unit) {
    val priorityColor = when (task.priority) {
        Priority.HIGH -> PriorityHigh
        Priority.MEDIUM -> PriorityMed
        Priority.LOW -> PriorityLow
    }
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = luxuryCardElevation()
    ) {
        Row(
            Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier.size(10.dp)
                    .background(priorityColor, shape = RoundedCornerShape(50))
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    task.title,
                    style = MaterialTheme.typography.titleMedium,
                    textDecoration = if (task.isDone) TextDecoration.LineThrough else TextDecoration.None
                )
                if (task.description.isNotBlank()) {
                    Text(task.description, style = MaterialTheme.typography.bodyMedium)
                }
                Row {
                    Text(task.category, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
                }
                if (task.attachmentUris.isNotBlank() || task.voiceNoteUri != null) {
                    Spacer(Modifier.height(4.dp))
                    AttachmentsPreview(task.attachmentUris, task.voiceNoteUri)
                }
            }
            Checkbox(checked = task.isDone, onCheckedChange = { onToggle() })
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.action_delete))
            }
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun AddTaskDialog(onDismiss: () -> Unit, onConfirm: (TaskEntity) -> Unit) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(Priority.MEDIUM) }
    var reminderEnabled by remember { mutableStateOf(false) }
    var reminderDayOffset by remember { mutableStateOf(0) } // 0 = today, 1 = tomorrow
    var showTimePicker by remember { mutableStateOf(false) }
    val timePickerState = rememberTimePickerState(is24Hour = true)
    var pickedHour by remember { mutableStateOf<Int?>(null) }
    var pickedMinute by remember { mutableStateOf<Int?>(null) }
    val generalCategory = stringResource(R.string.category_general)

    var attachments by remember { mutableStateOf(listOf<Uri>()) }
    var voiceNoteUri by remember { mutableStateOf<Uri?>(null) }
    var isRecording by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val recorder = remember { VoiceRecorder(context) }

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
        title = { Text(stringResource(R.string.task_dialog_title)) },
        text = {
            Column {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text(stringResource(R.string.label_title)) }, singleLine = true)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text(stringResource(R.string.label_description)) })
                Spacer(Modifier.height(8.dp))
                Row {
                    Priority.values().forEach { p ->
                        FilterChip(
                            selected = priority == p,
                            onClick = { priority = p },
                            label = { Text(
                                when (p) {
                                    Priority.HIGH -> stringResource(R.string.priority_high)
                                    Priority.MEDIUM -> stringResource(R.string.priority_medium)
                                    Priority.LOW -> stringResource(R.string.priority_low)
                                }
                            ) },
                            modifier = Modifier.padding(end = 6.dp)
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = reminderEnabled, onCheckedChange = { reminderEnabled = it })
                    Text(stringResource(R.string.reminder_checkbox_label))
                }
                if (reminderEnabled) {
                    Row {
                        FilterChip(selected = reminderDayOffset == 0, onClick = { reminderDayOffset = 0 }, label = { Text(stringResource(R.string.reminder_today)) }, modifier = Modifier.padding(end = 6.dp))
                        FilterChip(selected = reminderDayOffset == 1, onClick = { reminderDayOffset = 1 }, label = { Text(stringResource(R.string.reminder_tomorrow)) })
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(stringResource(R.string.reminder_pick_time), style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.height(4.dp))
                    val presets = listOf(
                        Triple(9, 0, stringResource(R.string.reminder_preset_morning)),
                        Triple(13, 0, stringResource(R.string.reminder_preset_noon)),
                        Triple(18, 0, stringResource(R.string.reminder_preset_evening)),
                        Triple(21, 0, stringResource(R.string.reminder_preset_night))
                    )
                    androidx.compose.foundation.layout.FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        presets.forEach { (h, m, label) ->
                            FilterChip(
                                selected = pickedHour == h && pickedMinute == m,
                                onClick = { pickedHour = h; pickedMinute = m },
                                label = { Text(label) }
                            )
                        }
                        FilterChip(
                            selected = showTimePicker || (pickedHour != null && presets.none { it.first == pickedHour && it.second == pickedMinute }),
                            onClick = { showTimePicker = true },
                            label = {
                                Text(
                                    if (pickedHour != null && presets.none { it.first == pickedHour && it.second == pickedMinute })
                                        stringResource(R.string.reminder_time_format, pickedHour!!, pickedMinute ?: 0)
                                    else stringResource(R.string.reminder_preset_custom)
                                )
                            }
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
                if (title.isNotBlank()) {
                    val reminderMillis = if (reminderEnabled && pickedHour != null) {
                        val date = LocalDate.now().plusDays(reminderDayOffset.toLong())
                        date.atTime(pickedHour!!, pickedMinute ?: 0)
                            .atZone(java.time.ZoneId.systemDefault())
                            .toInstant().toEpochMilli()
                    } else null
                    onConfirm(
                        TaskEntity(
                            title = title,
                            description = description,
                            dueDateEpochDay = LocalDate.now().plusDays(reminderDayOffset.toLong()).toEpochDay(),
                            reminderEpochMillis = reminderMillis,
                            priority = priority,
                            category = generalCategory,
                            attachmentUris = attachments.joinToString(",") { it.toString() },
                            voiceNoteUri = voiceNoteUri?.toString(),
                            createdAtEpochMillis = System.currentTimeMillis()
                        )
                    )
                }
            }) { Text(stringResource(R.string.action_add)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } }
    )

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text(stringResource(R.string.time_picker_title)) },
            text = { TimePicker(state = timePickerState) },
            confirmButton = {
                TextButton(onClick = {
                    pickedHour = timePickerState.hour
                    pickedMinute = timePickerState.minute
                    showTimePicker = false
                }) { Text(stringResource(R.string.action_confirm)) }
            },
            dismissButton = { TextButton(onClick = { showTimePicker = false }) { Text(stringResource(R.string.action_cancel)) } }
        )
    }
}
