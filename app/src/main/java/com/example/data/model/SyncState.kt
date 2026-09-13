package com.example.data.model

data class GoogleAccountInfo(
    val email: String = "",
    val displayName: String = "",
    val isLinked: Boolean = false,
    val driveFolder: String = "Google Drive/RaiFish_Backups",
    val autoSyncEnabled: Boolean = false,
    val lastSyncTimestamp: Long = 0L
)

enum class SyncStatus {
    IDLE,
    SYNCING,
    SUCCESS,
    ERROR
}
