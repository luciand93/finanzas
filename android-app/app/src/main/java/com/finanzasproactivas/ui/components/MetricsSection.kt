package com.finanzasproactivas.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.finanzasproactivas.ui.theme.*

@Composable
fun MetricsSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Métricas del Mes",
            style = MaterialTheme.typography.headlineMedium,
            color = TextPrimary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                icon = Icons.Default.TrendingUp,
                label = "Ingresos del Mes",
                value = "+€2,500.00",
                iconColor = GreenSuccess
            )
            
            MetricCard(
                icon = Icons.Default.Calculate,
                label = "Gasto Promedio",
                value = "€1,200.00",
                iconColor = Primary
            )
            
            MetricCard(
                icon = Icons.Default.Savings,
                label = "Capacidad Ahorro",
                value = "€1,300.00",
                iconColor = GreenSuccess
            )
            
            MetricCard(
                icon = Icons.Default.AccountBalanceWallet,
                label = "Hucha Anual",
                value = "€500.00",
                iconColor = Primary
            )
            
            MetricCard(
                icon = Icons.Default.CalendarMonth,
                label = "Promedio Mensual",
                value = "€1,200.00",
                iconColor = TextSecondary
            )
            
            MetricCardHighlight(
                label = "Acumulado Conjunto",
                value = "€2,400.00",
                trend = "+4.2%"
            )
        }
    }
}

@Composable
fun MetricCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    iconColor: androidx.compose.ui.graphics.Color
) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .height(140.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardBg
        ),
        border = CardDefaults.outlinedCardBorder(enabled = true).copy(
            brush = null,
            width = 1.dp,
            shape = MaterialTheme.shapes.medium
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
            }
            
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary
            )
        }
    }
}

@Composable
fun MetricCardHighlight(
    label: String,
    value: String,
    trend: String
) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .height(140.dp),
        colors = CardDefaults.cardColors(
            containerColor = Primary.copy(alpha = 0.1f)
        ),
        border = CardDefaults.outlinedCardBorder(enabled = true).copy(
            brush = null,
            width = 1.dp,
            shape = MaterialTheme.shapes.medium
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = Primary
            )
            
            Text(
                text = value,
                style = MaterialTheme.typography.displaySmall,
                color = TextPrimary
            )
            
            Row(
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.TrendingUp,
                    contentDescription = null,
                    tint = GreenSuccess,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = trend,
                    style = MaterialTheme.typography.labelLarge,
                    color = GreenSuccess
                )
            }
        }
    }
}
