package com.parsaplanner.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.parsaplanner.app.R
import com.parsaplanner.app.data.NoteEntity
import com.parsaplanner.app.data.NotesViewModel
import com.parsaplanner.app.ui.components.AttachmentsPreview
import com.parsaplanner.app.ui.components.GradientFab

private const val ALL_NOTEBOOKS = "همه"

/**
 * Evernote-style clean list (search-first, no colored sticky-note cards) combined with
 * OneNote-style horizontal section/notebook tabs for grouping.
 */
@Composable
fun NotesScreen(vm: NotesViewModel = viewModel()) {
    val notes by vm.notes.collectAsState()
    var editingNote by remember { mutableStateOf<NoteEntity?>(null) }
    var showEditor by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedNotebook by remember { mutableStateOf(ALL_NOTEBOOKS) }

    val notebooks = remember(notes) { listOf(ALL_NOTEBOOKS) + notes.map { it.notebook }.distinct().sorted() }
    val filtered = remember(notes, searchQuery, selectedNotebook) {
        notes.filter { note ->
            (selectedNotebook == ALL_NOTEBOOKS || note.notebook == selectedNotebook) &&
                (searchQuery.isBlank() || note.title.contains(searchQuery, true) || note.content.contains(searchQuery, true))
        }.sortedWith(compareByDescending<NoteEntity> { it.pinned }.thenByDescending { it.updatedAtEpochMillis })
    }

    Scaffold(
        floatingActionButton = {
            GradientFab(icon = Icons.Filled.Add, contentDescription = stringResource(R.string.action_add)) {
                editingNote = null
                showEditor = true
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(stringResource(R.string.notes_search_hint)) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Notebook / section tabs
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                items(notebooks) { nb ->
                    FilterChip(
                        selected = selectedNotebook == nb,
                        onClick = { selectedNotebook = nb },
                        label = { Text(nb) }
                    )
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

            if (filtered.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.notes_empty))
                }
            } else {
                LazyColumn {
                    items(filtered, key = { it.id }) { note ->
                        NoteListRow(note, onPin = { vm.togglePin(note) }, onOpen = { editingNote = note; showEditor = true })
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                    }
                }
            }
        }
    }

    if (showEditor) {
        Dialog(onDismissRequest = { showEditor = false }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            NoteEditorScreen(
                existing = editingNote,
                existingNotebooks = notebooks.filter { it != ALL_NOTEBOOKS },
                onDismiss = { showEditor = false },
                onSave = { note -> vm.add(note); showEditor = false },
                onDelete = editingNote?.let { note -> { vm.delete(note); showEditor = false } }
            )
        }
    }
}

/** A clean, single-color list row — Evernote's note list style, not a colored sticky card. */
@Composable
private fun NoteListRow(note: NoteEntity, onPin: () -> Unit, onOpen: () -> Unit) {
    val accentColor = Color(android.graphics.Color.parseColor(note.colorHex))
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            Modifier
                .padding(top = 4.dp)
                .size(8.dp)
                .background(accentColor, CircleShape)
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (note.stickerId != null) {
                    Text(note.stickerId, style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.width(4.dp))
                }
                Text(
                    note.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (note.pinned) {
                    Icon(Icons.Filled.PushPin, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                }
            }
            if (note.content.isNotBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    note.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(note.notebook, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
            if (note.attachmentUris.isNotBlank() || note.voiceNoteUri != null) {
                Spacer(Modifier.height(4.dp))
                AttachmentsPreview(note.attachmentUris, note.voiceNoteUri)
            }
        }
    }
}
