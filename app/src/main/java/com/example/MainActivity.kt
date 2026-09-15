package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.ui.components.AppHeader
import com.example.ui.components.AppNavigation
import com.example.ui.screens.AssistantScreen
import com.example.ui.screens.FocusScreen
import com.example.ui.screens.HabitsScreen
import com.example.ui.screens.MetricsScreen
import com.example.ui.screens.NotesScreen
import com.example.ui.screens.TasksScreen
import com.example.ui.screens.WorkspaceScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.Slate950
import com.example.ui.viewmodel.AssistantViewModel
import com.example.ui.viewmodel.NavTab
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    private val viewModel: AssistantViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                FamousAssistantApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun FamousAssistantApp(viewModel: AssistantViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()
    val tasks by viewModel.tasks.collectAsState()
    val habits by viewModel.habits.collectAsState()
    val productivityScore by viewModel.productivityScore.collectAsState()
    val soundEnabled by viewModel.soundEnabled.collectAsState()

    val pendingTasksCount = tasks.count { !it.isCompleted }
    val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val habitsDueTodayCount = habits.count { !it.completedDatesCsv.contains(todayStr) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate950),
        topBar = {
            AppHeader(
                productivityScore = productivityScore,
                soundEnabled = soundEnabled,
                onToggleSound = { viewModel.toggleSound() }
            )
        },
        bottomBar = {
            AppNavigation(
                currentTab = currentTab,
                pendingTasksCount = pendingTasksCount,
                habitsDueTodayCount = habitsDueTodayCount,
                onSelectTab = { tab -> viewModel.setTab(tab) }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                NavTab.ASISTENTE -> AssistantScreen(viewModel = viewModel)
                NavTab.TAREAS -> TasksScreen(viewModel = viewModel)
                NavTab.HABITOS -> HabitsScreen(viewModel = viewModel)
                NavTab.ENFOQUE -> FocusScreen(viewModel = viewModel)
                NavTab.NOTAS -> NotesScreen(viewModel = viewModel)
                NavTab.WORKSPACE -> WorkspaceScreen(viewModel = viewModel)
                NavTab.METRICAS -> MetricsScreen(viewModel = viewModel)
            }
        }
    }
}

// Kept for backwards compatibility with screenshot and robolectric tests
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyApplicationTheme { Greeting("Android") }
}
