package com.aistudio.tradesync.wxqpzr.data.model

import com.squareup.moshi.JsonClass

enum class TransactionType {
    GIVEN, RECEIVED
}

data class TransactionEntry(
    val id: String = "",
    val partyName: String = "",
    val amount: Double = 0.0,
    val type: TransactionType = TransactionType.GIVEN,
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

sealed interface VoiceUiState {
    object Idle : VoiceUiState
    object Listening : VoiceUiState
    object Processing : VoiceUiState
    data class AwaitingConfirmation(val entry: TransactionEntry, val prompt: String) : VoiceUiState
    data class Completed(val message: String) : VoiceUiState
    data class Error(val errorMessage: String) : VoiceUiState
}

enum class IntentType {
    RECORD_ENTRY, GET_BALANCE, GENERAL_CHAT
}

@JsonClass(generateAdapter = true)
data class AiIntentResponse(
    val intent: IntentType,
    val partyName: String? = null,
    val amount: Double? = null,
    val transactionType: TransactionType? = null,
    val note: String? = null,
    val chatResponse: String? = null
)
