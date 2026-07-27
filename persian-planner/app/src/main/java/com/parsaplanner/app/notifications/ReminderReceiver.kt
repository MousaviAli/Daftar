package com.parsaplanner.app.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.parsaplanner.app.R

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra(NotificationHelper.EXTRA_TASK_ID, -1L)
        val title = intent.getStringExtra(NotificationHelper.EXTRA_TASK_TITLE) ?: context.getString(R.string.notif_title)
        if (taskId != -1L) {
            NotificationHelper.showReminder(context, taskId, title)
        }
    }
}
