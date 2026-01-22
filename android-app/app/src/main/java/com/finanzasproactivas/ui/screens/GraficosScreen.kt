package com.finanzasproactivas.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController

@Composable
fun GraficosScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("📊 Gráficos") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Text("Gráficos - En desarrollo")
        }
    }
}
