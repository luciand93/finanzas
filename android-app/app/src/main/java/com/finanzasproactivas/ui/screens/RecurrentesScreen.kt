package com.finanzasproactivas.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.finanzasproactivas.data.model.Frecuencia
import com.finanzasproactivas.ui.components.MenuDrawer
import com.finanzasproactivas.ui.theme.*
import com.finanzasproactivas.ui.viewmodel.FinanzasViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurrentesScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel: FinanzasViewModel = viewModel { FinanzasViewModel(context.applicationContext as android.app.Application) }
    
    val movimientos by viewModel.movimientos.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    val recurrentes = movimientos.filter { 
        it.frecuencia != Frecuencia.PUNTUAL 
    }
    var showMenu by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🔄 Movimientos Recurrentes") },
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
        ) {
            if (isLoading && movimientos.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (recurrentes.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Repeat,
                            null,
                            modifier = Modifier.size(64.dp),
                            tint = TextSecondary
                        )
                        Text(
                            "No hay movimientos recurrentes",
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
                    items(recurrentes) { movimiento ->
                        RecurrenteCard(movimiento)
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
    }
}

@Composable
fun RecurrenteCard(movimiento: com.finanzasproactivas.data.model.Movimiento) {
    val formato = NumberFormat.getCurrencyInstance(Locale("es", "ES"))
    val fechaFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                        imageVector = when (movimiento.frecuencia) {
                            Frecuencia.MENSUAL -> Icons.Default.CalendarMonth
                            Frecuencia.ANUAL -> Icons.Default.CalendarToday
                            else -> Icons.Default.Event
                        },
                        contentDescription = null,
                        tint = Primary,
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
                    AssistChip(
                        onClick = {},
                        label = { Text(movimiento.frecuencia.name.lowercase().capitalize()) }
                    )
                    if (movimiento.esConjunto) {
                        AssistChip(
                            onClick = {},
                            label = { Text("Conjunto") }
                        )
                    }
                }
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (movimiento.tipo == com.finanzasproactivas.data.model.TipoMovimiento.INGRESO) 
                        "+${formato.format(movimiento.importe)}" 
                    else 
                        "-${formato.format(movimiento.importe)}",
                    style = MaterialTheme.typography.titleLarge,
                    color = if (movimiento.tipo == com.finanzasproactivas.data.model.TipoMovimiento.INGRESO) 
                        GreenSuccess else RedError
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "Impacto: ${formato.format(movimiento.impactoMensual)}/mes",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }
        }
    }
}
