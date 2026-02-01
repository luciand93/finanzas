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
fun PatternAnalysisSection(movimientos: List<com.finanzasproactivas.data.model.Movimiento> = emptyList()) {
    val formato = java.text.NumberFormat.getCurrencyInstance(java.util.Locale("es", "ES"))
    
    // Calcular top categorías
    val topCategorias = movimientos
        .filter { it.tipo == com.finanzasproactivas.data.model.TipoMovimiento.GASTO }
        .groupBy { it.categoria }
        .mapValues { it.value.sumOf { m -> m.importe } }
        .toList()
        .sortedByDescending { it.second }
        .take(5)
    
    val totalGastos = topCategorias.sumOf { it.second }
    
    // Calcular gastos por día de la semana
    val gastosPorDia = movimientos
        .filter { it.tipo == com.finanzasproactivas.data.model.TipoMovimiento.GASTO }
        .groupBy { 
            val cal = java.util.Calendar.getInstance()
            cal.time = it.fecha
            cal.get(java.util.Calendar.DAY_OF_WEEK)
        }
        .mapValues { it.value.sumOf { m -> m.importe } }
    
    val maxGastoDia = gastosPorDia.values.maxOrNull() ?: 1.0
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
                imageVector = Icons.Default.Analytics,
                contentDescription = null,
                tint = Primary
            )
            Text(
                text = "Análisis de Patrones",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Top 5 Categorías
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = CardBg
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "TOP 5 CATEGORÍAS",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Mostrar top categorías reales
                if (topCategorias.isEmpty()) {
                    Text(
                        text = "No hay datos de gastos disponibles",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                } else {
                    topCategorias.forEachIndexed { index, (categoria, monto) ->
                        val porcentaje = if (totalGastos > 0) (monto / totalGastos * 100).toFloat() else 0f
                        CategoryProgressBar(categoria, porcentaje, formato.format(monto))
                        if (index < topCategorias.size - 1) {
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Gastos por día
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = CardBg
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "GASTOS POR DÍA",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Días de la semana: L=2, M=3, X=4, J=5, V=6, S=7, D=1
                    val dias = listOf(
                        "L" to 2, "M" to 3, "X" to 4, "J" to 5,
                        "V" to 6, "S" to 7, "D" to 1
                    )
                    dias.forEach { (dia, diaSemana) ->
                        val gasto = gastosPorDia[diaSemana] ?: 0.0
                        val altura = (gasto / maxGastoDia * 100).toFloat().coerceIn(0f, 100f)
                        DayBar(dia, altura, isWeekend = diaSemana >= 7)
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryProgressBar(categoria: String, porcentaje: Float, monto: String = "") {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = categoria,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary
            )
            Text(
                text = if (monto.isNotEmpty()) "$monto (${porcentaje.toInt()}%)" else "${porcentaje.toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        LinearProgressIndicator(
            progress = porcentaje / 100f,
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
            color = Primary,
            trackColor = CardBorder
        )
    }
}

@Composable
fun DayBar(dia: String, altura: Float, isWeekend: Boolean = false) {
    Column(
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .width(32.dp)
                .height(96.dp),
            contentAlignment = androidx.compose.ui.Alignment.BottomCenter
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                color = CardBorder,
                shape = MaterialTheme.shapes.extraSmall
            ) {}
            
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(altura / 100f),
                color = if (isWeekend) RedError.copy(alpha = 0.4f) else Primary.copy(alpha = 0.4f),
                shape = MaterialTheme.shapes.extraSmall
            ) {}
        }
        
        Text(
            text = dia,
            style = MaterialTheme.typography.labelSmall,
            color = if (isWeekend) RedError else TextMuted
        )
    }
}
