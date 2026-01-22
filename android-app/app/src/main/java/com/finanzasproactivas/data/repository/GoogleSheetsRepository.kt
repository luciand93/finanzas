package com.finanzasproactivas.data.repository

import com.finanzasproactivas.data.model.Movimiento
import com.finanzasproactivas.data.model.TipoMovimiento
import com.finanzasproactivas.data.model.Frecuencia
import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import com.google.api.services.sheets.v4.model.ValueRange
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.*

class GoogleSheetsRepository(
    private val applicationContext: android.content.Context
) {
    companion object {
        private const val APPLICATION_NAME = "Finanzas Proactivas"
        private val JSON_FACTORY: JsonFactory = GsonFactory.getDefaultInstance()
        private const val TOKENS_DIRECTORY_PATH = "tokens"
        private val SCOPES = listOf(SheetsScopes.SPREADSHEETS)
        private const val CREDENTIALS_FILE_PATH = "/credentials.json"
        private const val SPREADSHEET_ID = "TU_SPREADSHEET_ID" // Configurar desde secrets
        private const val RANGE = "Finanzas!A2:H"
        
        private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    }

    private fun getCredentials(httpTransport: NetHttpTransport): Credential {
        val inputStream = applicationContext.assets.open("credentials.json")
        val clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, InputStreamReader(inputStream))
        
        val flow = GoogleAuthorizationCodeFlow.Builder(
            httpTransport, JSON_FACTORY, clientSecrets, SCOPES
        )
            .setDataStoreFactory(FileDataStoreFactory(File(applicationContext.filesDir, TOKENS_DIRECTORY_PATH)))
            .setAccessType("offline")
            .build()
        
        val receiver = LocalServerReceiver.Builder().setPort(8888).build()
        return AuthorizationCodeInstalledApp(flow, receiver).authorize("user")
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
            
            values.mapNotNull { row ->
                if (row.size >= 8) {
                    try {
                        Movimiento(
                            id = row[0]?.toString() ?: "",
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
        } catch (e: Exception) {
            emptyList()
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
        } catch (e: Exception) {
            false
        }
    }
}
