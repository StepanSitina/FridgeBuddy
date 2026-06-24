package com.example.data

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.Timestamp
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.UUID

data class FridgeActivity(
    val logId: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val userId: String = "",
    val userName: String = "",
    val actionType: String = "",
    val ingredientName: String = "",
    val amountString: String = ""
)

data class Household(
    val id: String = "",
    val name: String = "",
    val members: List<String> = emptyList() // List of User IDs
)

/**
 * Handles all Household sync operations via Cloud Firestore.
 */
class HouseholdManager() {
    private var firestore: FirebaseFirestore? = null

    init {
        // Enable offline persistence only if it hasn't been enabled or accessed yet
        try {
            firestore = FirebaseFirestore.getInstance()
            val settings = com.google.firebase.firestore.FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()
            firestore?.firestoreSettings = settings
        } catch (e: Exception) {
            Log.w("HouseholdManager", "Firestore is not available or could not be initialized: ${e.message}")
        }
    }

    private val householdsCollection = firestore?.collection("Households")
    private var fridgeListener: ListenerRegistration? = null
    private var activityListener: ListenerRegistration? = null

    /**
     * Zaznamená aktivitu do rodinného logu.
     * Udržuje maximálně posledních 30 záznamů (promazává starší).
     */
    fun logActivity(
        householdId: String,
        userId: String,
        userName: String,
        actionType: String,
        ingredientName: String,
        amountString: String
    ) {
        if (!householdId.startsWith("NK-FAMILY-")) return

        val col = householdsCollection ?: return
        val logId = UUID.randomUUID().toString()
        val activity = FridgeActivity(
            logId = logId,
            timestamp = Timestamp.now(),
            userId = userId,
            userName = userName,
            actionType = actionType,
            ingredientName = ingredientName,
            amountString = amountString
        )

        val colRef = col.document(householdId).collection("activityLog")

        colRef.document(logId).set(activity)
            .addOnSuccessListener {
                // Automatické promazávání (držíme max 30)
                colRef.orderBy("timestamp", Query.Direction.DESCENDING)
                    .get()
                    .addOnSuccessListener { snapshot ->
                        if (snapshot.size() > 30) {
                            val docsToDelete = snapshot.documents.drop(30)
                            docsToDelete.forEach { doc ->
                                doc.reference.delete()
                            }
                        }
                    }
            }
            .addOnFailureListener {
                Log.e("HouseholdManager", "Failed to log activity.", it)
            }
    }

    /**
     * Plynulý real-time feed aktivit s limitem 15 pro UI.
     */
    fun syncActivityLogLive(householdId: String): Flow<List<FridgeActivity>> = callbackFlow {
        activityListener?.remove()

        val col = householdsCollection
        if (col == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        activityListener = col.document(householdId)
            .collection("activityLog")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(15)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("HouseholdManager", "Activity log listen failed.", error)
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val logs = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(FridgeActivity::class.java)
                    }
                    trySend(logs)
                }
            }

        awaitClose {
            activityListener?.remove()
            activityListener = null
        }
    }

    /**
     * Lingvistická šablona a zobrazení UI textu.
     */
    fun formatActivityLog(activity: FridgeActivity, isSlovak: Boolean): String {
        return when (activity.actionType) {
            "FRIDGE_ADD" -> if (isSlovak) "${activity.userName} pridal(a): ${activity.amountString} ${activity.ingredientName}" else "${activity.userName} přidal(a): ${activity.amountString} ${activity.ingredientName}"
            "FRIDGE_REMOVE" -> if (isSlovak) "${activity.userName} odobral(a): ${activity.amountString} ${activity.ingredientName}" else "${activity.userName} odebral(a): ${activity.amountString} ${activity.ingredientName}"
            "SHOPPING_BUY" -> if (isSlovak) "${activity.userName} nakúpil(a): ${activity.ingredientName}" else "${activity.userName} nakoupil(a): ${activity.ingredientName}"
            "SHOPPING_ADD" -> if (isSlovak) "${activity.userName} pridal(a) na nákup: ${activity.ingredientName}" else "${activity.userName} přidal(a) na nákup: ${activity.ingredientName}"
            else -> ""
        }
    }

    /**
     * Získává PantryItems pomocí real-time Snapshot Listeneru.
     * Jakmile jakýkoliv člen domácnosti odebere nebo přidá surovinu, změna se okamžitě 
     * propíše do telefonů všech ostatních členů bez nutnosti manuálního refreshe.
     */
    fun syncPantryItemsLive(householdId: String): Flow<List<PantryItem>> = callbackFlow {
        // Zrušení případného předchozího listeneru
        fridgeListener?.remove()

        val col = householdsCollection
        if (col == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        fridgeListener = col.document(householdId)
            .collection("sharedFridge")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("HouseholdManager", "Listen failed.", error)
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val items = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(PantryItem::class.java)?.copy(id = doc.id.hashCode()) // Dummy ID map pro UI
                    }
                    trySend(items)
                }
            }

        // Cleanup proveden po zrušení Flow
        awaitClose {
            fridgeListener?.remove()
            fridgeListener = null
        }
    }

    /**
     * Ověří ve Firestore, zda daný dokument v kolekci Households reálně existuje, 
     * aby nedošlo k zápisu neexistujícího ID.
     */
    fun verifyHouseholdExists(householdId: String, onComplete: (Boolean) -> Unit) {
        if (!householdId.startsWith("NK-FAMILY-")) {
            onComplete(false)
            return
        }

        val col = householdsCollection
        if (col == null) {
            onComplete(false)
            return
        }

        col.document(householdId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    onComplete(true)
                } else {
                    onComplete(false)
                }
            }
            .addOnFailureListener {
                Log.e("HouseholdManager", "Error verifying household", it)
                onComplete(false)
            }
    }
}

object PairingUtils {

    /**
     * Vykreslí QR kód z Deep Link URI pro párování telefonů foťákem
     */
    fun generatePairingQRCode(householdId: String): Bitmap? {
        val uriStr = "https://join.nutrikalk.app/family?id=$householdId"
        val writer = QRCodeWriter()
        return try {
            val bitMatrix = writer.encode(uriStr, BarcodeFormat.QR_CODE, 512, 512)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE)
                }
            }
            bitmap
        } catch (e: WriterException) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Zachytí univerzální odkaz nebo custom SCHEME a extrahuje Household ID
     * např. z textového kódu přečteného ze skeneru.
     */
    fun parseDeepLinkUrlOrCode(text: String?): String? {
        if (text == null) return null
        
        try {
            val uri = Uri.parse(text)
            if (uri.scheme == "nutrikalk" && uri.host == "join") {
                val pathSegment = uri.lastPathSegment
                if (pathSegment?.startsWith("NK-FAMILY-") == true) {
                    return pathSegment
                }
            }
            if ((uri.scheme == "https" || uri.scheme == "http") && uri.host == "join.nutrikalk.app") {
                val queryParam = uri.getQueryParameter("id")
                if (queryParam?.startsWith("NK-FAMILY-") == true) {
                    return queryParam
                }
            }
        } catch (e: Exception) {
            Log.e("PairingUtils", "Failed to parse URI: $text", e)
        }
        
        // Zkus prosté id
        if (text.startsWith("NK-FAMILY-")) {
            return text
        }
        return null
    }

    /**
     * Zachytí univerzální odkaz nebo custom SCHEME a vrátí Household ID, 
     * null pokud intent neobsahuje správná data.
     */
    fun parseDeepLinkIntent(intent: Intent?): String? {
        if (intent == null) return null
        val action = intent.action
        val data: Uri? = intent.data

        if (Intent.ACTION_VIEW == action && data != null) {
            return parseDeepLinkUrlOrCode(data.toString())
        }
        return null
    }
}
