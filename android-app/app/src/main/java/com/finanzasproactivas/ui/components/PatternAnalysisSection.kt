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
fun PatternAnalysisSection() {
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
                
                // Ejemplo de categorías
                CategoryProgressBar("Restaurantes", 35f)
                Spacer(modifier = Modifier.height(12.dp))
                CategoryProgressBar("Transporte", 25f)
                Spacer(modifier = Modifier.height(12.dp))
                CategoryProgressBar("Compras", 20f)
                Spacer(modifier = Modifier.height(12.dp))
                CategoryProgressBar("Ocio", 15f)
                Spacer(modifier = Modifier.height(12.dp))
                CategoryProgressBar("Otros", 5f)
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
                    DayBar("L", 60f)
                    DayBar("M", 80f)
                    DayBar("X", 70f)
                    DayBar("J", 90f)
                    DayBar("V", 100f)
                    DayBar("S", 120f, isWeekend = true)
                    DayBar("D", 110f, isWeekend = true)
                }
            }
        }
    }
}

@Composable
fun CategoryProgressBar(categoria: String, porcentaje: Float) {
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
                text = "${porcentaje.toInt()}%",
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
