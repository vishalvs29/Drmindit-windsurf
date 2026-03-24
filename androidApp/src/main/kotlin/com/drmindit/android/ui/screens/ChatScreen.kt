package com.drmindit.android.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.drmindit.android.ui.components.*
import com.drmindit.android.ui.viewmodel.ChatViewModel
import com.drmindit.shared.domain.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    onNavigateToSession: (String) -> Unit,
    onNavigateToHelplines: () -> Unit,
    onNavigateBack: () -> Unit,
    chatViewModel: ChatViewModel = hiltViewModel()
) {
    val chatState by chatViewModel.chatState.collectAsStateWithLifecycle()
    val isTyping by chatViewModel.isTyping.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    
    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(chatState.messages) {
        if (chatState.messages.isNotEmpty()) {
            kotlinx.coroutines.delay(100)
            listState.animateScrollToItem(chatState.messages.size - 1)
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            ChatHeader(
                onNavigateBack = onNavigateBack,
                onClearChat = { chatViewModel.clearChat() }
            )
            
            // Messages
            Box(
                modifier = Modifier.weight(1f)
            ) {
                if (chatState.messages.isEmpty()) {
                    EmptyChatState()
                } else {
                    MessagesList(
                        messages = chatState.messages,
                        listState = listState,
                        onSessionClick = onNavigateToSession,
                        onHelplineClick = onNavigateToHelplines,
                        onQuickReplyClick = chatViewModel::sendMessage
                    )
                }
                
                // Typing indicator
                if (isTyping) {
                    TypingIndicator(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    )
                }
            }
            
            // Input area
            ChatInput(
                message = chatViewModel.currentMessage,
                onMessageChange = chatViewModel::updateMessage,
                onSendMessage = chatViewModel::sendMessage,
                onQuickReplyClick = chatViewModel::sendMessage,
                suggestedReplies = chatState.suggestedReplies,
                isLoading = chatState.isLoading,
                enabled = !chatState.isLoading
            )
        }
        
        // Error snackbar
        chatState.error?.let { error ->
            LaunchedEffect(error) {
                // Show error snackbar (implementation depends on your scaffold setup)
                chatViewModel.clearError()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatHeader(
    onNavigateBack: () -> Unit,
    onClearChat: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateBack
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back"
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Column {
                Text(
                    text = "Wellness Chat",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "Your AI wellness companion",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
        
        IconButton(
            onClick = onClearChat
        ) {
            Icon(
                imageVector = Icons.Default.Clear,
                contentDescription = "Clear chat"
            )
        }
    }
}

@Composable
private fun EmptyChatState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(80.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Icon(
                imageVector = Icons.Default.Chat,
                contentDescription = "Chat",
                modifier = Modifier.padding(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Welcome to Wellness Chat",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold
            ),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "I'm here to support you with stress, anxiety, sleep, and more. How can I help you today?",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        QuickReplyChips(
            replies = listOf(
                "I feel anxious",
                "I can't sleep",
                "I feel stressed",
                "I feel low"
            ),
            onReplyClick = { /* Will be handled by parent */ }
        )
    }
}

@Composable
private fun MessagesList(
    messages: List<ChatMessage>,
    listState: androidx.compose.foundation.lazy.LazyListState,
    onSessionClick: (String) -> Unit,
    onHelplineClick: () -> Unit,
    onQuickReplyClick: (String) -> Unit
) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(messages, key = { it.id }) { message ->
            when (message.sender) {
                MessageSender.USER -> UserMessageBubble(message = message)
                MessageSender.AI -> AIMessageBubble(
                    message = message,
                    onSessionClick = onSessionClick,
                    onHelplineClick = onHelplineClick,
                    onQuickReplyClick = onQuickReplyClick
                )
                MessageSender.SYSTEM -> SystemMessageBubble(message = message)
            }
        }
    }
}

@Composable
private fun UserMessageBubble(
    message: ChatMessage
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        Column(
            modifier = Modifier.widthIn(max = 280.dp),
            horizontalAlignment = Alignment.End
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primary
            ) {
                Text(
                    text = message.text,
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
            
            Text(
                text = formatTime(message.timestamp),
                modifier = Modifier.padding(start = 4.dp, top = 2.dp),
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

@Composable
private fun AIMessageBubble(
    message: ChatMessage,
    onSessionClick: (String) -> Unit,
    onHelplineClick: () -> Unit,
    onQuickReplyClick: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Column(
            modifier = Modifier.widthIn(max = 320.dp)
        ) {
            // AI Avatar
            Row(
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    modifier = Modifier.size(32.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = "AI",
                        modifier = Modifier.padding(8.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Column {
                    // Message bubble
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = message.text,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            
                            // Recommended sessions
                            message.getRecommendedSessions().takeIf { it.isNotEmpty() }?.let { sessions ->
                                Spacer(modifier = Modifier.height(12.dp))
                                RecommendedSessionsSection(
                                    sessions = sessions,
                                    onSessionClick = onSessionClick
                                )
                            }
                            
                            // Safety alert
                            if (message.isSafetyAlert()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                SafetyAlertSection(
                                    message = message,
                                    onHelplineClick = onHelplineClick
                                )
                            }
                        }
                    }
                    
                    // Quick replies
                    message.getQuickReplies().takeIf { it.isNotEmpty() }?.let { replies ->
                        Spacer(modifier = Modifier.height(8.dp))
                        QuickReplyChips(
                            replies = replies,
                            onReplyClick = onQuickReplyClick
                        )
                    }
                    
                    Text(
                        text = formatTime(message.timestamp),
                        modifier = Modifier.padding(start = 4.dp, top = 2.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun SystemMessageBubble(
    message: ChatMessage
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ) {
            Text(
                text = message.text,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

@Composable
private fun RecommendedSessionsSection(
    sessions: List<RecommendedSession>,
    onSessionClick: (String) -> Unit
) {
    Column {
        Text(
            text = "Recommended sessions for you:",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        sessions.forEach { session ->
            SessionRecommendationCard(
                session = session,
                onSessionClick = onSessionClick
            )
            
            if (session != sessions.last()) {
                Spacer(modifier = Modifier.height(6.dp))
            }
        }
    }
}

@Composable
private fun SessionRecommendationCard(
    session: RecommendedSession,
    onSessionClick: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSessionClick(session.id) },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = session.thumbnailUrl,
                contentDescription = session.title,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = session.title,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1
                )
                
                Text(
                    text = "${session.duration / 60} min • ${session.instructorName}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    maxLines = 1
                )
                
                if (session.reason.isNotEmpty()) {
                    Text(
                        text = session.reason,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.primary
                        ),
                        maxLines = 2
                    )
                }
            }
            
            IconButton(
                onClick = { onSessionClick(session.id) }
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play session",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun SafetyAlertSection(
    message: ChatMessage,
    onHelplineClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Safety alert",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = "Support Available",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "If you're in crisis, please reach out for help. You're not alone.",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = onHelplineClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(
                    text = "Get Help Now",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onError
                    )
                )
            }
        }
    }
}

@Composable
private fun QuickReplyChips(
    replies: List<String>,
    onReplyClick: (String) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        replies.forEach { reply ->
            FilterChip(
                selected = false,
                onClick = { onReplyClick(reply) },
                label = {
                    Text(
                        text = reply,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            )
        }
    }
}

@Composable
private fun TypingIndicator(
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(8.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary
            ) {}
            
            Spacer(modifier = Modifier.width(4.dp))
            
            Surface(
                modifier = Modifier.size(8.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary
            ) {}
            
            Spacer(modifier = Modifier.width(4.dp))
            
            Surface(
                modifier = Modifier.size(8.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary
            ) {}
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatInput(
    message: String,
    onMessageChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    onQuickReplyClick: (String) -> Unit,
    suggestedReplies: List<String>,
    isLoading: Boolean,
    enabled: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        // Suggested quick replies
        if (suggestedReplies.isNotEmpty()) {
            QuickReplyChips(
                replies = suggestedReplies,
                onReplyClick = onQuickReplyClick
            )
            
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        // Input field
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            OutlinedTextField(
                value = message,
                onValueChange = onMessageChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Type your message...") },
                shape = RoundedCornerShape(24.dp),
                maxLines = 4,
                enabled = enabled
            )
            
            IconButton(
                onClick = onSendMessage,
                enabled = enabled && message.isNotBlank() && !isLoading,
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.primary,
                        CircleShape
                    )
                    .size(48.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

private fun formatTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60000 -> "Just now"
        diff < 3600000 -> "${diff / 60000}m ago"
        diff < 86400000 -> "${diff / 3600000}h ago"
        else -> java.text.SimpleDateFormat("MMM dd", java.util.Locale.getDefault())
            .format(java.util.Date(timestamp))
    }
}
