package com.drmindit.android.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.drmindit.shared.domain.analytics.*
import com.drmindit.shared.data.repository.MoodAnalyticsRepository
import kotlinx.coroutines.launch

/**
 * Mood Entry Screen
 * Interface for users to log their mood and related data
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoodEntryScreen(
    repository: MoodAnalyticsRepository,
    userId: String,
    onNavigateBack: () -> Unit,
    onSaveSuccess: () -> Unit
) {
    val scope = rememberCoroutineScope()
    
    // Form state
    var selectedMood by remember { mutableStateOf<MoodType?>(null) }
    var moodScore by remember { mutableStateOf(3) }
    var selectedEnergy by remember { mutableStateOf<EnergyLevel?>(null) }
    var selectedTags by remember { mutableStateOf<Set<String>>(emptySet()) }
    var notes by remember { mutableStateOf("") }
    var sleepQuality by remember { mutableStateOf<Int?>(null) }
    var stressLevel by remember { mutableStateOf<Int?>(null) }
    var socialInteraction by remember { mutableStateOf<Boolean?>(null) }
    var medicationTaken by remember { mutableStateOf<Boolean?>(null) }
    var therapySession by remember { mutableStateOf<Boolean?>(null) }
    
    // UI state
    var isSaving by remember { mutableStateOf(false) }
    var showSuccessMessage by remember { mutableStateOf(false) }
    
    // Available tags
    val availableTags = listOf(
        "work", "sleep", "relationships", "family", "friends", "exercise",
        "meditation", "stress", "anxiety", "health", "weather",
        "food", "social", "alone", "creative", "learning", "travel"
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE8F5E8), // Soft green
                        Color(0xFFF3E5F5), // Soft purple
                        Color(0xFFE3F2FD)  // Soft blue
                    ),
                    startY = 0f
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            MoodEntryHeader(onNavigateBack = onNavigateBack)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Main form content
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Mood Selection
                    MoodSelectionSection(
                        selectedMood = selectedMood,
                        onMoodSelected = { selectedMood = it },
                        moodScore = moodScore,
                        onScoreChanged = { moodScore = it }
                    )
                    
                    // Energy Level Selection
                    EnergyLevelSection(
                        selectedEnergy = selectedEnergy,
                        onEnergySelected = { selectedEnergy = it }
                    )
                    
                    // Tags Selection
                    TagsSelectionSection(
                        selectedTags = selectedTags,
                        availableTags = availableTags,
                        onTagsChanged = { selectedTags = it }
                    )
                    
                    // Additional Details
                    AdditionalDetailsSection(
                        sleepQuality = sleepQuality,
                        onSleepQualityChanged = { sleepQuality = it },
                        stressLevel = stressLevel,
                        onStressLevelChanged = { stressLevel = it },
                        socialInteraction = socialInteraction,
                        onSocialInteractionChanged = { socialInteraction = it },
                        medicationTaken = medicationTaken,
                        onMedicationTakenChanged = { medicationTaken = it },
                        therapySession = therapySession,
                        onTherapySessionChanged = { therapySession = it }
                    )
                    
                    // Notes
                    NotesSection(
                        notes = notes,
                        onNotesChanged = { notes = it }
                    )
                    
                    // Save Button
                    SaveButton(
                        isSaving = isSaving,
                        showSuccessMessage = showSuccessMessage,
                        onSave = {
                            if (validateForm(selectedMood, selectedEnergy)) {
                                scope.launch {
                                    isSaving = true
                                    
                                    val moodEntry = MoodEntry(
                                        id = generateMoodEntryId(),
                                        userId = userId,
                                        mood = selectedMood!!,
                                        score = moodScore,
                                        energyLevel = selectedEnergy!!,
                                        tags = selectedTags.toList(),
                                        notes = notes.ifBlank { null },
                                        sleepQuality = sleepQuality,
                                        stressLevel = stressLevel,
                                        socialInteraction = socialInteraction,
                                        medicationTaken = medicationTaken,
                                        therapySession = therapySession
                                    )
                                    
                                    val result = repository.saveMoodEntry(moodEntry)
                                    
                                    isSaving = false
                                    
                                    if (result.isSuccess) {
                                        showSuccessMessage = true
                                        onSaveSuccess()
                                        
                                        // Reset form after delay
                                        kotlinx.coroutines.delay(2000)
                                        resetForm(
                                            onMoodReset = { selectedMood = null },
                                            onEnergyReset = { selectedEnergy = null },
                                            onTagsReset = { selectedTags = emptySet() },
                                            onNotesReset = { notes = "" },
                                            onSleepQualityReset = { sleepQuality = null },
                                            onStressLevelReset = { stressLevel = null },
                                            onSocialInteractionReset = { socialInteraction = null },
                                            onMedicationTakenReset = { medicationTaken = null },
                                            onTherapySessionReset = { therapySession = null }
                                        )
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

/**
 * Mood Entry Header
 */
@Composable
private fun MoodEntryHeader(onNavigateBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "How are you feeling?",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "Track your mood and emotions",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
        
        Spacer(modifier = Modifier.width(40.dp)) // Balance the back button
    }
}

/**
 * Mood Selection Section
 */
@Composable
private fun MoodSelectionSection(
    selectedMood: MoodType?,
    onMoodSelected: (MoodType) -> Unit,
    moodScore: Int,
    onScoreChanged: (Int) -> Unit
) {
    Column {
        Text(
            text = "Current Mood",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Mood selection grid
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(MoodType.values()) { mood ->
                MoodOptionCard(
                    mood = mood,
                    isSelected = selectedMood == mood,
                    onSelected = { onMoodSelected(mood) }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Mood score slider
        Text(
            text = "Mood Intensity: $moodScore",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Slider(
            value = moodScore.toFloat(),
            onValueChange = { onScoreChanged(it.toInt()) },
            valueRange = 1f..5f,
            steps = 4,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
            )
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "1",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Text(
                text = "5",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

/**
 * Individual Mood Option Card
 */
@Composable
private fun MoodOptionCard(
    mood: MoodType,
    isSelected: Boolean,
    onSelected: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) {
                    Color(android.graphics.Color.parseColor(mood.color)).copy(alpha = 0.2f)
                } else {
                    MaterialTheme.colorScheme.surface
                }
            ),
        onClick = onSelected,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                Color(android.graphics.Color.parseColor(mood.color)).copy(alpha = 0.2f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 1.dp
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Mood color indicator
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(android.graphics.Color.parseColor(mood.color)))
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = mood.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = mood.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = Color(android.graphics.Color.parseColor(mood.color)),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

/**
 * Energy Level Section
 */
@Composable
private fun EnergyLevelSection(
    selectedEnergy: EnergyLevel?,
    onEnergySelected: (EnergyLevel) -> Unit
) {
    Column {
        Text(
            text = "Energy Level",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(EnergyLevel.values()) { energy ->
                EnergyOptionCard(
                    energy = energy,
                    isSelected = selectedEnergy == energy,
                    onSelected = { onEnergySelected(energy) }
                )
            }
        }
    }
}

/**
 * Individual Energy Option Card
 */
@Composable
private fun EnergyOptionCard(
    energy: EnergyLevel,
    isSelected: Boolean,
    onSelected: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) {
                    Color(android.graphics.Color.parseColor(energy.color)).copy(alpha = 0.2f)
                } else {
                    MaterialTheme.colorScheme.surface
                }
            ),
        onClick = onSelected,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                Color(android.graphics.Color.parseColor(energy.color)).copy(alpha = 0.2f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 1.dp
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Energy color indicator
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(android.graphics.Color.parseColor(energy.color)))
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = energy.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
            }
            
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = Color(android.graphics.Color.parseColor(energy.color)),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

/**
 * Tags Selection Section
 */
@Composable
private fun TagsSelectionSection(
    selectedTags: Set<String>,
    availableTags: List<String>,
    onTagsChanged: (Set<String>) -> Unit
) {
    Column {
        Text(
            text = "Tags (What's influencing your mood?)",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(availableTags.chunked(2)) { tagRow ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    tagRow.forEach { tag ->
                        FilterChip(
                            onClick = {
                                val newTags = if (selectedTags.contains(tag)) {
                                    selectedTags - tag
                                } else {
                                    selectedTags + tag
                                }
                                onTagsChanged(newTags)
                            },
                            label = tag,
                            selected = selectedTags.contains(tag),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    // If odd number of tags, add empty space
                    if (tagRow.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

/**
 * Additional Details Section
 */
@Composable
private fun AdditionalDetailsSection(
    sleepQuality: Int?,
    onSleepQualityChanged: (Int?) -> Unit,
    stressLevel: Int?,
    onStressLevelChanged: (Int?) -> Unit,
    socialInteraction: Boolean?,
    onSocialInteractionChanged: (Boolean?) -> Unit,
    medicationTaken: Boolean?,
    onMedicationTakenChanged: (Boolean?) -> Unit,
    therapySession: Boolean?,
    onTherapySessionChanged: (Boolean?) -> Unit
) {
    Column {
        Text(
            text = "Additional Details",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Sleep Quality
        RatingSliderSection(
            title = "Sleep Quality",
            value = sleepQuality,
            onValueChanged = onSleepQualityChanged,
            min = 1,
            max = 5,
            description = "How well did you sleep last night?"
        )
        
        // Stress Level
        RatingSliderSection(
            title = "Stress Level",
            value = stressLevel,
            onValueChanged = onStressLevelChanged,
            min = 1,
            max = 5,
            description = "How stressed are you feeling?"
        )
        
        // Boolean toggles
        BooleanToggleSection(
            title = "Social Interaction",
            value = socialInteraction,
            onValueChanged = onSocialInteractionChanged,
            description = "Did you interact with others today?"
        )
        
        BooleanToggleSection(
            title = "Medication Taken",
            value = medicationTaken,
            onValueChanged = onMedicationTakenChanged,
            description = "Did you take your medication as prescribed?"
        )
        
        BooleanToggleSection(
            title = "Therapy Session",
            value = therapySession,
            onValueChanged = onTherapySessionChanged,
            description = "Did you have a therapy session today?"
        )
    }
}

/**
 * Rating Slider Section
 */
@Composable
private fun RatingSliderSection(
    title: String,
    value: Int?,
    onValueChanged: (Int?) -> Unit,
    min: Int,
    max: Int,
    description: String
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Optional checkbox
            Checkbox(
                checked = value != null,
                onCheckedChange = { isChecked ->
                    onValueChanged(if (isChecked) min else null)
                }
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Slider (enabled only when checkbox is checked)
            Slider(
                value = value?.toFloat() ?: min.toFloat(),
                onValueChange = { onValueChanged(it.toInt()) },
                enabled = value != null,
                valueRange = min.toFloat()..max.toFloat(),
                steps = max - min,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                ),
                modifier = Modifier.weight(1f)
            )
            
            Text(
                text = value?.toString() ?: "-",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.width(20.dp)
            )
        }
    }
}

/**
 * Boolean Toggle Section
 */
@Composable
private fun BooleanToggleSection(
    title: String,
    value: Boolean?,
    onValueChanged: (Boolean?) -> Unit,
    description: String
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Optional checkbox
            Checkbox(
                checked = value != null,
                onCheckedChange = { isChecked ->
                    onValueChanged(if (isChecked) true else null)
                }
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Yes/No buttons (enabled only when checkbox is checked)
            Row {
                FilterChip(
                    onClick = { onValueChanged(true) },
                    label = "Yes",
                    selected = value == true,
                    enabled = value != null,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                FilterChip(
                    onClick = { onValueChanged(false) },
                    label = "No",
                    selected = value == false,
                    enabled = value != null,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }
    }
}

/**
 * Notes Section
 */
@Composable
private fun NotesSection(
    notes: String,
    onNotesChanged: (String) -> Unit
) {
    Column {
        Text(
            text = "Notes (Optional)",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = notes,
            onValueChange = onNotesChanged,
            placeholder = { Text("Add any additional thoughts...") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                capitalization = KeyboardCapitalization.Sentences
            ),
            maxLines = 4,
            shape = RoundedCornerShape(12.dp)
        )
    }
}

/**
 * Save Button
 */
@Composable
private fun SaveButton(
    isSaving: Boolean,
    showSuccessMessage: Boolean,
    onSave: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (showSuccessMessage) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f),
                    contentColor = Color(0xFF4CAF50)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(24.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Text(
                        text = "Mood entry saved successfully!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else {
            Button(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSaving,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Save Mood Entry",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }
    }
}

/**
 * Helper functions
 */
private fun validateForm(selectedMood: MoodType?, selectedEnergy: EnergyLevel?): Boolean {
    return selectedMood != null && selectedEnergy != null
}

private fun resetForm(
    onMoodReset: () -> Unit,
    onEnergyReset: () -> Unit,
    onTagsReset: () -> Unit,
    onNotesReset: () -> Unit,
    onSleepQualityReset: () -> Unit,
    onStressLevelReset: () -> Unit,
    onSocialInteractionReset: () -> Unit,
    onMedicationTakenReset: () -> Unit,
    onTherapySessionReset: () -> Unit
) {
    onMoodReset()
    onEnergyReset()
    onTagsReset()
    onNotesReset()
    onSleepQualityReset()
    onStressLevelReset()
    onSocialInteractionReset()
    onMedicationTakenReset()
    onTherapySessionReset()
}

private fun generateMoodEntryId(): String {
    return "mood_${System.currentTimeMillis()}_${(1000..9999).random()}"
}

private fun String.ifBlank(default: () -> String): String {
    return if (this.isBlank()) default() else this
}
