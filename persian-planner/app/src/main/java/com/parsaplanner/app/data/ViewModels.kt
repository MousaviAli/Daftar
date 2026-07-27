package com.parsaplanner.app.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.parsaplanner.app.notifications.ReminderScheduler
import com.parsaplanner.app.widget.TasksWidgetProvider
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TasksViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = AppDatabase.getInstance(app).taskDao()

    val tasks: StateFlow<List<TaskEntity>> = dao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun add(task: TaskEntity) = viewModelScope.launch {
        val id = dao.upsert(task)
        task.reminderEpochMillis?.let { ReminderScheduler.schedule(getApplication(), id, task.title, it) }
        TasksWidgetProvider.requestUpdate(getApplication())
    }

    fun update(task: TaskEntity) = viewModelScope.launch {
        dao.update(task)
        if (task.reminderEpochMillis != null) {
            ReminderScheduler.schedule(getApplication(), task.id, task.title, task.reminderEpochMillis)
        } else {
            ReminderScheduler.cancel(getApplication(), task.id, task.title)
        }
        TasksWidgetProvider.requestUpdate(getApplication())
    }

    fun delete(task: TaskEntity) = viewModelScope.launch {
        dao.delete(task)
        ReminderScheduler.cancel(getApplication(), task.id, task.title)
        TasksWidgetProvider.requestUpdate(getApplication())
    }

    fun toggleDone(task: TaskEntity) = viewModelScope.launch {
        dao.update(task.copy(isDone = !task.isDone))
        TasksWidgetProvider.requestUpdate(getApplication())
    }
}

class NotesViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = AppDatabase.getInstance(app).noteDao()

    val notes: StateFlow<List<NoteEntity>> = dao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun add(note: NoteEntity) = viewModelScope.launch { dao.upsert(note) }
    fun delete(note: NoteEntity) = viewModelScope.launch { dao.delete(note) }
    fun togglePin(note: NoteEntity) = viewModelScope.launch { dao.upsert(note.copy(pinned = !note.pinned)) }
}

class JournalViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = AppDatabase.getInstance(app).journalDao()

    val entries: StateFlow<List<JournalEntity>> = dao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun add(entry: JournalEntity) = viewModelScope.launch { dao.upsert(entry) }
    fun delete(entry: JournalEntity) = viewModelScope.launch { dao.delete(entry) }
}
