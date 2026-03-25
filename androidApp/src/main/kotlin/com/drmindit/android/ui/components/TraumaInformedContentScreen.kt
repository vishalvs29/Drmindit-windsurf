package com.drmindit.android.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.drmindit.android.content.*

/**
 * Trauma-Informed Content Screen for Police/Military Users
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TraumaInformedContentScreen(
    contentManager: TraumaInformedContentManager,
    userCategory: UserCategory,
    onSessionSelected: (TraumaInformedSession) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sessions by remember { 
        mutableStateOf(contentManager.getTraumaInformedSessions(userCategory)) 
    }
    val selectedTab by remember { mutableStateOf(ContentTab.SESSIONS) }
    
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        TopAppBar(
            title = { 
                Text(
                    text = when (userCategory) {
                        UserCategory.POLICE -> "Police Support"
                        UserCategory.MILITARY -> "Military Support"
                        UserCategory.FIRST_RESPONDER -> "First Responder Support"
                        else -> "Specialized Content"
                    }
                ) 
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                IconButton(
                    onClick = { /* Open crisis protocols */ }
                ) {
                    Icon(
                        imageVector = Icons.Default.HealthAndSafety,
                        contentDescription = "Crisis Protocols"
                    )
                }
            }
        )
        
        // Tab Row
        TabRow(
            selectedTabIndex = selectedTab.ordinal,
            modifier = Modifier.fillMaxWidth()
        ) {
            ContentTab.values().forEach { tab ->
                Tab(
                    selected = selectedTab == tab,
                    onClick = { selectedTab = tab },
                    text = { Text(tab.title) }
                )
            }
        }
        
        // Tab Content
        when (selectedTab) {
            ContentTab.SESSIONS -> {
                SessionsTabContent(
                    sessions = sessions,
                    onSessionSelected = onSessionSelected
                )
            }
            ContentTab.CRISIS_PROTOCOLS -> {
                CrisisProtocolsTabContent(
                    protocols = contentManager.getCrisisProtocols(userCategory)
                )
            }
            ContentTab.STRESS_TECHNIQUES -> {
                StressTechniquesTabContent(
                    techniques = contentManager.getStressManagementTechniques(userCategory)
                )
            }
        }
    }
}

@Composable
private fun SessionsTabContent(
    sessions: List<TraumaInformedSession>,
    onSessionSelected: (TraumaInformedSession) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Specialized Sessions for Your Profession",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Evidence-based content designed specifically for the unique stressors and experiences of your profession.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Sessions List
        sessions.forEach { session ->
            SessionCard(
                session = session,
                onSessionSelected = onSessionSelected
            )
            
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun SessionCard(
    session: TraumaInformedSession,
    onSessionSelected: (TraumaInformedSession) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = { onSessionSelected(session) }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Warning Label
            session.warningLabel?.let { warning ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFF3E0)
                    )
                ) {
                    Text(
                        text = "⚠️ $warning",
                        modifier = Modifier.padding(8.dp),
                        fontSize = 12.sp,
                        color = Color(0xFFD84315)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // Session Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = session.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = session.description,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Tags
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        session.tags.take(3).forEach { tag ->
                            AssistChip(
                                onClick = { /* Filter by tag */ },
                                label = { Text(tag, fontSize = 10.sp) }
                            )
                        }
                    }
                }
                
                // Duration and Level
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "${session.duration / 60} min",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = session.contentLevel.name,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Additional Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Instructor: ${session.instructorName}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = "Target: ${session.targetAudience.name}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CrisisProtocolsTabContent(
    protocols: List<CrisisProtocol>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Crisis Response Protocols",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        protocols.forEach { protocol ->
            CrisisProtocolCard(protocol = protocol)
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun CrisisProtocolCard(protocol: CrisisProtocol) {
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = protocol.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                
                if (protocol.timeSensitive) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = "Time Sensitive",
                        tint = Color.Red
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = protocol.description,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Expand/Collapse button
            TextButton(
                onClick = { expanded = !expanded }
            ) {
                Text(if (expanded) "Show Less" else "Show Steps")
            }
            
            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))
                
                // Steps
                protocol.steps.forEachIndexed { index, step ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "${index + 1}.",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        
                        Text(
                            text = step,
                            fontSize = 14.sp,
                            lineHeight = 18.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                // Emergency Contacts
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFF3E0)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "Emergency Contacts",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD84315)
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        protocol.emergencyContacts.forEach { contact ->
                            Text(
                                text = contact,
                                fontSize = 12.sp,
                                color = Color(0xFF92400)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StressTechniquesTabContent(
    techniques: List<StressManagementTechnique>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Quick Stress Management Techniques",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        techniques.forEach { technique ->
            TechniqueCard(technique = technique)
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun TechniqueCard(technique: StressManagementTechnique) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = technique.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                
                // Effectiveness and Difficulty badges
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    AssistChip(
                        onClick = { },
                        label = { 
                            Text(
                                text = technique.effectiveness.name,
                                fontSize = 10.sp
                            ) 
                        }
                    )
                    AssistChip(
                        onClick = { },
                        label = { 
                            Text(
                                text = technique.difficulty.name,
                                fontSize = 10.sp
                            ) 
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = technique.description,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Duration: ${technique.duration / 60} minutes",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

enum class ContentTab(val title: String) {
    SESSIONS("Sessions"),
    CRISIS_PROTOCOLS("Crisis Protocols"),
    STRESS_TECHNIQUES("Stress Techniques")
}
