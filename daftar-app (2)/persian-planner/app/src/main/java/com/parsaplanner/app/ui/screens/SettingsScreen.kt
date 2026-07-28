package com.parsaplanner.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.parsaplanner.app.R
import com.parsaplanner.app.util.LocaleManager
import com.parsaplanner.app.util.ThemePrefs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit, onLanguageChanged: (String) -> Unit, onThemeChanged: (String) -> Unit) {
    val context = LocalContext.current
    var currentLang by remember { mutableStateOf(LocaleManager.getSavedLanguage(context) ?: "fa") }
    var currentTheme by remember { mutableStateOf(ThemePrefs.getMode(context)) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).padding(20.dp)) {
            Text(stringResource(R.string.settings_language), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
            Spacer(Modifier.height(10.dp))

            listOf("fa" to stringResource(R.string.lang_persian), "en" to stringResource(R.string.lang_english)).forEach { (code, label) ->
                SettingsOptionCard(
                    label = label,
                    selected = currentLang == code,
                    onClick = {
                        currentLang = code
                        onLanguageChanged(code)
                    }
                )
            }

            Spacer(Modifier.height(28.dp))

            Text(stringResource(R.string.settings_theme), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
            Spacer(Modifier.height(10.dp))

            listOf(
                "system" to stringResource(R.string.theme_system),
                "light" to stringResource(R.string.theme_light),
                "dark" to stringResource(R.string.theme_dark)
            ).forEach { (mode, label) ->
                SettingsOptionCard(
                    label = label,
                    selected = currentTheme == mode,
                    onClick = {
                        currentTheme = mode
                        onThemeChanged(mode)
                    }
                )
            }
        }
    }
}

@Composable
private fun SettingsOptionCard(label: String, selected: Boolean, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
            else MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (selected) 2.dp else 0.dp),
        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp)
    ) {
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 14.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            RadioButton(selected = selected, onClick = onClick)
        }
    }
}
