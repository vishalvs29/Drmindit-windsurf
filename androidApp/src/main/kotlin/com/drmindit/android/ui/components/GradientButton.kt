package com.drmindit.android.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    gradient: Brush = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF4FD1C5), // Teal
            Color(0xFF667EEA), // Purple
        )
    ),
    textColor: Color = Color.White,
    cornerRadius: androidx.compose.ui.unit.Dp = 50.dp,
    contentPadding: PaddingValues = PaddingValues(horizontal = 24.dp, vertical = 16.dp)
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(durationMillis = 100)
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .clickable(
                enabled = enabled && !loading,
                onClick = {
                    isPressed = true
                    onClick()
                    isPressed = false
                }
            )
            .background(gradient)
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0x33FFFFFF),
                        Color(0x1AFFFFFF)
                    )
                ),
                shape = RoundedCornerShape(cornerRadius)
            )
            .padding(contentPadding),
        contentAlignment = Alignment.Center
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = textColor,
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = text,
                color = textColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    textColor: Color = Color(0xFF4FD1C5),
    borderColor: Color = Color(0xFF4FD1C5),
    cornerRadius: androidx.compose.ui.unit.Dp = 50.dp,
    contentPadding: PaddingValues = PaddingValues(horizontal = 24.dp, vertical = 16.dp)
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(durationMillis = 100)
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .clickable(
                enabled = enabled,
                onClick = {
                    isPressed = true
                    onClick()
                    isPressed = false
                }
            )
            .background(Color(0x0DFFFFFF)) // 5% white
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(cornerRadius)
            )
            .padding(contentPadding),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun IconButton(
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: androidx.compose.ui.unit.Dp = 56.dp,
    backgroundColor: Color = Color(0xFF4FD1C5),
    contentColor: Color = Color.White,
    cornerRadius: androidx.compose.ui.unit.Dp = 50.dp
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(durationMillis = 100)
    )

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(cornerRadius))
            .clickable(
                enabled = enabled,
                onClick = {
                    isPressed = true
                    onClick()
                    isPressed = false
                }
            )
            .background(backgroundColor)
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            icon()
        }
    }
}
