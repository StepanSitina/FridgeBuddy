package com.example.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.Serializable
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * WorkManager tichý proces, který zajistí automatické odeslání 
 * fotek a EAN dat do studia StepIn Tech z pozadí aplikace, i když to chvíli 
 * počká kvůli Wi-Fi.
 */
class SubmitFoodWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // 1. Získáme vstupní parametry (EAN, zemi, e-mail a fotky komprimované do base64 stringů z databáze)
            val ean = inputData.getString("EAN") ?: return@withContext Result.failure()
            val country = inputData.getString("COUNTRY") ?: "CZ"
            val userEmail = inputData.getString("USER_EMAIL") ?: ""
            // AI hodnoty
            val cal = inputData.getDouble("MACRO_CAL", 0.0)
            val pro = inputData.getDouble("MACRO_PRO", 0.0)
            val car = inputData.getDouble("MACRO_CAR", 0.0)
            val sug = inputData.getDouble("MACRO_SUG", 0.0)
            val fat = inputData.getDouble("MACRO_FAT", 0.0)
            // Fotky z Array
            val imageArray = inputData.getStringArray("IMAGES_BASE64") ?: emptyArray()

            // 2. Sestavíme model pro náš NodeJS Backend endpoint 
            // (např. Google Cloud Functions url nebo Firebase Functions)
            val payload = FoodSubmissionPayload(
                ean = ean,
                country = country,
                userEmail = userEmail,
                macros = Macros(cal, pro, car, sug, fat),
                images = imageArray.toList()
            )

            // Serializovat pomocí kotlinx.serialization
            val jsonPayload = Json.encodeToString(payload)

            // 3. Pošleme HTTP POST request do Cloudové funkce
            val url = URL("https://us-central1-nutrikalk.cloudfunctions.net/submitNewFood") // Sem vložit reálnou URL Firebase funkce po deployi
            val connection = url.openConnection() as HttpURLConnection
            
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true

            // Zápis JSONu přes OutputStream
            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(jsonPayload)
            }

            // 4. Vyhodnocení odpovědi serveru (zda se emaily úspěšně odeslaly)
            val responseCode = connection.responseCode
            if (responseCode == 200 || responseCode == 201) {
                Log.d("SBER_POTRAVIN", "Data a e-maily odeslány úspěšně na stepintech.cz@gmail.com")
                Result.success()
            } else {
                Log.e("SBER_POTRAVIN", "Server vrátil chybu $responseCode, budeme opakovat (Backoff)")
                Result.retry() // Zkusí odeslat znovu později podle Backoff politiky
            }

        } catch (e: Exception) {
            e.printStackTrace()
            // Při výpadku sítě nebo erroru zkusí WorkManager úlohu opakovat automaticky
            Result.retry()
        }
    }
}

// Data Classes pro JSON serializaci
@Serializable
data class FoodSubmissionPayload(
    val ean: String,
    val country: String,
    val userEmail: String,
    val macros: Macros,
    val images: List<String>
)

@Serializable
data class Macros(
    val calories: Double,
    val proteins: Double,
    val carbs: Double,
    val sugars: Double,
    val fats: Double
)
