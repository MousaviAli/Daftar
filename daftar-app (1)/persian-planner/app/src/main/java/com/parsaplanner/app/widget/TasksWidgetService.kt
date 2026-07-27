package com.parsaplanner.app.widget

import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.parsaplanner.app.R
import com.parsaplanner.app.data.AppDatabase
import com.parsaplanner.app.data.Priority
import com.parsaplanner.app.data.TaskEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class TasksWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent) = TasksRemoteViewsFactory(applicationContext)
}

class TasksRemoteViewsFactory(private val context: android.content.Context) :
    RemoteViewsService.RemoteViewsFactory {

    private var tasks: List<TaskEntity> = emptyList()

    override fun onCreate() {}

    override fun onDataSetChanged() {
        // Widgets refresh infrequently, so a small blocking read here is acceptable.
        tasks = runBlocking {
            AppDatabase.getInstance(context).taskDao().getAll().first().filter { !it.isDone }
        }
    }

    override fun onDestroy() {}
    override fun getCount(): Int = tasks.size
    override fun getViewTypeCount(): Int = 1
    override fun getItemId(position: Int): Long = tasks[position].id
    override fun hasStableIds(): Boolean = true
    override fun getLoadingView(): RemoteViews? = null

    override fun getViewAt(position: Int): RemoteViews {
        val task = tasks[position]
        val views = RemoteViews(context.packageName, R.layout.widget_task_item)
        views.setTextViewText(R.id.item_task_title, task.title)
        val dotColor = when (task.priority) {
            Priority.HIGH -> "#C1613A"
            Priority.MEDIUM -> "#D9A441"
            Priority.LOW -> "#6E7F52"
        }
        views.setInt(R.id.item_priority_dot, "setColorFilter", android.graphics.Color.parseColor(dotColor))
        return views
    }
}
