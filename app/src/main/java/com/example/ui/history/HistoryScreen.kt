package com.example.ui.history

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
import com.example.data.IntentSession
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(historicalSessions: List<IntentSession>) {
    var sortOption by remember { mutableStateOf(SortOption.TIME_DESC) }
    
    val sortedSessions = remember(historicalSessions, sortOption) {
        when (sortOption) {
            SortOption.TIME_DESC -> historicalSessions.sortedByDescending { it.timestamp }
            SortOption.TIME_ASC -> historicalSessions.sortedBy { it.timestamp }
            SortOption.APP_NAME -> historicalSessions.sortedBy { it.appName }
            SortOption.ACTION -> historicalSessions.sortedBy { it.userContinued }
        }
    }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text("Recent Intercepts", fontWeight = FontWeight.Black) },
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
                .padding(horizontal = 24.dp)
        ) {
            ScrollableTabRow(
                selectedTabIndex = sortOption.ordinal,
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                edgePadding = 0.dp
            ) {
                SortOption.entries.forEachIndexed { index, option ->
                    Tab(
                        selected = sortOption == option,
                        onClick = { sortOption = option },
                        text = { Text(option.title) }
                    )
                }
            }
            
            if (sortedSessions.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No intercepts yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(sortedSessions, key = { it.id }) { session ->
                        SessionHistoryItem(session = session)
                    }
                }
            }
        }
    }
}

enum class SortOption(val title: String) {
    TIME_DESC("Newest"),
    TIME_ASC("Oldest"),
    APP_NAME("App Name"),
    ACTION("Action")
}

@Composable
fun SessionHistoryItem(session: IntentSession) {
    val formatter = remember { SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()) }
    val timeString = formatter.format(Date(session.timestamp))
    
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (session.userContinued) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(if (session.userContinued) "X" else "✓", fontWeight = FontWeight.Black, color = if (session.userContinued) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer)
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(session.appName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(if (session.userContinued) "Opened after delay" else "Avoided opening", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            
            Text(timeString, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
