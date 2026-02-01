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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.finanzasproactivas.data.model.Presupuesto
import com.finanzasproactivas.data.repository.CategoriasRepository
import com.finanzasproactivas.ui.components.MenuDrawer
import com.finanzasproactivas.ui.theme.*
import com.finanzasproactivas.ui.viewmodel.FinanzasViewModel
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PresupuestosScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel: FinanzasViewModel = viewModel { FinanzasViewModel(context.applicationContext as android.app.Application) }
    
    val movimientos by viewModel.movimientos.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var showMenu by remember { mutableStateOf(false) }
    var showAddPresupuestoDialog by remember { mutableStateOf(false) }
    
    val categoriasRepo = remember { CategoriasRepository(context) }
    val categorias = remember { categoriasRepo.obtenerCategorias() }
    
    val ahora = Calendar.getInstance()
    val mesActual = ahora.get(Calendar.MONTH)
    val añoActual = ahora.get(Calendar.YEAR)
    
    var presupuestos by remember { mutableStateOf(viewModel.obtenerPresupuestosDelMes()) }
    
    // Calcular gastos por categoría del mes actual
    val gastosPorCategoria = movimientos
        .filter { it.tipo == com.finanzasproactivas.data.model.TipoMovimiento.GASTO }
        .filter { 
            val cal = Calendar.getInstance().apply { time = it.fecha }
            cal.get(Calendar.YEAR) == añoActual && cal.get(Calendar.MONTH) == mesActual
        }
        .groupBy { it.categoria }
        .mapValues { it.value.sumOf { m -> m.importe } }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("💰 Presupuestos") },
                actions = {
                    IconButton(onClick = { showAddPresupuestoDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Agregar presupuesto")
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
                            text = "RESUMEN DE PRESUPUESTOS",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        if (presupuestos.isEmpty()) {
                            Text(
                                "No hay presupuestos configurados. Agrega uno usando el botón +",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        } else {
                            presupuestos.forEach { presupuesto ->
                                val gasto = gastosPorCategoria[presupuesto.categoria] ?: 0.0
                                PresupuestoItemConLimite(
                                    presupuesto = presupuesto,
                                    gasto = gasto,
                                    onEliminar = {
                                        viewModel.eliminarPresupuesto(presupuesto.categoria, presupuesto.mes, presupuesto.año)
                                        presupuestos = viewModel.obtenerPresupuestosDelMes()
                                    }
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }
                    }
                }
                
                // Mostrar categorías sin presupuesto
                if (gastosPorCategoria.isNotEmpty()) {
                    val categoriasSinPresupuesto = gastosPorCategoria.keys.filter { categoria ->
                        !presupuestos.any { it.categoria == categoria }
                    }
                    
                    if (categoriasSinPresupuesto.isNotEmpty()) {
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
                                    text = "CATEGORÍAS SIN PRESUPUESTO",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted
                                )
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                categoriasSinPresupuesto.forEach { categoria ->
                                    val gasto = gastosPorCategoria[categoria] ?: 0.0
                                    PresupuestoItem(categoria, gasto)
                                    Spacer(modifier = Modifier.height(12.dp))
                                }
                            }
                        }
                    }
                }
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
        
        // Dialog para agregar presupuesto
        if (showAddPresupuestoDialog) {
            AddPresupuestoDialog(
                categorias = categorias,
                mes = mesActual,
                año = añoActual,
                onDismiss = { showAddPresupuestoDialog = false },
                onSave = { presupuesto ->
                    viewModel.guardarPresupuesto(presupuesto)
                    presupuestos = viewModel.obtenerPresupuestosDelMes()
                    showAddPresupuestoDialog = false
                }
            )
        }
    }
}

@Composable
fun PresupuestoItemConLimite(
    presupuesto: Presupuesto,
    gasto: Double,
    onEliminar: () -> Unit
) {
    val formato = NumberFormat.getCurrencyInstance(Locale("es", "ES"))
    val porcentaje = (gasto / presupuesto.limite * 100).toFloat().coerceIn(0f, 100f)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBg)
    ) {
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        presupuesto.categoria,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    Text(
                        "Gasto: ${formato.format(gasto)} / Límite: ${formato.format(presupuesto.limite)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "${porcentaje.toInt()}%",
                        style = MaterialTheme.typography.titleLarge,
                        color = if (porcentaje > 100) RedError else if (porcentaje > 80) YellowWarning else GreenSuccess
                    )
                    IconButton(onClick = onEliminar) {
                        Icon(Icons.Default.Delete, "Eliminar", tint = RedError)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LinearProgressIndicator(
                progress = porcentaje / 100f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = if (porcentaje > 100) RedError else if (porcentaje > 80) YellowWarning else GreenSuccess,
                trackColor = CardBorder
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPresupuestoDialog(
    categorias: List<String>,
    mes: Int,
    año: Int,
    onDismiss: () -> Unit,
    onSave: (Presupuesto) -> Unit
) {
    var categoriaSeleccionada by remember { mutableStateOf(categorias.firstOrNull() ?: "") }
    var limiteTexto by remember { mutableStateOf("") }
    var categoriaExpanded by remember { mutableStateOf(false) }
    val formato = NumberFormat.getCurrencyInstance(Locale("es", "ES"))
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Agregar Presupuesto") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Categoría
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = categoriaSeleccionada,
                        onValueChange = {},
                        label = { Text("Categoría") },
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        trailingIcon = {
                            IconButton(onClick = { categoriaExpanded = true }) {
                                Icon(Icons.Default.ArrowDropDown, null)
                            }
                        }
                    )
                    DropdownMenu(
                        expanded = categoriaExpanded,
                        onDismissRequest = { categoriaExpanded = false }
                    ) {
                        categorias.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    categoriaSeleccionada = cat
                                    categoriaExpanded = false
                                }
                            )
                        }
                    }
                }
                
                // Límite
                OutlinedTextField(
                    value = limiteTexto,
                    onValueChange = { newValue ->
                        if (newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                            limiteTexto = newValue
                        }
                    },
                    label = { Text("Límite mensual (€)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = KeyboardType.Decimal
                    ),
                    singleLine = true,
                    leadingIcon = {
                        Icon(Icons.Default.Euro, null)
                    }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val limite = limiteTexto.toDoubleOrNull() ?: 0.0
                    if (categoriaSeleccionada.isNotEmpty() && limite > 0) {
                        val presupuesto = Presupuesto(
                            categoria = categoriaSeleccionada,
                            limite = limite,
                            mes = mes,
                            año = año
                        )
                        onSave(presupuesto)
                    }
                },
                enabled = categoriaSeleccionada.isNotEmpty() && limiteTexto.toDoubleOrNull() ?: 0.0 > 0
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun PresupuestoItem(categoria: String, gasto: Double) {
    val formato = NumberFormat.getCurrencyInstance(Locale("es", "ES"))
    
    // Presupuesto sugerido basado en el gasto actual (120% del gasto promedio)
    val presupuesto = gasto * 1.2
    val porcentaje = (gasto / presupuesto * 100).toFloat().coerceIn(0f, 100f)
    
    Column {
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
                    "Gasto: ${formato.format(gasto)} / Presupuesto sugerido: ${formato.format(presupuesto)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            Text(
                "${porcentaje.toInt()}%",
                style = MaterialTheme.typography.bodyMedium,
                color = if (porcentaje > 80) RedError else if (porcentaje > 60) Primary else GreenSuccess
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        LinearProgressIndicator(
            progress = porcentaje / 100f,
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
            color = if (porcentaje > 80) RedError else if (porcentaje > 60) Primary else GreenSuccess,
            trackColor = CardBorder
        )
    }
}
