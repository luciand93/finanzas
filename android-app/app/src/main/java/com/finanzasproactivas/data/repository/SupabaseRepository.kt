package com.finanzasproactivas.data.repository

import com.finanzasproactivas.data.api.MovimientoDto
import com.finanzasproactivas.data.api.RetrofitClient
import com.finanzasproactivas.data.model.Movimiento
import com.finanzasproactivas.data.model.TipoMovimiento
import com.finanzasproactivas.data.model.Frecuencia
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

/**
 * Repository que se conecta al backend de Vercel (Supabase)
 * Reemplaza a GoogleSheetsRepository
 */
class SupabaseRepository {
    
    private val apiService = RetrofitClient.apiService
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val isoDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    /**
     * Obtener todos los movimientos desde el backend
     */
    suspend fun obtenerMovimientos(): List<Movimiento> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getMovimientos()
            
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true) {
                    apiResponse.data?.map { dto -> dto.toMovimiento() } ?: emptyList()
                } else {
                    throw Exception(apiResponse?.message ?: "Error desconocido al obtener movimientos")
                }
            } else {
                throw Exception("Error HTTP ${response.code()}: ${response.message()}")
            }
        } catch (e: Exception) {
            throw Exception("❌ Error al conectar con el servidor: ${e.message}")
        }
    }
    
    /**
     * Guardar un nuevo movimiento
     */
    suspend fun guardarMovimiento(movimiento: Movimiento): Boolean = withContext(Dispatchers.IO) {
        try {
            val dto = movimiento.toDto()
            val response = apiService.createMovimiento(dto)
            
            if (response.isSuccessful) {
                val apiResponse = response.body()
                apiResponse?.success == true
            } else {
                throw Exception("Error HTTP ${response.code()}: ${response.message()}")
            }
        } catch (e: Exception) {
            throw Exception("❌ Error al guardar movimiento: ${e.message}")
        }
    }
    
    /**
     * Actualizar un movimiento existente
     */
    suspend fun actualizarMovimiento(movimiento: Movimiento): Boolean = withContext(Dispatchers.IO) {
        try {
            val dto = movimiento.toDto()
            val response = apiService.updateMovimiento(movimiento.id, dto)
            
            if (response.isSuccessful) {
                val apiResponse = response.body()
                apiResponse?.success == true
            } else {
                throw Exception("Error HTTP ${response.code()}: ${response.message()}")
            }
        } catch (e: Exception) {
            throw Exception("❌ Error al actualizar movimiento: ${e.message}")
        }
    }
    
    /**
     * Eliminar un movimiento
     */
    suspend fun eliminarMovimiento(movimiento: Movimiento): Boolean = withContext(Dispatchers.IO) {
        try {
            val response = apiService.deleteMovimiento(movimiento.id)
            
            if (response.isSuccessful) {
                val apiResponse = response.body()
                apiResponse?.success == true
            } else {
                throw Exception("Error HTTP ${response.code()}: ${response.message()}")
            }
        } catch (e: Exception) {
            throw Exception("❌ Error al eliminar movimiento: ${e.message}")
        }
    }
    
    // ==================== MAPPERS ====================
    
    /**
     * Convertir MovimientoDto (backend) a Movimiento (app)
     */
    private fun MovimientoDto.toMovimiento(): Movimiento {
        return Movimiento(
            id = this.id ?: UUID.randomUUID().toString(),
            fecha = try {
                isoDateFormat.parse(this.fecha) ?: Date()
            } catch (e: Exception) {
                Date()
            },
            tipo = if (this.tipo == "Ingreso") TipoMovimiento.INGRESO else TipoMovimiento.GASTO,
            categoria = this.categoria,
            concepto = this.concepto,
            importe = this.importe,
            frecuencia = when (this.frecuencia) {
                "Mensual" -> Frecuencia.MENSUAL
                "Anual" -> Frecuencia.ANUAL
                else -> Frecuencia.PUNTUAL
            },
            impactoMensual = this.impacto_mensual,
            esConjunto = this.es_conjunto,
            fechaFin = this.fecha_fin?.let { try { isoDateFormat.parse(it) } catch (e: Exception) { null } }
        )
    }
    
    /**
     * Convertir Movimiento (app) a MovimientoDto (backend)
     */
    private fun Movimiento.toDto(): MovimientoDto {
        return MovimientoDto(
            id = if (this.id.isNotEmpty()) this.id else null,
            fecha = isoDateFormat.format(this.fecha),
            tipo = if (this.tipo == TipoMovimiento.INGRESO) "Ingreso" else "Gasto",
            categoria = this.categoria,
            concepto = this.concepto,
            importe = this.importe,
            frecuencia = when (this.frecuencia) {
                Frecuencia.MENSUAL -> "Mensual"
                Frecuencia.ANUAL -> "Anual"
                Frecuencia.PUNTUAL -> "Puntual"
            },
            impacto_mensual = this.impactoMensual,
            es_conjunto = this.esConjunto,
            fecha_fin = this.fechaFin?.let { isoDateFormat.format(it) }
        )
    }
}
