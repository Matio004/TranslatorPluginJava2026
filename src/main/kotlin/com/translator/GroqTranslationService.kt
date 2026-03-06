package com.translator

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger
import java.net.HttpURLConnection
import java.net.URL

@Service(Service.Level.APP)
class GroqTranslationService {
    private val log = Logger.getInstance(GroqTranslationService::class.java)
    private val gson = Gson()

    fun translateToEnglish(variableName: String, sourceLanguage: String): String? {
        val apiKey = ApiCredentialsManager.apiKey?.trim() // Trim usuwa ewentualne ukryte spacje/nowe linie
        if (apiKey.isNullOrBlank()) {
            log.warn("Brak klucza API Groq. Użytkownik nie skonfigurował pluginu.")
            return null
        }

        val model = AppSettingsState.instance.modelName.ifBlank { "openai/gpt-oss-120b" }
        val url = "https://api.groq.com/openai/v1/chat/completions"

        val systemPrompt = """
            You are a coding assistant.
            When given a variable name in $sourceLanguage,
            translate it into English while preserving the original naming convention.
            Return ONLY the translated variable name—no additional text, no markdown formatting, and no quotation marks.
            Examples:
            maksymalnaWysokość = maxHeight
            minimalnaWysokość = minHeight
            średniaWysokość = averageHeight
            prędkość_pojazdu = vehicle_speed
            maksymalna_prędkość = max_speed
            minimalna_prędkość = min_speed
            liczbaPomiarów = numberOfReadings
            liczba_próbek = number_of_samples
            czasStartu = startTime
            czas_zakończenia = end_time
            dataUtworzenia = creationDate
            identyfikatorUżytkownika = userId
            nazwaPliku = fileName
            ścieżka_do_pliku = file_path
            rozmiarPliku = fileSize
            temperaturaSilnika = engineTemperature
            poziomPaliwa = fuelLevel
            statusSystemu = systemStatus
            błądKrytyczny = criticalError
        """.trimIndent()

        val requestBody = mapOf(
            "model" to model,
            "messages" to listOf(
                mapOf("role" to "system", "content" to systemPrompt),
                mapOf("role" to "user", "content" to variableName)
            ),
            "temperature" to 0.0
        )

        val jsonPayload = gson.toJson(requestBody)

        return try {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            connection.setRequestProperty("Authorization", "Bearer $apiKey")
            connection.setRequestProperty("Accept", "application/json")
            connection.doOutput = true

            // Wysyłanie ładunku JSON
            connection.outputStream.use { os ->
                val input = jsonPayload.toByteArray(Charsets.UTF_8)
                os.write(input, 0, input.size)
            }

            // ODCZYT KODU BŁĘDU
            val statusCode = connection.responseCode
            if (statusCode >= 400) {
                // Jeśli serwer zwraca błąd, czytamy errorStream zamiast inputStream!
                val errorResponse = connection.errorStream?.bufferedReader()?.use { it.readText() }
                log.error(">>> Błąd Groq API (HTTP $statusCode): $errorResponse")
                connection.disconnect()
                return null
            }

            // Sukces - czytamy odpowiedź
            val responseString = connection.inputStream.bufferedReader().use { it.readText() }
            val responseJson = gson.fromJson(responseString, JsonObject::class.java)
            val choices = responseJson.getAsJsonArray("choices")

            if (choices != null && !choices.isEmpty) {
                val message = choices.get(0).asJsonObject.getAsJsonObject("message")
                message.get("content").asString.trim()
            } else {
                null
            }
        } catch (e: Exception) {
            log.error(">>> Wyjątek podczas komunikacji z API Groq", e)
            null
        }
    }
}