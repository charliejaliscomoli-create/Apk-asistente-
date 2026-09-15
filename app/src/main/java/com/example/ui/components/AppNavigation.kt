package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.Indigo400
import com.example.ui.theme.Indigo600
import com.example.ui.theme.Rose500
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import com.example.ui.viewmodel.NavTab

@Composable
fun AppNavigation(
    currentTab: NavTab,
    pendingTasksCount: Int,
    habitsDueTodayCount: Int,
    onSelectTab: (NavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Slate950.copy(alpha = 0.98f),
        tonalElevation = 8.dp,
        border = androidx.compose.foundation.BorderStroke(0.5.dp, Slate900)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 4.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val tabs = listOf(
                NavigationItem(NavTab.ASISTENTE, Icons.Default.SmartToy, "Asistente", null),
                NavigationItem(NavTab.TAREAS, Icons.Default.CheckCircle, "Tareas", if (pendingTasksCount > 0) pendingTasksCount else null),
                NavigationItem(NavTab.HABITOS, Icons.Default.LocalFireDepartment, "Hábitos", if (habitsDueTodayCount > 0) habitsDueTodayCount else null),
                NavigationItem(NavTab.ENFOQUE, Icons.Default.Timer, "Enfoque", null),
                NavigationItem(NavTab.NOTAS, Icons.Default.Description, "Notas", null),
                NavigationItem(NavTab.WORKSPACE, Icons.Default.CalendarMonth, "Agenda", null),
                NavigationItem(NavTab.METRICAS, Icons.Default.BarChart, "Métricas", null)
            )

            tabs.forEach { item ->
                val isSelected = currentTab == item.tab
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onSelectTab(item.tab) }
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                        .testTag("nav_tab_${item.tab.name.lowercase()}"),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(contentAlignment = Alignment.TopEnd) {
                        Surface(
                            modifier = Modifier.size(32.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) Indigo600.copy(alpha = 0.25f) else Color.Transparent
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.label,
                                    tint = if (isSelected) Indigo400 else Slate400,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // Badge
                        if (item.badgeCount != null) {
                            Surface(
                                modifier = Modifier.size(14.dp),
                                shape = CircleShape,
                                color = Rose500
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "${item.badgeCount}",
                                        color = Color.White,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                    Text(
                        text = item.label,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Indigo400 else Slate400
                    )
                }
            }
        }
    }
}

private data class NavigationItem(
    val tab: NavTab,
    val icon: ImageVector,
    val label: String,
    val badgeCount: Int?
)
