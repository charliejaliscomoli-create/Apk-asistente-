package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.Amber400
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

@Composable
fun MetricsScreen(
    viewModel: AssistantViewModel,
    modifier: Modifier = Modifier
) {
    val tasks by viewModel.tasks.collectAsState()
    val habits by viewModel.habits.collectAsState()
    val focusMinutes by viewModel.focusMinutesToday.collectAsState()
    val pomodoros by viewModel.completedPomodorosToday.collectAsState()
    val productivityScore by viewModel.productivityScore.collectAsState()

    val totalTasks = tasks.size
    val completedTasks = tasks.count { it.isCompleted }
    val activeHabits = habits.size

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Slate950)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Header
            Text(
                text = "Métricas de Productividad",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Rendimiento y hábitos medidos en tiempo real",
                fontSize = 12.sp,
                color = Slate400
            )
        }

        item {
            // Score Hero Card
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Slate900,
                border = BorderStroke(1.dp, Slate800),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Índice de Rendimiento",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = when {
                                productivityScore >= 80 -> "Excelente disciplina ejecutiva. ¡Mantén el ritmo!"
                                productivityScore >= 50 -> "Buen progreso diario. Completa más tareas para subir a 85%."
                                else -> "Arrancando el día. Ejecuta tu primera sesión de enfoque."
                            },
                            fontSize = 12.sp,
                            color = Slate400,
                            lineHeight = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    // Circular Score Gauge
                    Box(
                        modifier = Modifier.size(90.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val stroke = 8.dp.toPx()
                            drawCircle(
                                color = Color(0xFF1E293B),
                                style = Stroke(width = stroke)
                            )
                            drawArc(
                                brush = Brush.sweepGradient(listOf(Indigo500, Emerald400, Cyan400, Indigo500)),
                                startAngle = -90f,
                                sweepAngle = (productivityScore / 100f) * 360f,
                                useCenter = false,
                                style = Stroke(width = stroke, cap = StrokeCap.Round)
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$productivityScore%",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = Emerald400
                            )
                            Text(
                                text = "Score",
                                fontSize = 9.sp,
                                color = Slate400
                            )
                        }
                    }
                }
            }
        }

        item {
            // Stats Grid 2x2
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricStatCard(
                        title = "Tareas Realizadas",
                        value = "$completedTasks / $totalTasks",
                        subtitle = "${if (totalTasks > 0) (completedTasks * 100 / totalTasks) else 0}% completado",
                        icon = Icons.Default.CheckCircle,
                        tint = Emerald400,
                        modifier = Modifier.weight(1f)
                    )
                    MetricStatCard(
                        title = "Hábitos en Curso",
                        value = "$activeHabits",
                        subtitle = "Rachas activas",
                        icon = Icons.Default.LocalFireDepartment,
                        tint = Amber400,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricStatCard(
                        title = "Tiempo de Enfoque",
                        value = "${focusMinutes}m",
                        subtitle = "Minutos concentrado",
                        icon = Icons.Default.Timer,
                        tint = Cyan400,
                        modifier = Modifier.weight(1f)
                    )
                    MetricStatCard(
                        title = "Bloques Pomodoro",
                        value = "$pomodoros",
                        subtitle = "Sesiones cumplidas",
                        icon = Icons.Default.AutoAwesome,
                        tint = Indigo400,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        item {
            // Recommendation Card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Slate850,
                border = BorderStroke(1.dp, Slate800),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Amber400,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Recomendación de Famous",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Aplica la regla de los dos minutos: si una tarea toma menos de 120 segundos, resuélvela de inmediato en lugar de agendarla. Para proyectos de mayor escala, utiliza el botón \"Desglosar con IA\" en la pestaña de Tareas.",
                        fontSize = 12.sp,
                        color = Slate400,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricStatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Slate900,
        border = BorderStroke(1.dp, Slate800),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, fontSize = 11.sp, color = Slate400)
                Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(text = subtitle, fontSize = 10.sp, color = tint)
        }
    }
}
