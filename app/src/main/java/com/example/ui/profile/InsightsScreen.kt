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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.Icons
import com.example.data.IntentSession
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(historicalSessions: List<IntentSession>, onBack: () -> Unit) {
    val todaysData = remember(historicalSessions) {
        val startOfDay = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        historicalSessions.filter { it.timestamp >= startOfDay }
    }

    val appStats = remember(todaysData) {
        todaysData.groupBy { it.appName }.mapValues { entry ->
            val total = entry.value.size
            val skipped = entry.value.count { !it.userContinued }
            val opened = entry.value.count { it.userContinued }
            AppStat(entry.key, total, skipped, opened)
        }.values.toList().sortedByDescending { it.total }
    }
    
    val totalIntercepts = todaysData.size
    val totalSkipped = todaysData.count { !it.userContinued }
    val improvementPoints = remember(appStats) {
        val points = mutableListOf<String>()
        if (totalIntercepts == 0) {
            points.add("Looks like a quiet day! Make sure Accessibility is enabled if you aren't seeing data.")
        } else {
            val mostOpened = appStats.maxByOrNull { it.opened }
            if (mostOpened != null && mostOpened.opened > 0) {
                points.add("You've pushed through the breathing window to open ${mostOpened.appName} ${mostOpened.opened} times today. Try finding an alternative activity when you feel the urge to check it.")
            }
            val bestSkipped = appStats.maxByOrNull { it.skipped }
            if (bestSkipped != null && bestSkipped.skipped > 0) {
                points.add("Great job avoiding ${bestSkipped.appName}! You skipped it ${bestSkipped.skipped} times.")
            }
            
            val successRate = if (totalIntercepts > 0) (totalSkipped.toFloat() / totalIntercepts * 100).toInt() else 0
            if (successRate > 60) {
                points.add("You have a high success rate ($successRate%) today. Keep up the good work!")
            } else if (successRate > 0) {
                points.add("A $successRate% success rate means there is still room to pause and reconsider before opening apps.")
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
                        Icon(androidx.compose.material.icons.Icons.Default.ArrowBack, contentDescription = "Back")
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
                Text("Improvement Points", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                improvementPoints.forEach { point ->
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
