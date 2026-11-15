package com.example.app_signia

import android.content.Context
import android.content.SharedPreferences

object ProfileManager {
    private const val PREF_NAME = "signia_profile_prefs"
    private const val KEY_FULL_NAME = "full_name"
    private const val KEY_USERNAME = "username"
    private const val KEY_LOCATION = "location"
    private const val KEY_ROLE_INDEX = "role_index"
    private const val KEY_CAMERA_PRECISION = "camera_precision"
    private const val KEY_HIGH_CONTRAST = "high_contrast"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveProfile(context: Context, fullName: String, username: String, location: String, roleIndex: Int) {
        getPrefs(context).edit()
            .putString(KEY_FULL_NAME, fullName)
            .putString(KEY_USERNAME, username)
            .putString(KEY_LOCATION, location)
            .putInt(KEY_ROLE_INDEX, roleIndex)
            .apply()
    }

    fun saveAccessibility(context: Context, cameraPrecision: Boolean, highContrast: Boolean) {
        getPrefs(context).edit()
            .putBoolean(KEY_CAMERA_PRECISION, cameraPrecision)
            .putBoolean(KEY_HIGH_CONTRAST, highContrast)
            .apply()
    }

    fun getFullName(context: Context, defaultVal: String = "Valeria Mendoza"): String {
        return getPrefs(context).getString(KEY_FULL_NAME, defaultVal) ?: defaultVal
    }

    fun getUsername(context: Context, defaultVal: String = "@valeria_kp"): String {
        return getPrefs(context).getString(KEY_USERNAME, defaultVal) ?: defaultVal
    }

    fun getLocation(context: Context, defaultVal: String = "Lima, Perú"): String {
        return getPrefs(context).getString(KEY_LOCATION, defaultVal) ?: defaultVal
    }

    fun getRoleIndex(context: Context, defaultVal: Int = 0): Int {
        return getPrefs(context).getInt(KEY_ROLE_INDEX, defaultVal)
    }

    fun getCameraPrecision(context: Context, defaultVal: Boolean = true): Boolean {
        return getPrefs(context).getBoolean(KEY_CAMERA_PRECISION, defaultVal)
    }

    fun getHighContrast(context: Context, defaultVal: Boolean = false): Boolean {
        return getPrefs(context).getBoolean(KEY_HIGH_CONTRAST, defaultVal)
    }
}
