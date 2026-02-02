package com.finanzasproactivas.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.finanzasproactivas.data.repository.GeminiRepository
import com.finanzasproactivas.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun ChatSection(movimientos: List<com.finanzasproactivas.data.model.Movimiento> = emptyList()) {
    var pregunta by remember { mutableStateOf("") }
    val mensajes = remember { mutableStateListOf<ChatMessage>() }
    var isLoading by remember { mutableStateOf(false) }
    
    val geminiRepo = remember { GeminiRepository().apply { initialize() } }
    val scope = rememberCoroutineScope()
    
    // Contexto financiero basado en datos reales
    val contextoFinanciero = remember(movimientos) {
        val ingresos = movimientos.filter { it.tipo == com.finanzasproactivas.data.model.TipoMovimiento.INGRESO }
            .sumOf { it.importe }
        val gastos = movimientos.filter { it.tipo == com.finanzasproactivas.data.model.TipoMovimiento.GASTO }
            .sumOf { it.importe }
        val categorias = movimientos
            .filter { it.tipo == com.finanzasproactivas.data.model.TipoMovimiento.GASTO }
            .groupBy { it.categoria }
            .mapValues { it.value.sumOf { m -> m.importe } }
            .toList()
            .sortedByDescending { it.second }
            .take(5)
            .joinToString(", ") { "${it.first} (€${String.format("%.2f", it.second)})" }
        
        """
        Resumen financiero basado en tus datos:
        - Total de movimientos: ${movimientos.size}
        - Ingresos totales: €${String.format("%.2f", ingresos)}
        - Gastos totales: €${String.format("%.2f", gastos)}
        - Ahorro disponible: €${String.format("%.2f", ingresos - gastos)}
        - Categorías principales: $categorias
        - Promedio por movimiento: €${String.format("%.2f", if (movimientos.isNotEmpty()) (ingresos + gastos) / movimientos.size else 0.0)}
        """.trimIndent()
    }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.SmartToy,
                contentDescription = null,
                tint = Primary
            )
            Text(
                text = "Asistente IA con Gemini",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = CardBg
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Área de mensajes
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    mensajes.forEach { mensaje ->
                        ChatBubble(mensaje)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Chips de sugerencias
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SuggestionChip("¿Cuánto he gastado este mes?") {
                        pregunta = it
                    }
                    SuggestionChip("¿Cuál es mi categoría con más gastos?") {
                        pregunta = it
                    }
                    SuggestionChip("¿Cómo van mis presupuestos?") {
                        pregunta = it
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Input de chat
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = pregunta,
                        onValueChange = { pregunta = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Pregunta a Gemini...") },
                        trailingIcon = {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Primary
                                )
                            } else {
                                IconButton(
                                    onClick = {
                                        if (pregunta.isNotEmpty() && !isLoading) {
                                            val preguntaActual = pregunta.trim()
                                            pregunta = ""
                                            mensajes.add(ChatMessage(texto = preguntaActual, esUsuario = true))
                                            isLoading = true
                                            scope.launch {
                                                try {
                                                    val respuesta = geminiRepo.chat(
                                                        pregunta = preguntaActual,
                                                        contexto = contextoFinanciero
                                                    )
                                                    mensajes.add(ChatMessage(texto = respuesta, esUsuario = false))
                                                } catch (e: Exception) {
                                                    mensajes.add(ChatMessage(
                                                        texto = "Error: ${e.message ?: "No se pudo obtener respuesta"}",
                                                        esUsuario = false
                                                    ))
                                                } finally {
                                                    isLoading = false
                                                }
                                            }
                                        }
                                    },
                                    enabled = !isLoading
                                ) {
                                    Icon(Icons.Default.Send, null)
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

data class ChatMessage(
    val texto: String,
    val esUsuario: Boolean
)

@Composable
fun ChatBubble(mensaje: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (mensaje.esUsuario) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            modifier = Modifier.widthIn(max = 280.dp),
            shape = MaterialTheme.shapes.medium,
            color = if (mensaje.esUsuario) Primary else CardBg
        ) {
            Text(
                text = mensaje.texto,
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = if (mensaje.esUsuario) TextPrimary else TextPrimary
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuggestionChip(texto: String, onClick: (String) -> Unit) {
    FilterChip(
        selected = false,
        onClick = { onClick(texto) },
        label = { Text(texto, style = MaterialTheme.typography.labelSmall) }
    )
}
