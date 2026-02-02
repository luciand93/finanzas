package com.finanzasproactivas.ui.screens

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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.finanzasproactivas.ui.components.MenuDrawer
import com.finanzasproactivas.ui.theme.*
import com.finanzasproactivas.ui.viewmodel.FinanzasViewModel
import com.finanzasproactivas.ui.viewmodel.PeriodoEstadisticas
import java.text.NumberFormat
import java.util.*

private fun filtrarPorPeriodo(movimientos: List<com.finanzasproactivas.data.model.Movimiento>, periodo: PeriodoEstadisticas): List<com.finanzasproactivas.data.model.Movimiento> {
    if (periodo == PeriodoEstadisticas.GENERAL) return movimientos
    val ahora = Calendar.getInstance()
    val mesActual = ahora.get(Calendar.MONTH)
    val añoActual = ahora.get(Calendar.YEAR)
    return movimientos.filter { m ->
        val cal = Calendar.getInstance().apply { time = m.fecha }
        cal.get(Calendar.YEAR) == añoActual && cal.get(Calendar.MONTH) == mesActual
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GraficosScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel: FinanzasViewModel = viewModel { FinanzasViewModel(context.applicationContext as android.app.Application) }
    
    val movimientos by viewModel.movimientos.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var showMenu by remember { mutableStateOf(false) }
    var periodoGraficos by remember { mutableStateOf(PeriodoEstadisticas.MES_ACTUAL) }
    
    val movimientosFiltrados = remember(movimientos, periodoGraficos) {
        filtrarPorPeriodo(movimientos, periodoGraficos)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📊 Gráficos") },
                actions = {
                    Row(
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        FilterChip(
                            selected = periodoGraficos == PeriodoEstadisticas.MES_ACTUAL,
                            onClick = { periodoGraficos = PeriodoEstadisticas.MES_ACTUAL },
                            label = { Text("Mes actual", style = MaterialTheme.typography.labelSmall) }
                        )
                        FilterChip(
                            selected = periodoGraficos == PeriodoEstadisticas.GENERAL,
                            onClick = { periodoGraficos = PeriodoEstadisticas.GENERAL },
                            label = { Text("General", style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                    IconButton(onClick = { showMenu = !showMenu }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menú")
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading && movimientos.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {
                // Gráfico de ingresos vs gastos
                GraficoIngresosGastos(movimientosFiltrados)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Gráfico por categorías
                GraficoPorCategorias(movimientosFiltrados)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Gráfico temporal
                GraficoTemporal(movimientosFiltrados)
            }
        }
        
        // Menú lateral
        if (showMenu) {
            MenuDrawer(
                onDismiss = { showMenu = false },
                onNavigate = { route ->
                    showMenu = false
                    navController.navigate(route)
                }
            )
        }
    }
}

@Composable
fun GraficoIngresosGastos(movimientos: List<com.finanzasproactivas.data.model.Movimiento>) {
    val formato = NumberFormat.getCurrencyInstance(Locale("es", "ES"))
    
    val ingresos = movimientos
        .filter { it.tipo == com.finanzasproactivas.data.model.TipoMovimiento.INGRESO }
        .sumOf { it.importe }
    
    val gastos = movimientos
        .filter { it.tipo == com.finanzasproactivas.data.model.TipoMovimiento.GASTO }
        .sumOf { it.importe }
    
    val total = ingresos + gastos
    val porcentajeIngresos = if (total > 0) (ingresos / total * 100).toFloat() else 0f
    val porcentajeGastos = if (total > 0) (gastos / total * 100).toFloat() else 0f
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "INGRESOS VS GASTOS",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.Start) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.TrendingUp, null, tint = GreenSuccess, modifier = Modifier.size(20.dp))
                        Text("Ingresos", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                    }
                    Text(
                        formato.format(ingresos),
                        style = MaterialTheme.typography.headlineMedium,
                        color = GreenSuccess
                    )
                    Text(
                        "${porcentajeIngresos.toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.TrendingDown, null, tint = RedError, modifier = Modifier.size(20.dp))
                        Text("Gastos", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                    }
                    Text(
                        formato.format(gastos),
                        style = MaterialTheme.typography.headlineMedium,
                        color = RedError
                    )
                    Text(
                        "${porcentajeGastos.toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Barras de progreso
            LinearProgressIndicator(
                progress = porcentajeIngresos / 100f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = GreenSuccess,
                trackColor = CardBorder
            )
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = porcentajeGastos / 100f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = RedError,
                trackColor = CardBorder
            )
        }
    }
}

@Composable
fun GraficoPorCategorias(movimientos: List<com.finanzasproactivas.data.model.Movimiento>) {
    val formato = NumberFormat.getCurrencyInstance(Locale("es", "ES"))
    
    val categorias = movimientos
        .filter { it.tipo == com.finanzasproactivas.data.model.TipoMovimiento.GASTO }
        .groupBy { it.categoria }
        .mapValues { it.value.sumOf { m -> m.importe } }
        .toList()
        .sortedByDescending { it.second }
        .take(10)
    
    val total = categorias.sumOf { it.second }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "GASTOS POR CATEGORÍA",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (categorias.isEmpty()) {
                Text(
                    "No hay datos de gastos",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            } else {
                categorias.forEach { (categoria, monto) ->
                    val porcentaje = if (total > 0) (monto / total * 100).toFloat() else 0f
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                categoria,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary
                            )
                            Text(
                                formato.format(monto),
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                        Text(
                            "${porcentaje.toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
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
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun GraficoTemporal(movimientos: List<com.finanzasproactivas.data.model.Movimiento>) {
    val formato = NumberFormat.getCurrencyInstance(Locale("es", "ES"))
    val fechaFormat = java.text.SimpleDateFormat("MMM", Locale.getDefault())
    
    val gastosPorMes = movimientos
        .filter { it.tipo == com.finanzasproactivas.data.model.TipoMovimiento.GASTO }
        .groupBy {
            val cal = Calendar.getInstance()
            cal.time = it.fecha
            cal.get(Calendar.MONTH)
        }
        .mapValues { it.value.sumOf { m -> m.importe } }
    
    val maxGasto = gastosPorMes.values.maxOrNull() ?: 1.0
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "GASTOS POR MES",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (gastosPorMes.isEmpty()) {
                Text(
                    "No hay datos disponibles",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    (0..11).forEach { mes ->
                        val gasto = gastosPorMes[mes] ?: 0.0
                        val altura = (gasto / maxGasto * 100).toFloat().coerceIn(0f, 100f)
                        
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(24.dp)
                                    .height(120.dp),
                                contentAlignment = Alignment.BottomCenter
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
                                    color = Primary.copy(alpha = 0.6f),
                                    shape = MaterialTheme.shapes.extraSmall
                                ) {}
                            }
                            
                            Text(
                                fechaFormat.format(Calendar.getInstance().apply { set(Calendar.MONTH, mes) }.time),
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                        }
                    }
                }
            }
        }
    }
}
