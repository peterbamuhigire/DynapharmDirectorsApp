package com.dynapharm.owner.data.local.prefs

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages JWT access and refresh tokens using EncryptedSharedPreferences.
 * All tokens are stored encrypted on disk for security.
 *
 * Usage:
 * ```
 * @Inject lateinit var tokenManager: TokenManager
 *
 * // After successful login
 * tokenManager.saveTokens(accessToken, refreshToken)
 *
 * // Get access token for API calls
 * val token = tokenManager.getAccessToken()
 *
 * // On logout
 * tokenManager.clearTokens()
 * ```
 */
@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private companion object {
        const val PREFS_FILE_NAME = "encrypted_prefs"
        const val KEY_ACCESS_TOKEN = "access_token"
        const val KEY_REFRESH_TOKEN = "refresh_token"
    }

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val encryptedPrefs = EncryptedSharedPreferences.create(
        context,
        PREFS_FILE_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    /**
     * Saves both access and refresh tokens.
     * @param accessToken The JWT access token
     * @param refreshToken The JWT refresh token
     */
    fun saveTokens(accessToken: String, refreshToken: String) {
        encryptedPrefs.edit().apply {
            putString(KEY_ACCESS_TOKEN, accessToken)
            putString(KEY_REFRESH_TOKEN, refreshToken)
            apply()
        }
    }

    /**
     * Retrieves the stored access token.
     * @return The access token, or null if not found
     */
    fun getAccessToken(): String? {
        return encryptedPrefs.getString(KEY_ACCESS_TOKEN, null)
    }

    /**
     * Retrieves the stored refresh token.
     * @return The refresh token, or null if not found
     */
    fun getRefreshToken(): String? {
        return encryptedPrefs.getString(KEY_REFRESH_TOKEN, null)
    }

    /**
     * Saves only the access token. Used after token refresh.
     * @param accessToken The new JWT access token
     */
    fun saveAccessToken(accessToken: String) {
        encryptedPrefs.edit().apply {
            putString(KEY_ACCESS_TOKEN, accessToken)
            apply()
        }
    }

    /**
     * Clears all stored tokens. Should be called on logout.
     */
    fun clearTokens() {
        encryptedPrefs.edit().apply {
            remove(KEY_ACCESS_TOKEN)
            remove(KEY_REFRESH_TOKEN)
            apply()
        }
    }

    /**
     * Checks if the user has a valid access token stored.
     * Note: This only checks if a token exists, not if it's valid or expired.
     * @return true if access token exists, false otherwise
     */
    fun hasAccessToken(): Boolean {
        return getAccessToken() != null
    }

    /**
     * Checks if both access and refresh tokens are present.
     * @return true if both tokens exist, false otherwise
     */
    fun hasValidSession(): Boolean {
        return getAccessToken() != null && getRefreshToken() != null
    }
}
