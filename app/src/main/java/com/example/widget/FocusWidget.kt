package com.example.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionStartActivity
import android.content.ComponentName
import com.example.MainActivity
import androidx.glance.LocalContext
import android.content.Intent

class FocusWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            WidgetContent()
        }
    }
}

@Composable
fun WidgetContent() {
    val context = LocalContext.current
    Column(
        modifier = GlanceModifier.fillMaxSize()
            .background(Color(0xFFEADDFF))
            .padding(12.dp)
            .clickable(actionStartActivity(Intent(context, MainActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK })),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Focus",
            style = TextStyle(
                color = ColorProvider(Color(0xFF21005D)),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        )
        Text(
            text = "Intent",
            style = TextStyle(
                color = ColorProvider(Color(0xFF6750A4)),
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
        )
        Text(
            text = "Open Dashboard",
            style = TextStyle(
                color = ColorProvider(Color(0xFF21005D)),
                fontSize = 12.sp
            ),
            modifier = GlanceModifier.padding(top = 12.dp)
        )
    }
}

class FocusWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = FocusWidget()
}
