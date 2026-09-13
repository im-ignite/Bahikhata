package com.example.data.auth

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.example.data.model.GoogleAccountInfo
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class GoogleAuthManager(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("google_auth_prefs", Context.MODE_PRIVATE)

    private val credentialManager: CredentialManager = CredentialManager.create(context)

    private val _accountInfo = MutableStateFlow(loadSavedAccount())
    val accountInfo: StateFlow<GoogleAccountInfo> = _accountInfo.asStateFlow()

    private fun loadSavedAccount(): GoogleAccountInfo {
        val email = prefs.getString("google_email", "") ?: ""
        val displayName = prefs.getString("google_display_name", "") ?: ""
        val photoUrl = prefs.getString("google_photo_url", "") ?: ""
        val isLinked = prefs.getBoolean("google_is_linked", false)
        val autoSync = prefs.getBoolean("google_auto_sync", true)
        val lastSync = prefs.getLong("google_last_sync", 0L)
        val syncedCount = prefs.getInt("google_synced_count", 0)

        return GoogleAccountInfo(
            email = email,
            displayName = displayName,
            photoUrl = photoUrl,
            isLinked = isLinked && email.isNotBlank(),
            autoSyncEnabled = autoSync,
            lastSyncTimestamp = lastSync,
            totalSyncedCount = syncedCount
        )
    }

    private fun saveAccount(info: GoogleAccountInfo) {
        prefs.edit()
            .putString("google_email", info.email)
            .putString("google_display_name", info.displayName)
            .putString("google_photo_url", info.photoUrl)
            .putBoolean("google_is_linked", info.isLinked)
            .putBoolean("google_auto_sync", info.autoSyncEnabled)
            .putLong("google_last_sync", info.lastSyncTimestamp)
            .putInt("google_synced_count", info.totalSyncedCount)
            .apply()
        _accountInfo.value = info
    }

    fun getDeviceGoogleAccounts(): List<String> {
        return try {
            val am = AccountManager.get(context)
            val accounts: Array<Account> = am.getAccountsByType("com.google")
            accounts.map { it.name }.filter { it.isNotBlank() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun createSystemAccountPickerIntent(): Intent {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            AccountManager.newChooseAccountIntent(
                null,
                null,
                arrayOf("com.google"),
                null,
                null,
                null,
                null
            )
        } else {
            @Suppress("DEPRECATION")
            AccountManager.newChooseAccountIntent(
                null,
                null,
                arrayOf("com.google"),
                false,
                null,
                null,
                null,
                null
            )
        }
    }

    suspend fun signInWithCredentialManager(webClientId: String?): Result<GoogleAccountInfo> =
        withContext(Dispatchers.IO) {
            try {
                if (webClientId.isNullOrBlank()) {
                    return@withContext Result.failure(
                        IllegalStateException("Server Web Client ID not provided. Use device account selection.")
                    )
                }

                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(webClientId)
                    .setAutoSelectEnabled(true)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val response = credentialManager.getCredential(
                    request = request,
                    context = context
                )

                val credential = response.credential
                if (credential is CustomCredential &&
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {
                    val googleIdTokenCredential =
                        GoogleIdTokenCredential.createFrom(credential.data)

                    val email = googleIdTokenCredential.id
                    val displayName = googleIdTokenCredential.displayName ?: formatDisplayName(email)
                    val photoUrl = googleIdTokenCredential.profilePictureUri?.toString() ?: ""

                    val accountInfo = GoogleAccountInfo(
                        email = email,
                        displayName = displayName,
                        photoUrl = photoUrl,
                        isLinked = true,
                        autoSyncEnabled = true,
                        lastSyncTimestamp = System.currentTimeMillis()
                    )
                    saveAccount(accountInfo)
                    return@withContext Result.success(accountInfo)
                } else {
                    return@withContext Result.failure(
                        IllegalStateException("Unexpected credential type: ${credential.type}")
                    )
                }
            } catch (e: GetCredentialCancellationException) {
                return@withContext Result.failure(Exception("Sign-in cancelled by user"))
            } catch (e: GetCredentialException) {
                return@withContext Result.failure(e)
            } catch (e: Exception) {
                return@withContext Result.failure(e)
            }
        }

    fun completeSignInWithEmail(email: String, customName: String? = null): GoogleAccountInfo {
        val trimmedEmail = email.trim()
        val name = if (!customName.isNullOrBlank()) customName.trim() else formatDisplayName(trimmedEmail)
        val info = GoogleAccountInfo(
            email = trimmedEmail,
            displayName = name,
            isLinked = true,
            autoSyncEnabled = true,
            lastSyncTimestamp = System.currentTimeMillis()
        )
        saveAccount(info)
        return info
    }

    fun updateSyncMetadata(timestamp: Long, count: Int) {
        val current = _accountInfo.value
        val updated = current.copy(
            lastSyncTimestamp = timestamp,
            totalSyncedCount = count
        )
        saveAccount(updated)
    }

    fun toggleAutoSync(enabled: Boolean) {
        val current = _accountInfo.value
        val updated = current.copy(autoSyncEnabled = enabled)
        saveAccount(updated)
    }

    fun signOut() {
        prefs.edit().clear().apply()
        _accountInfo.value = GoogleAccountInfo(isLinked = false)
    }

    private fun formatDisplayName(email: String): String {
        val prefix = email.substringBefore("@")
        return prefix.replace(".", " ")
            .replace("_", " ")
            .split(" ")
            .filter { it.isNotBlank() }
            .joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
            .ifBlank { "Google User" }
    }
}
