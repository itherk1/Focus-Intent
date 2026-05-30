package com.example.ui.profile

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background

import com.example.data.IntentSession
import java.util.Calendar
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    totalInterceptsWeek: Int,
    preventedWeek: Int,
    historicalSessions: List<IntentSession>
) {
    val context = LocalContext.current
    
    // Group real data by day for the last 6 days + today
    val weeklyData: List<Pair<Int, Int>> = remember(historicalSessions) {
        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance()
        
        (0..6).map { daysAgo ->
            val daysAgoInt = 6 - daysAgo
            val startOfDay = calendar.apply {
                timeInMillis = now
                add(Calendar.DAY_OF_YEAR, -daysAgoInt)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            
            val endOfDay = startOfDay + TimeUnit.DAYS.toMillis(1)
            
            val sessionsInDay = historicalSessions.filter { it.timestamp in startOfDay until endOfDay }
            val total = sessionsInDay.size
            val prevented = sessionsInDay.count { !it.userContinued }
            
            total to prevented
        }
    }
    
    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text("Profile & Insights", fontWeight = FontWeight.Black) },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { paddingVals ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingVals)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            
            Text("Weekly Focus Chart", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            
            Card(
                modifier = Modifier.fillMaxWidth().height(200.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(24.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    val preventedColor = MaterialTheme.colorScheme.primary
                    val failedColor = MaterialTheme.colorScheme.error
                    
                    if (totalInterceptsWeek == 0) {
                        Text(
                            "Start blocking apps to build your real focus history.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.align(Alignment.TopCenter)
                        )
                    }
                    
                    Canvas(modifier = Modifier.fillMaxSize().padding(top = 24.dp)) {
                        val barWidth = 32.dp.toPx()
                        val maxVal = maxOf(weeklyData.maxOf { it.first }, 10).toFloat()
                        
                        // Draw the bars using real data
                        weeklyData.forEachIndexed { i, (totalRaw, preventedRaw) ->
                            
                            val totalH = (totalRaw / maxVal) * size.height
                            val preventedH = (preventedRaw / maxVal) * size.height
                            
                            val xPos = i * (size.width / 7) + (size.width / 7 - barWidth) / 2
                            
                            // Draw background (failed/opened)
                            drawRoundRect(
                                color = failedColor.copy(alpha = if (totalInterceptsWeek == 0) 0.3f else 1f),
                                topLeft = Offset(xPos, size.height - totalH),
                                size = Size(barWidth, totalH),
                                cornerRadius = CornerRadius(8.dp.toPx())
                            )
                            
                            // Draw foreground (prevented)
                            drawRoundRect(
                                color = preventedColor.copy(alpha = if (totalInterceptsWeek == 0) 0.3f else 1f),
                                topLeft = Offset(xPos, size.height - preventedH),
                                size = Size(barWidth, preventedH),
                                cornerRadius = CornerRadius(8.dp.toPx())
                            )
                        }
                    }
                }
            }
            
            val todaysData = historicalSessions.filter { 
               it.timestamp >= Calendar.getInstance().apply { 
                  set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
               }.timeInMillis 
            }
            
            Text("Today's Breakdown", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            if (todaysData.isEmpty()) {
                Text("No intercepts today.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                val appCounts = todaysData.groupBy { it.appName }.mapValues { it.value.size }.toList().sortedByDescending { it.second }
                val colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.tertiary, MaterialTheme.colorScheme.error)
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Canvas(modifier = Modifier.size(100.dp)) {
                                var currentAngle = -90f
                                val total = todaysData.size.toFloat()
                                appCounts.forEachIndexed { index, pair ->
                                    val sweepAngle = (pair.second / total) * 360f
                                    drawArc(
                                        color = colors[index % colors.size],
                                        startAngle = currentAngle,
                                        sweepAngle = sweepAngle,
                                        useCenter = true
                                    )
                                    currentAngle += sweepAngle
                                }
                            }
                            Spacer(modifier = Modifier.width(24.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                appCounts.take(4).forEachIndexed { index, pair ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(modifier = Modifier.size(12.dp).clip(RoundedCornerShape(4.dp)).background(colors[index % colors.size]))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("${pair.first}: ${pair.second}", style = MaterialTheme.typography.bodyMedium)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                }
                            }
                        }
                    }
                }
            }
            
            var hasUsagePermission by remember { mutableStateOf(com.example.utils.UsageStatsHelper.hasUsageStatsPermission(context)) }
            var todayUsageStats by remember { mutableStateOf<Map<String, Long>>(emptyMap()) }
            
            val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
            DisposableEffect(lifecycleOwner) {
                val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
                    if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                        hasUsagePermission = com.example.utils.UsageStatsHelper.hasUsageStatsPermission(context)
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                }
            }
            
            LaunchedEffect(hasUsagePermission) {
                if (hasUsagePermission) {
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                        todayUsageStats = com.example.utils.UsageStatsHelper.getTodayUsageStats(context)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (hasUsagePermission) {
                Text("Today's Screen Time", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                if (todayUsageStats.isEmpty()) {
                    Text("No screen time data available.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    val sortedUsage = todayUsageStats.toList().sortedByDescending { it.second }.take(10)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            sortedUsage.forEachIndexed { index, pair ->
                                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(pair.first, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                                    val minutes = TimeUnit.MILLISECONDS.toMinutes(pair.second)
                                    val hours = minutes / 60
                                    val mins = minutes % 60
                                    val timeStr = if (hours > 0) "${hours}h ${mins}m" else "${mins}m"
                                    Text(timeStr, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    shape = RoundedCornerShape(24.dp),
                    onClick = {
                        com.example.utils.UsageStatsHelper.requestUsageStatsPermission(context)
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Unlock Screen Time Insights", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Grant Usage Access to view your actual digital wellbeing stats directly in the app.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f))
                        }
                    }
                }
            }
            
        }
    }
}
