package com.parsaplanner.app.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.parsaplanner.app.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val now = System.currentTimeMillis()
                val tasks = AppDatabase.getInstance(context).taskDao().getAll().first()
                tasks.filter { !it.isDone && it.reminderEpochMillis != null && it.reminderEpochMillis > now }
                    .forEach { ReminderScheduler.schedule(context, it.id, it.title, it.reminderEpochMillis!!) }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
