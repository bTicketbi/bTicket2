package com.bticket.app

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/** Préférences locales (thème clair/sombre). Le mode sombre est le défaut, comme sur le site. */
object AppPrefs {
    private var prefs: SharedPreferences? = null

    var darkTheme by mutableStateOf(true)
        private set

    fun init(context: Context) {
        val p = context.getSharedPreferences("bticket", Context.MODE_PRIVATE)
        prefs = p
        darkTheme = p.getBoolean("dark", true)
    }

    fun setDark(value: Boolean) {
        darkTheme = value
        prefs?.edit()?.putBoolean("dark", value)?.apply()
    }
}
