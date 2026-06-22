package com.example.ui.breathe

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun BreathingExerciseScreen(
    appName: String,
    continuousUsageMinutes: Int = 0,
    onFinish: (Boolean) -> Unit // true if opening, false if skipping
) {
    var secondsLeft by remember { mutableIntStateOf(if (continuousUsageMinutes >= 60) 20 else 10) }
    
    LaunchedEffect(Unit) {
        while (secondsLeft > 0) {
            delay(1000)
            secondsLeft--
        }
    }
    
    val infiniteTransition = rememberInfiniteTransition(label = "breathe")
    
    // Smooth, organic scale animation using keyframes
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = androidx.compose.animation.core.keyframes {
                durationMillis = 19000
                0.5f at 0
                1.0f at 4000 using FastOutSlowInEasing // Inhale
                1.0f at 11000 using androidx.compose.animation.core.LinearEasing // Hold
                0.5f at 19000 using FastOutSlowInEasing // Exhale
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "scale"
    )

    // Parallel animation for tracking phase time
    val cycleTime by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 19000f,
        animationSpec = infiniteRepeatable(
            animation = tween(19000, easing = androidx.compose.animation.core.LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "cycleTime"
    )

    val phaseText = when {
        cycleTime < 4000f -> "Inhale"
        cycleTime < 11000f -> "Hold"
        else -> "Exhale"
    }
    
    val primaryColor = MaterialTheme.colorScheme.primary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                )
            )
            .windowInsetsPadding(WindowInsets.safeDrawing),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp).fillMaxWidth()
        ) {
            Text(
                text = "Take a breath",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = if (continuousUsageMinutes > 0) "You've continuously used $appName for $continuousUsageMinutes minutes" else "Before you open $appName",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(300.dp)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(primaryColor.copy(alpha = 0.4f), tertiaryColor.copy(alpha = 0.1f)),
                            radius = maxOf(1f, size.minDimension / 1.5f * scale)
                        ),
                        radius = size.minDimension / 1.5f * scale
                    )
                    drawCircle(
                        color = primaryColor,
                        radius = size.minDimension / 2.5f * scale,
                        style = Stroke(width = 12.dp.toPx())
                    )
                }
                Text(
                    text = phaseText,
                    style = MaterialTheme.typography.displaySmall,
                    color = primaryColor,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            if (secondsLeft > 0) {
                Text(
                    text = "$secondsLeft",
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Black
                )
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FilledTonalButton(
                        onClick = { onFinish(false) }, 
                        modifier = Modifier.fillMaxWidth().height(64.dp),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text(if (continuousUsageMinutes > 0) "I'll close the app" else "I don't need this app right now", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    }
                    TextButton(
                        onClick = { onFinish(true) }, 
                        modifier = Modifier.fillMaxWidth().height(56.dp)
                    ) {
                        Text(if (continuousUsageMinutes > 0) "Continue using $appName" else "Continue to $appName", style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
