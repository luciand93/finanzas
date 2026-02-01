package com.finanzasproactivas.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.finanzasproactivas.data.repository.CategoriasRepository
import com.finanzasproactivas.ui.components.MenuDrawer
import com.finanzasproactivas.ui.theme.*
import com.finanzasproactivas.ui.viewmodel.FinanzasViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel: FinanzasViewModel = viewModel { FinanzasViewModel(context.applicationContext as android.app.Application) }
    val categoriasRepo = remember { CategoriasRepository(context) }
    
    val movimientos by viewModel.movimientos.collectAsState()
    var categorias by remember { mutableStateOf(categoriasRepo.obtenerCategorias()) }
    var showMenu by remember { mutableStateOf(false) }
    var nuevaCategoria by remember { mutableStateOf("") }
    var showAddCategoriaDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("⚙️ Configuración") },
                actions = {
                    IconButton(onClick = { showMenu = !showMenu }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menú")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Información de la app
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
                        text = "INFORMACIÓN DE LA APLICACIÓN",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    ConfigItem("Versión", "1.0.0")
                    ConfigItem("Total de movimientos", "${movimientos.size}")
                    ConfigItem("Google Sheets", "Conectado")
                    ConfigItem("Gemini AI", "Conectado")
                }
            }
            
            // Configuración de APIs
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
                        text = "CONFIGURACIÓN DE APIs",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Google Sheets API",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    Text(
                        text = "Configurado en: GoogleSheetsRepository.kt",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = "Gemini AI API",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    Text(
                        text = "Configurado en: GeminiRepository.kt",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedButton(
                        onClick = { /* TODO: Abrir documentación */ },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Info, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Ver documentación")
                    }
                }
            }
            
            // Gestión de Categorías
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Text(
                            text = "CATEGORÍAS",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                        IconButton(onClick = { showAddCategoriaDialog = true }) {
                            Icon(Icons.Default.Add, "Agregar categoría", tint = Primary)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    categorias.forEach { categoria ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            Text(
                                text = categoria,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary
                            )
                            IconButton(
                                onClick = {
                                    categoriasRepo.eliminarCategoria(categoria)
                                    categorias = categoriasRepo.obtenerCategorias()
                                }
                            ) {
                                Icon(Icons.Default.Delete, "Eliminar", tint = RedError)
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedButton(
                        onClick = {
                            categoriasRepo.resetearCategorias()
                            categorias = categoriasRepo.obtenerCategorias()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Restore, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Restaurar categorías por defecto")
                    }
                }
            }
            
            // Generación de Movimientos Mensuales
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
                        text = "GENERACIÓN DE MOVIMIENTOS MENSUALES",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Los movimientos mensuales se generan automáticamente al inicio de cada mes. Puedes forzar la generación para un mes específico.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    var showGenerarDialog by remember { mutableStateOf(false) }
                    
                    OutlinedButton(
                        onClick = { showGenerarDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.CalendarMonth, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Generar Movimientos Mensuales")
                    }
                    
                    if (showGenerarDialog) {
                        GenerarMovimientosDialog(
                            onDismiss = { showGenerarDialog = false },
                            onGenerar = { mes, año ->
                                viewModel.generarMovimientosMensuales(mes, año)
                                showGenerarDialog = false
                            }
                        )
                    }
                }
            }
            
            // Acciones
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
                        text = "ACCIONES",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedButton(
                        onClick = { viewModel.cargarMovimientos() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Refresh, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Recargar datos")
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
        
        // Dialog para agregar categoría
        if (showAddCategoriaDialog) {
            AlertDialog(
                onDismissRequest = { showAddCategoriaDialog = false },
                title = { Text("Agregar Categoría") },
                text = {
                    OutlinedTextField(
                        value = nuevaCategoria,
                        onValueChange = { nuevaCategoria = it },
                        label = { Text("Nombre de la categoría") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (nuevaCategoria.isNotEmpty()) {
                                categoriasRepo.agregarCategoria(nuevaCategoria.trim())
                                categorias = categoriasRepo.obtenerCategorias()
                                nuevaCategoria = ""
                                showAddCategoriaDialog = false
                            }
                        },
                        enabled = nuevaCategoria.isNotEmpty()
                    ) {
                        Text("Agregar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddCategoriaDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

@Composable
fun ConfigItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenerarMovimientosDialog(
    onDismiss: () -> Unit,
    onGenerar: (Int, Int) -> Unit
) {
    val ahora = Calendar.getInstance()
    val siguienteMes = Calendar.getInstance().apply {
        add(Calendar.MONTH, 1)
    }
    
    var mesSeleccionado by remember { mutableStateOf(siguienteMes.get(Calendar.MONTH)) }
    var añoSeleccionado by remember { mutableStateOf(siguienteMes.get(Calendar.YEAR)) }
    var mesExpanded by remember { mutableStateOf(false) }
    var añoExpanded by remember { mutableStateOf(false) }
    
    val meses = listOf(
        "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
        "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    )
    val años = (ahora.get(Calendar.YEAR)..ahora.get(Calendar.YEAR) + 2).toList()
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Generar Movimientos Mensuales") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Selecciona el mes para el cual generar los movimientos mensuales recurrentes:",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Mes
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = meses[mesSeleccionado],
                            onValueChange = {},
                            label = { Text("Mes") },
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                IconButton(onClick = { mesExpanded = true }) {
                                    Icon(Icons.Default.ArrowDropDown, null)
                                }
                            }
                        )
                        DropdownMenu(
                            expanded = mesExpanded,
                            onDismissRequest = { mesExpanded = false }
                        ) {
                            meses.forEachIndexed { index, mes ->
                                DropdownMenuItem(
                                    text = { Text(mes) },
                                    onClick = {
                                        mesSeleccionado = index
                                        mesExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    
                    // Año
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = añoSeleccionado.toString(),
                            onValueChange = {},
                            label = { Text("Año") },
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                IconButton(onClick = { añoExpanded = true }) {
                                    Icon(Icons.Default.ArrowDropDown, null)
                                }
                            }
                        )
                        DropdownMenu(
                            expanded = añoExpanded,
                            onDismissRequest = { añoExpanded = false }
                        ) {
                            años.forEach { año ->
                                DropdownMenuItem(
                                    text = { Text(año.toString()) },
                                    onClick = {
                                        añoSeleccionado = año
                                        añoExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onGenerar(mesSeleccionado, añoSeleccionado) }
            ) {
                Text("Generar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
