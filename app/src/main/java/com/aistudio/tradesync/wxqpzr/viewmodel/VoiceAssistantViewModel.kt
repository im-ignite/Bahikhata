package com.aistudio.tradesync.wxqpzr.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aistudio.tradesync.wxqpzr.data.model.IntentType
import com.aistudio.tradesync.wxqpzr.data.model.TransactionEntry
import com.aistudio.tradesync.wxqpzr.data.model.VoiceUiState
import com.aistudio.tradesync.wxqpzr.data.repository.BahikhataRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.abs

class VoiceAssistantViewModel : ViewModel() {
    
    private val repository = BahikhataRepository()
    
    private val _uiState = MutableStateFlow<VoiceUiState>(VoiceUiState.Idle)
    val uiState: StateFlow<VoiceUiState> = _uiState.asStateFlow()

    fun onSpeechResult(text: String) {
        val currentState = _uiState.value
        
        if (currentState is VoiceUiState.AwaitingConfirmation) {
            handleConfirmationResponse(text, currentState)
        } else {
            processInitialIntent(text)
        }
    }
    
    private fun processInitialIntent(text: String) {
        _uiState.value = VoiceUiState.Processing
        viewModelScope.launch {
            val response = repository.processVoiceInput(text)
            if (response == null) {
                _uiState.value = VoiceUiState.Error("Mai samajh nahi paya. Kripya fir se bolen.")
                return@launch
            }
            
            when (response.intent) {
                IntentType.RECORD_ENTRY -> {
                    if (response.partyName != null && response.amount != null && response.transactionType != null) {
                        val entry = TransactionEntry(
                            partyName = response.partyName,
                            amount = response.amount,
                            type = response.transactionType,
                            note = response.note ?: ""
                        )
                        val prompt = "${response.partyName} ke naam ${response.amount} rupaye likhna hai, kya save kar doon?"
                        _uiState.value = VoiceUiState.AwaitingConfirmation(entry, prompt)
                    } else {
                        _uiState.value = VoiceUiState.Error("Puri jankari nahi mili. Kripya party ka naam aur amount batayen.")
                    }
                }
                IntentType.GET_BALANCE -> {
                    if (response.partyName != null) {
                        try {
                            val balance = repository.getPartyBalance(response.partyName)
                            val message = if (balance > 0) {
                                "${response.partyName} ka ${abs(balance)} rupaye baki hai (unhe dena hai)."
                            } else if (balance < 0) {
                                "${response.partyName} se ${abs(balance)} rupaye lene baki hain."
                            } else {
                                "${response.partyName} ka hisab barabar hai."
                            }
                            _uiState.value = VoiceUiState.Completed(message)
                        } catch (e: Exception) {
                            _uiState.value = VoiceUiState.Error("Balance nikalne me dikkat aayi.")
                        }
                    } else {
                        _uiState.value = VoiceUiState.Error("Party ka naam samajh nahi aaya.")
                    }
                }
                IntentType.GENERAL_CHAT -> {
                    _uiState.value = VoiceUiState.Completed(response.chatResponse ?: "Ji, bataiye.")
                }
            }
            
            if (_uiState.value is VoiceUiState.Completed || _uiState.value is VoiceUiState.Error) {
                resetToIdleDelayed()
            }
        }
    }
    
    private fun handleConfirmationResponse(text: String, state: VoiceUiState.AwaitingConfirmation) {
        val lowerText = text.lowercase()
        val affirmativeWords = listOf("haan", "ha", "yes", "theek hai", "kardo", "sahi", "han")
        val negativeWords = listOf("nahi", "na", "no", "ruko", "mat karo")
        
        val isAffirmative = affirmativeWords.any { lowerText.contains(it) }
        val isNegative = negativeWords.any { lowerText.contains(it) }
        
        if (isAffirmative && !isNegative) {
            confirmTransaction(state.entry)
        } else if (isNegative && !isAffirmative) {
            cancelTransaction()
        } else {
            _uiState.value = VoiceUiState.AwaitingConfirmation(
                state.entry, 
                "Mai samajh nahi paya. Kripya saaf saaf 'Haan' ya 'Nahi' bolen."
            )
        }
    }
    
    fun confirmTransaction(entry: TransactionEntry) {
        _uiState.value = VoiceUiState.Processing
        viewModelScope.launch {
            val result = repository.commitTransaction(entry)
            if (result.isSuccess) {
                _uiState.value = VoiceUiState.Completed("Save kar diya gaya hai.")
            } else {
                _uiState.value = VoiceUiState.Error("Save karne me dikkat aayi.")
            }
            resetToIdleDelayed()
        }
    }
    
    fun cancelTransaction() {
        _uiState.value = VoiceUiState.Completed("Theek hai, ise radd kiya gaya.")
        resetToIdleDelayed()
    }
    
    private fun resetToIdleDelayed() {
        viewModelScope.launch {
            delay(3000) // Wait enough time for the user to hear the message
            if (_uiState.value is VoiceUiState.Completed || _uiState.value is VoiceUiState.Error) {
                _uiState.value = VoiceUiState.Idle
            }
        }
    }

    fun setStateListening() {
        _uiState.value = VoiceUiState.Listening
    }
    
    fun resetToIdle() {
        _uiState.value = VoiceUiState.Idle
    }
    
    fun setError(msg: String) {
        _uiState.value = VoiceUiState.Error(msg)
        resetToIdleDelayed()
    }
}
