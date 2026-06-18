package com.example.seapedia.data.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager (context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREF_NAME, Context.MODE_PRIVATE
    )

    companion object {
        private const val PREF_NAME = "sea_catering_session"

        private const val KEY_TOKEN = "token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_FULL_NAME = "full_name"
        private const val KEY_EMAIL = "email"
        private const val KEY_ACTIVE_ROLE = "active_role"
        private const val KEY_ACTIVE_ROLE_ID = "active_role_id"
    }

    fun saveSession(
        token: String,
        userId: Int,
        fullName: String,
        email: String,
        activeRole: String,
        activeRoleId: Int
    ) {
        prefs.edit()
            .putString(KEY_TOKEN, token)
            .putInt(KEY_USER_ID, userId)
            .putString(KEY_FULL_NAME, fullName)
            .putString(KEY_EMAIL, email)
            .putString(KEY_ACTIVE_ROLE, activeRole)
            .putInt(KEY_ACTIVE_ROLE_ID, activeRoleId)
            .apply()
    }

    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    fun getUserId(): Int = prefs.getInt(KEY_USER_ID, -1)

    fun getFullName(): String? = prefs.getString(KEY_FULL_NAME, null)

    fun getEmail(): String? = prefs.getString(KEY_EMAIL, null)

    fun getActiveRole(): String? = prefs.getString(KEY_ACTIVE_ROLE, null)

    fun getActiveRoleId(): Int = prefs.getInt(KEY_ACTIVE_ROLE_ID, -1)

    fun isLoggedIn(): Boolean = getToken() != null

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}