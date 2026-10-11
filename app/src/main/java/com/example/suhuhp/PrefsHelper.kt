package com.example.suhuhp

import android.content.Context

class PrefsHelper(context: Context) {
    private val prefs = context.getSharedPreferences("suhuhp_data", Context.MODE_PRIVATE)

    /**
     * Model widget yang dipilih user.
     * Nilai: "kotak", "bulat", "minimal", "digital"
     */
    var modelWidget: String
        get() = prefs.getString("model_widget", "kotak") ?: "kotak"
        set(value) = prefs.edit().putString("model_widget", value).apply()
}
