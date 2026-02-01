package com.finanzasproactivas.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.finanzasproactivas.ui.theme.*
import com.finanzasproactivas.ui.viewmodel.PeriodoEstadisticas

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MetricsSection(
    ingresos: Double = 0.0,
    gastos: Double = 0.0,
    ahorro: Double = 0.0,
    gastoPromedio: Double = 0.0,
    ahorroNecesarioMensual: Double = 0.0,
    tasaAhorro: Double = 0.0,
    saludFinanciera: Int = 50,
    gastoPromedioDiario: Double = 0.0,
    proyeccionGastoFinMes: Double = 0.0,
    gastosFijos: Double = 0.0,
    gastosVariables: Double = 0.0,
    diasRestantes: Int = 0,
    periodoActual: PeriodoEstadisticas = PeriodoEstadisticas.MES_ACTUAL,
    onCambioPeriodo: (PeriodoEstadisticas) -> Unit = {},
    onClickIndicador: (String) -> Unit = {}
) {
    val formato = java.text.NumberFormat.getCurrencyInstance(java.util.Locale("es", "ES"))
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (periodoActual == PeriodoEstadisticas.MES_ACTUAL) "Métricas del Mes" else "Métricas Generales",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary
            )
            
            // Selector de período
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                FilterChip(
                    selected = periodoActual == PeriodoEstadisticas.MES_ACTUAL,
                    onClick = { onCambioPeriodo(PeriodoEstadisticas.MES_ACTUAL) },
                    label = { Text("Mes", style = MaterialTheme.typography.labelSmall) },
                    leadingIcon = {
                        if (periodoActual == PeriodoEstadisticas.MES_ACTUAL) {
                            Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
                        }
                    }
                )
                FilterChip(
                    selected = periodoActual == PeriodoEstadisticas.GENERAL,
                    onClick = { onCambioPeriodo(PeriodoEstadisticas.GENERAL) },
                    label = { Text("General", style = MaterialTheme.typography.labelSmall) },
                    leadingIcon = {
                        if (periodoActual == PeriodoEstadisticas.GENERAL) {
                            Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
                        }
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Balance principal
            MetricCardHighlight(
                label = if (periodoActual == PeriodoEstadisticas.MES_ACTUAL) "Balance Mensual" else "Balance Total",
                value = formato.format(ahorro),
                trend = if (ahorro >= 0) "+${String.format("%.1f", (ahorro / ingresos.coerceAtLeast(1.0)) * 100)}%" else "-${String.format("%.1f", (ahorro / ingresos.coerceAtLeast(1.0)) * 100)}%",
                onClick = { onClickIndicador("balance") }
            )
            
            // Salud financiera
            MetricCard(
                icon = Icons.Default.Favorite,
                label = "Salud Financiera",
                value = "$saludFinanciera/100",
                iconColor = when {
                    saludFinanciera >= 70 -> GreenSuccess
                    saludFinanciera >= 50 -> YellowWarning
                    else -> RedError
                },
                onClick = { onClickIndicador("salud") }
            )
            
            // Tasa de ahorro
            MetricCard(
                icon = Icons.Default.Percent,
                label = "Tasa de Ahorro",
                value = "${String.format("%.1f", tasaAhorro)}%",
                iconColor = when {
                    tasaAhorro >= 20 -> GreenSuccess
                    tasaAhorro >= 10 -> YellowWarning
                    else -> RedError
                },
                onClick = { onClickIndicador("tasa_ahorro") }
            )
            
            MetricCard(
                icon = Icons.Default.TrendingUp,
                label = if (periodoActual == PeriodoEstadisticas.MES_ACTUAL) "Ingresos del Mes" else "Ingresos Totales",
                value = "+${formato.format(ingresos)}",
                iconColor = GreenSuccess,
                onClick = { onClickIndicador("ingresos") }
            )
            
            MetricCard(
                icon = Icons.Default.CalendarMonth,
                label = if (periodoActual == PeriodoEstadisticas.MES_ACTUAL) "Gastos del Mes" else "Gastos Totales",
                value = formato.format(gastos),
                iconColor = Primary,
                onClick = { onClickIndicador("gastos") }
            )
            
            if (periodoActual == PeriodoEstadisticas.MES_ACTUAL) {
                MetricCard(
                    icon = Icons.Default.TrendingDown,
                    label = "Gasto Diario",
                    value = formato.format(gastoPromedioDiario),
                    iconColor = Primary,
                    onClick = { onClickIndicador("gasto_diario") }
                )
                
                MetricCard(
                    icon = Icons.Default.Savings,
                    label = "Proyección Fin Mes",
                    value = formato.format(proyeccionGastoFinMes),
                    iconColor = if (proyeccionGastoFinMes <= ingresos) GreenSuccess else RedError,
                    onClick = { onClickIndicador("proyeccion") }
                )
                
                if (diasRestantes > 0) {
                    MetricCard(
                        icon = Icons.Default.CalendarToday,
                        label = "Días Restantes",
                        value = diasRestantes.toString(),
                        iconColor = TextSecondary,
                        onClick = { onClickIndicador("dias_restantes") }
                    )
                }
            }
            
            MetricCard(
                icon = Icons.Default.Lock,
                label = "Gastos Fijos",
                value = formato.format(gastosFijos),
                iconColor = RedError,
                onClick = { onClickIndicador("gastos_fijos") }
            )
            
            MetricCard(
                icon = Icons.Default.ShoppingCart,
                label = "Gastos Variables",
                value = formato.format(gastosVariables),
                iconColor = YellowWarning,
                onClick = { onClickIndicador("gastos_variables") }
            )
            
            MetricCard(
                icon = Icons.Default.Calculate,
                label = "Gasto Promedio",
                value = formato.format(gastoPromedio),
                iconColor = Primary,
                onClick = { onClickIndicador("gasto_promedio") }
            )
            
            MetricCard(
                icon = Icons.Default.AccountBalanceWallet,
                label = "Ahorro Necesario/Mes",
                value = formato.format(ahorroNecesarioMensual),
                iconColor = if (ahorroNecesarioMensual > 0) YellowWarning else TextSecondary,
                onClick = { onClickIndicador("ahorro_necesario") }
            )
        }
    }
}

@Composable
fun MetricCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    iconColor: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .height(140.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = CardBg
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = CardBorder
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
                    color = TextSecondary,
                    maxLines = 2
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
    trend: String,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .width(220.dp)
            .height(140.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = Primary.copy(alpha = 0.1f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 2.dp,
            color = Primary
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
                    imageVector = if (trend.startsWith("+")) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                    contentDescription = null,
                    tint = if (trend.startsWith("+")) GreenSuccess else RedError,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = trend,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (trend.startsWith("+")) GreenSuccess else RedError
                )
            }
        }
    }
}
