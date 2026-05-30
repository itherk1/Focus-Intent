package com.example.ui.home

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.example.data.IntentSession
import com.example.utils.AccessibilityHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    totalIntercepts: Int,
    preventedLaunches: Int,
    totalInterceptsWeek: Int,
    preventedLaunchesWeek: Int,
    blockedApps: Set<String>,
    onManageBlockedApps: () -> Unit,
    onTestIntercept: (String) -> Unit,
    onOpenProfile: () -> Unit
) {
    val context = LocalContext.current
    var isAccessibilityEnabled by remember { mutableStateOf(true) }

    // Check accessibility status when screen is displayed
    LaunchedEffect(Unit) {
        isAccessibilityEnabled = AccessibilityHelper.isAccessibilityServiceEnabled(context, com.example.service.FocusAccessibilityService::class.java)
    }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text("Focus Intent", fontWeight = FontWeight.Black) },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { paddingVals ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 300.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingVals)
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            if (!isAccessibilityEnabled) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier.fillMaxWidth().clickable { AccessibilityHelper.openAccessibilitySettings(context) }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Accessibility Service Required", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onErrorContainer)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Tap here to enable it in Settings so Focus Intent can detect when you open distractions.", color = MaterialTheme.colorScheme.onErrorContainer)
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth().clickable(onClick = onOpenProfile),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text("Today's Focus", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Spacer(modifier = Modifier.height(8.dp))
                        val successRate = if (totalIntercepts > 0) ((preventedLaunches.toFloat() / totalIntercepts.toFloat()) * 100).toInt() else 0
                        val savedTimeSeconds = preventedLaunches * 300 // estimate 5 min saved per prevention
                        val savedTimeMinutes = savedTimeSeconds / 60
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text("$successRate%", style = MaterialTheme.typography.displayLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text(" avoided", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 8.dp, start = 8.dp), color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("$preventedLaunches / $totalIntercepts", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                Text("Apps Dodged", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("~${savedTimeMinutes} min", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                Text("Time Saved", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                            }
                        }
                    }
                }
            }
            
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().clickable(onClick = onOpenProfile),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text("This Week", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSecondaryContainer)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("$preventedLaunchesWeek / $totalInterceptsWeek", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                Text("Apps Dodged", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f))
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("~${(preventedLaunchesWeek * 300) / 60} min", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                Text("Time Saved", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f))
                            }
                        }
                    }
                }
            }

            item {
                StatCard(
                    title = "Manage Blocked Apps",
                    subtitle = "${blockedApps.size} apps are currently delayed",
                    onClick = onManageBlockedApps
                )
            }
            
            if (blockedApps.isNotEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "System Limitation Notes:\n• To intercept notifications or deep links directly, Accessibility Services are required (which block play store apps without special approval).\n• Digital Wellbeing integration is restricted by Android OS and cannot be directly queried or written to by 3rd party apps without Root or System-level permissions.",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            item(span = { GridItemSpan(maxLineSpan) }) { Spacer(modifier = Modifier.height(48.dp)) }
        }
    }
}


@Composable
fun StatCard(title: String, subtitle: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                Spacer(modifier = Modifier.height(4.dp))
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f))
            }
            Icon(Icons.AutoMirrored.Filled.List, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
        }
    }
}
