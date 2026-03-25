package com.drmindit.android.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * DPDP Act 2023 Consent Screen
 * Displays comprehensive consent information and captures user agreement
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DPDPConsentScreen(
    onConsentGiven: () -> Unit,
    onConsentDeclined: () -> Unit,
    onViewPrivacyPolicy: () -> Unit,
    modifier: Modifier = Modifier
) {
    var agreedToTerms by remember { mutableStateOf(false) }
    var agreedToDataProcessing by remember { mutableStateOf(false) }
    var agreedToSharing by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    
    val scrollState = rememberScrollState()
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Data Protection & Privacy",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Text(
                    text = "DPDP Act 2023 Compliance",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        
        // Introduction
        ConsentSection(
            title = "Your Privacy Matters",
            content = "DrMindit is committed to protecting your personal data in accordance with the Digital Personal Data Protection (DPDP) Act 2023. We collect and process only the data necessary to provide you with personalized mental health support."
        )
        
        // Data Collection
        ConsentSection(
            title = "What Data We Collect",
            content = buildString {
                appendLine("• Personal Information: Name, email, age")
                appendLine("• Health Data: Mood tracking, session progress, wellness assessments")
                appendLine("• Usage Data: App usage patterns, feature interactions")
                appendLine("• Device Data: Device type, operating system")
                appendLine("• Communication Data: Chat messages with AI assistant")
            }
        )
        
        // Purpose of Processing
        ConsentSection(
            title = "How We Use Your Data",
            content = buildString {
                appendLine("• Service Provision: To provide personalized mental health support")
                appendLine("• Personalization: To tailor content and recommendations")
                appendLine("• Safety Monitoring: To detect and respond to crisis situations")
                appendLine("• Analytics: To improve our services and user experience")
                appendLine("• Legal Compliance: To meet regulatory requirements")
            }
        )
        
        // Data Sharing
        ConsentSection(
            title = "Data Sharing & Security",
            content = "We share your data only when necessary for service provision, legal compliance, or with your explicit consent. All data is encrypted and stored securely in compliance with Indian data protection laws."
        )
        
        // Your Rights
        ConsentSection(
            title = "Your Rights Under DPDP Act",
            content = buildString {
                appendLine("• Right to Access: Request a copy of your personal data")
                appendLine("• Right to Correction: Update inaccurate personal data")
                appendLine("• Right to Deletion: Request deletion of your personal data")
                appendLine("• Right to Portability: Request data transfer to other services")
                appendLine("• Right to Withdraw Consent: Revoke consent at any time")
            }
        )
        
        // Consent Checkboxes
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Please provide your consent:",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                
                ConsentCheckbox(
                    text = "I have read and understood the Privacy Policy",
                    checked = agreedToTerms,
                    onCheckedChange = { agreedToTerms = it }
                )
                
                ConsentCheckbox(
                    text = "I consent to the collection and processing of my personal data for service provision",
                    checked = agreedToDataProcessing,
                    onCheckedChange = { agreedToDataProcessing = it }
                )
                
                ConsentCheckbox(
                    text = "I consent to data processing for personalization and analytics",
                    checked = agreedToSharing,
                    onCheckedChange = { agreedToSharing = it }
                )
            }
        }
        
        // Action Buttons
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    isLoading = true
                    onConsentGiven()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = agreedToTerms && agreedToDataProcessing && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("I Agree & Continue")
            }
            
            OutlinedButton(
                onClick = onConsentDeclined,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                Text("Decline")
            }
            
            TextButton(
                onClick = onViewPrivacyPolicy,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("View Full Privacy Policy")
            }
        }
        
        // Footer Information
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Important Information",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "• You can withdraw your consent at any time through Settings\n" +
                          "• Withdrawing consent may affect service functionality\n" +
                          "• Data will be deleted within 30 days of consent withdrawal\n" +
                          "• For questions, contact privacy@drmindit.com",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
private fun ConsentSection(
    title: String,
    content: String
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = content,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun ConsentCheckbox(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = MaterialTheme.colorScheme.primary
            )
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = text,
            fontSize = 14.sp,
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * Privacy Policy Viewer Screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(
    privacyPolicy: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        TopAppBar(
            title = { Text("Privacy Policy") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = "Back")
                }
            }
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = privacyPolicy,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }
    }
}
