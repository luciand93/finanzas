package com.finanzasproactivas.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.finanzasproactivas.data.model.*
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewMovementDialog(
    onDismiss: () -> Unit,
    onSave: (Movimiento) -> Unit
) {
    var tipo by remember { mutableStateOf(TipoMovimiento.GASTO) }
    var categoria by remember { mutableStateOf("") }
    var concepto by remember { mutableStateOf("") }
    var importe by remember { mutableStateOf("") }
    var frecuencia by remember { mutableStateOf(Frecuencia.PUNTUAL) }
    var esConjunto by remember { mutableStateOf(false) }
    var fecha by remember { mutableStateOf(Date()) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = null,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
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
                
                // Fecha y Categoría
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = fecha.toString(),
                        onValueChange = {},
                        label = { Text("📅 Fecha") },
                        readOnly = true,
                        modifier = Modifier.weight(1f),
                        leadingIcon = {
                            Icon(Icons.Default.CalendarToday, null)
                        }
                    )
                    
                    OutlinedTextField(
                        value = categoria,
                        onValueChange = { categoria = it },
                        label = { Text("Categoría") },
                        modifier = Modifier.weight(1f)
                    )
                }
                
                // Concepto
                OutlinedTextField(
                    value = concepto,
                    onValueChange = { concepto = it },
                    label = { Text("Concepto") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Ej: Cena en terraza") }
                )
                
                // Importe y Frecuencia
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = importe,
                        onValueChange = { importe = it },
                        label = { Text("Importe Total (€)") },
                        modifier = Modifier.weight(2f),
                        leadingIcon = {
                            Icon(Icons.Default.Euro, null)
                        }
                    )
                    
                    var expanded by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = when (frecuencia) {
                                Frecuencia.PUNTUAL -> "Puntual"
                                Frecuencia.MENSUAL -> "Mensual"
                                Frecuencia.ANUAL -> "Anual"
                            },
                            onValueChange = {},
                            label = { Text("Frecuencia") },
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { expanded = true },
                            trailingIcon = {
                                Icon(Icons.Default.ArrowDropDown, null)
                            }
                        )
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Puntual") },
                                onClick = {
                                    frecuencia = Frecuencia.PUNTUAL
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
                }
                
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
                        fecha = fecha,
                        tipo = tipo,
                        categoria = categoria,
                        concepto = concepto,
                        importe = importeReal,
                        frecuencia = frecuencia,
                        impactoMensual = impacto,
                        esConjunto = esConjunto
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
