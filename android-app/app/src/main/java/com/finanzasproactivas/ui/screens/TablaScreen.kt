package com.finanzasproactivas.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.finanzasproactivas.data.model.*
import com.finanzasproactivas.data.repository.CategoriasRepository
import com.finanzasproactivas.ui.components.MenuDrawer
import com.finanzasproactivas.ui.theme.*
import com.finanzasproactivas.ui.viewmodel.FinanzasViewModel
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TablaScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel: FinanzasViewModel = viewModel { FinanzasViewModel(context.applicationContext as android.app.Application) }
    
    val movimientos by viewModel.movimientos.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    var showMenu by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf<Movimiento?>(null) }
    var showFiltros by remember { mutableStateOf(false) } // Ocultos por defecto
    var showDeleteConfirm by remember { mutableStateOf<Movimiento?>(null) }
    
    // Filtros
    val ahora = Calendar.getInstance()
    val mesActual = ahora.get(Calendar.MONTH)
    val añoActual = ahora.get(Calendar.YEAR)
    
    // Por defecto, mostrar todos los movimientos (fecha muy antigua hasta fecha futura)
    var fechaInicio by remember { 
        mutableStateOf(Calendar.getInstance().apply {
            set(Calendar.YEAR, 2000) // Año 2000 como inicio
            set(Calendar.MONTH, Calendar.JANUARY)
            set(Calendar.DAY_OF_MONTH, 1)
        }.time)
    }
    var fechaFin by remember { 
        mutableStateOf(Calendar.getInstance().apply {
            set(Calendar.YEAR, 2099) // Año 2099 como fin
            set(Calendar.MONTH, Calendar.DECEMBER)
            set(Calendar.DAY_OF_MONTH, 31)
        }.time)
    }
    var buscarTexto by remember { mutableStateOf("") }
    var importeMin by remember { mutableStateOf("") }
    var importeMax by remember { mutableStateOf("") }
    var filtroTipo by remember { mutableStateOf<TipoMovimiento?>(null) }
    var filtroFrecuencia by remember { mutableStateOf<Frecuencia?>(null) }
    var categoriaFiltro by remember { mutableStateOf("") }
    var categoriaExpanded by remember { mutableStateOf(false) }
    
    val categoriasRepo = remember { CategoriasRepository(context) }
    val categorias = remember { categoriasRepo.obtenerCategorias() }
    
    // Filtrar movimientos
    val movimientosFiltrados = remember(movimientos, fechaInicio, fechaFin, buscarTexto, importeMin, importeMax, filtroTipo, filtroFrecuencia, categoriaFiltro) {
        movimientos.filter { movimiento ->
            val cal = Calendar.getInstance().apply { time = movimiento.fecha }
            val fechaMovimiento = cal.time
            
            // Filtro por fecha
            val enRangoFecha = fechaMovimiento >= fechaInicio && fechaMovimiento <= fechaFin
            
            // Filtro por texto (descripción o concepto)
            val coincideTexto = buscarTexto.isEmpty() || 
                    movimiento.concepto.contains(buscarTexto, ignoreCase = true) ||
                    movimiento.categoria.contains(buscarTexto, ignoreCase = true)
            
            // Filtro por importe
            val importeMinVal = importeMin.toDoubleOrNull() ?: Double.NEGATIVE_INFINITY
            val importeMaxVal = importeMax.toDoubleOrNull() ?: Double.POSITIVE_INFINITY
            val enRangoImporte = movimiento.importe >= importeMinVal && movimiento.importe <= importeMaxVal
            
            // Filtro por tipo
            val coincideTipo = filtroTipo == null || movimiento.tipo == filtroTipo
            
            // Filtro por cadencia (frecuencia)
            val coincideFrecuencia = filtroFrecuencia == null || movimiento.frecuencia == filtroFrecuencia
            
            // Filtro por categoría
            val coincideCategoria = categoriaFiltro.isEmpty() || movimiento.categoria == categoriaFiltro
            
            enRangoFecha && coincideTexto && enRangoImporte && coincideTipo && coincideFrecuencia && coincideCategoria
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🔍 Tabla de Movimientos") },
                actions = {
                    IconButton(onClick = { showFiltros = !showFiltros }) {
                        Icon(
                            if (showFiltros) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (showFiltros) "Ocultar filtros" else "Mostrar filtros"
                        )
                    }
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
        ) {
            // Panel de filtros (ocultable)
            if (showFiltros) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBg)
                ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Filtros",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Filtro por fecha
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        
                        // Campo Desde con overlay clickeable
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = dateFormat.format(fechaInicio),
                                onValueChange = {},
                                label = { Text("Desde") },
                                readOnly = true,
                                modifier = Modifier.fillMaxWidth(),
                                leadingIcon = { Icon(Icons.Default.CalendarToday, null) }
                            )
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clickable {
                                        val datePicker = android.app.DatePickerDialog(
                                            context,
                                            { _, year, month, dayOfMonth ->
                                                fechaInicio = Calendar.getInstance().apply {
                                                    set(year, month, dayOfMonth)
                                                }.time
                                            },
                                            Calendar.getInstance().apply { time = fechaInicio }.get(Calendar.YEAR),
                                            Calendar.getInstance().apply { time = fechaInicio }.get(Calendar.MONTH),
                                            Calendar.getInstance().apply { time = fechaInicio }.get(Calendar.DAY_OF_MONTH)
                                        )
                                        datePicker.show()
                                    }
                            )
                        }
                        
                        // Campo Hasta con overlay clickeable
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = dateFormat.format(fechaFin),
                                onValueChange = {},
                                label = { Text("Hasta") },
                                readOnly = true,
                                modifier = Modifier.fillMaxWidth(),
                                leadingIcon = { Icon(Icons.Default.CalendarToday, null) }
                            )
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clickable {
                                        val datePicker = android.app.DatePickerDialog(
                                            context,
                                            { _, year, month, dayOfMonth ->
                                                fechaFin = Calendar.getInstance().apply {
                                                    set(year, month, dayOfMonth)
                                                }.time
                                            },
                                            Calendar.getInstance().apply { time = fechaFin }.get(Calendar.YEAR),
                                            Calendar.getInstance().apply { time = fechaFin }.get(Calendar.MONTH),
                                            Calendar.getInstance().apply { time = fechaFin }.get(Calendar.DAY_OF_MONTH)
                                        )
                                        datePicker.show()
                                    }
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Buscar por descripción
                    OutlinedTextField(
                        value = buscarTexto,
                        onValueChange = { buscarTexto = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Buscar por descripción o categoría") },
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        trailingIcon = {
                            if (buscarTexto.isNotEmpty()) {
                                IconButton(onClick = { buscarTexto = "" }) {
                                    Icon(Icons.Default.Clear, null)
                                }
                            }
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Filtro por importe
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = importeMin,
                            onValueChange = { newValue ->
                                if (newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                                    importeMin = newValue
                                }
                            },
                            label = { Text("Importe min") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            leadingIcon = { Icon(Icons.Default.Euro, null) }
                        )
                        
                        OutlinedTextField(
                            value = importeMax,
                            onValueChange = { newValue ->
                                if (newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                                    importeMax = newValue
                                }
                            },
                            label = { Text("Importe max") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            leadingIcon = { Icon(Icons.Default.Euro, null) }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Filtro por tipo
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = filtroTipo == null,
                            onClick = { filtroTipo = null },
                            label = { Text("Todos") }
                        )
                        FilterChip(
                            selected = filtroTipo == TipoMovimiento.INGRESO,
                            onClick = { filtroTipo = if (filtroTipo == TipoMovimiento.INGRESO) null else TipoMovimiento.INGRESO },
                            label = { Text("Ingresos") }
                        )
                        FilterChip(
                            selected = filtroTipo == TipoMovimiento.GASTO,
                            onClick = { filtroTipo = if (filtroTipo == TipoMovimiento.GASTO) null else TipoMovimiento.GASTO },
                            label = { Text("Gastos") }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Filtro por cadencia (frecuencia)
                    Text("Cadencia", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        FilterChip(
                            selected = filtroFrecuencia == null,
                            onClick = { filtroFrecuencia = null },
                            label = { Text("Todas") }
                        )
                        FilterChip(
                            selected = filtroFrecuencia == Frecuencia.PUNTUAL,
                            onClick = { filtroFrecuencia = if (filtroFrecuencia == Frecuencia.PUNTUAL) null else Frecuencia.PUNTUAL },
                            label = { Text("Puntual") }
                        )
                        FilterChip(
                            selected = filtroFrecuencia == Frecuencia.MENSUAL,
                            onClick = { filtroFrecuencia = if (filtroFrecuencia == Frecuencia.MENSUAL) null else Frecuencia.MENSUAL },
                            label = { Text("Mensual") }
                        )
                        FilterChip(
                            selected = filtroFrecuencia == Frecuencia.ANUAL,
                            onClick = { filtroFrecuencia = if (filtroFrecuencia == Frecuencia.ANUAL) null else Frecuencia.ANUAL },
                            label = { Text("Anual") }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Filtro por categoría
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = categoriaFiltro,
                            onValueChange = {},
                            label = { Text("Categoría") },
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Todas las categorías") },
                            trailingIcon = {
                                if (categoriaFiltro.isNotEmpty()) {
                                    IconButton(onClick = { categoriaFiltro = "" }) {
                                        Icon(Icons.Default.Clear, null)
                                    }
                                } else {
                                    IconButton(onClick = { categoriaExpanded = true }) {
                                        Icon(Icons.Default.ArrowDropDown, null)
                                    }
                                }
                            }
                        )
                        DropdownMenu(
                            expanded = categoriaExpanded,
                            onDismissRequest = { categoriaExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Todas") },
                                onClick = {
                                    categoriaFiltro = ""
                                    categoriaExpanded = false
                                }
                            )
                            categorias.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat) },
                                    onClick = {
                                        categoriaFiltro = cat
                                        categoriaExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                }
            }
            
            // Lista de movimientos
            if (isLoading && movimientos.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (movimientosFiltrados.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Inbox,
                            null,
                            modifier = Modifier.size(64.dp),
                            tint = TextSecondary
                        )
                        Text(
                            "No hay movimientos que coincidan con los filtros",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextSecondary
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Text(
                            "${movimientosFiltrados.size} movimiento(s) encontrado(s)",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    items(movimientosFiltrados) { movimiento ->
                        MovimientoCard(
                            movimiento = movimiento,
                            onEdit = { showEditDialog = movimiento },
                            onDelete = { showDeleteConfirm = movimiento }
                        )
                    }
                }
            }
            
            error?.let {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = RedError.copy(alpha = 0.2f)
                    )
                ) {
                    Text(
                        text = it,
                        modifier = Modifier.padding(16.dp),
                        color = RedError
                    )
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
        
        // Dialog de edición
        showEditDialog?.let { movimiento ->
            EditMovementDialog(
                movimiento = movimiento,
                onDismiss = { showEditDialog = null },
                onSave = { movimientoActualizado ->
                    viewModel.actualizarMovimiento(movimientoActualizado)
                    showEditDialog = null
                }
            )
        }
        
        // Dialog de confirmación de eliminación
        showDeleteConfirm?.let { movimiento ->
            AlertDialog(
                onDismissRequest = { showDeleteConfirm = null },
                title = { Text("Eliminar Movimiento") },
                text = {
                    Text("¿Estás seguro de que quieres eliminar el movimiento \"${movimiento.concepto}\"? Esta acción no se puede deshacer.")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.eliminarMovimiento(movimiento)
                            showDeleteConfirm = null
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RedError
                        )
                    ) {
                        Text("Eliminar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirm = null }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

@Composable
fun MovimientoCard(
    movimiento: Movimiento,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val formato = NumberFormat.getCurrencyInstance(Locale("es", "ES"))
    val fechaFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() },
        colors = CardDefaults.cardColors(
            containerColor = CardBg
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (movimiento.tipo == TipoMovimiento.INGRESO) 
                            Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                        contentDescription = null,
                        tint = if (movimiento.tipo == TipoMovimiento.INGRESO) GreenSuccess else RedError,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = movimiento.concepto,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = movimiento.categoria,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = fechaFormat.format(movimiento.fecha),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                    if (movimiento.frecuencia != Frecuencia.PUNTUAL) {
                        Text(
                            text = "• ${movimiento.frecuencia.name.lowercase()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                }
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (movimiento.tipo == TipoMovimiento.INGRESO) 
                        "+${formato.format(movimiento.importe)}" 
                    else 
                        "-${formato.format(movimiento.importe)}",
                    style = MaterialTheme.typography.titleLarge,
                    color = if (movimiento.tipo == TipoMovimiento.INGRESO) GreenSuccess else RedError
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, "Editar", tint = Primary)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, "Eliminar", tint = RedError)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.ui.ExperimentalComposeUiApi::class)
@Composable
fun EditMovementDialog(
    movimiento: Movimiento,
    onDismiss: () -> Unit,
    onSave: (Movimiento) -> Unit
) {
    val context = LocalContext.current
    val categoriasRepo = remember { CategoriasRepository(context) }
    var categoriasDefault by remember { mutableStateOf(categoriasRepo.obtenerCategorias()) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    
    LaunchedEffect(Unit) {
        categoriasDefault = categoriasRepo.obtenerCategorias()
    }
    
    var tipo by remember { mutableStateOf(movimiento.tipo) }
    var categoria by remember { mutableStateOf(movimiento.categoria) }
    var categoriaExpanded by remember { mutableStateOf(false) }
    var concepto by remember { mutableStateOf(movimiento.concepto) }
    var importe by remember { mutableStateOf(movimiento.importe.toString()) }
    var frecuencia by remember { mutableStateOf(movimiento.frecuencia) }
    var esConjunto by remember { mutableStateOf(movimiento.esConjunto) }
    var fecha by remember { mutableStateOf(movimiento.fecha) }
    var tieneFechaFin by remember { mutableStateOf(movimiento.fechaFin != null) }
    var fechaFin by remember { mutableStateOf(movimiento.fechaFin ?: Calendar.getInstance().apply { add(Calendar.YEAR, 1) }.time) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Movimiento") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Tipo
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = tipo == TipoMovimiento.INGRESO,
                        onClick = { tipo = TipoMovimiento.INGRESO },
                        label = { Text("Ingreso") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = tipo == TipoMovimiento.GASTO,
                        onClick = { tipo = TipoMovimiento.GASTO },
                        label = { Text("Gasto") },
                        modifier = Modifier.weight(1f)
                    )
                }
                
                // Fecha
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val calendar = remember { Calendar.getInstance().apply { time = fecha } }
                
                val abrirDatePicker = {
                    keyboardController?.hide()
                    val datePicker = android.app.DatePickerDialog(
                        context,
                        { _, year, month, dayOfMonth ->
                            calendar.set(year, month, dayOfMonth)
                            fecha = calendar.time
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    )
                    datePicker.show()
                }
                
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = dateFormat.format(fecha),
                        onValueChange = {},
                        label = { Text("📅 Fecha") },
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        leadingIcon = {
                            Icon(Icons.Default.CalendarToday, null)
                        },
                        trailingIcon = {
                            IconButton(onClick = abrirDatePicker) {
                                Icon(Icons.Default.CalendarToday, null)
                            }
                        }
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .matchParentSize()
                            .clickable { abrirDatePicker() }
                    )
                }
                
                // Categoría
                val abrirCategoria = {
                    keyboardController?.hide()
                    categoriaExpanded = true
                }
                
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = categoria,
                        onValueChange = {},
                        label = { Text("Categoría") },
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        trailingIcon = {
                            IconButton(onClick = abrirCategoria) {
                                Icon(Icons.Default.ArrowDropDown, null)
                            }
                        }
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .matchParentSize()
                            .clickable { abrirCategoria() }
                    )
                    DropdownMenu(
                        expanded = categoriaExpanded,
                        onDismissRequest = { categoriaExpanded = false }
                    ) {
                        categoriasDefault.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat, maxLines = 1) },
                                onClick = {
                                    categoria = cat
                                    categoriaExpanded = false
                                }
                            )
                        }
                    }
                }
                
                // Concepto
                OutlinedTextField(
                    value = concepto,
                    onValueChange = { concepto = it },
                    label = { Text("Concepto") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                // Frecuencia
                var expanded by remember { mutableStateOf(false) }
                val abrirFrecuencia = {
                    keyboardController?.hide()
                    expanded = true
                }
                
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = when (frecuencia) {
                            Frecuencia.PUNTUAL -> "Puntual"
                            Frecuencia.MENSUAL -> "Mensual"
                            Frecuencia.ANUAL -> "Anual"
                        },
                        onValueChange = { },
                        label = { Text("Frecuencia") },
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        trailingIcon = {
                            IconButton(onClick = abrirFrecuencia) {
                                Icon(Icons.Default.ArrowDropDown, null)
                            }
                        }
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .matchParentSize()
                            .clickable { abrirFrecuencia() }
                    )
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Puntual") },
                            onClick = {
                                frecuencia = Frecuencia.PUNTUAL
                                tieneFechaFin = false
                                expanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Mensual") },
                            onClick = {
                                frecuencia = Frecuencia.MENSUAL
                                expanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Anual") },
                            onClick = {
                                frecuencia = Frecuencia.ANUAL
                                expanded = false
                            }
                        )
                    }
                }
                
                // Fecha final (solo para mensual/anual)
                if (frecuencia == Frecuencia.MENSUAL || frecuencia == Frecuencia.ANUAL) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = tieneFechaFin,
                            onCheckedChange = { tieneFechaFin = it }
                        )
                        Text("Fecha final (opcional)")
                    }
                    if (tieneFechaFin) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = dateFormat.format(fechaFin),
                                onValueChange = {},
                                label = { Text("📅 Fecha final") },
                                readOnly = true,
                                modifier = Modifier.fillMaxWidth(),
                                leadingIcon = { Icon(Icons.Default.CalendarToday, null) },
                                trailingIcon = {
                                    IconButton(onClick = {
                                        val datePicker = android.app.DatePickerDialog(
                                            context,
                                            { _, y, m, d ->
                                                fechaFin = Calendar.getInstance().apply { set(y, m, d) }.time
                                            },
                                            Calendar.getInstance().apply { time = fechaFin }.get(Calendar.YEAR),
                                            Calendar.getInstance().apply { time = fechaFin }.get(Calendar.MONTH),
                                            Calendar.getInstance().apply { time = fechaFin }.get(Calendar.DAY_OF_MONTH)
                                        )
                                        datePicker.show()
                                    }) {
                                        Icon(Icons.Default.CalendarToday, null)
                                    }
                                }
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .matchParentSize()
                                    .clickable {
                                        val datePicker = android.app.DatePickerDialog(
                                            context,
                                            { _, y, m, d ->
                                                fechaFin = Calendar.getInstance().apply { set(y, m, d) }.time
                                            },
                                            Calendar.getInstance().apply { time = fechaFin }.get(Calendar.YEAR),
                                            Calendar.getInstance().apply { time = fechaFin }.get(Calendar.MONTH),
                                            Calendar.getInstance().apply { time = fechaFin }.get(Calendar.DAY_OF_MONTH)
                                        )
                                        datePicker.show()
                                    }
                            )
                        }
                    }
                }
                
                // Importe
                OutlinedTextField(
                    value = importe,
                    onValueChange = { newValue ->
                        if (newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                            importe = newValue
                        }
                    },
                    label = { Text("Importe Total (€)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged {
                            if (it.isFocused) {
                                coroutineScope.launch {
                                    kotlinx.coroutines.delay(200)
                                    scrollState.animateScrollTo(scrollState.maxValue)
                                }
                            }
                        },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = androidx.compose.ui.text.input.ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            keyboardController?.hide()
                            coroutineScope.launch {
                                scrollState.animateScrollTo(scrollState.maxValue)
                            }
                        }
                    ),
                    singleLine = true,
                    leadingIcon = {
                        Icon(Icons.Default.Euro, null)
                    }
                )
                
                // Gasto conjunto
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = esConjunto,
                        onCheckedChange = { esConjunto = it }
                    )
                    Text("👥 Gasto Conjunto")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val importeReal = if (esConjunto && tipo == TipoMovimiento.GASTO) {
                        importe.toDoubleOrNull()?.div(2) ?: 0.0
                    } else {
                        importe.toDoubleOrNull() ?: 0.0
                    }
                    
                    val impacto = when (frecuencia) {
                        Frecuencia.ANUAL -> importeReal / 12
                        Frecuencia.MENSUAL -> importeReal
                        Frecuencia.PUNTUAL -> importeReal
                    }
                    
                    val movimientoActualizado = movimiento.copy(
                        fecha = fecha,
                        tipo = tipo,
                        categoria = categoria,
                        concepto = concepto,
                        importe = importeReal,
                        frecuencia = frecuencia,
                        impactoMensual = impacto,
                        esConjunto = esConjunto,
                        fechaFin = if (frecuencia == Frecuencia.MENSUAL || frecuencia == Frecuencia.ANUAL) {
                            if (tieneFechaFin) fechaFin else null
                        } else null
                    )
                    onSave(movimientoActualizado)
                },
                enabled = concepto.isNotEmpty() && importe.isNotEmpty() && categoria.isNotEmpty()
            ) {
                Text("💾 Guardar Cambios")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
        containerColor = ModalBg
    )
}
