package com.finanzasproactivas.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.finanzasproactivas.ui.theme.*

@Composable
fun RecommendationsSection(
    presupuestosCercaDelLimite: List<Pair<com.finanzasproactivas.data.model.Presupuesto, Double>> = emptyList()
) {
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
                imageVector = Icons.Default.Lightbulb,
                contentDescription = null,
                tint = YellowWarning
            )
            Text(
                text = "Recomendaciones Inteligentes",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (presupuestosCercaDelLimite.isEmpty()) {
            RecommendationCard(
                tipo = "info",
                titulo = "Gastos bajo control",
                descripcion = "Tus gastos están dentro del presupuesto este mes"
            )
        } else {
            presupuestosCercaDelLimite.forEach { (presupuesto, porcentaje) ->
                val tipo = when {
                    porcentaje >= 100 -> "error"
                    porcentaje >= 90 -> "error"
                    porcentaje >= 80 -> "warning"
                    else -> "info"
                }
                val mensaje = when {
                    porcentaje >= 100 -> "Has superado el presupuesto"
                    porcentaje >= 90 -> "Casi alcanzaste el límite"
                    else -> "Estás cerca del límite"
                }
                RecommendationCard(
                    tipo = tipo,
                    titulo = "⚠️ ${presupuesto.categoria}",
                    descripcion = "$mensaje: ${porcentaje.toInt()}% del presupuesto (${String.format("%.0f", presupuesto.limite)}€)"
                )
            }
        }
    }
}

@Composable
fun RecommendationCard(
    tipo: String,
    titulo: String,
    descripcion: String
) {
    val iconColor = when (tipo) {
        "info" -> BlueInfo
        "warning" -> YellowWarning
        "error" -> RedError
        else -> TextSecondary
    }
    
    val icon = when (tipo) {
        "info" -> Icons.Default.Info
        "warning" -> Icons.Default.Warning
        "error" -> Icons.Default.Error
        else -> Icons.Default.Info
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardBg
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = MaterialTheme.shapes.medium,
                color = iconColor.copy(alpha = 0.2f)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = descripcion,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }
    }
}
