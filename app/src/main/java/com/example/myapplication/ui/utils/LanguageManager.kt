package com.example.myapplication.ui.utils

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import java.util.Locale

object LanguageManager {

    private const val PREF_NAME = "language_pref"
    private const val KEY_LANGUAGE = "language"
    private const val DEFAULT_LANG = "vi"

    fun setLanguage(context: Context, lang: String) {
        saveLanguage(context, lang)
    }

    fun getLanguage(context: Context): String {
        return getPrefs(context).getString(KEY_LANGUAGE, DEFAULT_LANG)
            ?: DEFAULT_LANG
    }

    fun applyLanguage(context: Context): Context {
        val lang = getLanguage(context)
        val locale = Locale(lang)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)

        return context.createConfigurationContext(config)
    }

    private fun saveLanguage(context: Context, lang: String) {
        getPrefs(context)
            .edit()
            .putString(KEY_LANGUAGE, lang)
            .apply()
    }

    private fun getPrefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
}
