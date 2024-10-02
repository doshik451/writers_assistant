package com.example.writersassistant.utils

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import java.util.Locale

object LoadSettings {
    private const val LANGUAGE_KEY = "My_Lang"
    private const val THEME_KEY = "My_Theme"
    private const val PREFS_NAME = "Settings"

    fun setLocale(context: Context, languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration()
        config.setLocale(locale)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
        saveLanguage(context, languageCode)
    }

    fun applyLocale(context: Context) {
        val language = loadLanguage(context)
        if (language.isNotEmpty()) setLocale(context, language)
    }

    private fun saveLanguage(context: Context, languageCode: String) {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(LANGUAGE_KEY, languageCode).apply()
    }

    private fun loadLanguage(context: Context): String {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(LANGUAGE_KEY, Locale.getDefault().language) ?: Locale.getDefault().language
    }

    fun setTheme(context: Context, isNightMode: Boolean) {
        if (isNightMode) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        else AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        saveTheme(context, isNightMode)
    }

    fun applyTheme(context: Context) {
        val isNightMode = loadTheme(context)
        setTheme(context, isNightMode)
    }

    private fun saveTheme(context: Context, isNightMode: Boolean) {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(THEME_KEY, isNightMode).apply()
    }

    fun loadTheme(context: Context): Boolean {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(THEME_KEY, false)
    }

}