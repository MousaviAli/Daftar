package com.parsaplanner.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class Priority { LOW, MEDIUM, HIGH }

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val dueDateEpochDay: Long?,        // stored as Gregorian epoch day; converted to Jalali for display
    val reminderEpochMillis: Long? = null,  // exact moment to fire the reminder notification, if set
    val isDone: Boolean = false,
    val priority: Priority = Priority.MEDIUM,
    val category: String = "General",
    val attachmentUris: String = "",
    val voiceNoteUri: String? = null,
    val createdAtEpochMillis: Long
)

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val colorHex: String = "#FFFDF8",
    val stickerId: String? = null,     // reference to a decorative sticker asset
    val pinned: Boolean = false,
    val notebook: String = "عمومی",    // OneNote-style section/notebook grouping
    val attachmentUris: String = "",   // comma-separated content:// URIs (images, documents)
    val voiceNoteUri: String? = null,  // recorded voice memo, if any
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long
)

@Entity(tableName = "journal_entries")
data class JournalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val entryDateEpochDay: Long,       // the day the entry is about
    val title: String,
    val content: String,
    val mood: String,                  // e.g. "خوشحال", "خسته", "آرام" — free-form emoji/mood tag
    val stickerId: String? = null,
    val attachmentUris: String = "",   // comma-separated content:// URIs (images, documents)
    val voiceNoteUri: String? = null,  // recorded voice memo, if any
    val createdAtEpochMillis: Long
)
