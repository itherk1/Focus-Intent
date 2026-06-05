package com.example.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.Icons
import com.example.data.IntentSession
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(historicalSessions: List<IntentSession>, onBack: () -> Unit) {
    var activeDate by remember { mutableStateOf(Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis) }

    val activeDayData = remember(historicalSessions, activeDate) {
        historicalSessions.filter { it.timestamp >= activeDate && it.timestamp < activeDate + 86400000L }
    }

    val appStats = remember(activeDayData) {
        activeDayData.groupBy { it.appName }.mapValues { entry ->
            val total = entry.value.size
            val skipped = entry.value.count { !it.userContinued }
            val opened = entry.value.count { it.userContinued }
            AppStat(entry.key, total, skipped, opened)
        }.values.toList().sortedByDescending { it.total }
    }
    
    val totalIntercepts = activeDayData.size
    val totalSkipped = activeDayData.count { !it.userContinued }
    val improvementPoints = remember(appStats) {
        val points = mutableListOf<String>()
        if (totalIntercepts == 0) {
            points.add("Looks like a quiet day! Make sure Accessibility is enabled if you aren't seeing data.")
        } else {
            val mostOpened = appStats.maxByOrNull { it.opened }
            val bestSkipped = appStats.maxByOrNull { it.skipped }
            val successRate = if (totalIntercepts > 0) (totalSkipped.toFloat() / totalIntercepts * 100).toInt() else 0
            
            if (successRate >= 80) {
                points.add("🧘 Zen Master: You brushed off almost all distractions today ($successRate% success). Keep building this unshakeable habit!")
            } else if (successRate > 40) {
                points.add("📈 Gaining Ground: You successfully paused and avoided distractions $successRate% of the time today.")
            } else if (successRate > 0) {
                points.add("⚠️ Friction is Good: A lower success rate ($successRate%) means you're still relying heavily on willpower. Try physically moving distracting apps off your home screen!")
            } else {
                points.add("🚨 Distraction Heavy: You pushed through every single breathing window today. Tomorrow is a fresh start to rebuild your patience.")
            }

            if (mostOpened != null && mostOpened.opened > 0) {
                if (mostOpened.opened > 5) {
                    points.add("📱 Arch Nemesis: ${mostOpened.appName} broke through your focus ${mostOpened.opened} times today. Consider increasing the breathing time for this app or removing it temporarily.")
                } else {
                    points.add("🔍 Minor Leaks: You pushed through the breathing window to open ${mostOpened.appName} ${mostOpened.opened} times today.")
                }
            }
            
            if (bestSkipped != null && bestSkipped.skipped > 0) {
                if (bestSkipped.skipped > (mostOpened?.opened ?: 0)) {
                    points.add("🛡️ Epic Defense: Incredible job avoiding ${bestSkipped.appName}! You successfully walked away from it ${bestSkipped.skipped} times.")
                } else {
                    points.add("🛡️ Small Wins: You successfully avoided ${bestSkipped.appName} ${bestSkipped.skipped} times.")
                }
            }
            
            // Peak Active Hour
            if (activeDayData.isNotEmpty()) {
                val mostActiveHour = activeDayData.groupBy { 
                    val cal = Calendar.getInstance()
                    cal.timeInMillis = it.timestamp
                    cal.get(Calendar.HOUR_OF_DAY)
                }.maxByOrNull { it.value.size }
                
                if (mostActiveHour != null) {
                    val hour = mostActiveHour.key
                    val ampm = if (hour >= 12) "PM" else "AM"
                    val formattedHour = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
                    points.add("🕰️ Peak Temptation: You were most distracted around $formattedHour:00 $ampm (${mostActiveHour.value.size} attempts). Try structuring deep work or taking a walk during this window.")
                }
            }

            // Streak insight if they opened a bunch of apps in a short window
            if (activeDayData.size > 3) {
                var maxStreak = 0
                var currentStreak = 0
                val sortedData = activeDayData.sortedBy { it.timestamp }
                for (i in 1 until sortedData.size) {
                    val diff = kotlin.math.abs(sortedData[i].timestamp - sortedData[i-1].timestamp)
                    if (diff < 5 * 60 * 1000) { // within 5 minutes
                        currentStreak++
                        if (currentStreak > maxStreak) maxStreak = currentStreak
                    } else {
                        currentStreak = 0
                    }
                }
                if (maxStreak >= 3) {
                    points.add("🌪️ Doomscroll Alert: You had a sequence of trying to open ${maxStreak + 1} blocked apps within 5-minute windows. When you feel this urge, put your phone in another room.")
                }
            }
        }
        points
    }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text("Detailed Insights", fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors()
            )
        }
    ) { paddingVals ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingVals)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { activeDate -= 86400000L }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Day")
                    }
                    val cal = Calendar.getInstance().apply { timeInMillis = activeDate }
                    val isToday = Calendar.getInstance().apply { 
                        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                    }.timeInMillis == activeDate
                    Text(
                        if (isToday) "Today" else "${cal.get(Calendar.MONTH) + 1}/${cal.get(Calendar.DAY_OF_MONTH)}", 
                        style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { activeDate += 86400000L }, enabled = !isToday) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Day")
                    }
                }
            }
            item {
                Text("Improvement Points", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
            }
            if (improvementPoints.isEmpty()) {
                item {
                    Text("No points available yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                items(improvementPoints) { point ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(8.dp).clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.onTertiaryContainer)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(point, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onTertiaryContainer)
                        }
                    }
                }
            }
            
            item {
                Text("App Breakdown", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            if (appStats.isEmpty()) {
                item {
                    Text("No intercepts today.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                items(appStats) { stat ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                            Text(stat.appName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text("Total Attempts", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("${stat.total}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Avoided (Skipped)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("${stat.skipped}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Continued", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("${stat.opened}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

data class AppStat(val appName: String, val total: Int, val skipped: Int, val opened: Int)
