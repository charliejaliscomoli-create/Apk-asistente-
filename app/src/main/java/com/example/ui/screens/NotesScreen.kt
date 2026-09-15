package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PushPin
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.NoteEntity
import com.example.ui.theme.Amber400
import com.example.ui.theme.Emerald400
import com.example.ui.theme.Emerald500
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
import kotlinx.coroutines.launch

@Composable
fun NotesScreen(
    viewModel: AssistantViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val notes by viewModel.notes.collectAsState()
    val scope = rememberCoroutineScope()
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedNoteForAi by remember { mutableStateOf<NoteEntity?>(null) }
    var aiResultText by remember { mutableStateOf("") }
    var isAiLoading by remember { mutableStateOf(false) }

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
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Notas Rápidas",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "${notes.size} notas guardadas",
                        fontSize = 12.sp,
                        color = Slate400
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (notes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = Slate700,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No hay notas guardadas aún",
                            fontSize = 14.sp,
                            color = Slate400
                        )
                        Text(
                            text = "Captura ideas, citas o recordatorios al instante",
                            fontSize = 12.sp,
                            color = Slate700
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
                    items(notes, key = { it.id }) { note ->
                        NoteItemCard(
                            note = note,
                            onTogglePin = { viewModel.toggleNotePin(note) },
                            onDelete = { viewModel.deleteNote(note.id) },
                            onCopy = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Nota", "${note.title}\n\n${note.content}")
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Nota copiada al portapapeles", Toast.LENGTH_SHORT).show()
                            },
                            onAiEnhance = {
                                selectedNoteForAi = note
                                aiResultText = ""
                            }
                        )
                    }
                }
            }
        }

        // FAB
        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = Indigo600,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("fab_add_note")
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Nueva nota")
        }
    }

    // Modal: Add Note
    if (showAddDialog) {
        var noteTitle by remember { mutableStateOf("") }
        var noteContent by remember { mutableStateOf("") }
        var noteCategory by remember { mutableStateOf("General") }
        var selectedColor by remember { mutableStateOf("#6366F1") }
        val colors = listOf("#6366F1", "#0EA5E9", "#10B981", "#F59E0B", "#EC4899", "#8B5CF6")

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            containerColor = Slate900,
            title = {
                Text(text = "Capturar Nueva Nota", color = Color.White, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = noteTitle,
                        onValueChange = { noteTitle = it },
                        label = { Text("Título") },
                        placeholder = { Text("Ej. Idea para proyecto") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_new_note_title"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    OutlinedTextField(
                        value = noteContent,
                        onValueChange = { noteContent = it },
                        label = { Text("Contenido") },
                        placeholder = { Text("Escribe tus apuntes o notas...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    // Category
                    Text("Categoría", fontSize = 12.sp, color = Slate400)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Ideas", "General", "Finanzas", "Estrategia").forEach { cat ->
                            val isSel = noteCategory == cat
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSel) Indigo500.copy(alpha = 0.25f) else Slate800,
                                border = BorderStroke(1.dp, if (isSel) Indigo400 else Slate700),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { noteCategory = cat }
                            ) {
                                Text(
                                    text = cat,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(vertical = 6.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }

                    // Color choice
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        colors.forEach { hex ->
                            val c = try {
                                Color(android.graphics.Color.parseColor(hex))
                            } catch (e: Exception) {
                                Indigo500
                            }
                            val isSel = selectedColor == hex
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(c)
                                    .clickable { selectedColor = hex },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSel) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(Color.White)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (noteTitle.isNotBlank() || noteContent.isNotBlank()) {
                            viewModel.addNote(noteTitle, noteContent, noteCategory, selectedColor)
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Indigo600),
                    modifier = Modifier.testTag("btn_confirm_add_note")
                ) {
                    Text("Guardar Nota")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancelar", color = Slate400)
                }
            }
        )
    }

    // Modal: AI Note Enhancement
    selectedNoteForAi?.let { note ->
        AlertDialog(
            onDismissRequest = { selectedNoteForAi = null },
            containerColor = Slate900,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = Amber400)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Análisis de Nota con IA", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(text = "Nota: ${note.title}", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)

                    // 3 Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = {
                                isAiLoading = true
                                scope.launch {
                                    aiResultText = viewModel.enhanceNote(note.content, "summarize")
                                    isAiLoading = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Slate800),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Resumir", fontSize = 10.sp, color = Indigo400)
                        }

                        Button(
                            onClick = {
                                isAiLoading = true
                                scope.launch {
                                    aiResultText = viewModel.enhanceNote(note.content, "action_items")
                                    isAiLoading = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Slate800),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Acciones", fontSize = 10.sp, color = Indigo400)
                        }

                        Button(
                            onClick = {
                                isAiLoading = true
                                scope.launch {
                                    aiResultText = viewModel.enhanceNote(note.content, "polish")
                                    isAiLoading = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Slate800),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Pulir", fontSize = 10.sp, color = Indigo400)
                        }
                    }

                    if (isAiLoading) {
                        Box(modifier = Modifier.fillMaxWidth().height(90.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Indigo500)
                        }
                    } else if (aiResultText.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Slate950,
                            border = BorderStroke(1.dp, Slate700),
                            modifier = Modifier.fillMaxWidth().height(120.dp)
                        ) {
                            LazyColumn(modifier = Modifier.padding(10.dp)) {
                                item {
                                    Text(text = aiResultText, fontSize = 12.sp, color = Color.White, lineHeight = 18.sp)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                if (aiResultText.isNotBlank()) {
                    Button(
                        onClick = {
                            viewModel.addNote("[IA] ${note.title}", aiResultText, note.category, note.colorHex)
                            selectedNoteForAi = null
                            Toast.makeText(context, "Nota guardada con éxito", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald500)
                    ) {
                        Text("Guardar como nueva nota")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedNoteForAi = null }) {
                    Text("Cerrar", color = Slate400)
                }
            }
        )
    }
}

@Composable
private fun NoteItemCard(
    note: NoteEntity,
    onTogglePin: () -> Unit,
    onDelete: () -> Unit,
    onCopy: () -> Unit,
    onAiEnhance: () -> Unit
) {
    val noteColor = remember(note.colorHex) {
        try {
            Color(android.graphics.Color.parseColor(note.colorHex))
        } catch (e: Exception) {
            Indigo500
        }
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Slate850,
        border = BorderStroke(
            1.dp,
            if (note.isPinned) Indigo500.copy(alpha = 0.6f) else Slate800
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Title & Pin
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(noteColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = note.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                IconButton(
                    onClick = onTogglePin,
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PushPin,
                        contentDescription = "Fijar nota",
                        tint = if (note.isPinned) Indigo400 else Slate700,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = note.content,
                fontSize = 12.sp,
                color = Slate400,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Footer: Category + AI Analyze + Copy + Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Slate950
                ) {
                    Text(
                        text = note.category,
                        fontSize = 9.sp,
                        color = Slate400,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onAiEnhance, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Analizar con IA",
                            tint = Amber400,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    IconButton(onClick = onCopy, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copiar",
                            tint = Slate400,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar",
                            tint = Slate400,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }
        }
    }
}
