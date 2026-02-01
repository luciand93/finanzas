package com.finanzasproactivas.data.repository

import com.finanzasproactivas.data.api.RetrofitClient
import com.finanzasproactivas.data.api.GeminiChatRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository para comunicarse con Gemini a través del backend de Vercel
 * Esto resuelve problemas de restricciones regionales
 */
class GeminiRepository {
    
    private val apiService = RetrofitClient.apiService
    
    /**
     * Ya no es necesario inicializar - el backend maneja la conexión con Gemini
     */
    fun initialize(apiKey: String? = null) {
        // Mantenido por compatibilidad pero ya no es necesario
        // El backend de Vercel maneja la conexión con Gemini
    }
    
    /**
     * Envía una pregunta a Gemini a través del backend de Vercel
     * Esto resuelve problemas de restricciones regionales
     * 
     * @param pregunta La pregunta del usuario
     * @param contexto Los datos financieros formateados como texto
     * @return La respuesta de Gemini o un mensaje de error
     */
    suspend fun chat(pregunta: String, contexto: String): String = withContext(Dispatchers.IO) {
        try {
            // Crear el mensaje completo con contexto
            val mensajeCompleto = """
                DATOS FINANCIEROS DEL USUARIO:
                $contexto
                
                PREGUNTA DEL USUARIO:
                $pregunta
            """.trimIndent()
            
            // Preparar request para el backend
            val request = GeminiChatRequest(
                message = mensajeCompleto,
                context = mapOf(
                    "app" to "Finanzas Proactivas",
                    "tipo" to "consulta_financiera"
                )
            )
            
            // Llamar al backend de Vercel
            val response = apiService.chatGemini(request)
            
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true) {
                    apiResponse.data?.response ?: "Lo siento, no pude generar una respuesta."
                } else {
                    """
                    ❌ Error al consultar el asistente IA
                    
                    ${apiResponse?.message ?: "Error desconocido"}
                    
                    Por favor, intenta de nuevo.
                    """.trimIndent()
                }
            } else {
                """
                ❌ Error de conexión con el servidor
                
                Código: ${response.code()}
                
                Verifica tu conexión a internet y vuelve a intentar.
                """.trimIndent()
            }
            
        } catch (e: Exception) {
            """
            ❌ Error al comunicarse con el asistente IA
            
            Error: ${e.message ?: "Error desconocido"}
            
            Verifica tu conexión a internet.
            """.trimIndent()
        }
    }
}
