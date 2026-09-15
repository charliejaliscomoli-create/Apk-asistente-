package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.TaskEntity
import com.example.ui.theme.Amber400
import com.example.ui.theme.Emerald400
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Indigo400
import com.example.ui.theme.Indigo500
import com.example.ui.theme.Indigo600
import com.example.ui.theme.Rose400
import com.example.ui.theme.Rose500
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import com.example.ui.viewmodel.AssistantViewModel

@Composable
fun TasksScreen(
    viewModel: AssistantViewModel,
    modifier: Modifier = Modifier
) {
    val tasks by viewModel.tasks.collectAsState()
    val isBreakingDown by viewModel.isBreakingDown.collectAsState()
    val breakdownTasks by viewModel.breakdownTasks.collectAsState()
    var selectedCategory by remember { mutableStateOf("Todas") }
    var selectedStatus by remember { mutableStateOf("Todas") }
    var showAddDialog by remember { mutableStateOf(false) }
    var showAiBreakdownDialog by remember { mutableStateOf(false) }
    var goalInput by remember { mutableStateOf("") }

    val categories = listOf(
        "Todas", "Trabajo", "Finanzas", "Campo/Inventario", "General", "Estudio", "Personal", "Salud", "Proyectos"
    )

    val filteredTasks = tasks.filter { task ->
        val matchCat = selectedCategory == "Todas" || task.category.equals(selectedCategory, ignoreCase = true)
        val matchStat = when (selectedStatus) {
            "Pendientes" -> !task.isCompleted
            "Completadas" -> task.isCompleted
            else -> true
        }
        matchCat && matchStat
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Slate950)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            // Action Bar: Title + Desglosar IA
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Tareas del Día",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "${tasks.count { !it.isCompleted }} pendientes",
                        fontSize = 12.sp,
                        color = Slate400
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { showAiBreakdownDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Slate800),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("btn_ai_breakdown_trigger")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Desglosar con IA",
                            tint = Amber400,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Desglosar con IA",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Indigo400
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Categories Filter Row
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { cat ->
                    val isSelected = selectedCategory == cat
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) Indigo500.copy(alpha = 0.25f) else Slate900,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) Indigo400 else Slate800
                        ),
                        modifier = Modifier.clickable { selectedCategory = cat }
                    ) {
                        Text(
                            text = cat,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else Slate400,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tasks List
            if (filteredTasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Slate700,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No hay tareas en esta categoría",
                            fontSize = 14.sp,
                            color = Slate400
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredTasks, key = { it.id }) { task ->
                        TaskItemRow(
                            task = task,
                            onToggle = { viewModel.toggleTask(task) },
                            onDelete = { viewModel.deleteTask(task.id) }
                        )
                    }
                }
            }
        }

        // Floating Action Button to add task
        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = Indigo600,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("fab_add_task")
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Añadir tarea")
        }
    }

    // Modal: Add New Task
    if (showAddDialog) {
        var taskTitle by remember { mutableStateOf("") }
        var taskDesc by remember { mutableStateOf("") }
        var taskCategory by remember { mutableStateOf("Trabajo") }
        var taskPriority by remember { mutableStateOf("media") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            containerColor = Slate900,
            title = {
                Text(text = "Nueva Tarea", color = Color.White, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = taskTitle,
                        onValueChange = { taskTitle = it },
                        label = { Text("Título de la tarea") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_new_task_title"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    OutlinedTextField(
                        value = taskDesc,
                        onValueChange = { taskDesc = it },
                        label = { Text("Descripción o notas (opcional)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    // Priority Selector
                    Text("Prioridad", fontSize = 12.sp, color = Slate400)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("alta" to "Alta", "media" to "Media", "baja" to "Baja").forEach { (key, label) ->
                            val isSel = taskPriority == key
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSel) when (key) {
                                    "alta" -> Rose500.copy(alpha = 0.25f)
                                    "media" -> Amber400.copy(alpha = 0.25f)
                                    else -> Emerald400.copy(alpha = 0.25f)
                                } else Slate800,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSel) Color.White else Slate700
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { taskPriority = key }
                            ) {
                                Text(
                                    text = label,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (taskTitle.isNotBlank()) {
                            viewModel.addTask(taskTitle, taskCategory, taskPriority, taskDesc)
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Indigo600),
                    modifier = Modifier.testTag("btn_confirm_add_task")
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancelar", color = Slate400)
                }
            }
        )
    }

    // Modal: AI Project Breakdown
    if (showAiBreakdownDialog) {
        AlertDialog(
            onDismissRequest = {
                showAiBreakdownDialog = false
                viewModel.clearBreakdown()
            },
            containerColor = Slate900,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Amber400,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Desglosar Proyecto con IA",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Ingresa tu meta o proyecto y Gemini generará los pasos accionables:",
                        fontSize = 12.sp,
                        color = Slate400
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = goalInput,
                            onValueChange = { goalInput = it },
                            placeholder = { Text("Ej. Lanzamiento de producto, Cierre contable...", fontSize = 12.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                        Button(
                            onClick = {
                                if (goalInput.isNotBlank()) {
                                    viewModel.breakdownGoalWithAi(goalInput)
                                }
                            },
                            enabled = goalInput.isNotBlank() && !isBreakingDown,
                            colors = ButtonDefaults.buttonColors(containerColor = Indigo600)
                        ) {
                            if (isBreakingDown) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                            } else {
                                Text("Generar", fontSize = 12.sp)
                            }
                        }
                    }

                    if (breakdownTasks.isNotEmpty()) {
                        Text(
                            text = "Tareas sugeridas (${breakdownTasks.size}):",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Emerald400
                        )
                        LazyColumn(
                            modifier = Modifier.height(180.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(breakdownTasks) { t ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Slate850,
                                    border = androidx.compose.foundation.BorderStroke(0.5.dp, Slate700)
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text(text = t.title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        if (t.description.isNotBlank()) {
                                            Text(text = t.description, color = Slate400, fontSize = 10.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                if (breakdownTasks.isNotEmpty()) {
                    Button(
                        onClick = {
                            viewModel.addAllBreakdownTasks()
                            showAiBreakdownDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald500)
                    ) {
                        Text("+ Añadir todas a mi lista")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAiBreakdownDialog = false
                        viewModel.clearBreakdown()
                    }
                ) {
                    Text("Cerrar", color = Slate400)
                }
            }
        )
    }
}

@Composable
private fun TaskItemRow(
    task: TaskEntity,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val isCompleted = task.isCompleted

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (isCompleted) Slate900.copy(alpha = 0.5f) else Slate850,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isCompleted) Slate800 else Slate700
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Checkbox Icon
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isCompleted) Emerald500 else Slate800)
                    .clickable { onToggle() }
                    .testTag("task_toggle_${task.id}"),
                contentAlignment = Alignment.Center
            ) {
                if (isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completada",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Task Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isCompleted) Slate400 else Color.White,
                    textDecoration = if (isCompleted) TextDecoration.LineThrough else null
                )
                if (task.description.isNotBlank()) {
                    Text(
                        text = task.description,
                        fontSize = 11.sp,
                        color = Slate400,
                        textDecoration = if (isCompleted) TextDecoration.LineThrough else null,
                        maxLines = 2
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Priority Badge
                    val pColor = when (task.priority.lowercase()) {
                        "alta" -> Rose400
                        "media" -> Amber400
                        else -> Emerald400
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = pColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = task.priority.replaceFirstChar { it.uppercase() },
                            fontSize = 9.sp,
                            color = pColor,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    // Category Pill
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Slate800
                    ) {
                        Text(
                            text = task.category,
                            fontSize = 9.sp,
                            color = Slate400,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    if (task.dueDate.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = Slate400,
                                modifier = Modifier.size(10.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(text = task.dueDate, fontSize = 9.sp, color = Slate400)
                        }
                    }
                }
            }

            // Delete Action
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(32.dp)
                    .testTag("task_delete_${task.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar tarea",
                    tint = Slate400.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
