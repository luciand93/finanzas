package com.finanzasproactivas.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.finanzasproactivas.ui.theme.*

@Composable
fun ChatSection() {
    var pregunta by remember { mutableStateOf("") }
    var mensajes by remember { mutableStateOf<List<ChatMessage>>(emptyList()) }
    
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
                            IconButton(
                                onClick = {
                                    if (pregunta.isNotEmpty()) {
                                        mensajes = mensajes + ChatMessage(
                                            texto = pregunta,
                                            esUsuario = true
                                        )
                                        // Aquí se llamaría a Gemini
                                        pregunta = ""
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Send, null)
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

@Composable
fun SuggestionChip(texto: String, onClick: (String) -> Unit) {
    FilterChip(
        selected = false,
        onClick = { onClick(texto) },
        label = { Text(texto, style = MaterialTheme.typography.labelSmall) }
    )
}
