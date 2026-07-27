package com.parsaplanner.app.util

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object LocaleManager {
    private const val PREFS = "daftar_prefs"
    private const val KEY_LANG = "app_language"

    fun getSavedLanguage(context: Context): String? =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_LANG, null)

    fun setLanguage(context: Context, languageCode: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LANG, languageCode)
            .apply()
    }

    /** Wraps a Context so all string resources resolve in the saved language, if one was chosen. */
    fun wrapContext(context: Context): Context {
        val lang = getSavedLanguage(context) ?: return context
        val locale = Locale(lang)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        return context.createConfigurationContext(config)
    }
}
