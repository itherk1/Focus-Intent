package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.AppConfigRepository
import com.example.data.AppDatabase
import com.example.data.AppRepository
import com.example.ui.breathe.BreathingExerciseScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.FocusViewModel
import com.example.viewmodel.FocusViewModelFactory

class BreatheActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val database = AppDatabase.getDatabase(applicationContext)
        val appRepo = AppRepository(database.intentDao())
        val configRepo = AppConfigRepository(applicationContext)
        val factory = FocusViewModelFactory(appRepo, configRepo, applicationContext)

        val packageName = intent.getStringExtra("intercept_package") ?: "Unknown App"
        val continuousMinutes = intent.getIntExtra("continuous_minutes", 0)

        setContent {
            MyApplicationTheme {
                Surface(
                    color = MaterialTheme.colorScheme.background,
                    modifier = Modifier.fillMaxSize()
                ) {
                    val focusViewModel: FocusViewModel = viewModel(factory = factory)
                    
                    val displayAppName = remember(packageName) {
                        try {
                            packageManager.getApplicationLabel(
                                packageManager.getApplicationInfo(packageName, 0)
                            ).toString()
                        } catch (e: Exception) {
                            packageName
                        }
                    }
                    
                    BreathingExerciseScreen(
                        appName = displayAppName,
                        continuousUsageMinutes = continuousMinutes,
                        onFinish = { didContinue ->
                            if (didContinue) {
                                // Re-allow the app so the accessibility service doesn't immediately block it again
                                val allowance = if (continuousMinutes > 0) 35 else 15
                                focusViewModel.recordSession(displayAppName, packageName, if (continuousMinutes >= 60) 20 else 10, didContinue, allowance)
                                
                                if (continuousMinutes == 0) {
                                    val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
                                    if (launchIntent != null) {
                                        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                        startActivity(launchIntent)
                                    }
                                }
                            } else {
                                if (continuousMinutes == 0) {
                                    // Treat as prevented
                                    focusViewModel.recordSession(displayAppName, packageName, 10, didContinue)
                                }
                                val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                                    addCategory(Intent.CATEGORY_HOME)
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                startActivity(homeIntent)
                            }
                            
                            finishAndRemoveTask()
                        }
                    )
                }
            }
        }
    }
}
