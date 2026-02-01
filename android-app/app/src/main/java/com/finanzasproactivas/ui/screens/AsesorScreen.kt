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
import com.finanzasproactivas.ui.components.*
import com.finanzasproactivas.ui.navigation.Screen
import com.finanzasproactivas.ui.viewmodel.FinanzasViewModel
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AsesorScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel: FinanzasViewModel = viewModel { FinanzasViewModel(context.applicationContext as android.app.Application) }
    
    val movimientos by viewModel.movimientos.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val periodoActual by viewModel.periodoActual.collectAsState()
    
    var showNewMovementDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    
    val formato = NumberFormat.getCurrencyInstance(Locale("es", "ES"))
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Finanzas Proactivas €") },
                actions = {
                    IconButton(onClick = { showNewMovementDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Nuevo")
                    }
                    IconButton(onClick = { showMenu = !showMenu }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menú")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = com.finanzasproactivas.ui.theme.BackgroundDark,
                    titleContentColor = com.finanzasproactivas.ui.theme.TextPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Mostrar error si existe
            error?.let {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = com.finanzasproactivas.ui.theme.RedError.copy(alpha = 0.2f)
                    )
                ) {
                    Text(
                        text = it,
                        modifier = Modifier.padding(16.dp),
                        color = com.finanzasproactivas.ui.theme.RedError
                    )
                }
            }
            
            // Indicador de carga
            if (isLoading && movimientos.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                // Métricas con datos reales y selector de período
                MetricsSection(
                    ingresos = viewModel.ingresosDelPeriodo,
                    gastos = viewModel.gastosDelPeriodo,
                    ahorro = viewModel.capacidadAhorro,
                    gastoPromedio = viewModel.gastoPromedio,
                    ahorroNecesarioMensual = viewModel.ahorroNecesarioMensual,
                    tasaAhorro = viewModel.tasaAhorro,
                    saludFinanciera = viewModel.saludFinanciera,
                    gastoPromedioDiario = viewModel.gastoPromedioDiario,
                    proyeccionGastoFinMes = viewModel.proyeccionGastoFinMes,
                    gastosFijos = viewModel.gastosFijos,
                    gastosVariables = viewModel.gastosVariables,
                    diasRestantes = viewModel.diasRestantesMes,
                    periodoActual = periodoActual,
                    onCambioPeriodo = { periodo -> 
                        viewModel.cambiarPeriodo(periodo)
                    },
                    onClickIndicador = { indicadorId ->
                        navController.navigate("detalle_indicador/$indicadorId")
                    }
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Recomendaciones con presupuestos cerca del límite
                RecommendationsSection(
                    presupuestosCercaDelLimite = viewModel.obtenerPresupuestosCercaDelLimite()
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Análisis de patrones
                PatternAnalysisSection(movimientos = movimientos)
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Chat con Gemini
                ChatSection(movimientos = movimientos)
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
        
        // Dialog de nuevo movimiento
        if (showNewMovementDialog) {
            NewMovementDialog(
                onDismiss = { showNewMovementDialog = false },
                onSave = { movimiento ->
                    viewModel.guardarMovimiento(movimiento)
                    showNewMovementDialog = false
                }
            )
        }
    }
}
