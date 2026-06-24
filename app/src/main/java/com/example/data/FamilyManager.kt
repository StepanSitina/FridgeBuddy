package com.example.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.Timestamp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * Model reprezentující žádost o propojení domácností.
 */
data class PairingRequest(
    val id: String = "",
    val fromUserEmail: String = "",
    val fromUserNickname: String = "",
    val fromUserHouseholdId: String = "",
    val toUserEmail: String = "",
    val toUserNickname: String = "",
    val status: String = "PENDING", // PENDING, APPROVED, REJECTED, COMPLETED
    val sharedHouseholdId: String = ""
)

/**
 * FamilyManager spravuje bezpečné propojení dvou uživatelů pomocí unikátních
 * šestimístných kódů (např. NK-128492) a schvalování žádostí v reálném čase.
 */
class FamilyManager {

    // Bezpečná líná inicializace Firestore
    private val firestore: FirebaseFirestore? by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.e("FamilyManager", "Chyba při líné inicializaci FirebaseFirestore: ${e.message}", e)
            null
        }
    }

    /**
     * Vygeneruje nebo stáhne existující párovací kód pro aktuálního uživatele.
     * Zapisuje informace na Firestore pod unikátní email uživatele pro snadné vyhledávání.
     */
    fun getOrCreatePairingCode(
        email: String,
        currentNickname: String,
        currentHouseholdId: String,
        preferredCode: String? = null,
        callback: (String?) -> Unit
    ) {
        val db = firestore
        if (db == null) {
            callback(null)
            return
        }

        val sanitizedEmail = email.lowercase().trim()
        if (sanitizedEmail.isBlank()) {
            callback(null)
            return
        }

        db.collection("users").document(sanitizedEmail).get()
            .addOnSuccessListener { document ->
                val existingCode = document.getString("pairingCode")
                if (!existingCode.isNullOrBlank()) {
                    // Kód již existuje, pouze aktualizujeme doplňující pole, pokud se změnila
                    val updates = hashMapOf(
                        "nickname" to currentNickname,
                        "householdId" to currentHouseholdId,
                        "updatedAt" to Timestamp.now()
                    )
                    db.collection("users").document(sanitizedEmail).set(updates, SetOptions.merge())
                    callback(existingCode)
                } else {
                    // Vygenerujeme nový unikátní kód tvaru NK-XXXXXX (6 náhodných čísel) nebo použijeme preferredCode
                    val newCode = if (!preferredCode.isNullOrBlank()) {
                        preferredCode
                    } else {
                        val randomDigits = (100000..999999).random().toString()
                        "NK-$randomDigits"
                    }

                    val userData = hashMapOf(
                        "email" to sanitizedEmail,
                        "pairingCode" to newCode,
                        "nickname" to currentNickname,
                        "householdId" to currentHouseholdId,
                        "updatedAt" to Timestamp.now()
                    )

                    db.collection("users").document(sanitizedEmail).set(userData, SetOptions.merge())
                        .addOnSuccessListener {
                            callback(newCode)
                        }
                        .addOnFailureListener { e ->
                            Log.e("FamilyManager", "Chyba při ukládání nového kódu", e)
                            callback(newCode) // I tak vrátíme vygenerovaný kód pro UI
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("FamilyManager", "Chyba při načítání uživatele pro kód", e)
                // Nouzové lokální vygenerování
                val randomDigits = (100000..999999).random().toString()
                callback("NK-$randomDigits")
            }
    }

    /**
     * Odešle žádost o propojení cílovému uživateli na základě jeho párovacího kódu.
     */
    fun sendPairingRequest(
        myEmail: String,
        myNickname: String,
        myHouseholdId: String,
        targetCode: String,
        callback: (Boolean, String) -> Unit
    ) {
        val db = firestore
        if (db == null) {
            callback(false, "Vypadá to, že Firestore databáze není dostupná.")
            return
        }

        val cleanedTargetCode = targetCode.trim().uppercase()
        val cleanedMyEmail = myEmail.lowercase().trim()

        if (cleanedTargetCode.isBlank()) {
            callback(false, "Zadejte prosím párovací kód.")
            return
        }

        // 1. Najdeme uživatele s tímto párovacím kódem
        db.collection("users")
            .whereEqualTo("pairingCode", cleanedTargetCode)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.isEmpty) {
                    callback(false, "Uživatel s tímto kódem $cleanedTargetCode nebyl nalezen. Ověřte kód.")
                    return@addOnSuccessListener
                }

                val targetDoc = querySnapshot.documents.first()
                val targetEmail = targetDoc.getString("email") ?: ""
                val targetNickname = targetDoc.getString("nickname") ?: "Člen"

                if (targetEmail.lowercase().trim() == cleanedMyEmail) {
                    callback(false, "Zadali jste svůj vlastní kód. Nemůžete se připojit k sobě.")
                    return@addOnSuccessListener
                }

                val requestId = UUID.randomUUID().toString()
                val requestData = hashMapOf(
                    "id" to requestId,
                    "fromUserEmail" to cleanedMyEmail,
                    "fromUserNickname" to myNickname,
                    "fromUserHouseholdId" to myHouseholdId,
                    "toUserEmail" to targetEmail.lowercase().trim(),
                    "toUserNickname" to targetNickname,
                    "status" to "PENDING",
                    "timestamp" to Timestamp.now()
                )

                // 2. Vytvoříme dokument žádosti
                db.collection("PairingRequests").document(requestId)
                    .set(requestData)
                    .addOnSuccessListener {
                        callback(true, "Žádost o propojení byla úspěšně odeslána uživateli $targetNickname.")
                    }
                    .addOnFailureListener { e ->
                        callback(false, "Odeslání žádosti selhalo: ${e.message}")
                    }
            }
            .addOnFailureListener { e ->
                callback(false, "Chyba při hledání uživatele: ${e.message}")
            }
    }

    /**
     * Real-time tok příchozích nepotvrzených žádostí pro mě (toUserEmail == myEmail).
     */
    fun syncIncomingRequests(myEmail: String): Flow<List<PairingRequest>> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val sanitizedEmail = myEmail.lowercase().trim()
        val registration = db.collection("PairingRequests")
            .whereEqualTo("toUserEmail", sanitizedEmail)
            .whereEqualTo("status", "PENDING")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FamilyManager", "Chyba sledování příchozích žádostí", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(PairingRequest::class.java)
                    }
                    trySend(list)
                }
            }

        awaitClose {
            registration.remove()
        }
    }

    /**
     * Real-time tok odeslaných žádostí, které druhá strana schválila (fromUserEmail == myEmail && status == APPROVED).
     */
    fun syncOutgoingApprovedRequests(myEmail: String): Flow<List<PairingRequest>> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val sanitizedEmail = myEmail.lowercase().trim()
        val registration = db.collection("PairingRequests")
            .whereEqualTo("fromUserEmail", sanitizedEmail)
            .whereEqualTo("status", "APPROVED")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FamilyManager", "Chyba sledování schválených žádostí", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(PairingRequest::class.java)
                    }
                    trySend(list)
                }
            }

        awaitClose {
            registration.remove()
        }
    }

    /**
     * Odpoví na příchozí žádost (schválit / odmítnout).
     */
    fun respondToPairingRequest(
        requestId: String,
        accept: Boolean,
        incomingRequest: PairingRequest,
        myHouseholdId: String,
        myEmail: String,
        callback: (String?, Exception?) -> Unit
    ) {
        val db = firestore
        if (db == null) {
            callback(null, IllegalStateException("Databáze není dostupná."))
            return
        }

        val ref = db.collection("PairingRequests").document(requestId)

        if (!accept) {
            // Odmítnutí: Pouze nastavíme status REJECTED
            ref.update("status", "REJECTED")
                .addOnSuccessListener { callback(null, null) }
                .addOnFailureListener { e -> callback(null, e) }
            return
        }

        // Schválení:
        // A. Určíme cílové společné Household ID
        // Pokud odesílatel má reálnou domácnost, použijeme ji. Jinak pokud máme my reálnou domácnost, použijeme naši.
        // Jinak vygenerujeme zbrusu nové ID domácnosti.
        val targetHouseholdId = when {
            incomingRequest.fromUserHouseholdId.startsWith("NK-FAMILY-") && incomingRequest.fromUserHouseholdId != "NK-FAMILY-DEMO123" -> {
                incomingRequest.fromUserHouseholdId
            }
            myHouseholdId.startsWith("NK-FAMILY-") && myHouseholdId != "NK-FAMILY-DEMO123" -> {
                myHouseholdId
            }
            else -> {
                "NK-FAMILY-" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).uppercase()
            }
        }

        // B. Aktualizujeme žádost s novým statusem APPROVED a přidruženým sharedHouseholdId
        ref.update(
            "status", "APPROVED",
            "sharedHouseholdId", targetHouseholdId
        ).addOnSuccessListener {
            // C. Aktualizujeme profil přijímajícího uživatele na Firestore
            val sanitizedMyEmail = myEmail.lowercase().trim()
            db.collection("users").document(sanitizedMyEmail)
                .update("householdId", targetHouseholdId)
                .addOnFailureListener { e ->
                    Log.w("FamilyManager", "Chyba při aktualizaci householdId v mém profilu: ${e.message}")
                }

            // D. Vytvoříme domácnost ve Firestore, pokud ještě neexistuje
            val householdData = hashMapOf(
                "id" to targetHouseholdId,
                "name" to "Společná domácnost",
                "createdAt" to Timestamp.now(),
                "members" to listOf(incomingRequest.fromUserEmail, sanitizedMyEmail)
            )
            db.collection("Households").document(targetHouseholdId)
                .set(householdData, SetOptions.merge())

            callback(targetHouseholdId, null)
        }.addOnFailureListener { e ->
            callback(null, e)
        }
    }

    /**
     * Nastaví stav žádosti na COMPLETED, jakmile odesílatel úspěšně zaregistruje spárování.
     */
    fun markRequestCompleted(requestId: String, myEmail: String, sharedId: String) {
        val db = firestore ?: return
        db.collection("PairingRequests").document(requestId)
            .update("status", "COMPLETED")

        // Také aktualizujeme odesílatelův profil ve Firestore pro synchronizaci
        db.collection("users").document(myEmail.lowercase().trim())
            .update("householdId", sharedId)
    }

    /**
     * Založí novou prázdnou domácnost (pro kompatibilitu).
     */
    fun createHousehold(
        familyName: String,
        currentUserId: String,
        onComplete: (String?, Exception?) -> Unit
    ) {
        val uniqueId = "NK-FAMILY-" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).uppercase()
        onComplete(uniqueId, null)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = firestore
                if (db != null) {
                    val householdData = hashMapOf(
                        "id" to uniqueId,
                        "name" to familyName,
                        "createdAt" to Timestamp.now(),
                        "members" to listOf(currentUserId)
                    )

                    db.collection("Households").document(uniqueId)
                        .set(householdData)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                db.collection("users").document(currentUserId.lowercase().trim())
                                    .set(hashMapOf(
                                        "householdId" to uniqueId,
                                        "updatedAt" to Timestamp.now()
                                    ), SetOptions.merge())
                            }
                        }
                }
            } catch (e: Exception) {
                Log.e("FamilyManager", "Error in createHousehold background sync", e)
            }
        }
    }
}
