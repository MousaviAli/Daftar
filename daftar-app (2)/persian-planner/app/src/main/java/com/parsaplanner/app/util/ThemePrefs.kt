package com.parsaplanner.app.util

import android.content.Context

object ThemePrefs {
    private const val PREFS = "daftar_prefs"
    private const val KEY_THEME = "theme_mode" // "system" | "light" | "dark"

    fun getMode(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_THEME, "system") ?: "system"

    fun setMode(context: Context, mode: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(KEY_THEME, mode)
            .apply()
    }
}
