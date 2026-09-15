package com.example.data.remote

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

sealed class AssistantAction {
    data class CreateTask(val title: String, val category: String, val priority: String) : AssistantAction()
    data class CompleteTask(val taskTitle: String) : AssistantAction()
    object GetPendingTasks : AssistantAction()
    data class AddNote(val title: String, val content: String, val category: String) : AssistantAction()
    data class SetTimer(val seconds: Int, val label: String) : AssistantAction()
    data class CreateEvent(val title: String, val time: String, val description: String) : AssistantAction()
    object GetAgenda : AssistantAction()
    data class GeneralReply(val message: String) : AssistantAction()
}

data class AssistantResponse(
    val action: AssistantAction,
    val spokenReply: String
)

data class GeneratedTask(
    val title: String,
    val description: String,
    val priority: String,
    val category: String
)

object GeminiService {
    // Recommended default model for basic text and conversational tasks according to Gemini API guidance
    private const val MODEL = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL:generateContent"

    // OkHttpClient with 60s timeouts as mandated for Gemini API calls
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    /**
     * Checks if a real Gemini API Key has been configured in .env / BuildConfig.
     */
    fun isApiKeyConfigured(): Boolean {
        val key = BuildConfig.GEMINI_API_KEY
        return key.isNotBlank() && key != "MY_GEMINI_API_KEY"
    }

    /**
     * Connects to the Gemini REST API using the key configured in .env (BuildConfig.GEMINI_API_KEY)
     * to interpret and process spoken voice commands from the user.
     * Returns an AssistantResponse containing both the executable local action and a spoken reply for TextToSpeech.
     */
    suspend fun processVoiceCommand(
        spokenPrompt: String,
        pendingTasksPreview: String = "",
        todayEventsPreview: String = ""
    ): AssistantResponse = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (!isApiKeyConfigured()) {
            return@withContext fallbackProcessCommand(spokenPrompt)
        }

        try {
            val systemInstruction = """
                Eres Famous Asistente, el copiloto ejecutivo de productividad del usuario en su teléfono Android.
                El usuario te hablará mediante comandos de voz (o texto). Tu tarea es interpretar con precisión su intención,
                determinar la acción que debe realizar la aplicación y redactar una respuesta hablada ejecutiva, cordial y concisa.
                
                IMPORTANTE para la respuesta hablada (spokenReply):
                - Debe ser breve (máximo 1 a 2 oraciones).
                - Diseñada específicamente para ser leída en voz alta con Text-To-Speech (sin asteriscos, sin viñetas, sin markdown).
                - En español fluido y profesional.

                Debes responder EXCLUSIVAMENTE con un objeto JSON válido con esta estructura:
                {
                   "actionType": "CREATE_TASK" | "COMPLETE_TASK" | "GET_PENDING_TASKS" | "ADD_NOTE" | "SET_TIMER" | "CREATE_EVENT" | "GET_AGENDA" | "GENERAL_REPLY",
                   "spokenReply": "Respuesta limpia para síntesis de voz.",
                   "title": "Título conciso de la tarea, nota, evento o temporizador",
                   "content": "Cuerpo o detalle de la nota si aplica",
                   "category": "Trabajo" | "Finanzas" | "Campo/Inventario" | "General" | "Estudio" | "Personal" | "Salud" | "Proyectos",
                   "priority": "alta" | "media" | "baja",
                   "seconds": 10,
                   "time": "HH:mm"
                }

                Contexto del usuario:
                - Tareas pendientes actuales: $pendingTasksPreview
                - Agenda de hoy: $todayEventsPreview
            """.trimIndent()

            val requestJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply { put("text", spokenPrompt) })
                        })
                    })
                })
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", systemInstruction) })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("responseMimeType", "application/json")
                    put("temperature", 0.2)
                })
            }

            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .post(requestJson.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext fallbackProcessCommand(spokenPrompt)
            }

            val bodyString = response.body?.string() ?: return@withContext fallbackProcessCommand(spokenPrompt)
            val root = JSONObject(bodyString)
            val candidates = root.optJSONArray("candidates")
            val candidate = candidates?.optJSONObject(0)
            val content = candidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val rawText = parts?.optJSONObject(0)?.optString("text") ?: ""
            if (rawText.isBlank()) {
                return@withContext fallbackProcessCommand(spokenPrompt)
            }

            val cleanedText = rawText.trim()
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()

            val json = JSONObject(cleanedText)
            val actionType = json.optString("actionType", "GENERAL_REPLY")
            val spokenReply = json.optString("spokenReply", "Orden procesada con éxito.")
            val title = json.optString("title", "")
            val cat = json.optString("category", "General")
            val priority = json.optString("priority", "media")
            val seconds = json.optInt("seconds", 10)
            val time = json.optString("time", "12:00")
            val noteContent = json.optString("content", "")

            val action: AssistantAction = when (actionType) {
                "CREATE_TASK" -> AssistantAction.CreateTask(
                    title = if (title.isNotBlank()) title else spokenPrompt,
                    category = cat,
                    priority = priority
                )
                "COMPLETE_TASK" -> AssistantAction.CompleteTask(
                    taskTitle = if (title.isNotBlank()) title else spokenPrompt
                )
                "GET_PENDING_TASKS" -> AssistantAction.GetPendingTasks
                "ADD_NOTE" -> AssistantAction.AddNote(
                    title = if (title.isNotBlank()) title else "Nota rápida",
                    content = if (noteContent.isNotBlank()) noteContent else spokenPrompt,
                    category = cat
                )
                "SET_TIMER" -> AssistantAction.SetTimer(
                    seconds = if (seconds > 0) seconds else 10,
                    label = if (title.isNotBlank()) title else "Temporizador"
                )
                "CREATE_EVENT" -> AssistantAction.CreateEvent(
                    title = if (title.isNotBlank()) title else "Reunión",
                    time = if (time.isNotBlank()) time else "12:00",
                    description = spokenPrompt
                )
                "GET_AGENDA" -> AssistantAction.GetAgenda
                else -> AssistantAction.GeneralReply(spokenReply)
            }
            AssistantResponse(action, spokenReply)
        } catch (e: Exception) {
            fallbackProcessCommand(spokenPrompt)
        }
    }

    /**
     * Backward-compatible alias for processing commands.
     */
    suspend fun processCommand(
        prompt: String,
        pendingTasksPreview: String = "",
        todayEventsPreview: String = ""
    ): AssistantResponse = processVoiceCommand(prompt, pendingTasksPreview, todayEventsPreview)

    suspend fun breakdownGoal(goal: String): List<GeneratedTask> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext fallbackBreakdown(goal)
        }

        try {
            val prompt = """
                Desglosa la siguiente meta o proyecto en exactamente 3 o 4 tareas accionables: "$goal".
                Responde en formato JSON array con objetos que contengan:
                [
                  {
                    "title": "Nombre claro de la tarea",
                    "description": "Detalles o pasos",
                    "priority": "alta" | "media" | "baja",
                    "category": "Trabajo" | "Finanzas" | "Campo/Inventario" | "General" | "Estudio" | "Personal" | "Salud" | "Proyectos"
                  }
                ]
            """.trimIndent()

            val requestJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply { put("text", prompt) })
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("responseMimeType", "application/json")
                })
            }

            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .post(requestJson.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = okHttpClient.newCall(request).execute()
            val bodyString = response.body?.string() ?: return@withContext fallbackBreakdown(goal)
            val root = JSONObject(bodyString)
            val candidates = root.optJSONArray("candidates")
            val candidate = candidates?.optJSONObject(0)
            val content = candidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val rawText = parts?.optJSONObject(0)?.optString("text") ?: ""

            val cleaned = rawText.trim()
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()

            val array = JSONArray(cleaned)
            val list = mutableListOf<GeneratedTask>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    GeneratedTask(
                        title = obj.optString("title", "Tarea ${i + 1}"),
                        description = obj.optString("description", ""),
                        priority = obj.optString("priority", "media"),
                        category = obj.optString("category", "Trabajo")
                    )
                )
            }
            if (list.isNotEmpty()) list else fallbackBreakdown(goal)
        } catch (e: Exception) {
            fallbackBreakdown(goal)
        }
    }

    suspend fun enhanceNote(content: String, action: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        val actionPrompt = when (action) {
            "summarize" -> "Resume los puntos principales de la siguiente nota en viñetas concisas y claras:"
            "action_items" -> "Extrae las acciones clave y pendientes inmediatos de esta nota en una lista numerada:"
            else -> "Pule la redacción de la siguiente nota haciéndola más profesional, limpia y ejecutiva:"
        }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext when (action) {
                "summarize" -> "• Resumen clave: ${content.take(120)}..."
                "action_items" -> "1. Dar seguimiento al contenido principal.\n2. Archivar detalles y confirmar avances."
                else -> content.trim()
            }
        }

        try {
            val requestJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply { put("text", "$actionPrompt\n\n$content") })
                        })
                    })
                })
            }

            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .post(requestJson.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = okHttpClient.newCall(request).execute()
            val bodyString = response.body?.string() ?: return@withContext content
            val root = JSONObject(bodyString)
            val candidates = root.optJSONArray("candidates")
            val candidate = candidates?.optJSONObject(0)
            val parts = candidate?.optJSONObject("content")?.optJSONArray("parts")
            val text = parts?.optJSONObject(0)?.optString("text") ?: content
            text.trim()
        } catch (e: Exception) {
            content
        }
    }

    private fun fallbackProcessCommand(prompt: String): AssistantResponse {
        val clean = prompt.trim().lowercase()
        return when {
            clean.contains("temporizador") || clean.contains("alarma") -> {
                val digits = Regex("\\d+").find(clean)?.value?.toIntOrNull() ?: 10
                val seconds = if (clean.contains("minuto")) digits * 60 else digits
                AssistantResponse(
                    AssistantAction.SetTimer(seconds, "Temporizador"),
                    "Temporizador de $seconds segundos configurado en tu dispositivo."
                )
            }
            clean.contains("tarea") && (clean.contains("crear") || clean.contains("nueva") || clean.contains("agrega") || clean.contains("anota")) -> {
                val title = prompt.replace(Regex("(?i)(crear|nueva|agregar|agrega|anota|anotar)\\s+tarea\\s*"), "").trim()
                val finalTitle = if (title.isNotBlank()) title else "Nueva tarea"
                val cat = when {
                    clean.contains("finanz") -> "Finanzas"
                    clean.contains("campo") || clean.contains("inventario") -> "Campo/Inventario"
                    clean.contains("salud") -> "Salud"
                    clean.contains("estudio") -> "Estudio"
                    else -> "Trabajo"
                }
                AssistantResponse(
                    AssistantAction.CreateTask(finalTitle, cat, "alta"),
                    "Tarea registrada en $cat: \"$finalTitle\"."
                )
            }
            clean.contains("completar") || clean.contains("terminar") || clean.contains("hecha") || clean.contains("marcar") -> {
                val search = prompt.replace(Regex("(?i)(completar|terminar|marcar)\\s+(la\\s+)?(tarea\\s+)?"), "").trim()
                AssistantResponse(
                    AssistantAction.CompleteTask(search),
                    "Marcando tarea como completada."
                )
            }
            clean.contains("pendiente") || clean.contains("por hacer") -> {
                AssistantResponse(
                    AssistantAction.GetPendingTasks,
                    "Consultando lista de tareas pendientes."
                )
            }
            clean.contains("nota") && (clean.contains("guardar") || clean.contains("crear") || clean.contains("anota") || clean.contains("apunta")) -> {
                val body = prompt.replace(Regex("(?i)(guardar|crear|anotar|apuntar|agrega)\\s+nota\\s*"), "").trim()
                val finalBody = if (body.isNotBlank()) body else prompt
                AssistantResponse(
                    AssistantAction.AddNote("Nota rápida", finalBody, "General"),
                    "Nota guardada en el bloc de notas."
                )
            }
            clean.contains("agenda") || clean.contains("reunion") || clean.contains("reunión") || clean.contains("cita") || clean.contains("evento") -> {
                if (clean.contains("agendar") || clean.contains("crear") || clean.contains("programar")) {
                    AssistantResponse(
                        AssistantAction.CreateEvent("Reunión ejecutiva", "15:00", prompt),
                        "Reunión agendada en el calendario para hoy a las 15:00."
                    )
                } else {
                    AssistantResponse(
                        AssistantAction.GetAgenda,
                        "Consultando tus compromisos y agenda de hoy."
                    )
                }
            }
            else -> {
                AssistantResponse(
                    AssistantAction.GeneralReply("Entendido. Estoy a tu disposición para gestionar tareas, notas, agenda y enfoque."),
                    "Entendido. Estoy a tu disposición para gestionar tareas, notas, agenda y enfoque."
                )
            }
        }
    }

    private fun fallbackBreakdown(goal: String): List<GeneratedTask> {
        return listOf(
            GeneratedTask("Definir alcance y metas de $goal", "Especificar objetivos clave y recursos necesarios.", "alta", "Trabajo"),
            GeneratedTask("Organizar insumos y equipo para $goal", "Coordinar calendario y responsabilidades.", "media", "Finanzas"),
            GeneratedTask("Ejecutar primera fase de $goal", "Completar el primer hito accionable.", "alta", "Proyectos"),
            GeneratedTask("Validar resultados de $goal", "Revisión final de calidad e informe de cierre.", "baja", "General")
        )
    }
}
