package com.example.ui.home

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import android.graphics.drawable.Drawable
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class AppInfo(val packageName: String, val appName: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSelectionScreen(
    blockedApps: Set<String>,
    onToggleApp: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var installedApps by remember { mutableStateOf<List<AppInfo>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    val iconCache = remember { android.util.LruCache<String, Drawable>(100) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            try {
                val pm = context.packageManager
                val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
                val apps = packages
                    .filter { it.flags and ApplicationInfo.FLAG_SYSTEM == 0 } // Filter out system apps
                    .mapNotNull { 
                        try { 
                            val appName = pm.getApplicationLabel(it).toString()
                            val packageName = it.packageName
                            AppInfo(packageName, appName)
                        } catch (e: Exception) { null } 
                    }
                    .sortedBy { it.appName }
                    .distinctBy { it.packageName }
                installedApps = apps.ifEmpty { listOf(AppInfo("com.instagram.android", "Instagram"), AppInfo("com.facebook.katana", "Facebook")) }
            } catch (e: Exception) {
                installedApps = listOf(AppInfo("com.instagram.android", "Instagram"), AppInfo("com.facebook.katana", "Facebook"))
            }
        }
    }

    val filteredApps = remember(installedApps, searchQuery, blockedApps) {
        val list = if (searchQuery.isBlank()) {
            installedApps
        } else {
            installedApps.filter { it.appName.contains(searchQuery, ignoreCase = true) }
        }
        list.sortedWith(compareBy({ !blockedApps.contains(it.packageName) }, { it.appName }))
    }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text("Blocked Apps", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingVals ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 350.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingVals)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    text = "Select apps to add an intercept delay before opening.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    shape = MaterialTheme.shapes.medium,
                    placeholder = { Text("Search apps...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    singleLine = true
                )
            }
            if (installedApps.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            } else {
                items(filteredApps, key = { it.packageName }) { appInfo ->
                    val isBlocked = blockedApps.contains(appInfo.packageName)
                    Box(modifier = Modifier.animateItem(fadeInSpec = null, fadeOutSpec = null, placementSpec = spring())) {
                        AppListItem(
                            appName = appInfo.appName,
                            packageName = appInfo.packageName,
                            isBlocked = isBlocked,
                            iconCache = iconCache,
                            onToggle = { onToggleApp(appInfo.packageName) }
                        )
                    }
                }
            }
            item(span = { GridItemSpan(maxLineSpan) }) { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

@Composable
fun AppListItem(appName: String, packageName: String, isBlocked: Boolean, iconCache: android.util.LruCache<String, Drawable>, onToggle: () -> Unit) {
    val context = LocalContext.current
    var icon by remember(packageName) { mutableStateOf<Drawable?>(iconCache.get(packageName)) }
    
    if (icon == null) {
        LaunchedEffect(packageName) {
            withContext(Dispatchers.IO) {
                try {
                    val loadedIcon = context.packageManager.getApplicationIcon(packageName)
                    iconCache.put(packageName, loadedIcon)
                    icon = loadedIcon
                } catch (e: Exception) {
                    // Ignore
                }
            }
        }
    }
    
    val containerColor by animateColorAsState(
        targetValue = if (isBlocked) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        label = "color"
    )
    val tonalElevation by animateDpAsState(
        targetValue = if (isBlocked) 0.dp else 2.dp,
        label = "elevation"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle),
        shape = MaterialTheme.shapes.medium,
        color = containerColor,
        tonalElevation = tonalElevation
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Image(
                    painter = rememberDrawablePainter(drawable = icon),
                    contentDescription = null,
                    modifier = Modifier.size(40.dp).padding(end = 16.dp)
                )
            } else {
                Spacer(modifier = Modifier.size(40.dp).padding(end = 16.dp))
            }
            Text(
                text = appName,
                style = MaterialTheme.typography.titleMedium,
                color = if (isBlocked) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
                fontWeight = if (isBlocked) FontWeight.Bold else FontWeight.Normal
            )
            if (isBlocked) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
