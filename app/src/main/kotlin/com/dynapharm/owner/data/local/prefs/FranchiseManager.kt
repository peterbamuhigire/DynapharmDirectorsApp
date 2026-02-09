package com.dynapharm.owner.data.local.prefs

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.dynapharm.owner.domain.model.Franchise
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages active franchise selection and franchise list using EncryptedSharedPreferences.
 * Provides reactive StateFlow for franchise changes to trigger UI updates.
 *
 * Security: Same encryption as TokenManager (AES256-GCM).
 *
 * Usage:
 * ```
 * @Inject lateinit var franchiseManager: FranchiseManager
 *
 * // After successful login
 * franchiseManager.saveAllFranchises(franchises)
 * franchiseManager.setActiveFranchise(selectedFranchise)
 *
 * // Observe franchise changes in ViewModel
 * franchiseManager.activeFranchise.collectLatest { franchise ->
 *     // Reload data for new franchise
 * }
 *
 * // Get franchise ID for API header
 * val franchiseId = franchiseManager.getActiveFranchiseIdString()
 *
 * // On logout
 * franchiseManager.clearAll()
 * ```
 */
@Singleton
class FranchiseManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val json: Json
) {
    private companion object {
        const val PREFS_FILE_NAME = "franchise_encrypted_prefs"
        const val KEY_ACTIVE_FRANCHISE_ID = "active_franchise_id"
        const val KEY_ACTIVE_FRANCHISE_NAME = "active_franchise_name"
        const val KEY_ACTIVE_FRANCHISE_BRANCH_COUNT = "active_franchise_branch_count"
        const val KEY_ALL_FRANCHISES = "all_franchises"
    }

    @Serializable
    private data class FranchiseCache(
        val id: Int,
        val name: String,
        val branchCount: Int
    )

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

    private val _activeFranchise = MutableStateFlow<Franchise?>(null)
    val activeFranchise: StateFlow<Franchise?> = _activeFranchise.asStateFlow()

    init {
        loadActiveFranchise()
    }

    /**
     * Loads active franchise from storage on init.
     * Emits to StateFlow if found.
     */
    private fun loadActiveFranchise() {
        val id = encryptedPrefs.getInt(KEY_ACTIVE_FRANCHISE_ID, -1)
        if (id != -1) {
            val name = encryptedPrefs.getString(KEY_ACTIVE_FRANCHISE_NAME, null)
            val branchCount = encryptedPrefs.getInt(KEY_ACTIVE_FRANCHISE_BRANCH_COUNT, 0)
            if (name != null) {
                _activeFranchise.value = Franchise(
                    id = id,
                    name = name,
                    branchCount = branchCount
                )
            }
        }
    }

    /**
     * Sets the active franchise and triggers reactive state update.
     * This will cause ViewModels observing activeFranchise to reload data.
     *
     * @param franchise The franchise to set as active
     */
    fun setActiveFranchise(franchise: Franchise) {
        encryptedPrefs.edit().apply {
            putInt(KEY_ACTIVE_FRANCHISE_ID, franchise.id)
            putString(KEY_ACTIVE_FRANCHISE_NAME, franchise.name)
            putInt(KEY_ACTIVE_FRANCHISE_BRANCH_COUNT, franchise.branchCount)
            apply()
        }

        // Emit state change (triggers ViewModel reload via StateFlow)
        _activeFranchise.value = franchise
    }

    /**
     * Gets the active franchise ID as an Int.
     * @return The active franchise ID, or null if not set
     */
    fun getActiveFranchiseId(): Int? {
        val id = encryptedPrefs.getInt(KEY_ACTIVE_FRANCHISE_ID, -1)
        return if (id != -1) id else null
    }

    /**
     * Gets the active franchise ID as a String for API header injection.
     * @return The active franchise ID as String, or null if not set
     */
    fun getActiveFranchiseIdString(): String? {
        return getActiveFranchiseId()?.toString()
    }

    /**
     * Saves all franchises to storage for offline switching.
     * Allows users to switch franchises without re-fetching from API.
     *
     * @param franchises List of franchises to cache
     */
    fun saveAllFranchises(franchises: List<Franchise>) {
        val cacheList = franchises.map {
            FranchiseCache(
                id = it.id,
                name = it.name,
                branchCount = it.branchCount
            )
        }
        val jsonString = json.encodeToString(cacheList)
        encryptedPrefs.edit().apply {
            putString(KEY_ALL_FRANCHISES, jsonString)
            apply()
        }
    }

    /**
     * Retrieves all cached franchises.
     * @return List of franchises, empty if none cached
     */
    fun getAllFranchises(): List<Franchise> {
        val jsonString = encryptedPrefs.getString(KEY_ALL_FRANCHISES, null) ?: return emptyList()
        return try {
            val cacheList = json.decodeFromString<List<FranchiseCache>>(jsonString)
            cacheList.map {
                Franchise(
                    id = it.id,
                    name = it.name,
                    branchCount = it.branchCount
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Clears the active franchise selection.
     * Does NOT clear the cached franchise list.
     */
    fun clearActiveFranchise() {
        encryptedPrefs.edit().apply {
            remove(KEY_ACTIVE_FRANCHISE_ID)
            remove(KEY_ACTIVE_FRANCHISE_NAME)
            remove(KEY_ACTIVE_FRANCHISE_BRANCH_COUNT)
            apply()
        }
        _activeFranchise.value = null
    }

    /**
     * Clears all franchise data (active + cached list).
     * Should be called on logout.
     */
    fun clearAll() {
        encryptedPrefs.edit().apply {
            remove(KEY_ACTIVE_FRANCHISE_ID)
            remove(KEY_ACTIVE_FRANCHISE_NAME)
            remove(KEY_ACTIVE_FRANCHISE_BRANCH_COUNT)
            remove(KEY_ALL_FRANCHISES)
            apply()
        }
        _activeFranchise.value = null
    }

    /**
     * Checks if an active franchise is set.
     * @return true if active franchise exists, false otherwise
     */
    fun hasActiveFranchise(): Boolean {
        return getActiveFranchiseId() != null
    }
}
