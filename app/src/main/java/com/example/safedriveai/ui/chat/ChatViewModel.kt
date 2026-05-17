package com.example.safedriveai.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.safedriveai.ml.OllamaClient
import com.example.safedriveai.data.local.dao.ChatDao
import com.example.safedriveai.data.local.entity.ChatEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatMessage(
    val role: String,
    val content: String,
    val isUser: Boolean
)

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val conversations: List<String> = emptyList(),
    val currentConversationId: String = "Chat Principal",
    val models: List<String> = emptyList(),
    val selectedModel: String = "",
    val isLoading: Boolean = false,
    val input: String = ""
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatDao: ChatDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var responseJob: Job? = null

    init {
        loadModels()
        observeConversations()
        observeMessages()
    }

    private fun observeConversations() {
        viewModelScope.launch {
            chatDao.getConversationIds().collectLatest { ids ->
                _uiState.update { state ->
                    val finalIds = ids.ifEmpty { listOf("Chat Principal") }
                    state.copy(
                        conversations = finalIds,
                        currentConversationId = if (state.currentConversationId !in finalIds) {
                            finalIds.first()
                        } else {
                            state.currentConversationId
                        }
                    )
                }
            }
        }
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private fun observeMessages() {
        viewModelScope.launch {
            _uiState.map { it.currentConversationId }
                .distinctUntilChanged()
                .flatMapLatest { conversationId ->
                    chatDao.getMessagesByConversation(conversationId)
                }
                .collect { entities ->
                    val messages = entities.map { 
                        ChatMessage(it.role, it.content, it.isUser)
                    }
                    _uiState.update { it.copy(messages = messages) }
                }
        }
    }

    private fun loadModels() {
        viewModelScope.launch {
            val models = OllamaClient.getModels()
            _uiState.value = _uiState.value.copy(
                models = models,
                selectedModel = models.firstOrNull() ?: ""
            )
        }
    }

    fun onInputChanged(newInput: String) {
        _uiState.value = _uiState.value.copy(input = newInput)
    }

    fun onModelSelected(model: String) {
        _uiState.value = _uiState.value.copy(selectedModel = model)
    }

    fun onConversationSelected(id: String) {
        _uiState.value = _uiState.value.copy(currentConversationId = id)
    }

    fun createNewConversation() {
        val newId = "Chat ${System.currentTimeMillis() % 10000}"
        _uiState.value = _uiState.value.copy(currentConversationId = newId)
    }

    fun deleteConversation(id: String) {
        viewModelScope.launch {
            chatDao.clearConversation(id)
            if (_uiState.value.currentConversationId == id) {
                val remaining = _uiState.value.conversations.filter { it != id }
                _uiState.update { it.copy(currentConversationId = remaining.firstOrNull() ?: "Chat Principal") }
            }
        }
    }

    fun stopResponse() {
        responseJob?.cancel()
        _uiState.update { it.copy(isLoading = false) }
    }

    fun sendMessage() {
        val currentInput = _uiState.value.input
        val currentModel = _uiState.value.selectedModel
        val currentId = _uiState.value.currentConversationId
        if (currentInput.isBlank() || currentModel.isBlank() || _uiState.value.isLoading) return

        viewModelScope.launch {
            chatDao.insertMessage(
                ChatEntity(
                    conversationId = currentId,
                    role = "user", 
                    content = currentInput, 
                    isUser = true
                )
            )
        }

        val history = _uiState.value.messages.takeLast(10).joinToString("\n") {
            "${if (it.isUser) "Usuario" else "Asistente"}: ${it.content}"
        }

        _uiState.update { it.copy(input = "", isLoading = true) }

        responseJob = viewModelScope.launch {
            try {
                val response = OllamaClient.askModel(currentModel, currentInput, history)
                chatDao.insertMessage(
                    ChatEntity(
                        conversationId = currentId,
                        role = "assistant", 
                        content = response, 
                        isUser = false
                    )
                )
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            chatDao.clearConversation(_uiState.value.currentConversationId)
        }
    }
}
