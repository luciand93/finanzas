package com.finanzasproactivas.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController

@Composable
fun RecurrentesScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("🔄 Recurrentes") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Text("Recurrentes - En desarrollo")
        }
    }
}
