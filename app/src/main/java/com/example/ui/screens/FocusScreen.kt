package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.Amber400
import com.example.ui.theme.Amber500
import com.example.ui.theme.Cyan400
import com.example.ui.theme.Emerald400
import com.example.ui.theme.Indigo400
import com.example.ui.theme.Indigo500
import com.example.ui.theme.Indigo600
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import com.example.ui.viewmodel.AssistantViewModel
import com.example.ui.viewmodel.FocusMode

@Composable
fun FocusScreen(
    viewModel: AssistantViewModel,
    modifier: Modifier = Modifier
) {
    val focusMode by viewModel.focusMode.collectAsState()
    val timeLeft by viewModel.focusTimeLeft.collectAsState()
    val isRunning by viewModel.isFocusRunning.collectAsState()
    val completedSessions by viewModel.completedPomodorosToday.collectAsState()
    val focusMinutesToday by viewModel.focusMinutesToday.collectAsState()

    val totalDuration = focusMode.durationSeconds
    val progress = ((totalDuration - timeLeft).toFloat() / totalDuration.toFloat()).coerceIn(0f, 1f)
    val minutes = timeLeft / 60
    val seconds = timeLeft % 60
    val formattedTime = String.format("%02d:%02d", minutes, seconds)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Slate950)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header & Stats
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = Slate900,
            border = BorderStroke(1.dp, Slate800),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Temporizador de Enfoque",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Técnica Pomodoro sin distracciones",
                        fontSize = 11.sp,
                        color = Slate400
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Slate850,
                        border = BorderStroke(1.dp, Slate700)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Emerald400,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$completedSessions sesiones",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Mode Selector Pills
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Slate900,
            border = BorderStroke(1.dp, Slate800)
        ) {
            Row(
                modifier = Modifier.padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                FocusMode.values().forEach { mode ->
                    val isSelected = focusMode == mode
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) Indigo600 else Color.Transparent,
                        modifier = Modifier.clickable { viewModel.setFocusMode(mode) }
                    ) {
                        Text(
                            text = mode.label,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else Slate400,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Circular Timer Canvas
        Box(
            modifier = Modifier.size(240.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 14.dp.toPx()
                // Background track
                drawCircle(
                    color = Color(0xFF1E293B),
                    style = Stroke(width = strokeWidth)
                )
                // Animated Active Ring
                drawArc(
                    brush = Brush.sweepGradient(
                        listOf(Indigo500, Cyan400, Indigo600, Indigo500)
                    ),
                    startAngle = -90f,
                    sweepAngle = progress * 360f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = formattedTime,
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    color = Color.White
                )
                Text(
                    text = if (isRunning) "En progreso" else "En pausa",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isRunning) Emerald400 else Amber400
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        // Controls
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Reset Button
            IconButton(
                onClick = { viewModel.resetFocusTimer() },
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(Slate900)
                    .testTag("btn_focus_reset")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reiniciar temporizador",
                    tint = Slate400,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Start / Pause Button
            Button(
                onClick = { viewModel.toggleFocusTimer() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRunning) Amber500 else Indigo600
                ),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .height(54.dp)
                    .width(160.dp)
                    .testTag("btn_focus_toggle")
            ) {
                Icon(
                    imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isRunning) "Pausar" else "Comenzar",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Total concentrado hoy: $focusMinutesToday minutos",
            fontSize = 12.sp,
            color = Slate400
        )
    }
}
