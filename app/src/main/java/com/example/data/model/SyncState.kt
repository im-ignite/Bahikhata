package com.example.data.model

data class GoogleAccountInfo(
    val email: String = "",
    val displayName: String = "",
    val photoUrl: String = "",
    val isLinked: Boolean = false,
    val driveFolder: String = "Google Cloud / RaiFish_Backups",
    val autoSyncEnabled: Boolean = true,
    val lastSyncTimestamp: Long = 0L,
    val totalSyncedCount: Int = 0
) {
    val initials: String
        get() {
            if (displayName.isNotBlank()) {
                val parts = displayName.trim().split(" ")
                return if (parts.size > 1) {
                    "${parts[0].take(1)}${parts[1].take(1)}".uppercase()
                } else {
                    displayName.take(2).uppercase()
                }
            }
            if (email.isNotBlank()) {
                return email.take(2).uppercase()
            }
            return "G"
        }
}

enum class SyncStatus {
    IDLE,
    SYNCING,
    SUCCESS,
    ERROR
}

