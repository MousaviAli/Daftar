package com.parsaplanner.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.parsaplanner.app.R
import com.parsaplanner.app.data.Priority
import com.parsaplanner.app.data.TaskEntity
import com.parsaplanner.app.data.TasksViewModel
import com.parsaplanner.app.ui.components.luxuryCardElevation
import com.parsaplanner.app.ui.theme.PriorityHigh
import com.parsaplanner.app.ui.theme.PriorityLow
import com.parsaplanner.app.ui.theme.PriorityMed
import com.parsaplanner.app.util.JalaliCalendar
import java.time.LocalDate

@Composable
fun TimelineScreen(vm: TasksViewModel = viewModel()) {
    val tasks by vm.tasks.collectAsState()

    // Group by due date, newest first; each group renders as a point on the vertical timeline.
    val grouped = remember(tasks) {
        tasks.filter { it.dueDateEpochDay != null }
            .sortedByDescending { it.dueDateEpochDay }
            .groupBy { it.dueDateEpochDay!! }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text(stringResource(R.string.nav_timeline), style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(14.dp))

        if (grouped.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.timeline_empty))
            }
        } else {
            LazyColumn {
                grouped.forEach { (epochDay, tasksForDay) ->
                    item {
                        TimelineDateGroup(epochDay, tasksForDay, isLast = epochDay == grouped.keys.last())
                    }
                }
            }
        }
    }
}

@Composable
private fun TimelineDateGroup(epochDay: Long, tasks: List<TaskEntity>, isLast: Boolean) {
    val date = LocalDate.ofEpochDay(epochDay)
    val jalali = JalaliCalendar.gregorianToJalali(date)
    val isPast = date.isBefore(LocalDate.now())
    val isToday = date.isEqual(LocalDate.now())

    Row(Modifier.fillMaxWidth()) {
        // The vertical line + dot
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(28.dp)) {
            Box(
                Modifier
                    .padding(top = 6.dp)
                    .size(if (isToday) 14.dp else 10.dp)
                    .background(
                        color = if (isToday) MaterialTheme.colorScheme.primary
                        else if (isPast) MaterialTheme.colorScheme.secondary
                        else MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(50)
                    )
            )
            if (!isLast) {
                Box(
                    Modifier
                        .width(2.dp)
                        .weight(1f)
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                )
            }
        }

        Spacer(Modifier.width(10.dp))

        Column(Modifier.padding(bottom = 20.dp)) {
            Text(
                jalali.formatted(),
                style = MaterialTheme.typography.titleMedium,
                color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(8.dp))
            tasks.forEach { task ->
                TimelineTaskRow(task)
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun TimelineTaskRow(task: TaskEntity) {
    val priorityColor = when (task.priority) {
        Priority.HIGH -> PriorityHigh
        Priority.MEDIUM -> PriorityMed
        Priority.LOW -> PriorityLow
    }
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)),
        elevation = luxuryCardElevation()
    ) {
        Row(
            Modifier.padding(horizontal = 12.dp, vertical = 10.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(8.dp).background(priorityColor, RoundedCornerShape(50)))
            Spacer(Modifier.width(8.dp))
            Text(
                task.title,
                style = MaterialTheme.typography.bodyMedium,
                textDecoration = if (task.isDone) TextDecoration.LineThrough else TextDecoration.None,
                modifier = Modifier.weight(1f)
            )
            if (task.isDone) {
                Text("✓", color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
