package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.theme.Amber400
import com.example.ui.theme.Cyan400
import com.example.ui.theme.Emerald400
import com.example.ui.theme.Indigo400
import com.example.ui.theme.Indigo500
import com.example.ui.theme.Indigo600
import com.example.ui.theme.Rose500
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import com.example.ui.viewmodel.AssistantMessage
import com.example.ui.viewmodel.AssistantViewModel

@Composable
fun AssistantScreen(
    viewModel: AssistantViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val messages by viewModel.messages.collectAsState()
    val isProcessingAi by viewModel.isProcessingAi.collectAsState()
    val isListening by viewModel.voiceManager.isListening.collectAsState()
    val isSpeaking by viewModel.voiceManager.isSpeaking.collectAsState()
    val voiceError by viewModel.voiceManager.errorMessage.collectAsState()
    var textInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.voiceManager.startListening { spoken ->
                viewModel.sendAssistantCommand(spoken)
            }
        }
    }

    LaunchedEffect(messages.size, isProcessingAi) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val quickPrompts = listOf(
        "📋 Tareas pendientes" to "¿Cuáles son mis tareas pendientes?",
        "📅 Agenda de hoy" to "Consultar mi agenda de hoy",
        "⏱️ Temporizador (10s)" to "Poner temporizador de 10 segundos",
        "✍️ Crear tarea" to "Crear tarea Revisar cotizaciones de proveedores en Finanzas",
        "📝 Guardar nota" to "Guardar nota Comprar insumos de bodega urgente",
        "🤝 Reunión a las 15:00" to "Agendar reunión con cliente a las 15:00"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Slate950)
    ) {
        // Assistant Subheader
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Slate900,
            border = androidx.compose.foundation.BorderStroke(1.dp, Slate800)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isProcessingAi) Amber400 else Emerald400)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isProcessingAi) "Procesando con Gemini 2.5 Flash..." else "Asistente Ejecutivo Activo",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isSpeaking) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Indigo500.copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Indigo400)
                        ) {
                            Row(
                                modifier = Modifier
                                    .clickable { viewModel.voiceManager.stopSpeaking() }
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VolumeOff,
                                    contentDescription = "Silenciar voz",
                                    tint = Indigo400,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Silenciar",
                                    fontSize = 10.sp,
                                    color = Indigo400,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    TextButton(onClick = { viewModel.clearChat() }) {
                        Text("Limpiar", fontSize = 11.sp, color = Slate400)
                    }
                }
            }
        }

        // Quick Action Chips Row
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(Slate900.copy(alpha = 0.7f))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(quickPrompts) { (label, command) ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Slate800,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate700),
                    modifier = Modifier.clickable {
                        viewModel.sendAssistantCommand(command)
                    }
                ) {
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        color = Slate400,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Voice Error Banner if any
        if (voiceError != null) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Rose500.copy(alpha = 0.15f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Rose500.copy(alpha = 0.3f))
            ) {
                Text(
                    text = voiceError ?: "",
                    fontSize = 11.sp,
                    color = Rose500,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }
        }

        // Chat Message Stream
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                ChatMessageItem(msg)
            }
            if (isProcessingAi) {
                item {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Slate850)
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = Cyan400
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Pensando orden con Gemini 2.5 Flash...",
                            fontSize = 12.sp,
                            color = Cyan400
                        )
                    }
                }
            }
        }

        // Voice and Text Input Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Slate900,
            border = androidx.compose.foundation.BorderStroke(1.dp, Slate800)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Microphone Button (Speech to Text)
                IconButton(
                    onClick = {
                        if (isListening) {
                            viewModel.voiceManager.stopListening()
                        } else {
                            val hasPerm = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.RECORD_AUDIO
                            ) == PackageManager.PERMISSION_GRANTED
                            if (hasPerm) {
                                viewModel.voiceManager.startListening { spoken ->
                                    viewModel.sendAssistantCommand(spoken)
                                }
                            } else {
                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (isListening)
                                Brush.linearGradient(listOf(Rose500, Rose500))
                            else
                                Brush.linearGradient(listOf(Indigo600, Indigo500))
                        )
                        .testTag("btn_voice_record")
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = if (isListening) "Detener micrófono" else "Hablar al Asistente",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Text Input Field
                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    placeholder = {
                        Text(
                            text = if (isListening) "Escuchando tu voz..." else "Escribe una orden o habla...",
                            fontSize = 13.sp,
                            color = Slate400
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("input_assistant_text"),
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Slate950,
                        unfocusedContainerColor = Slate950,
                        focusedBorderColor = Indigo500,
                        unfocusedBorderColor = Slate700,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    maxLines = 2
                )

                // Send Button
                IconButton(
                    onClick = {
                        if (textInput.isNotBlank()) {
                            val cmd = textInput
                            textInput = ""
                            viewModel.sendAssistantCommand(cmd)
                        }
                    },
                    enabled = textInput.isNotBlank() && !isProcessingAi,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (textInput.isNotBlank()) Indigo600 else Slate800)
                        .testTag("btn_send_command")
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Enviar orden",
                        tint = if (textInput.isNotBlank()) Color.White else Slate400,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatMessageItem(message: AssistantMessage) {
    val isUser = message.isUser
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 2.dp,
                bottomEnd = if (isUser) 2.dp else 16.dp
            ),
            color = if (isUser) Indigo600 else Slate850,
            border = if (!isUser) androidx.compose.foundation.BorderStroke(1.dp, Slate700) else null,
            modifier = Modifier.fillMaxWidth(0.88f)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Action tag if generated by assistant action
                if (!message.actionDetail.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Emerald400.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, Emerald400.copy(alpha = 0.3f)),
                        modifier = Modifier.padding(bottom = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Emerald400,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = message.actionDetail,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Emerald400
                            )
                        }
                    }
                }

                Text(
                    text = message.text,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = message.timestamp,
                    fontSize = 9.sp,
                    color = if (isUser) Color.White.copy(alpha = 0.6f) else Slate400,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}
