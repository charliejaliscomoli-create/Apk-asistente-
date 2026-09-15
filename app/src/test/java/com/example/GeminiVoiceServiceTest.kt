package com.example

import com.example.data.remote.AssistantAction
import com.example.data.remote.GeminiService
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class GeminiVoiceServiceTest {

    @Test
    fun testVoiceCommandCreateTask() = runBlocking {
        val result = GeminiService.processVoiceCommand("Crear nueva tarea Comprar repuestos en Finanzas")
        assertNotNull(result)
        assertTrue(result.spokenReply.isNotBlank())
        assertTrue(
            "Action should be CreateTask or GeneralReply",
            result.action is AssistantAction.CreateTask || result.action is AssistantAction.GeneralReply
        )
    }

    @Test
    fun testVoiceCommandSetTimer() = runBlocking {
        val result = GeminiService.processVoiceCommand("Poner un temporizador de 25 minutos")
        assertNotNull(result)
        assertTrue(result.spokenReply.isNotBlank())
        assertTrue(
            "Action should be SetTimer or GeneralReply",
            result.action is AssistantAction.SetTimer || result.action is AssistantAction.GeneralReply
        )
    }

    @Test
    fun testVoiceCommandAddNote() = runBlocking {
        val result = GeminiService.processVoiceCommand("Guardar nota recordar llamar al proveedor mañana temprano")
        assertNotNull(result)
        assertTrue(result.spokenReply.isNotBlank())
        assertTrue(
            "Action should be AddNote or GeneralReply",
            result.action is AssistantAction.AddNote || result.action is AssistantAction.GeneralReply
        )
    }

    @Test
    fun testVoiceCommandPendingTasks() = runBlocking {
        val result = GeminiService.processVoiceCommand("¿Cuáles son mis tareas pendientes?")
        assertNotNull(result)
        assertTrue(result.spokenReply.isNotBlank())
    }
}
