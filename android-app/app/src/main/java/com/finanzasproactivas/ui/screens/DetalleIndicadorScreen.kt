package com.finanzasproactivas.ui.screens

import android.app.Application
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.finanzasproactivas.ui.navigation.Screen
import com.finanzasproactivas.ui.theme.*
import com.finanzasproactivas.ui.viewmodel.FinanzasViewModel
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleIndicadorScreen(
    navController: NavController,
    indicadorId: String
) {
    val context = LocalContext.current
    val app = context.applicationContext as Application
    val parentEntry = remember(navController) {
        try { navController.getBackStackEntry(Screen.Asesor.route) } catch (_: Exception) { null }
    }
    val viewModel: FinanzasViewModel = if (parentEntry != null) {
        viewModel(
            viewModelStoreOwner = parentEntry,
            factory = object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return FinanzasViewModel(app) as T
                }
            }
        )
    } else {
        viewModel(factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return FinanzasViewModel(app) as T
            }
        })
    }
    
    val movimientos by viewModel.movimientos.collectAsState()
    val periodoActual by viewModel.periodoActual.collectAsState()
    
    val formato = NumberFormat.getCurrencyInstance(Locale("es", "ES"))
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(getTituloIndicador(indicadorId)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundDark,
                    titleContentColor = TextPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            when (indicadorId) {
                "balance" -> DetalleBalance(viewModel, formato, periodoActual)
                "salud" -> DetalleSaludFinanciera(viewModel, formato)
                "tasa_ahorro" -> DetalleTasaAhorro(viewModel, formato)
                "ingresos" -> DetalleIngresos(viewModel, formato, periodoActual, movimientos)
                "gastos" -> DetalleGastos(viewModel, formato, periodoActual, movimientos)
                "gasto_diario" -> DetalleGastoDiario(viewModel, formato)
                "proyeccion" -> DetalleProyeccion(viewModel, formato)
                "gastos_fijos" -> DetalleGastosFijos(viewModel, formato, movimientos)
                "gastos_variables" -> DetalleGastosVariables(viewModel, formato, movimientos)
                "gasto_promedio" -> DetalleGastoPromedio(viewModel, formato)
                "ahorro_necesario" -> DetalleAhorroNecesario(viewModel, formato, movimientos)
                else -> {
                    Text("Indicador no encontrado", color = RedError)
                }
            }
        }
    }
}

fun getTituloIndicador(id: String): String {
    return when (id) {
        "balance" -> "Balance"
        "salud" -> "Salud Financiera"
        "tasa_ahorro" -> "Tasa de Ahorro"
        "ingresos" -> "Ingresos"
        "gastos" -> "Gastos"
        "gasto_diario" -> "Gasto Diario"
        "proyeccion" -> "Proyección Fin de Mes"
        "gastos_fijos" -> "Gastos Fijos"
        "gastos_variables" -> "Gastos Variables"
        "gasto_promedio" -> "Gasto Promedio"
        "ahorro_necesario" -> "Ahorro Necesario"
        "dias_restantes" -> "Días Restantes"
        else -> "Detalle"
    }
}

@Composable
fun DetalleBalance(viewModel: FinanzasViewModel, formato: NumberFormat, periodo: com.finanzasproactivas.ui.viewmodel.PeriodoEstadisticas) {
    val ingresos = viewModel.ingresosDelPeriodo
    val gastos = viewModel.gastosDelPeriodo
    val balance = viewModel.capacidadAhorro
    
    InfoCard(
        title = "Balance",
        icon = Icons.Default.AccountBalance,
        iconColor = if (balance >= 0) GreenSuccess else RedError
    ) {
        MetricRow("Ingresos", "+${formato.format(ingresos)}", GreenSuccess)
        MetricRow("Gastos", formato.format(gastos), RedError)
        Divider(modifier = Modifier.padding(vertical = 8.dp))
        MetricRow(
            "Balance", 
            formato.format(balance), 
            if (balance >= 0) GreenSuccess else RedError,
            isLarge = true
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (balance >= 0) {
            Text(
                "Excelente! Tienes un balance positivo. Considera invertir o ahorrar este excedente.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        } else {
            Text(
                "⚠️ Atención: Tus gastos superan tus ingresos. Revisa tus gastos y considera reducir gastos no esenciales.",
                style = MaterialTheme.typography.bodyMedium,
                color = RedError
            )
        }
    }
}

@Composable
fun DetalleSaludFinanciera(viewModel: FinanzasViewModel, formato: NumberFormat) {
    val salud = viewModel.saludFinanciera
    val tasaAhorro = viewModel.tasaAhorro
    val gastosFijos = viewModel.gastosFijos
    val ingresos = viewModel.ingresosDelPeriodo
    
    InfoCard(
        title = "Salud Financiera",
        icon = Icons.Default.Favorite,
        iconColor = when {
            salud >= 70 -> GreenSuccess
            salud >= 50 -> YellowWarning
            else -> RedError
        }
    ) {
        // Barra de progreso
        Text("Puntuación: $salud/100", style = MaterialTheme.typography.headlineSmall, color = TextPrimary)
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = salud / 100f,
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp),
            color = when {
                salud >= 70 -> GreenSuccess
                salud >= 50 -> YellowWarning
                else -> RedError
            },
            trackColor = CardBorder
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text("Factores:", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
        Spacer(modifier = Modifier.height(8.dp))
        
        MetricRow("Tasa de Ahorro", "${String.format("%.1f", tasaAhorro)}%", 
            when {
                tasaAhorro >= 20 -> GreenSuccess
                tasaAhorro >= 10 -> YellowWarning
                else -> RedError
            }
        )
        
        val porcentajeGastosFijos = if (ingresos > 0) (gastosFijos / ingresos * 100) else 0.0
        MetricRow("Gastos Fijos", "${String.format("%.1f", porcentajeGastosFijos)}% de ingresos",
            if (porcentajeGastosFijos < 50) GreenSuccess else YellowWarning
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            when {
                salud >= 70 -> "Tu salud financiera es excelente. Mantén estos hábitos."
                salud >= 50 -> "Tu salud financiera es aceptable. Hay margen de mejora."
                else -> "Tu salud financiera necesita atención. Revisa tus gastos y aumenta tus ingresos si es posible."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
    }
}

@Composable
fun DetalleTasaAhorro(viewModel: FinanzasViewModel, formato: NumberFormat) {
    val tasaAhorro = viewModel.tasaAhorro
    val ingresos = viewModel.ingresosDelPeriodo
    val ahorro = viewModel.capacidadAhorro
    
    InfoCard(
        title = "Tasa de Ahorro",
        icon = Icons.Default.Percent,
        iconColor = when {
            tasaAhorro >= 20 -> GreenSuccess
            tasaAhorro >= 10 -> YellowWarning
            else -> RedError
        }
    ) {
        Text(
            "${String.format("%.1f", tasaAhorro)}%",
            style = MaterialTheme.typography.displayLarge,
            color = TextPrimary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        MetricRow("Ingresos", formato.format(ingresos), GreenSuccess)
        MetricRow("Ahorro", formato.format(ahorro), Primary)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text("Referencias:", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
        Text("• Excelente: 20% o más", style = MaterialTheme.typography.bodyMedium, color = GreenSuccess)
        Text("• Bueno: 10-20%", style = MaterialTheme.typography.bodyMedium, color = YellowWarning)
        Text("• Mejorable: menos de 10%", style = MaterialTheme.typography.bodyMedium, color = RedError)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            "La regla 50/30/20 sugiere ahorrar al menos el 20% de tus ingresos.",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
    }
}

@Composable
fun DetalleIngresos(viewModel: FinanzasViewModel, formato: NumberFormat, periodo: com.finanzasproactivas.ui.viewmodel.PeriodoEstadisticas, movimientos: List<com.finanzasproactivas.data.model.Movimiento>) {
    val ingresos = viewModel.ingresosDelPeriodo
    
    // Agrupar por categoría
    val ingresosPorCategoria = movimientos
        .filter { it.tipo == com.finanzasproactivas.data.model.TipoMovimiento.INGRESO }
        .groupBy { it.categoria }
        .mapValues { it.value.sumOf { m -> m.importe } }
        .toList()
        .sortedByDescending { it.second }
    
    InfoCard(
        title = "Ingresos",
        icon = Icons.Default.TrendingUp,
        iconColor = GreenSuccess
    ) {
        Text(
            formato.format(ingresos),
            style = MaterialTheme.typography.displayMedium,
            color = GreenSuccess
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (ingresosPorCategoria.isNotEmpty()) {
            Text("Por Categoría:", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
            Spacer(modifier = Modifier.height(8.dp))
            
            ingresosPorCategoria.forEach { (categoria, total) ->
                MetricRow(categoria, formato.format(total), GreenSuccess)
            }
        } else {
            Text("No hay ingresos registrados en este período.", color = TextSecondary)
        }
    }
}

@Composable
fun DetalleGastos(viewModel: FinanzasViewModel, formato: NumberFormat, periodo: com.finanzasproactivas.ui.viewmodel.PeriodoEstadisticas, movimientos: List<com.finanzasproactivas.data.model.Movimiento>) {
    val gastos = viewModel.gastosDelPeriodo
    val topCategorias = viewModel.getTopCategoriasGastos(10)
    
    InfoCard(
        title = "Gastos",
        icon = Icons.Default.ShoppingCart,
        iconColor = RedError
    ) {
        Text(
            formato.format(gastos),
            style = MaterialTheme.typography.displayMedium,
            color = RedError
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (topCategorias.isNotEmpty()) {
            Text("Top Categorías:", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
            Spacer(modifier = Modifier.height(8.dp))
            
            topCategorias.forEachIndexed { index, (categoria, total) ->
                val porcentaje = if (gastos > 0) (total / gastos * 100) else 0.0
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("${index + 1}. $categoria", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                    Column(horizontalAlignment = Alignment.End) {
                        Text(formato.format(total), style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                        Text("${String.format("%.1f", porcentaje)}%", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

@Composable
fun DetalleGastoDiario(viewModel: FinanzasViewModel, formato: NumberFormat) {
    val gastoDiario = viewModel.gastoPromedioDiario
    val gastosMes = viewModel.gastosDelPeriodo
    val ahora = Calendar.getInstance()
    val diaActual = ahora.get(Calendar.DAY_OF_MONTH)
    
    InfoCard(
        title = "Gasto Diario Promedio",
        icon = Icons.Default.CalendarToday,
        iconColor = Primary
    ) {
        Text(
            formato.format(gastoDiario),
            style = MaterialTheme.typography.displayMedium,
            color = TextPrimary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        MetricRow("Gastos acumulados", formato.format(gastosMes), RedError)
        MetricRow("Días transcurridos", diaActual.toString(), Primary)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            "Este es tu gasto promedio por día en lo que va del mes. Úsalo para planificar tus gastos futuros.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
    }
}

@Composable
fun DetalleProyeccion(viewModel: FinanzasViewModel, formato: NumberFormat) {
    val proyeccion = viewModel.proyeccionGastoFinMes
    val ingresos = viewModel.ingresosDelPeriodo
    val gastosActuales = viewModel.gastosDelPeriodo
    val diasRestantes = viewModel.diasRestantesMes
    
    InfoCard(
        title = "Proyección Fin de Mes",
        icon = Icons.Default.ShowChart,
        iconColor = if (proyeccion <= ingresos) GreenSuccess else RedError
    ) {
        Text(
            formato.format(proyeccion),
            style = MaterialTheme.typography.displayMedium,
            color = if (proyeccion <= ingresos) TextPrimary else RedError
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        MetricRow("Gastos actuales", formato.format(gastosActuales), TextPrimary)
        MetricRow("Ingresos del mes", formato.format(ingresos), GreenSuccess)
        MetricRow("Días restantes", diasRestantes.toString(), Primary)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (proyeccion <= ingresos) {
            val ahorroProyectado = ingresos - proyeccion
            Text(
                "Si mantienes este ritmo, ahorrarás ${formato.format(ahorroProyectado)} este mes.",
                style = MaterialTheme.typography.bodyMedium,
                color = GreenSuccess
            )
        } else {
            val deficit = proyeccion - ingresos
            Text(
                "⚠️ A este ritmo, gastarás ${formato.format(deficit)} más de lo que ingresas. Considera reducir gastos.",
                style = MaterialTheme.typography.bodyMedium,
                color = RedError
            )
        }
    }
}

@Composable
fun DetalleGastosFijos(viewModel: FinanzasViewModel, formato: NumberFormat, movimientos: List<com.finanzasproactivas.data.model.Movimiento>) {
    val gastosFijos = viewModel.gastosFijos
    val ingresos = viewModel.ingresosDelPeriodo
    val porcentaje = if (ingresos > 0) (gastosFijos / ingresos * 100) else 0.0
    
    InfoCard(
        title = "Gastos Fijos",
        icon = Icons.Default.Lock,
        iconColor = RedError
    ) {
        Text(
            formato.format(gastosFijos),
            style = MaterialTheme.typography.displayMedium,
            color = TextPrimary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        MetricRow("Porcentaje de ingresos", "${String.format("%.1f", porcentaje)}%", 
            if (porcentaje < 50) GreenSuccess else RedError
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            "Los gastos fijos son aquellos que se repiten cada mes o año (alquiler, suscripciones, seguros, etc.). Idealmente deberían ser menos del 50% de tus ingresos.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
    }
}

@Composable
fun DetalleGastosVariables(viewModel: FinanzasViewModel, formato: NumberFormat, movimientos: List<com.finanzasproactivas.data.model.Movimiento>) {
    val gastosVariables = viewModel.gastosVariables
    val gastosTotales = viewModel.gastosDelPeriodo
    val porcentaje = if (gastosTotales > 0) (gastosVariables / gastosTotales * 100) else 0.0
    
    InfoCard(
        title = "Gastos Variables",
        icon = Icons.Default.ShoppingCart,
        iconColor = YellowWarning
    ) {
        Text(
            formato.format(gastosVariables),
            style = MaterialTheme.typography.displayMedium,
            color = TextPrimary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        MetricRow("Porcentaje del total", "${String.format("%.1f", porcentaje)}%", Primary)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            "Los gastos variables son aquellos que cambian mes a mes (compras, ocio, restaurantes, etc.). Estos son los más fáciles de controlar y reducir si necesitas ahorrar más.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
    }
}

@Composable
fun DetalleGastoPromedio(viewModel: FinanzasViewModel, formato: NumberFormat) {
    val gastoPromedio = viewModel.gastoPromedio
    
    InfoCard(
        title = "Gasto Promedio por Movimiento",
        icon = Icons.Default.Calculate,
        iconColor = Primary
    ) {
        Text(
            formato.format(gastoPromedio),
            style = MaterialTheme.typography.displayMedium,
            color = TextPrimary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            "Este es el importe promedio de cada gasto que realizas. Te ayuda a entender tus patrones de consumo.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
    }
}

@Composable
fun DetalleAhorroNecesario(viewModel: FinanzasViewModel, formato: NumberFormat, movimientos: List<com.finanzasproactivas.data.model.Movimiento>) {
    val ahorroNecesario = viewModel.ahorroNecesarioMensual
    val capacidadAhorro = viewModel.capacidadAhorro
    
    // Obtener gastos anuales
    val gastosAnuales = movimientos
        .filter { it.tipo == com.finanzasproactivas.data.model.TipoMovimiento.GASTO }
        .filter { it.frecuencia == com.finanzasproactivas.data.model.Frecuencia.ANUAL }
        .groupBy { it.concepto }
        .mapValues { it.value.sumOf { m -> m.importe } }
        .toList()
        .sortedByDescending { it.second }
    
    InfoCard(
        title = "Ahorro Necesario Mensual",
        icon = Icons.Default.AccountBalanceWallet,
        iconColor = YellowWarning
    ) {
        Text(
            formato.format(ahorroNecesario),
            style = MaterialTheme.typography.displayMedium,
            color = TextPrimary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            "Debes reservar esta cantidad cada mes para cubrir tus gastos anuales (seguros, impuestos, etc.).",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (gastosAnuales.isNotEmpty()) {
            Text("Gastos Anuales:", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
            Spacer(modifier = Modifier.height(8.dp))
            
            gastosAnuales.forEach { (concepto, total) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(concepto, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                    Column(horizontalAlignment = Alignment.End) {
                        Text(formato.format(total), style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                        Text("${formato.format(total / 12)}/mes", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (capacidadAhorro >= ahorroNecesario) {
            Text(
                "✓ Tu capacidad de ahorro cubre tus gastos anuales.",
                style = MaterialTheme.typography.bodyMedium,
                color = GreenSuccess
            )
        } else {
            Text(
                "⚠️ Tu capacidad de ahorro no cubre los gastos anuales. Necesitas ahorrar más.",
                style = MaterialTheme.typography.bodyMedium,
                color = RedError
            )
        }
    }
}

@Composable
fun InfoCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: androidx.compose.ui.graphics.Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(32.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextPrimary
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            content()
        }
    }
}

@Composable
fun MetricRow(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color,
    isLarge: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = if (isLarge) MaterialTheme.typography.titleLarge else MaterialTheme.typography.bodyLarge,
            color = TextPrimary
        )
        Text(
            text = value,
            style = if (isLarge) MaterialTheme.typography.titleLarge else MaterialTheme.typography.bodyLarge,
            color = color
        )
    }
    Spacer(modifier = Modifier.height(4.dp))
}
