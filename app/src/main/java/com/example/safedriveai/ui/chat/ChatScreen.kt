package com.example.safedriveai.ui.chat

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun ChatScreen(
    viewModel: ChatViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        ChatHeader(onClearHistory = viewModel::clearHistory)

        ChatTabs(
            conversations = uiState.conversations,
            currentId = uiState.currentConversationId,
            onSelect = viewModel::onConversationSelected,
            onClose = viewModel::deleteConversation,
            onAdd = viewModel::createNewConversation
        )

        ModelSelector(
            selected = uiState.selectedModel,
            models = uiState.models,
            onSelect = viewModel::onModelSelected
        )

        MessageList(
            messages = uiState.messages,
            isLoading = uiState.isLoading,
            listState = listState,
            onStop = viewModel::stopResponse,
            modifier = Modifier.weight(1f)
        )

        ChatInput(
            input = uiState.input,
            isLoading = uiState.isLoading,
            onInputChanged = viewModel::onInputChanged,
            onSend = viewModel::sendMessage
        )
    }
}

@Composable
private fun ChatHeader(onClearHistory: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Asistente SafeDrive AI",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        IconButton(onClick = onClearHistory) {
            Icon(Icons.Default.Delete, "Borrar historial", tint = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun ChatTabs(
    conversations: List<String>,
    currentId: String,
    onSelect: (String) -> Unit,
    onClose: (String) -> Unit,
    onAdd: () -> Unit
) {
    ScrollableTabRow(
        selectedTabIndex = conversations.indexOf(currentId).coerceAtLeast(0),
        edgePadding = 16.dp,
        containerColor = Color.Transparent,
        divider = {},
        indicator = { tabPositions ->
            val index = conversations.indexOf(currentId)
            if (index != -1 && index < tabPositions.size) {
                SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[index]),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    ) {
        conversations.forEachIndexed { index, id ->
            Tab(
                selected = currentId == id,
                onClick = { onSelect(id) },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (id.startsWith("Chat ")) "Chat ${index + 1}" else id,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (conversations.size > 1) {
                            IconButton(onClick = { onClose(id) }, modifier = Modifier.size(16.dp)) {
                                Icon(Icons.Default.Close, "Cerrar", modifier = Modifier.size(12.dp))
                            }
                        }
                    }
                }
            )
        }
        IconButton(onClick = onAdd) {
            Icon(Icons.Default.Add, "Nuevo Chat", tint = MaterialTheme.colorScheme.primary)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModelSelector(selected: String, models: List<String>, onSelect: (String) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
        Text("Modelo: ", style = MaterialTheme.typography.bodyMedium)
        var expanded by remember { mutableStateOf(false) }
        Box {
            FilterChip(true, { expanded = true }, label = { Text(selected.ifEmpty { "Cargando..." }) })
            DropdownMenu(expanded, { expanded = false }) {
                models.forEach { model ->
                    DropdownMenuItem(text = { Text(model) }, onClick = { onSelect(model); expanded = false })
                }
            }
        }
    }
}

@Composable
private fun MessageList(
    messages: List<ChatMessage>,
    isLoading: Boolean,
    listState: LazyListState,
    onStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(state = listState, modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(messages) { ChatBubble(it) }
        if (isLoading) {
            item {
                Row(Modifier.fillMaxWidth(), Arrangement.Center, Alignment.CenterVertically) {
                    CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
                    TextButton(onClick = onStop) {
                        Icon(Icons.Default.Stop, null, Modifier.size(16.dp))
                        Text(" Detener")
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatInput(input: String, isLoading: Boolean, onInputChanged: (String) -> Unit, onSend: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = input,
            onValueChange = onInputChanged,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Pregunta algo...") },
            shape = RoundedCornerShape(24.dp)
        )
        IconButton(onClick = onSend, enabled = input.isNotBlank() && !isLoading) {
            Icon(Icons.AutoMirrored.Filled.Send, "Enviar", tint = MaterialTheme.colorScheme.primary)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatBubble(message: ChatMessage) {
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) }

    val alignment = if (message.isUser) Alignment.CenterEnd else Alignment.CenterStart
    val color = if (message.isUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
    val shape = if (message.isUser) RoundedCornerShape(16.dp, 16.dp, 0.dp, 16.dp) else RoundedCornerShape(16.dp, 16.dp, 16.dp, 0.dp)

    Box(Modifier.fillMaxWidth(), alignment) {
        Column(
            Modifier.widthIn(max = 280.dp).clip(shape).background(color)
                .combinedClickable(onClick = {}, onLongClick = { showMenu = true }).padding(12.dp)
        ) {
            Text(
                text = if (message.isUser) "Tú" else "AI",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                ),
                fontWeight = FontWeight.Bold
            )
            Text(message.content, style = MaterialTheme.typography.bodyMedium)

            DropdownMenu(showMenu, { showMenu = false }) {
                DropdownMenuItem(
                    text = { Text("Copiar") },
                    onClick = {
                        clipboard.setText(AnnotatedString(message.content))
                        Toast.makeText(context, "Copiado", Toast.LENGTH_SHORT).show()
                        showMenu = false
                    },
                    leadingIcon = { Icon(Icons.Default.ContentCopy, null, Modifier.size(18.dp)) }
                )
                DropdownMenuItem(
                    text = { Text("Compartir") },
                    onClick = {
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            putExtra(Intent.EXTRA_TEXT, message.content)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(intent, null))
                        showMenu = false
                    },
                    leadingIcon = { Icon(Icons.Default.Share, null, Modifier.size(18.dp)) }
                )
            }
        }
    }
}
