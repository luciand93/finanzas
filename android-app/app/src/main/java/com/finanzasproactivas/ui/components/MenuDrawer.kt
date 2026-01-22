package com.finanzasproactivas.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.finanzasproactivas.ui.navigation.Screen
import com.finanzasproactivas.ui.theme.*

@Composable
fun MenuDrawer(
    onDismiss: () -> Unit,
    onNavigate: (String) -> Unit
) {
    val alpha = animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(300),
        label = "alpha"
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(alpha.value)
    ) {
        // Overlay oscuro
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .clickable { onDismiss() },
            color = androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.5f)
        ) {}
        
        // Menú lateral derecho
        Surface(
            modifier = Modifier
                .fillMaxHeight()
                .width(280.dp)
                .align(Alignment.CenterEnd),
            color = BackgroundDark,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Navegación",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Divider()
                
                Spacer(modifier = Modifier.height(16.dp))
                
                MenuItem(
                    icon = Icons.Default.SmartToy,
                    text = "🤖 Asesor",
                    onClick = {
                        onNavigate(Screen.Asesor.route)
                    }
                )
                
                MenuItem(
                    icon = Icons.Default.BarChart,
                    text = "📊 Gráficos",
                    onClick = {
                        onNavigate(Screen.Graficos.route)
                    }
                )
                
                MenuItem(
                    icon = Icons.Default.TableChart,
                    text = "🔍 Tabla",
                    onClick = {
                        onNavigate(Screen.Tabla.route)
                    }
                )
                
                MenuItem(
                    icon = Icons.Default.Repeat,
                    text = "🔄 Recurrentes",
                    onClick = {
                        onNavigate(Screen.Recurrentes.route)
                    }
                )
                
                MenuItem(
                    icon = Icons.Default.Edit,
                    text = "📝 Editar",
                    onClick = {
                        onNavigate(Screen.Editar.route)
                    }
                )
                
                MenuItem(
                    icon = Icons.Default.ImportExport,
                    text = "📤 Exportar/Importar",
                    onClick = {
                        onNavigate(Screen.ExportarImportar.route)
                    }
                )
                
                MenuItem(
                    icon = Icons.Default.AccountBalance,
                    text = "💰 Presupuestos",
                    onClick = {
                        onNavigate(Screen.Presupuestos.route)
                    }
                )
                
                MenuItem(
                    icon = Icons.Default.Settings,
                    text = "⚙️ Config",
                    onClick = {
                        onNavigate(Screen.Config.route)
                    }
                )
            }
        }
    }
}

@Composable
fun MenuItem(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = TextSecondary
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = TextPrimary
        )
    }
}
