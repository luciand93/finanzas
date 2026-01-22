package com.finanzasproactivas.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.finanzasproactivas.ui.screens.AsesorScreen
import com.finanzasproactivas.ui.screens.GraficosScreen
import com.finanzasproactivas.ui.screens.TablaScreen
import com.finanzasproactivas.ui.screens.RecurrentesScreen
import com.finanzasproactivas.ui.screens.EditarScreen
import com.finanzasproactivas.ui.screens.ExportarImportarScreen
import com.finanzasproactivas.ui.screens.PresupuestosScreen
import com.finanzasproactivas.ui.screens.ConfigScreen

sealed class Screen(val route: String) {
    object Asesor : Screen("asesor")
    object Graficos : Screen("graficos")
    object Tabla : Screen("tabla")
    object Recurrentes : Screen("recurrentes")
    object Editar : Screen("editar")
    object ExportarImportar : Screen("exportar_importar")
    object Presupuestos : Screen("presupuestos")
    object Config : Screen("config")
}

@Composable
fun FinanzasNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Asesor.route
    ) {
        composable(Screen.Asesor.route) { AsesorScreen(navController) }
        composable(Screen.Graficos.route) { GraficosScreen(navController) }
        composable(Screen.Tabla.route) { TablaScreen(navController) }
        composable(Screen.Recurrentes.route) { RecurrentesScreen(navController) }
        composable(Screen.Editar.route) { EditarScreen(navController) }
        composable(Screen.ExportarImportar.route) { ExportarImportarScreen(navController) }
        composable(Screen.Presupuestos.route) { PresupuestosScreen(navController) }
        composable(Screen.Config.route) { ConfigScreen(navController) }
    }
}
