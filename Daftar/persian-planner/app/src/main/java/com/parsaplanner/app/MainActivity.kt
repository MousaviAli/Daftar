package com.parsaplanner.app

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.parsaplanner.app.ui.screens.JournalScreen
import com.parsaplanner.app.ui.screens.NotesScreen
import com.parsaplanner.app.ui.screens.TasksScreen
import com.parsaplanner.app.ui.theme.ParsaPlannerTheme

sealed class Destination(val route: String, val labelRes: Int) {
    object Tasks : Destination("tasks", R.string.nav_tasks)
    object Notes : Destination("notes", R.string.nav_notes)
    object Journal : Destination("journal", R.string.nav_journal)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val requestPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { }
            requestPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent {
            ParsaPlannerTheme {
                ParsaPlannerApp()
            }
        }
    }
}

@Composable
fun ParsaPlannerApp() {
    val navController = rememberNavController()
    val items = listOf(Destination.Tasks, Destination.Notes, Destination.Journal)

    Scaffold(
        bottomBar = {
            NavigationBar {
                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = backStackEntry?.destination?.route
                items.forEach { dest ->
                    NavigationBarItem(
                        selected = currentRoute == dest.route,
                        onClick = {
                            navController.navigate(dest.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            val icon = when (dest) {
                                Destination.Tasks -> Icons.Filled.CheckCircle
                                Destination.Notes -> Icons.Filled.EditNote
                                Destination.Journal -> Icons.Filled.AutoStories
                            }
                            Icon(icon, contentDescription = stringResource(dest.labelRes))
                        },
                        label = { Text(stringResource(dest.labelRes)) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Destination.Tasks.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Destination.Tasks.route) { TasksScreen() }
            composable(Destination.Notes.route) { NotesScreen() }
            composable(Destination.Journal.route) { JournalScreen() }
        }
    }
}
