package com.example.widget

import android.content.Context
import android.content.Intent
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.example.MainActivity
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.color.ColorProvider
import androidx.glance.appwidget.updateAll

class LiveHomeScreenWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = LiveHomeScreenWidget()
}

class LiveHomeScreenWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        
        provideContent {
            GlanceTheme {
                Column(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(ColorProvider(day = androidx.compose.ui.graphics.Color(0xFF1E1E1E), night = androidx.compose.ui.graphics.Color(0xFF1E1E1E)))
                        .padding(12.dp)
                ) {
                    // Horní zóna (Makra)
                    Text(
                        text = "Dnešní makra",
                        style = TextStyle(
                            color = ColorProvider(day = androidx.compose.ui.graphics.Color.White, night = androidx.compose.ui.graphics.Color.White),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = GlanceModifier.height(8.dp))
                    Row(
                        modifier = GlanceModifier.fillMaxWidth()
                    ) {
                        Box(modifier = GlanceModifier.defaultWeight(), contentAlignment = androidx.glance.layout.Alignment.Center) {
                            MacroIndicator("Bílkoviny", "120g", androidx.compose.ui.graphics.Color(0xFF4CAF50))
                        }
                        Box(modifier = GlanceModifier.defaultWeight(), contentAlignment = androidx.glance.layout.Alignment.Center) {
                            MacroIndicator("Sacharidy", "200g", androidx.compose.ui.graphics.Color(0xFF2196F3))
                        }
                        Box(modifier = GlanceModifier.defaultWeight(), contentAlignment = androidx.glance.layout.Alignment.Center) {
                            MacroIndicator("Tuky", "60g", androidx.compose.ui.graphics.Color(0xFFFFC107))
                        }
                    }

                    Spacer(modifier = GlanceModifier.height(16.dp))

                    // Spodní zóna (Rodinný Feed)
                    Text(
                        text = "Rodinná lednice",
                        style = TextStyle(
                            color = ColorProvider(day = androidx.compose.ui.graphics.Color.White, night = androidx.compose.ui.graphics.Color.White),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = GlanceModifier.height(4.dp))
                    Row(
                        modifier = GlanceModifier.fillMaxWidth().background(ColorProvider(day = androidx.compose.ui.graphics.Color(0xFF2D2D2D), night = androidx.compose.ui.graphics.Color(0xFF2D2D2D))).padding(8.dp),
                        verticalAlignment = androidx.glance.layout.Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Taťka nakoupil: mléko",
                            style = TextStyle(
                                color = ColorProvider(day = androidx.compose.ui.graphics.Color.LightGray, night = androidx.compose.ui.graphics.Color.LightGray),
                                fontSize = 12.sp
                            ),
                            modifier = GlanceModifier.defaultWeight()
                        )
                    }

                    Spacer(modifier = GlanceModifier.height(16.dp))
                    
                    // Quick Action Tlačítka
                    Row(
                        modifier = GlanceModifier.fillMaxWidth()
                    ) {
                        Box(modifier = GlanceModifier.defaultWeight(), contentAlignment = androidx.glance.layout.Alignment.Center) {
                            WidgetButton("+ Zápis", actionStartActivity(Intent(context, MainActivity::class.java).apply {
                                action = "com.example.ACTION_ADD_FOOD"
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            }))
                        }
                        Spacer(modifier = GlanceModifier.width(8.dp))
                        Box(modifier = GlanceModifier.defaultWeight(), contentAlignment = androidx.glance.layout.Alignment.Center) {
                            WidgetButton("Skener", actionStartActivity(Intent(context, MainActivity::class.java).apply {
                                action = "com.example.ACTION_OPEN_SCANNER"
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            }))
                        }
                    }
                }
            }
        }
    }
}

@androidx.compose.runtime.Composable
fun MacroIndicator(label: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = androidx.glance.layout.Alignment.CenterHorizontally) {
        androidx.glance.text.Text(
            text = value,
            style = TextStyle(
                color = ColorProvider(day = color, night = color),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        )
        Spacer(modifier = GlanceModifier.height(4.dp))
        androidx.glance.text.Text(
            text = label,
            style = TextStyle(
                color = ColorProvider(day = androidx.compose.ui.graphics.Color.Gray, night = androidx.compose.ui.graphics.Color.Gray),
                fontSize = 10.sp
            )
        )
    }
}

@androidx.compose.runtime.Composable
fun WidgetButton(text: String, onClickAction: androidx.glance.action.Action) {
    Box(
        modifier = GlanceModifier
            .background(ColorProvider(day = androidx.compose.ui.graphics.Color(0xFF4CAF50), night = androidx.compose.ui.graphics.Color(0xFF4CAF50))) // FreshGreen
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClickAction),
        contentAlignment = androidx.glance.layout.Alignment.Center
    ) {
        androidx.glance.text.Text(
            text = text,
            style = TextStyle(
                color = ColorProvider(day = androidx.compose.ui.graphics.Color.White, night = androidx.compose.ui.graphics.Color.White),
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp
            )
        )
    }
}

class WidgetUpdateWorker(
    appContext: android.content.Context,
    workerParams: androidx.work.WorkerParameters
) : androidx.work.CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        LiveHomeScreenWidget().updateAll(applicationContext)
        return Result.success()
    }
}

