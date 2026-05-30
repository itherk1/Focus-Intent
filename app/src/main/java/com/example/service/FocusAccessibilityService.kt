package com.example.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import com.example.data.AppConfigRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class FocusAccessibilityService : AccessibilityService() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)
    private lateinit var configRepo: AppConfigRepository

    override fun onServiceConnected() {
        super.onServiceConnected()
        configRepo = AppConfigRepository(applicationContext)
    }

    private var lastInterceptedPackage: String? = null
    private var lastInterceptTime: Long = 0

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString() ?: return
            
            // Exclude our own app
            if (packageName == applicationContext.packageName) return
            
            // Check if it's a launcher (optional, but good)
            val launcherIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
            }
            val pm = packageManager
            val resolveInfo = pm.resolveActivity(launcherIntent, 0)
            val isLauncher = resolveInfo?.activityInfo?.packageName == packageName

            configRepo.clearAllAllowancesExcept(packageName)

            if (isLauncher) {
                return
            }

            scope.launch {
                val blockedApps = configRepo.blockedApps.firstOrNull() ?: emptySet()
                if (blockedApps.contains(packageName) && !configRepo.isAppTemporarilyAllowed(packageName)) {
                    val now = System.currentTimeMillis()
                    if (packageName == lastInterceptedPackage && now - lastInterceptTime < 2000) {
                        return@launch // debounce
                    }
                    lastInterceptedPackage = packageName
                    lastInterceptTime = now

                    // Block the app by launching our intercept screen
                    val intent = Intent(applicationContext, com.example.MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        putExtra("intercept_package", packageName)
                    }
                    startActivity(intent)
                }
            }
        }
    }

    override fun onInterrupt() {
        // No op
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
