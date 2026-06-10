package com.example.shakeflashlight

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

/*
Design comment:
- Problem solved: app language must be user-selectable and stable across app restarts.
- Main idea: keep a single persisted language tag and drive AppCompat application locales from it.
- Alternatives rejected:
  1) Activity-only transient locale changes: setting is lost after process recreation.
  2) Manual ContextWrapper-per-screen approach: more boilerplate than needed.
*/
object AppLanguageSettings {
    private const val PREFS_NAME = "shake_flashlight_prefs"
    private const val KEY_APP_LANGUAGE_TAG = "app_language_tag"

    val supportedLanguageTags = listOf("it", "en", "fr", "es", "pt", "de")

    fun normalizeSupportedTag(rawTag: String?): String {
        val language = rawTag
            ?.trim()
            ?.lowercase(Locale.ROOT)
            ?.substringBefore('-')
            ?.substringBefore('_')
            ?: "it"
        return if (supportedLanguageTags.contains(language)) language else "it"
    }

    fun saveLanguageTag(context: Context, languageTag: String) {
        val safeTag = normalizeSupportedTag(languageTag)
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_APP_LANGUAGE_TAG, safeTag).apply()
    }

    fun readLanguageTag(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val stored = prefs.getString(KEY_APP_LANGUAGE_TAG, null) ?: return null
        return normalizeSupportedTag(stored)
    }

    fun applyStoredLanguageIfNeeded(context: Context) {
        val stored = readLanguageTag(context) ?: return
        val currentTag = AppCompatDelegate.getApplicationLocales()[0]?.language
        val normalizedCurrent = normalizeSupportedTag(currentTag)
        if (normalizedCurrent == stored) return
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(stored))
    }

    fun resolveActiveLanguageTag(context: Context): String {
        val explicitTag = AppCompatDelegate.getApplicationLocales()[0]?.language
        if (!explicitTag.isNullOrBlank()) {
            return normalizeSupportedTag(explicitTag)
        }

        val storedTag = readLanguageTag(context)
        if (!storedTag.isNullOrBlank()) return normalizeSupportedTag(storedTag)

        val systemTag = context.resources.configuration.locales[0]?.language
        return normalizeSupportedTag(systemTag)
    }
}
