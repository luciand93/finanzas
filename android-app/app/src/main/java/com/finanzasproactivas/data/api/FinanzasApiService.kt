package com.finanzasproactivas.data.api

import com.finanzasproactivas.data.model.Movimiento
import retrofit2.Response
import retrofit2.http.*

/**
 * Interfaz de Retrofit para comunicarse con el backend de Vercel
 */
interface FinanzasApiService {
    
    @GET("movimientos")
    suspend fun getMovimientos(): Response<ApiResponse<List<MovimientoDto>>>
    
    @GET("movimientos/{id}")
    suspend fun getMovimiento(@Path("id") id: String): Response<ApiResponse<MovimientoDto>>
    
    @POST("movimientos")
    suspend fun createMovimiento(@Body movimiento: MovimientoDto): Response<ApiResponse<MovimientoDto>>
    
    @PUT("movimientos/{id}")
    suspend fun updateMovimiento(
        @Path("id") id: String,
        @Body movimiento: MovimientoDto
    ): Response<ApiResponse<MovimientoDto>>
    
    @DELETE("movimientos/{id}")
    suspend fun deleteMovimiento(@Path("id") id: String): Response<ApiResponse<Unit>>
    
    @GET("categorias")
    suspend fun getCategorias(): Response<ApiResponse<List<CategoriaDto>>>
    
    @POST("gemini/chat")
    suspend fun chatGemini(@Body request: GeminiChatRequest): Response<ApiResponse<GeminiChatResponse>>
}

/**
 * Respuesta estándar de la API
 */
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: Boolean? = null,
    val message: String? = null,
    val count: Int? = null
)

/**
 * DTO para movimientos (formato backend)
 */
data class MovimientoDto(
    val id: String? = null,
    val fecha: String,
    val tipo: String,
    val categoria: String,
    val concepto: String,
    val importe: Double,
    val frecuencia: String = "Puntual",
    val impacto_mensual: Double = 0.0,
    val es_conjunto: Boolean = false,
    val fecha_fin: String? = null,
    val created_at: String? = null,
    val updated_at: String? = null
)

/**
 * DTO para categorías
 */
data class CategoriaDto(
    val id: String,
    val nombre: String,
    val icono: String?,
    val color: String?,
    val created_at: String? = null,
    val updated_at: String? = null
)

/**
 * Request para chat con Gemini
 */
data class GeminiChatRequest(
    val message: String,
    val context: Map<String, Any>? = null
)

/**
 * Response del chat con Gemini
 */
data class GeminiChatResponse(
    val response: String
)
