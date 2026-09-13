package com.example.data.model

data class GoogleAccountInfo(
    val email: String = "trader.sync@gmail.com",
    val displayName: String = "Sync Master",
    val isLinked: Boolean = true,
    val driveFolder: String = "Google Drive/TradeSync_Backups",
    val autoSyncEnabled: Boolean = true,
    val lastSyncTimestamp: Long = System.currentTimeMillis() - 15 * 60 * 1000L
)

enum class SyncStatus {
    IDLE,
    SYNCING,
    SUCCESS,
    ERROR
}
