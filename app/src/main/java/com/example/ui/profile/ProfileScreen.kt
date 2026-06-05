package com.example.ui.profile

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.drawscope.clipPath
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
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.toArgb

import com.example.data.IntentSession
import java.util.Calendar
import java.util.concurrent.TimeUnit

import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.items

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    totalInterceptsWeek: Int,
    preventedWeek: Int,
    historicalSessions: List<IntentSession>,
    onOpenInsights: () -> Unit
) {
    val context = LocalContext.current
    
    data class DayStats(val total: Int, val preventedByApp: Map<String, Int>)
    
    var weeklyData by remember { mutableStateOf<List<DayStats>>(emptyList()) }
    var todaysData by remember { mutableStateOf<List<IntentSession>>(emptyList()) }
    var appCounts by remember { mutableStateOf<List<Pair<String, Int>>>(emptyList()) }

    LaunchedEffect(historicalSessions) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
            val now = System.currentTimeMillis()
            val calendar = Calendar.getInstance()
            
            val computedWeekly = (0..6).map { daysAgo ->
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
                
                val preventedSessions = sessionsInDay.filter { !it.userContinued }
                val preventedByApp = preventedSessions.groupBy { it.appName }.mapValues { it.value.size }
                
                DayStats(total, preventedByApp)
            }
            weeklyData = computedWeekly
            
            val todayStart = calendar.apply { 
                timeInMillis = now
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }.timeInMillis 
            
            val computedToday = historicalSessions.filter { it.timestamp >= todayStart }
            todaysData = computedToday
            appCounts = computedToday.groupBy { it.appName }.mapValues { it.value.size }.toList().sortedByDescending { it.second }
        }
    }
    val baseColor = MaterialTheme.colorScheme.primary
    val dynamicColors = remember(appCounts, baseColor) {
        appCounts.mapIndexed { i, _ ->
            val hsl = FloatArray(3)
            androidx.core.graphics.ColorUtils.colorToHSL(baseColor.toArgb(), hsl)
            hsl[0] = (hsl[0] + (i * (360f / appCounts.size))) % 360f
            androidx.compose.ui.graphics.Color(androidx.core.graphics.ColorUtils.HSLToColor(hsl))
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
        LazyVerticalGrid(
            columns = androidx.compose.foundation.lazy.grid.GridCells.Adaptive(minSize = 350.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingVals)
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            
            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                Text("Weekly Focus Chart", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            
            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                Card(
                    modifier = Modifier.fillMaxWidth().clickable(onClick = onOpenInsights),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        val preventedColor = MaterialTheme.colorScheme.primary
                        val failedColor = MaterialTheme.colorScheme.error
                        val maxVal = maxOf(if(weeklyData.isEmpty()) 5 else weeklyData.maxOf { it.total }, 5).toFloat()
                        
                        if (totalInterceptsWeek == 0) {
                            Text(
                                "Start blocking apps to build your real focus history.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 16.dp)
                            )
                        }
                        
                        Row(modifier = Modifier.fillMaxWidth().height(200.dp)) {
                            // Y-Axis Metric
                            Column(
                                modifier = Modifier.width(32.dp).fillMaxHeight().padding(bottom = 24.dp), // align with bottom of graph
                                verticalArrangement = Arrangement.SpaceBetween,
                                horizontalAlignment = Alignment.End
                            ) {
                                Text(maxVal.toInt().toString(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text((maxVal / 2).toInt().toString(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("0", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            // Graph and X-Axis
                            Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                                Canvas(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                    val barWidth = 24.dp.toPx()
                                    
                                    // Draw horizontal grid lines
                                    val gridColor = preventedColor.copy(alpha = 0.1f)
                                    drawLine(color = gridColor, start = Offset(0f, 0f), end = Offset(size.width, 0f), strokeWidth = 1.dp.toPx())
                                    drawLine(color = gridColor, start = Offset(0f, size.height / 2), end = Offset(size.width, size.height / 2), strokeWidth = 1.dp.toPx())
                                    drawLine(color = gridColor, start = Offset(0f, size.height), end = Offset(size.width, size.height), strokeWidth = 1.dp.toPx())

                                     // Draw the bars using real data
                                    weeklyData.forEachIndexed { i, dayStat ->
                                        val totalH = (dayStat.total / maxVal) * size.height
                                        
                                        val sectionWidth = size.width / 7
                                        val xPos = i * sectionWidth + (sectionWidth - barWidth) / 2
                                        
                                        val barPath = androidx.compose.ui.graphics.Path().apply {
                                            addRoundRect(
                                                androidx.compose.ui.geometry.RoundRect(
                                                    left = xPos,
                                                    top = size.height - totalH,
                                                    right = xPos + barWidth,
                                                    bottom = size.height,
                                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx())
                                                )
                                            )
                                        }

                                        clipPath(barPath) {
                                            // Draw background (failed/opened)
                                            drawRect(
                                                color = failedColor.copy(alpha = if (totalInterceptsWeek == 0) 0.3f else 1f),
                                                topLeft = Offset(xPos, size.height - totalH),
                                                size = Size(barWidth, totalH)
                                            )
                                            
                                            // Draw foreground (prevented) - STACKED
                                            var currentY = size.height
                                            dayStat.preventedByApp.forEach { (appName, count) ->
                                                val partH = (count * 1f / maxVal) * size.height
                                                // Get color for app
                                                val appIndex = appCounts.indexOfFirst { it.first == appName }
                                                val barColor = if(appIndex >= 0 && dynamicColors.isNotEmpty()) dynamicColors[appIndex % dynamicColors.size] else preventedColor
                                                
                                                drawRect(
                                                    color = barColor.copy(alpha = if (totalInterceptsWeek == 0) 0.3f else 1f),
                                                    topLeft = Offset(xPos, currentY - partH),
                                                    size = Size(barWidth, partH)
                                                )
                                                currentY -= partH
                                            }
                                        }
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                // X-Axis Labels
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    listOf("6d", "5d", "4d", "3d", "2d", "1d", "Today").forEach { label ->
                                        Text(
                                            text = label, 
                                            style = MaterialTheme.typography.labelSmall, 
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.weight(1f),
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                Text("Today's Breakdown", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            
            item {
                if (todaysData.isEmpty()) {
                    Text("No intercepts today.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable(onClick = onOpenInsights),
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
                                            color = dynamicColors[index % dynamicColors.size],
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
                                            Box(modifier = Modifier.size(12.dp).clip(RoundedCornerShape(4.dp)).background(dynamicColors[index % dynamicColors.size]))
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
            }
            
            if (hasUsagePermission) {
                item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                    Text("Today's Screen Time", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
                item {
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
                                        val minutes = java.util.concurrent.TimeUnit.MILLISECONDS.toMinutes(pair.second)
                                        val hours = minutes / 60
                                        val mins = minutes % 60
                                        val timeStr = if (hours > 0) "${hours}h ${mins}m" else "${mins}m"
                                        Text(timeStr, style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                item {
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
}
