package com.finanzasproactivas.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.finanzasproactivas.data.model.*
import com.finanzasproactivas.data.repository.CategoriasRepository
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.ui.ExperimentalComposeUiApi::class)
@Composable
fun NewMovementDialog(
    onDismiss: () -> Unit,
    onSave: (Movimiento) -> Unit
) {
    val context = LocalContext.current
    val categoriasRepo = remember { CategoriasRepository(context) }
    var categoriasDefault by remember { mutableStateOf(categoriasRepo.obtenerCategorias()) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val importeFocusRequester = remember { FocusRequester() }
    val conceptoFocusRequester = remember { FocusRequester() }
    
    // Actualizar categorías cuando cambien
    LaunchedEffect(Unit) {
        categoriasDefault = categoriasRepo.obtenerCategorias()
    }
    
    var tipo by remember { mutableStateOf(TipoMovimiento.GASTO) }
    var categoria by remember { mutableStateOf("") }
    var categoriaExpanded by remember { mutableStateOf(false) }
    var concepto by remember { mutableStateOf("") }
    var importe by remember { mutableStateOf("") }
    var frecuencia by remember { mutableStateOf(Frecuencia.PUNTUAL) }
    var esConjunto by remember { mutableStateOf(false) }
    var fecha by remember { mutableStateOf(Date()) }
    var tieneFechaFin by remember { mutableStateOf(false) }
    var fechaFin by remember { mutableStateOf<Date?>(null) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = null,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Handle visual
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier
                            .width(48.dp)
                            .height(6.dp),
                        shape = MaterialTheme.shapes.small,
                        color = com.finanzasproactivas.ui.theme.InputBorder
                    ) {}
                }
                
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
                
                // Gasto conjunto
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = esConjunto,
                        onCheckedChange = { esConjunto = it }
                    )
                    Text("👥 Gasto Conjunto")
                }
                
                // Fecha (línea completa)
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
                    // Overlay clickeable sobre todo el campo
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .matchParentSize()
                            .clickable { abrirDatePicker() }
                    )
                }
                
                // Categoría (línea completa)
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
                        maxLines = 1,
                        trailingIcon = {
                            IconButton(onClick = abrirCategoria) {
                                Icon(Icons.Default.ArrowDropDown, null)
                            }
                        }
                    )
                    // Overlay clickeable sobre todo el campo
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
                                    // Navegar automáticamente al campo concepto y abrir teclado
                                    coroutineScope.launch {
                                        kotlinx.coroutines.delay(100)
                                        scrollState.animateScrollTo(200) // Aproximadamente donde está concepto
                                        conceptoFocusRequester.requestFocus()
                                        keyboardController?.show()
                                    }
                                }
                            )
                        }
                    }
                }
                
                // Concepto (línea completa)
                OutlinedTextField(
                    value = concepto,
                    onValueChange = { concepto = it },
                    label = { Text("Concepto") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(conceptoFocusRequester),
                    singleLine = true,
                    placeholder = { Text("Ej: Cena en terraza") }
                )
                
                // Frecuencia (línea completa)
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
                        maxLines = 1,
                        trailingIcon = {
                            IconButton(onClick = abrirFrecuencia) {
                                Icon(Icons.Default.ArrowDropDown, null)
                            }
                        }
                    )
                    // Overlay clickeable sobre todo el campo
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
                                fechaFin = null
                                expanded = false
                                coroutineScope.launch {
                                    kotlinx.coroutines.delay(200)
                                    importeFocusRequester.requestFocus()
                                    scrollState.animateScrollTo(scrollState.maxValue)
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Mensual") },
                            onClick = {
                                frecuencia = Frecuencia.MENSUAL
                                expanded = false
                                // Dar foco al campo importe
                                coroutineScope.launch {
                                    kotlinx.coroutines.delay(200)
                                    importeFocusRequester.requestFocus()
                                    scrollState.animateScrollTo(scrollState.maxValue)
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Anual") },
                            onClick = {
                                frecuencia = Frecuencia.ANUAL
                                expanded = false
                                // Dar foco al campo importe
                                coroutineScope.launch {
                                    kotlinx.coroutines.delay(200)
                                    importeFocusRequester.requestFocus()
                                    scrollState.animateScrollTo(scrollState.maxValue)
                                }
                            }
                        )
                    }
                }
                
                // Fecha final (solo para mensual/anual, opcional)
                if (frecuencia == Frecuencia.MENSUAL || frecuencia == Frecuencia.ANUAL) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = tieneFechaFin,
                            onCheckedChange = {
                                tieneFechaFin = it
                                if (!it) fechaFin = null else fechaFin = Calendar.getInstance().apply { add(Calendar.YEAR, 1) }.time
                            }
                        )
                        Text("Fecha final (opcional)")
                    }
                    if (tieneFechaFin) {
                        val fechaFinVal = fechaFin ?: Calendar.getInstance().apply { add(Calendar.YEAR, 1) }.time
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = dateFormat.format(fechaFinVal),
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
                                            Calendar.getInstance().apply { time = fechaFinVal }.get(Calendar.YEAR),
                                            Calendar.getInstance().apply { time = fechaFinVal }.get(Calendar.MONTH),
                                            Calendar.getInstance().apply { time = fechaFinVal }.get(Calendar.DAY_OF_MONTH)
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
                                            Calendar.getInstance().apply { time = fechaFinVal }.get(Calendar.YEAR),
                                            Calendar.getInstance().apply { time = fechaFinVal }.get(Calendar.MONTH),
                                            Calendar.getInstance().apply { time = fechaFinVal }.get(Calendar.DAY_OF_MONTH)
                                        )
                                        datePicker.show()
                                    }
                            )
                        }
                        Text(
                            "No se generarán movimientos después de esta fecha.",
                            style = MaterialTheme.typography.bodySmall,
                            color = com.finanzasproactivas.ui.theme.TextSecondary
                        )
                    }
                }
                
                // Importe (línea completa) - con scroll automático cuando se escribe
                OutlinedTextField(
                    value = importe,
                    onValueChange = { newValue ->
                        // Solo permitir números y punto decimal
                        if (newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                            importe = newValue
                            // Scroll automático cuando se escribe para ver el botón guardar
                            if (newValue.isNotEmpty()) {
                                coroutineScope.launch {
                                    kotlinx.coroutines.delay(100)
                                    scrollState.animateScrollTo(scrollState.maxValue)
                                }
                            }
                        }
                    },
                    label = { Text("Importe Total (€)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(importeFocusRequester)
                        .onFocusChanged {
                            if (it.isFocused) {
                                // Scroll cuando se enfoca el campo
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
                            // Scroll al final para mostrar el botón guardar
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
                
                if (esConjunto && tipo == TipoMovimiento.GASTO && importe.isNotEmpty()) {
                    val importeReal = importe.toDoubleOrNull()?.div(2) ?: 0.0
                    Text(
                        text = "ℹ️ Se registrarán ${String.format("%.2f", importeReal)} € (mitad del total)",
                        style = MaterialTheme.typography.bodySmall,
                        color = com.finanzasproactivas.ui.theme.BlueInfo
                    )
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
                    
                    val movimiento = Movimiento(
                        id = UUID.randomUUID().toString(),
                        fecha = fecha,
                        tipo = tipo,
                        categoria = categoria,
                        concepto = concepto,
                        importe = importeReal,
                        frecuencia = frecuencia,
                        impactoMensual = impacto,
                        esConjunto = esConjunto,
                        fechaFin = if (frecuencia == Frecuencia.MENSUAL || frecuencia == Frecuencia.ANUAL) fechaFin else null
                    )
                    onSave(movimiento)
                },
                enabled = concepto.isNotEmpty() && importe.isNotEmpty() && categoria.isNotEmpty()
            ) {
                Text("💾 Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
        containerColor = com.finanzasproactivas.ui.theme.ModalBg
    )
}
