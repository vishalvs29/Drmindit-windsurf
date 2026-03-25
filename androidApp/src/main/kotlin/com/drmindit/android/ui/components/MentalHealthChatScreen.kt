package com.drmindit.android.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.drmindit.android.ai.MentalHealthChatManager
import com.drmindit.shared.domain.model.ChatMessage
import com.drmindit.shared.domain.model.ChatMessageType
import com.drmindit.shared.domain.model.ChatSender

/**
 * Mental Health Chat Screen with AI integration
 */
@Composable
fun MentalHealthChatScreen(
    chatManager: MentalHealthChatManager,
    onNavigateBack: () -> Unit
) {
    val chatState by chatManager.chatState.collectAsStateWithLifecycle()
    val messages by chatManager.messages.collectAsStateWithLifecycle()
    val isTyping by chatManager.isTyping.collectAsStateWithLifecycle()
    
    var messageText by remember { mutableStateOf("") }
    var selectedEmotionTags by remember { mutableStateOf(listOf<String>()) }
    var showEmotionSelector by remember { mutableStateOf(false) }
    
    val keyboardController = LocalSoftwareKeyboardController.current
    
    // Initialize chat when screen is first composed
    LaunchedEffect(Unit) {
        chatManager.initializeChat()
    }
    
    // Save chat state when it changes
    LaunchedEffect(chatState) {
        chatManager.saveChatState()
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1E3A8), // Soft blue
                        Color(0xFF2E7D32)  // Gentle purple
                    ),
                    startY = 0f
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Header
            MentalHealthChatHeader(
                onNavigateBack = onNavigateBack,
                currentSessionId = chatState.currentSessionId
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Messages Area
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Messages List
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(messages) { message ->
                            ChatMessageItem(
                                message = message,
                                onUserMessageSend = { text, emotions ->
                                    messageText = text
                                    selectedEmotionTags = emotions
                                    chatManager.sendUserMessage(text, emotions)
                                }
                            )
                        }
                        
                        // Typing Indicator
                        if (isTyping) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = MaterialTheme.colorScheme.primary,
                                        strokeWidth = 2.dp
                                    )
                                    
                                    Spacer(modifier = Modifier.width(8.dp))
                                    
                                    Text(
                                        text = "DrMindit is typing...",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                        
                        // Input Area
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            // Emotion Tag Selector
                            if (showEmotionSelector) {
                                EmotionTagSelector(
                                    selectedTags = selectedEmotionTags,
                                    onTagSelected = { tag ->
                                        selectedEmotionTags = if (tag in selectedEmotionTags) {
                                            selectedEmotionTags - tag
                                        } else {
                                            selectedEmotionTags + tag
                                        }
                                    },
                                    onDismiss = { showEmotionSelector = false }
                                )
                            }
                            
                            // Message Input
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                            ) {
                                // Selected Emotion Tags
                                if (selectedEmotionTags.isNotEmpty()) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 8.dp, vertical = 4.dp),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        selectedEmotionTags.forEach { tag ->
                                            AssistChip(
                                                onClick = { 
                                                    selectedEmotionTags = if (tag in selectedEmotionTags) {
                                                        selectedEmotionTags - tag
                                                    } else {
                                                        selectedEmotionTags + tag
                                                    }
                                                },
                                                label = tag,
                                                colors = AssistChipDefaults.assistChipColors(
                                                    leadingIconContentColor = MaterialTheme.colorScheme.primary,
                                                    labelColor = MaterialTheme.colorScheme.onPrimary
                                                )
                                            )
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.height(4.dp))
                                }
                                
                                // Message Input Field
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            color = MaterialTheme.colorScheme.surface,
                                            shape = RoundedCornerShape(24.dp)
                                        ),
                                        .padding(8.dp)
                                ) {
                                    BasicTextField(
                                        value = messageText,
                                        onValueChange = { messageText = it },
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(12.dp),
                                        placeholder = {
                                            Text(
                                                text = "Share what's on your mind...",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                            )
                                        },
                                        textStyle = MaterialTheme.typography.bodyMedium,
                                        colors = TextFieldDefaults.colors(
                                            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                                            unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                            cursorColor = MaterialTheme.colorScheme.primary
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        singleLine = true,
                                        maxLines = 4
                                    )
                                    
                                    // Send Button
                                    IconButton(
                                        onClick = { 
                                            if (messageText.isNotBlank()) {
                                                messageText = ""
                                                keyboardController?.hide()
                                            }
                                        },
                                        enabled = messageText.isNotBlank(),
                                        modifier = Modifier
                                            .padding(8.dp)
                                            .size(48.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                color = MaterialTheme.colorScheme.primary,
                                            ),
                                        colors = IconButtonDefaults.iconButtonColors(
                                            contentColor = MaterialTheme.colorScheme.onPrimary
                                        )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Send,
                                            contentDescription = "Send message",
                                            tint = MaterialTheme.colorScheme.onPrimary
                                        )
                                    }
                                    
                                    // Emotion Selector Button
                                    IconButton(
                                        onClick = { showEmotionSelector = !showEmotionSelector },
                                        modifier = Modifier
                                            .padding(8.dp)
                                            .size(48.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                                            ),
                                        colors = IconButtonDefaults.iconButtonColors(
                                            contentColor = MaterialTheme.colorScheme.onPrimary
                                        )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Tag,
                                            contentDescription = "Add emotion tags",
                                            tint = MaterialTheme.colorScheme.onPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Chat message item component
 */
@Composable
private fun ChatMessageItem(
    message: ChatMessage,
    onUserMessageSend: (String, List<String>) -> Unit
) {
    val isUserMessage = message.sender == ChatSender.USER
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = if (isUserMessage) {
            Arrangement.End
        } else {
            Arrangement.Start
        }
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = if (isUserMessage) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.secondary
                    },
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isUserMessage) Icons.Default.Person else Icons.Default.SmartToy,
                contentDescription = if (isUserMessage) "User" else "AI",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Message Bubble
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(
                    horizontal = if (isUserMessage) 0.dp else 16.dp,
                    vertical = 8.dp
                ),
                .background(
                    color = if (isUserMessage) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    } else {
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.1f)
                    },
                    shape = RoundedCornerShape(
                        topStart = 20.dp,
                        topEnd = 20.dp,
                        bottomStart = if (isUserMessage) 4.dp else 20.dp,
                        bottomEnd = if (isUserMessage) 20.dp else 4.dp,
                        bottomStart = if (isUserMessage) 20.dp else 4.dp
                    )
                )
                .padding(12.dp)
        ) {
            // Message Type Indicator
            if (message.messageType != ChatMessageType.USER) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.End
                ) {
                    when (message.messageType) {
                        ChatMessageType.AI_WELCOME -> {
                            Icon(
                                imageVector = Icons.Default.WavingHand,
                                contentDescription = "Welcome",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "DrMindit",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                        ChatMessageType.AI_RESPONSE -> {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = "AI Response",
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "AI Companion",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                        ChatMessageType.ERROR -> {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Error",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "System",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
            }
            
            // Message Content
            Text(
                text = message.content,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isUserMessage) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            // Emotion Analysis (for AI messages)
            if (message.emotionAnalysis != null) {
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Analytics,
                        contentDescription = "Emotion analysis",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    
                    Text(
                        text = "Detected: ${message.emotionAnalysis}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            // Safety Score (for AI messages)
            if (message.safetyScore != null) {
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = if (message.safetyScore < 0.5f) {
                            Icons.Default.Warning
                        } else {
                            Icons.Default.CheckCircle
                        },
                        contentDescription = "Safety score",
                        tint = if (message.safetyScore < 0.5f) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.primary
                        },
                        modifier = Modifier.size(16.dp)
                    )
                    
                    Text(
                        text = "Safety: ${(message.safetyScore * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (message.safetyScore < 0.5f) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.primary
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            // Timestamp
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = formatTimestamp(message.timestamp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

/**
 * Chat header component
 */
@Composable
private fun MentalHealthChatHeader(
    onNavigateBack: () -> Unit,
    currentSessionId: String?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Back Button
        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                ),
            colors = IconButtonDefaults.iconButtonColors(
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
        
        // Title
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            
            if (currentSessionId != null) {
                Text(
                    text = "Session: $currentSessionId",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

/**
 * Emotion tag selector component
 */
@Composable
private fun EmotionTagSelector(
    selectedTags: List<String>,
    onTagSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val emotionTags = listOf(
        "😊 Happy", "😢 Sad", "😡 Angry", "😰 Anxious",
        "😌 Calm", "🤔 Confused", "😴 Tired",
        "💪 Motivated", "🧘 Mindful", "🙏 Grateful",
        "💭 Thinking", "⚡ Energetic", "😌 Peaceful"
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "How are you feeling?",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp)
            ) {
                items(emotionTags) { tag ->
                    FilterChip(
                        onClick = { onTagSelected(tag) },
                        label = tag,
                        selected = tag in selectedTags,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.padding(4.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Done")
                }
            }
        }
    }
}

/**
 * Format timestamp for display
 */
private fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60000 -> "Just now"
        diff < 3600000 -> "${diff / 60000} min ago"
        diff < 86400000 -> "${diff / 3600000} hours ago"
        else -> "${diff / 86400000} days ago"
    }
}
