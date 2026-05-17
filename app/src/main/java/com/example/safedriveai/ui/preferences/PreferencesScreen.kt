package com.example.safedriveai.ui.preferences

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun PreferencesScreen(
    viewModel: PreferencesViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    var showThemeDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Cabecera igual a las otras pantallas
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Configuración",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        PreferenceCategory(title = "General") {
            PreferenceSwitchItem(
                title = "Sistema Métrico",
                subtitle = "Usar km/h en lugar de mph",
                icon = Icons.Default.Speed,
                checked = uiState.useMetricSystem,
                onCheckedChange = { viewModel.toggleMetricSystem(it) }
            )
            PreferenceSwitchItem(
                title = "Alertas de Voz",
                subtitle = "Recibir avisos acústicos de seguridad",
                icon = Icons.Default.RecordVoiceOver,
                checked = uiState.voiceAlerts,
                onCheckedChange = { viewModel.toggleVoiceAlerts(it) }
            )
        }

        PreferenceCategory(title = "Pantalla") {
            PreferenceDropdownItem(
                title = "Tema de la aplicación",
                subtitle = when (uiState.darkMode) {
                    DarkModeConfig.FOLLOW_SYSTEM -> "Seguir sistema"
                    DarkModeConfig.LIGHT -> "Claro"
                    DarkModeConfig.DARK -> "Oscuro"
                },
                icon = Icons.Default.Brightness6,
                onClick = { showThemeDialog = true }
            )
        }

        PreferenceCategory(title = "Datos y Sync") {
            PreferenceSwitchItem(
                title = "Solo WiFi",
                subtitle = "Sincronizar caja negra solo en redes WiFi",
                icon = Icons.Default.Wifi,
                checked = uiState.syncOnlyWifi,
                onCheckedChange = { viewModel.toggleSyncOnlyWifi(it) }
            )
            PreferenceSwitchItem(
                title = "Notificaciones",
                subtitle = "Recibir avisos sobre el estado del sistema",
                icon = Icons.Default.Notifications,
                checked = uiState.enableNotifications,
                onCheckedChange = { viewModel.toggleNotifications(it) }
            )
        }

        PreferenceCategory(title = "IA & Servidores") {
            var tempOllamaHost by remember { mutableStateOf(uiState.ollamaHost) }
            var tempRagHost by remember { mutableStateOf(uiState.ragHost) }

            Column(modifier = Modifier.padding(8.dp)) {
                // OLLAMA - Todo en una línea
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = tempOllamaHost,
                        onValueChange = { 
                            tempOllamaHost = it
                            viewModel.setOllamaHost(it)
                        },
                        label = { Text("Host Ollama") },
                        modifier = Modifier.weight(1f),
                        leadingIcon = { Icon(Icons.Default.Dns, contentDescription = null) },
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    OutlinedTextField(
                        value = "11434",
                        onValueChange = {},
                        label = { Text("Port") },
                        modifier = Modifier.width(75.dp),
                        enabled = false,
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            disabledBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    )
                    IconButton(onClick = { viewModel.testOllamaConnection() }) {
                        ConnectionStatusIcon(uiState.ollamaStatus)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // RAG - Todo en una línea
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = tempRagHost,
                        onValueChange = { 
                            tempRagHost = it
                            viewModel.setRagHost(it)
                        },
                        label = { Text("Host RAG") },
                        modifier = Modifier.weight(1f),
                        leadingIcon = { Icon(Icons.Default.Storage, contentDescription = null) },
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    OutlinedTextField(
                        value = "8000",
                        onValueChange = {},
                        label = { Text("Port") },
                        modifier = Modifier.width(75.dp),
                        enabled = false,
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            disabledBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    )
                    IconButton(onClick = { viewModel.testRagConnection() }) {
                        ConnectionStatusIcon(uiState.ragStatus)
                    }
                }
            }
            
            Text(
                text = "Pulsa el icono para probar la conexión.",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        PreferenceCategory(title = "Cuenta") {
            PreferenceItem(
                title = "Perfil de Usuario",
                subtitle = "Gestionar datos del conductor",
                icon = Icons.Default.AccountCircle,
                onClick = { /* Ir a perfil */ }
            )
            PreferenceItem(
                title = "Cerrar Sesión",
                subtitle = "Salir de la cuenta actual",
                icon = Icons.AutoMirrored.Filled.ExitToApp,
                onClick = { /* Logout */ },
                tint = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "SafeDrive AI v0.2.beta",
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    if (showThemeDialog) {
        ThemeSelectionDialog(
            currentMode = uiState.darkMode,
            onDismiss = { showThemeDialog = false },
            onSelect = {
                viewModel.setDarkMode(it)
                showThemeDialog = false
            }
        )
    }
}

@Composable
fun ConnectionStatusIcon(status: ConnectionStatus) {
    val color = when (status) {
        ConnectionStatus.ONLINE -> MaterialTheme.colorScheme.primary
        ConnectionStatus.OFFLINE -> MaterialTheme.colorScheme.error
        ConnectionStatus.CHECKING -> MaterialTheme.colorScheme.secondary
        ConnectionStatus.UNKNOWN -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    val icon = when (status) {
        ConnectionStatus.ONLINE -> Icons.Default.CheckCircle
        ConnectionStatus.OFFLINE -> Icons.Default.Error
        ConnectionStatus.CHECKING -> Icons.Default.Sync
        ConnectionStatus.UNKNOWN -> Icons.Default.PlayCircle
    }

    if (status == ConnectionStatus.CHECKING) {
        CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            strokeWidth = 2.dp,
            color = color
        )
    } else {
        Icon(
            imageVector = icon,
            contentDescription = status.name,
            tint = color,
            modifier = Modifier.size(28.dp)
        )
    }
}

@Composable
fun ThemeSelectionDialog(
    currentMode: DarkModeConfig,
    onDismiss: () -> Unit,
    onSelect: (DarkModeConfig) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Seleccionar Tema") },
        text = {
            Column {
                ThemeOption("Seguir sistema", currentMode == DarkModeConfig.FOLLOW_SYSTEM) {
                    onSelect(DarkModeConfig.FOLLOW_SYSTEM)
                }
                ThemeOption("Claro", currentMode == DarkModeConfig.LIGHT) {
                    onSelect(DarkModeConfig.LIGHT)
                }
                ThemeOption("Oscuro", currentMode == DarkModeConfig.DARK) {
                    onSelect(DarkModeConfig.DARK)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}

@Composable
fun ThemeOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = text, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun PreferenceCategory(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
        )
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                content()
            }
        }
    }
}

@Composable
fun PreferenceSwitchItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        leadingContent = { Icon(icon, contentDescription = null) },
        trailingContent = {
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    )
}

@Composable
fun PreferenceItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    tint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    ListItem(
        headlineContent = { Text(title, color = tint) },
        supportingContent = { Text(subtitle) },
        leadingContent = { Icon(icon, contentDescription = null, tint = tint) },
        modifier = Modifier.clickable { onClick() }
    )
}

@Composable
fun PreferenceDropdownItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        leadingContent = { Icon(icon, contentDescription = null) },
        trailingContent = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
        modifier = Modifier.clickable { onClick() }
    )
}
