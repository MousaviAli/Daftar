package com.parsaplanner.app

import android.Manifest
import android.content.Context
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
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.parsaplanner.app.ui.screens.JournalScreen
import com.parsaplanner.app.ui.screens.LanguageSelectScreen
import com.parsaplanner.app.ui.screens.NotesScreen
import com.parsaplanner.app.ui.screens.SettingsScreen
import com.parsaplanner.app.ui.screens.TasksScreen
import com.parsaplanner.app.ui.theme.ParsaPlannerTheme
import com.parsaplanner.app.util.LocaleManager

sealed class Destination(val route: String, val labelRes: Int) {
    object Tasks : Destination("tasks", R.string.nav_tasks)
    object Notes : Destination("notes", R.string.nav_notes)
    object Journal : Destination("journal", R.string.nav_journal)
}

class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleManager.wrapContext(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { }
                .launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }
            .launch(Manifest.permission.RECORD_AUDIO)

        setContent {
            ParsaPlannerTheme {
                var languageChosen by remember {
                    mutableStateOf(LocaleManager.getSavedLanguage(this) != null)
                }
                if (!languageChosen) {
                    LanguageSelectScreen(onLanguageChosen = { lang ->
                        LocaleManager.setLanguage(this, lang)
                        languageChosen = true
                        recreate()
                    })
                } else {
                    ParsaPlannerApp(onLanguageChanged = {
                        recreate()
                    })
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParsaPlannerApp(onLanguageChanged: (String) -> Unit) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val items = listOf(Destination.Tasks, Destination.Notes, Destination.Journal)

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val currentTitleRes = items.firstOrNull { it.route == currentRoute }?.labelRes ?: R.string.app_name

    Scaffold(
        topBar = {
            if (currentRoute != "settings") {
                TopAppBar(
                    title = {
                        Text(
                            stringResource(currentTitleRes),
                            fontWeight = FontWeight.Bold
                        )
                    },
                    actions = {
                        IconButton(onClick = { navController.navigate("settings") }) {
                            Icon(Icons.Filled.Settings, contentDescription = stringResource(R.string.settings_title))
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            }
        },
        bottomBar = {
            if (currentRoute != "settings") {
                NavigationBar {
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
            composable("settings") {
                SettingsScreen(
                    onBack = { navController.popBackStack() },
                    onLanguageChanged = { lang ->
                        LocaleManager.setLanguage(context, lang)
                        onLanguageChanged(lang)
                    }
                )
            }
        }
    }
}
