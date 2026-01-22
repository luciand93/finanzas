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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.finanzasproactivas.ui.components.*
import com.finanzasproactivas.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AsesorScreen(navController: NavController) {
    var showNewMovementDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    
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
            // Métricas
            MetricsSection()
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Recomendaciones
            RecommendationsSection()
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Análisis de patrones
            PatternAnalysisSection()
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Chat con Gemini
            ChatSection()
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
                    // Guardar movimiento
                    showNewMovementDialog = false
                }
            )
        }
    }
}
