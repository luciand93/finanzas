package com.finanzasproactivas.data.repository

import com.finanzasproactivas.data.model.Movimiento
import com.finanzasproactivas.data.model.TipoMovimiento
import com.finanzasproactivas.data.model.Frecuencia
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import com.google.api.services.sheets.v4.model.ValueRange
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.UUID

class GoogleSheetsRepository(
    private val applicationContext: android.content.Context
) {
    companion object {
        private const val APPLICATION_NAME = "Finanzas Proactivas"
        private val JSON_FACTORY: JsonFactory = GsonFactory.getDefaultInstance()
        private val SCOPES = listOf(SheetsScopes.SPREADSHEETS)
        private const val CREDENTIALS_FILE_PATH = "/credentials.json"
        private const val SPREADSHEET_ID = "17EBvx8s1IsxcV9-RigMxYvUxgz15ZA6yIuHyY9f8xGk"
        private const val RANGE = "Finanzas!A2:I" // Incluir columna I para esConjunto
        
        private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    }

    private fun getCredentials(httpTransport: NetHttpTransport): GoogleCredential {
        val inputStream: InputStream = applicationContext.assets.open("credentials.json")
        return GoogleCredential.fromStream(inputStream, httpTransport, JSON_FACTORY)
            .createScoped(SCOPES)
    }

    private fun getSheetsService(): Sheets {
        val httpTransport = GoogleNetHttpTransport.newTrustedTransport()
        val credentials = getCredentials(httpTransport)
        return Sheets.Builder(httpTransport, JSON_FACTORY, credentials)
            .setApplicationName(APPLICATION_NAME)
            .build()
    }

    suspend fun obtenerMovimientos(): List<Movimiento> = withContext(Dispatchers.IO) {
        try {
            val service = getSheetsService()
            val response: ValueRange = service.spreadsheets().values()
                .get(SPREADSHEET_ID, RANGE)
                .execute()
            
            val values = response.getValues() ?: emptyList()
            
            values.mapIndexedNotNull { index, row ->
                if (row.size >= 8) {
                    try {
                        val idOriginal = row[0]?.toString()?.trim() ?: ""
                        // Si no hay ID, generar uno basado en la posición de la fila (consistente entre recargas)
                        val idFinal = idOriginal.ifEmpty { "ROW_${index + 2}" } // +2 porque empieza en A2
                        
                        Movimiento(
                            id = idFinal,
                            fecha = dateFormat.parse(row[1]?.toString() ?: "") ?: Date(),
                            tipo = if (row[2]?.toString() == "Ingreso") TipoMovimiento.INGRESO else TipoMovimiento.GASTO,
                            categoria = row[3]?.toString() ?: "",
                            concepto = row[4]?.toString() ?: "",
                            importe = (row[5]?.toString() ?: "0").toDouble(),
                            frecuencia = when (row[6]?.toString()) {
                                "Mensual" -> Frecuencia.MENSUAL
                                "Anual" -> Frecuencia.ANUAL
                                else -> Frecuencia.PUNTUAL
                            },
                            impactoMensual = (row[7]?.toString() ?: "0").toDouble(),
                            esConjunto = row.getOrNull(8)?.toString() == "TRUE"
                        )
                    } catch (e: Exception) {
                        null
                    }
                } else null
            }
        } catch (e: java.io.FileNotFoundException) {
            throw Exception("❌ Error: No se encontró el archivo credentials.json. Asegúrate de que esté en app/src/main/assets/credentials.json")
        } catch (e: com.google.api.client.googleapis.json.GoogleJsonResponseException) {
            val errorMessage = when (e.statusCode) {
                403 -> "❌ Error 403: Sin permisos para acceder a Google Sheets.\n\n" +
                       "Verifica que:\n" +
                       "1. La hoja esté compartida con: ${getServiceAccountEmail()}\n" +
                       "2. El email tenga permisos de 'Editor'\n" +
                       "3. El SPREADSHEET_ID sea correcto: $SPREADSHEET_ID"
                404 -> "❌ Error 404: Hoja no encontrada.\n\n" +
                       "Verifica que:\n" +
                       "1. El SPREADSHEET_ID sea correcto: $SPREADSHEET_ID\n" +
                       "2. La hoja exista y no haya sido eliminada"
                401 -> "❌ Error 401: Credenciales inválidas.\n\n" +
                       "Verifica que el archivo credentials.json sea válido y no haya expirado"
                else -> "❌ Error ${e.statusCode} al conectar con Google Sheets: ${e.message}"
            }
            throw Exception(errorMessage)
        } catch (e: Exception) {
            throw Exception("❌ Error al cargar datos de Google Sheets: ${e.message}\n\n" +
                          "Verifica:\n" +
                          "1. Conexión a internet\n" +
                          "2. Archivo credentials.json en assets/\n" +
                          "3. Hoja compartida con service account")
        }
    }
    
    private fun getServiceAccountEmail(): String {
        return try {
            val inputStream: InputStream = applicationContext.assets.open("credentials.json")
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            // Parsear JSON simple para extraer client_email
            val emailRegex = "\"client_email\"\\s*:\\s*\"([^\"]+)\"".toRegex()
            emailRegex.find(jsonString)?.groupValues?.get(1) ?: "service account email"
        } catch (e: Exception) {
            "service account email (no disponible)"
        }
    }

    suspend fun guardarMovimiento(movimiento: Movimiento): Boolean = withContext(Dispatchers.IO) {
        try {
            val service = getSheetsService()
            val values = listOf(
                movimiento.id,
                dateFormat.format(movimiento.fecha),
                if (movimiento.tipo == TipoMovimiento.INGRESO) "Ingreso" else "Gasto",
                movimiento.categoria,
                movimiento.concepto,
                movimiento.importe.toString(),
                when (movimiento.frecuencia) {
                    Frecuencia.MENSUAL -> "Mensual"
                    Frecuencia.ANUAL -> "Anual"
                    else -> "Puntual"
                },
                movimiento.impactoMensual.toString(),
                if (movimiento.esConjunto) "TRUE" else "FALSE"
            )
            
            val body = ValueRange().setValues(listOf(values))
            service.spreadsheets().values()
                .append(SPREADSHEET_ID, RANGE, body)
                .setValueInputOption("RAW")
                .execute()
            
            true
        } catch (e: com.google.api.client.googleapis.json.GoogleJsonResponseException) {
            val errorMessage = when (e.statusCode) {
                403 -> "❌ Sin permisos para escribir en Google Sheets. Verifica que la hoja esté compartida con permisos de Editor"
                404 -> "❌ Hoja no encontrada. Verifica el SPREADSHEET_ID: $SPREADSHEET_ID"
                else -> "❌ Error ${e.statusCode} al guardar: ${e.message}"
            }
            throw Exception(errorMessage)
        } catch (e: Exception) {
            throw Exception("❌ Error al guardar movimiento: ${e.message}")
        }
    }
    
    suspend fun actualizarMovimiento(movimiento: Movimiento): Boolean = withContext(Dispatchers.IO) {
        try {
            val service = getSheetsService()
            
            // Obtener todos los movimientos para encontrar la fila
            val response: ValueRange = service.spreadsheets().values()
                .get(SPREADSHEET_ID, "Finanzas!A2:I1000")
                .execute()
            
            val values = response.getValues() ?: emptyList()
            
            // Buscar la fila por ID con trim y comparación case-sensitive
            val filaIndex = values.indexOfFirst { row ->
                row.isNotEmpty() && row[0]?.toString()?.trim() == movimiento.id.trim()
            }
            
            if (filaIndex == -1) {
                throw Exception("❌ No se encontró el movimiento para actualizar.\n\n" +
                               "ID buscado: '${movimiento.id}'\n" +
                               "Concepto: '${movimiento.concepto}'\n\n" +
                               "Verifica que el movimiento exista en Google Sheets.")
            }
            
            // La fila real en la hoja es filaIndex + 2 (porque empieza en A2 y el índice es base 0)
            val filaReal = filaIndex + 2
            
            // Si el movimiento tiene un ID temporal (ROW_X), convertirlo a un ID permanente
            val idFinal = if (movimiento.id.startsWith("ROW_")) {
                UUID.randomUUID().toString()
            } else {
                movimiento.id
            }
            
            val valoresActualizados = listOf(
                idFinal,
                dateFormat.format(movimiento.fecha),
                if (movimiento.tipo == TipoMovimiento.INGRESO) "Ingreso" else "Gasto",
                movimiento.categoria,
                movimiento.concepto,
                movimiento.importe.toString(),
                when (movimiento.frecuencia) {
                    Frecuencia.MENSUAL -> "Mensual"
                    Frecuencia.ANUAL -> "Anual"
                    else -> "Puntual"
                },
                movimiento.impactoMensual.toString(),
                if (movimiento.esConjunto) "TRUE" else "FALSE"
            )
            
            val body = ValueRange().setValues(listOf(valoresActualizados))
            val rangeToUpdate = "Finanzas!A${filaReal}:I${filaReal}"
            
            service.spreadsheets().values()
                .update(SPREADSHEET_ID, rangeToUpdate, body)
                .setValueInputOption("RAW")
                .execute()
            
            true
        } catch (e: com.google.api.client.googleapis.json.GoogleJsonResponseException) {
            val errorMessage = when (e.statusCode) {
                403 -> "❌ Sin permisos para actualizar en Google Sheets. Verifica que la hoja esté compartida con permisos de Editor"
                404 -> "❌ Hoja no encontrada. Verifica el SPREADSHEET_ID: $SPREADSHEET_ID"
                else -> "❌ Error ${e.statusCode} al actualizar: ${e.message}"
            }
            throw Exception(errorMessage)
        } catch (e: Exception) {
            if (e.message?.contains("❌") == true) {
                throw e // Re-lanzar si ya es un mensaje descriptivo
            }
            throw Exception("❌ Error al actualizar movimiento: ${e.message}")
        }
    }
    
    suspend fun eliminarMovimiento(movimiento: Movimiento): Boolean = withContext(Dispatchers.IO) {
        try {
            val service = getSheetsService()
            
            // Obtener todos los movimientos para encontrar la fila
            val response: ValueRange = service.spreadsheets().values()
                .get(SPREADSHEET_ID, "Finanzas!A2:I1000")
                .execute()
            
            val values = response.getValues() ?: emptyList()
            
            // Buscar la fila por ID con trim
            var filaIndex = values.indexOfFirst { row ->
                row.isNotEmpty() && row[0]?.toString()?.trim() == movimiento.id.trim()
            }
            
            // Si no encuentra por ID, buscar por combinación de campos únicos (fecha + concepto + importe + categoría)
            if (filaIndex == -1) {
                filaIndex = values.indexOfFirst { row ->
                    if (row.size >= 6) {
                        try {
                            val fechaRow = row[1]?.toString() ?: ""
                            val tipoRow = row[2]?.toString() ?: ""
                            val categoriaRow = row[3]?.toString() ?: ""
                            val conceptoRow = row[4]?.toString() ?: ""
                            val importeRow = row[5]?.toString() ?: ""
                            
                            fechaRow == dateFormat.format(movimiento.fecha) &&
                            tipoRow == (if (movimiento.tipo == TipoMovimiento.INGRESO) "Ingreso" else "Gasto") &&
                            categoriaRow == movimiento.categoria &&
                            conceptoRow == movimiento.concepto &&
                            importeRow == movimiento.importe.toString()
                        } catch (e: Exception) {
                            false
                        }
                    } else {
                        false
                    }
                }
            }
            
            if (filaIndex == -1) {
                throw Exception("❌ No se encontró el movimiento para eliminar.\n\n" +
                               "ID buscado: '${movimiento.id}'\n" +
                               "Concepto: '${movimiento.concepto}'\n" +
                               "Fecha: ${dateFormat.format(movimiento.fecha)}\n" +
                               "Importe: ${movimiento.importe}\n\n" +
                               "El movimiento puede haber sido eliminado previamente o no existe en Google Sheets.")
            }
            
            // La fila real en la hoja es filaIndex + 2 (porque empieza en A2 y el índice es base 0)
            val filaReal = filaIndex + 2
            
            // Eliminar la fila usando batchUpdate
            val deleteRequest = com.google.api.services.sheets.v4.model.Request()
                .setDeleteDimension(
                    com.google.api.services.sheets.v4.model.DeleteDimensionRequest()
                        .setRange(
                            com.google.api.services.sheets.v4.model.DimensionRange()
                                .setSheetId(0)
                                .setDimension("ROWS")
                                .setStartIndex(filaReal - 1)
                                .setEndIndex(filaReal)
                        )
                )
            
            val batchUpdateRequest = com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest()
                .setRequests(listOf(deleteRequest))
            
            service.spreadsheets().batchUpdate(SPREADSHEET_ID, batchUpdateRequest).execute()
            
            true
        } catch (e: com.google.api.client.googleapis.json.GoogleJsonResponseException) {
            val errorMessage = when (e.statusCode) {
                403 -> "❌ Sin permisos para eliminar en Google Sheets. Verifica que la hoja esté compartida con permisos de Editor"
                404 -> "❌ Hoja no encontrada. Verifica el SPREADSHEET_ID: $SPREADSHEET_ID"
                else -> "❌ Error ${e.statusCode} al eliminar: ${e.message}"
            }
            throw Exception(errorMessage)
        } catch (e: Exception) {
            if (e.message?.contains("❌") == true) {
                throw e // Re-lanzar si ya es un mensaje descriptivo
            }
            throw Exception("❌ Error al eliminar movimiento: ${e.message}")
        }
    }
}
