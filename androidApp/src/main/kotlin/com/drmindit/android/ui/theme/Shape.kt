package com.drmindit.android.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Premium Shape System for DrMindit
val DrMinditShapes = Shapes(
    // Extra small corners for tags and small elements
    extraSmall = RoundedCornerShape(4.dp),
    
    // Small corners for buttons and inputs
    small = RoundedCornerShape(8.dp),
    
    // Medium corners for cards and containers
    medium = RoundedCornerShape(16.dp),
    
    // Large corners for prominent cards and modals
    large = RoundedCornerShape(24.dp),
    
    // Extra large corners for special containers
    extraLarge = RoundedCornerShape(32.dp),
)

// Custom shapes for specific use cases
object CustomShapes {
    // Button shapes
    val buttonShape = RoundedCornerShape(50.dp) // Fully rounded
    val pillShape = RoundedCornerShape(100.dp) // Extra rounded for pills
    
    // Card shapes
    val cardShape = RoundedCornerShape(20.dp)
    val featuredCardShape = RoundedCornerShape(24.dp)
    val glassCardShape = RoundedCornerShape(16.dp)
    
    // Input shapes
    val inputShape = RoundedCornerShape(12.dp)
    val searchShape = RoundedCornerShape(50.dp)
    
    // Modal shapes
    val modalShape = RoundedCornerShape(28.dp)
    val bottomSheetShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    
    // Special shapes
    val breathingOrbShape = RoundedCornerShape(50.dp)
    val moodChipShape = RoundedCornerShape(20.dp)
    val categoryChipShape = RoundedCornerShape(50.dp)
    
    // Progress indicator shapes
    val progressShape = RoundedCornerShape(50.dp)
    val sliderShape = RoundedCornerShape(50.dp)
}

// Extension properties for easy access
val Shapes.button: RoundedCornerShape
    get() = CustomShapes.buttonShape

val Shapes.card: RoundedCornerShape
    get() = CustomShapes.cardShape

val Shapes.glassCard: RoundedCornerShape
    get() = CustomShapes.glassCardShape

val Shapes.input: RoundedCornerShape
    get() = CustomShapes.inputShape

val Shapes.modal: RoundedCornerShape
    get() = CustomShapes.modalShape
