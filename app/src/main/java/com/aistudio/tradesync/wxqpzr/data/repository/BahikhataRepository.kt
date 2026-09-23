package com.aistudio.tradesync.wxqpzr.data.repository

import com.aistudio.tradesync.wxqpzr.data.model.AiIntentResponse
import com.aistudio.tradesync.wxqpzr.data.model.TransactionEntry
import com.aistudio.tradesync.wxqpzr.data.model.TransactionType
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class BahikhataRepository {
    private val firestore: FirebaseFirestore = Firebase.firestore
    private val auth: FirebaseAuth = Firebase.auth
    private val moshi = Moshi.Builder().build()
    private val jsonAdapter = moshi.adapter(AiIntentResponse::class.java)

    private val generativeModel = Firebase.ai.generativeModel(
        modelName = "gemini-2.5-flash",
        systemInstruction = content {
            text("""
                You are an AI assistant for a Ledger (Udhar-Jama) App used by rural merchants and farmers in India.
                They will speak in a mix of Hindi and Bhojpuri (e.g., "Ramesh ko 500 ka beej diya", "pichla baki kitna hai", "50 jama mila").
                Your job is to understand the user's intent and output a JSON response.
                
                Intent Classification:
                1. RECORD_ENTRY: When the user wants to record a transaction (Udhar given or Jama received). Extract partyName, amount, transactionType (GIVEN or RECEIVED), note. 
                   Examples: "Raju ko 500 udhar diya" -> GIVEN, "Suresh se 200 jama mila" -> RECEIVED.
                2. GET_BALANCE: When the user asks about a party's balance. Extract partyName.
                   Examples: "Raju ka kitna baki hai", "Suresh ka hisab batao".
                3. GENERAL_CHAT: When the user asks a general query (farming, weather, greetings). Provide a helpful answer in natural Hindi in the `chatResponse` field.
                
                Always return a valid JSON matching this schema:
                {
                  "intent": "RECORD_ENTRY" | "GET_BALANCE" | "GENERAL_CHAT",
                  "partyName": "String or null",
                  "amount": "Number or null",
                  "transactionType": "GIVEN" | "RECEIVED" | null,
                  "note": "String or null",
                  "chatResponse": "String or null"
                }
            """.trimIndent())
        },
        generationConfig = generationConfig {
            responseMimeType = "application/json"
        }
    )

    private val currentUserId: String?
        get() = auth.currentUser?.uid

    suspend fun processVoiceInput(text: String): AiIntentResponse? = withContext(Dispatchers.IO) {
        try {
            val response = generativeModel.generateContent(text)
            val jsonText = response.text
            if (jsonText != null) {
                // Clean up markdown code blocks if any
                val cleanedJson = jsonText.replace("```json", "").replace("```", "").trim()
                jsonAdapter.fromJson(cleanedJson)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getPartyBalance(partyName: String): Double = withContext(Dispatchers.IO) {
        val uid = currentUserId ?: throw Exception("User not logged in")
        val snapshot = firestore.collection("users").document(uid)
            .collection("transactions")
            .whereEqualTo("partyName", partyName)
            .get()
            .await()

        var netOwedToUser = 0.0
        for (doc in snapshot.documents) {
            val typeStr = doc.getString("type")
            val amount = doc.getDouble("amount") ?: 0.0
            
            if (typeStr == TransactionType.GIVEN.name) {
                netOwedToUser += amount // Given to them, so they owe us
            } else if (typeStr == TransactionType.RECEIVED.name) {
                netOwedToUser -= amount // Received from them, so their debt decreases
            }
        }
        
        netOwedToUser
    }

    suspend fun commitTransaction(entry: TransactionEntry): Result<Unit> = withContext(Dispatchers.IO) {
        val uid = currentUserId ?: return@withContext Result.failure(Exception("User not logged in"))
        try {
            val docRef = firestore.collection("users").document(uid)
                .collection("transactions")
                .document()
            
            val entryWithId = entry.copy(id = docRef.id)
            docRef.set(entryWithId).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
