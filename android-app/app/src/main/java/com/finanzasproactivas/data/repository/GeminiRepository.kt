package com.finanzasproactivas.data.repository

import com.finanzasproactivas.data.api.RetrofitClient
import com.finanzasproactivas.data.api.GeminiChatRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

/**
 * Repository para comunicarse con Gemini a través del backend de Vercel.
 * El servidor de Vercel ejecuta las llamadas a la API de Gemini (útil desde Andorra y otras regiones).
 */
class GeminiRepository {
    
    private val apiService = RetrofitClient.apiService
    
    fun initialize(apiKey: String? = null) {
        // No necesario: el backend de Vercel usa su propia GEMINI_API_KEY
    }
    
    /**
     * Envía una pregunta al asistente IA vía Vercel (Vercel llama a Gemini en el servidor).
     */
    suspend fun chat(pregunta: String, contexto: String): String = withContext(Dispatchers.IO) {
        try {
            val mensajeCompleto = """
                DATOS FINANCIEROS DEL USUARIO:
                $contexto
                
                PREGUNTA DEL USUARIO:
                $pregunta
            """.trimIndent()
            
            val request = GeminiChatRequest(
                message = mensajeCompleto,
                context = mapOf(
                    "app" to "Finanzas Proactivas",
                    "tipo" to "consulta_financiera"
                )
            )
            
            val response = apiService.chatGemini(request)
            
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true) {
                    apiResponse.data?.response ?: "Lo siento, no pude generar una respuesta."
                } else {
                    "❌ ${apiResponse?.message ?: "Error desconocido"}\n\nPor favor, intenta de nuevo."
                }
            } else {
                val serverMessage = response.errorBody()?.string()?.let { body ->
                    try {
                        JSONObject(body).optString("message", "").takeIf { it.isNotEmpty() }
                    } catch (_: Exception) { null }
                }
                buildString {
                    append("❌ Error del servidor (${response.code()})")
                    if (!serverMessage.isNullOrBlank()) append("\n\n$serverMessage")
                    else append("\n\nVerifica tu conexión y que la API en Vercel tenga GEMINI_API_KEY configurada.")
                }
            }
            
        } catch (e: java.net.UnknownHostException) {
            "❌ No se pudo conectar al servidor.\n\nComprueba que puedes abrir en el navegador:\nhttps://finanzas-api-three.vercel.app/api/health\n\nSi no carga, el problema es la red o el DNS de tu dispositivo."
        } catch (e: java.net.SocketTimeoutException) {
            "❌ Tiempo de espera agotado. El servidor tardó demasiado en responder. Intenta de nuevo."
        } catch (e: Exception) {
            "❌ Error: ${e.message ?: "No se pudo conectar al asistente IA"}\n\nVerifica tu conexión a internet."
        }
    }
}
