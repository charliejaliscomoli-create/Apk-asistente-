package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entity.CalendarEventEntity
import com.example.data.local.entity.HabitEntity
import com.example.data.local.entity.NoteEntity
import com.example.data.local.entity.TaskEntity
import com.example.data.remote.GeneratedTask
import com.example.data.repository.AssistantRepository
import com.example.utils.AudioFeedback
import com.example.utils.VoiceManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class NavTab(val label: String) {
    ASISTENTE("Asistente IA"),
    TAREAS("Tareas"),
    HABITOS("Hábitos"),
    ENFOQUE("Enfoque"),
    NOTAS("Notas"),
    WORKSPACE("Agenda"),
    METRICAS("Métricas")
}

data class AssistantMessage(
    val id: String = "msg_${System.currentTimeMillis()}_${(0..999).random()}",
    val isUser: Boolean,
    val text: String,
    val actionDetail: String? = null,
    val timestamp: String = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
)

enum class FocusMode(val label: String, val durationSeconds: Int) {
    POMODORO("Enfoque Profundo", 25 * 60),
    SHORT_BREAK("Pausa Corta", 5 * 60),
    LONG_BREAK("Pausa Larga", 15 * 60)
}

class AssistantViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = AssistantRepository(database)

    val audioFeedback = AudioFeedback(application)
    val voiceManager = VoiceManager(application)

    val tasks: StateFlow<List<TaskEntity>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val habits: StateFlow<List<HabitEntity>> = repository.allHabits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notes: StateFlow<List<NoteEntity>> = repository.allNotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val events: StateFlow<List<CalendarEventEntity>> = repository.allEvents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentTab = MutableStateFlow(NavTab.ASISTENTE)
    val currentTab: StateFlow<NavTab> = _currentTab.asStateFlow()

    private val _messages = MutableStateFlow(
        listOf(
            AssistantMessage(
                isUser = false,
                text = "¡Hola! Soy Famous Asistente, tu mano derecha de productividad. Puedes dictarme u ordenar por texto: \"Crear tarea\", \"Consultar agenda de hoy\", \"Temporizador de 10 segundos\" o \"Guardar nota\"."
            )
        )
    )
    val messages: StateFlow<List<AssistantMessage>> = _messages.asStateFlow()

    private val _isProcessingAi = MutableStateFlow(false)
    val isProcessingAi: StateFlow<Boolean> = _isProcessingAi.asStateFlow()

    private val _soundEnabled = MutableStateFlow(true)
    val soundEnabled: StateFlow<Boolean> = _soundEnabled.asStateFlow()

    // Focus / Pomodoro State
    private val _focusMode = MutableStateFlow(FocusMode.POMODORO)
    val focusMode: StateFlow<FocusMode> = _focusMode.asStateFlow()

    private val _focusTimeLeft = MutableStateFlow(FocusMode.POMODORO.durationSeconds)
    val focusTimeLeft: StateFlow<Int> = _focusTimeLeft.asStateFlow()

    private val _isFocusRunning = MutableStateFlow(false)
    val isFocusRunning: StateFlow<Boolean> = _isFocusRunning.asStateFlow()

    private val _focusMinutesToday = MutableStateFlow(50)
    val focusMinutesToday: StateFlow<Int> = _focusMinutesToday.asStateFlow()

    private val _completedPomodorosToday = MutableStateFlow(2)
    val completedPomodorosToday: StateFlow<Int> = _completedPomodorosToday.asStateFlow()

    private var focusTimerJob: Job? = null

    // Goal Breakdown AI Modal State
    private val _breakdownTasks = MutableStateFlow<List<GeneratedTask>>(emptyList())
    val breakdownTasks: StateFlow<List<GeneratedTask>> = _breakdownTasks.asStateFlow()

    private val _isBreakingDown = MutableStateFlow(false)
    val isBreakingDown: StateFlow<Boolean> = _isBreakingDown.asStateFlow()

    // Productivity Score (Calculated reactive flow)
    val productivityScore: StateFlow<Int> = combine(tasks, habits, focusMinutesToday) { tList, hList, fMins ->
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val completedTasks = tList.count { it.isCompleted }
        val taskRate = if (tList.isNotEmpty()) (completedTasks.toFloat() / tList.size) * 40f else 20f
        val habitsDone = hList.count { it.completedDatesCsv.contains(todayStr) }
        val habitRate = if (hList.isNotEmpty()) (habitsDone.toFloat() / hList.size) * 40f else 20f
        val focusRate = (fMins.toFloat() / 60f * 20f).coerceAtMost(20f)
        (taskRate + habitRate + focusRate).toInt().coerceIn(0, 100)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 75)

    fun setTab(tab: NavTab) {
        if (_soundEnabled.value) audioFeedback.playClick()
        audioFeedback.vibrate(25)
        _currentTab.value = tab
    }

    fun toggleSound() {
        _soundEnabled.value = !_soundEnabled.value
        if (_soundEnabled.value) audioFeedback.playClick()
    }

    // --- Assistant Execution ---
    fun sendAssistantCommand(prompt: String) {
        val clean = prompt.trim()
        if (clean.isBlank() || _isProcessingAi.value) return

        if (_soundEnabled.value) audioFeedback.playClick()
        voiceManager.stopListening()

        val userMsg = AssistantMessage(isUser = true, text = clean)
        _messages.value = _messages.value + userMsg
        _isProcessingAi.value = true

        viewModelScope.launch {
            try {
                val (response, detail) = repository.processAssistantCommand(clean)
                val assistantMsg = AssistantMessage(
                    isUser = false,
                    text = response.spokenReply,
                    actionDetail = detail
                )
                _messages.value = _messages.value + assistantMsg

                if (_soundEnabled.value) {
                    audioFeedback.playCompletionSound()
                }
                audioFeedback.vibrate(40)

                // Speak reply using native Android TextToSpeech
                voiceManager.speak(response.spokenReply)
            } catch (e: Exception) {
                _messages.value = _messages.value + AssistantMessage(
                    isUser = false,
                    text = "No pude procesar la solicitud: ${e.message}"
                )
            } finally {
                _isProcessingAi.value = false
            }
        }
    }

    fun clearChat() {
        _messages.value = listOf(
            AssistantMessage(
                isUser = false,
                text = "Historial reiniciado. ¿En qué puedo colaborar contigo?"
            )
        )
    }

    // --- Task Actions ---
    fun addTask(title: String, category: String, priority: String, description: String = "", dueDate: String = "") {
        if (title.isBlank()) return
        viewModelScope.launch {
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            repository.insertTask(
                TaskEntity(
                    title = title.trim(),
                    description = description.trim(),
                    category = category,
                    priority = priority,
                    dueDate = if (dueDate.isNotBlank()) dueDate else today
                )
            )
            if (_soundEnabled.value) audioFeedback.playClick()
        }
    }

    fun toggleTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.toggleTaskStatus(task)
            if (!task.isCompleted) {
                if (_soundEnabled.value) audioFeedback.playCompletionSound()
                audioFeedback.vibrate(60)
            } else {
                if (_soundEnabled.value) audioFeedback.playClick()
            }
        }
    }

    fun deleteTask(id: Long) {
        viewModelScope.launch {
            repository.deleteTask(id)
            if (_soundEnabled.value) audioFeedback.playClick()
        }
    }

    // --- Habit Actions ---
    fun addHabit(name: String, category: String, colorHex: String, targetPerWeek: Int) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.insertHabit(
                HabitEntity(
                    name = name.trim(),
                    category = category,
                    colorHex = colorHex,
                    targetPerWeek = targetPerWeek
                )
            )
            if (_soundEnabled.value) audioFeedback.playClick()
        }
    }

    fun toggleHabitDay(habit: HabitEntity, dateStr: String) {
        viewModelScope.launch {
            val wasDone = habit.completedDatesCsv.contains(dateStr)
            repository.toggleHabitDate(habit, dateStr)
            if (!wasDone) {
                if (_soundEnabled.value) audioFeedback.playCompletionSound()
                audioFeedback.vibrate(50)
            } else {
                if (_soundEnabled.value) audioFeedback.playClick()
            }
        }
    }

    fun deleteHabit(id: Long) {
        viewModelScope.launch {
            repository.deleteHabit(id)
            if (_soundEnabled.value) audioFeedback.playClick()
        }
    }

    // --- Note Actions ---
    fun addNote(title: String, content: String, category: String, colorHex: String = "#6366F1") {
        if (title.isBlank() && content.isBlank()) return
        viewModelScope.launch {
            repository.insertNote(
                NoteEntity(
                    title = if (title.isNotBlank()) title.trim() else "Nota rápida",
                    content = content.trim(),
                    category = category,
                    colorHex = colorHex
                )
            )
            if (_soundEnabled.value) audioFeedback.playClick()
        }
    }

    fun toggleNotePin(note: NoteEntity) {
        viewModelScope.launch {
            repository.toggleNotePin(note)
            if (_soundEnabled.value) audioFeedback.playClick()
        }
    }

    fun deleteNote(id: Long) {
        viewModelScope.launch {
            repository.deleteNote(id)
            if (_soundEnabled.value) audioFeedback.playClick()
        }
    }

    // --- Calendar Event Actions ---
    fun addCalendarEvent(title: String, time: String, description: String = "", location: String = "Oficina / Meet") {
        if (title.isBlank()) return
        viewModelScope.launch {
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            repository.insertEvent(
                CalendarEventEntity(
                    title = title.trim(),
                    date = today,
                    startTime = time,
                    endTime = calculateEnd(time),
                    location = location,
                    description = description.trim()
                )
            )
            if (_soundEnabled.value) audioFeedback.playCompletionSound()
        }
    }

    fun deleteCalendarEvent(id: Long) {
        viewModelScope.launch {
            repository.deleteEvent(id)
            if (_soundEnabled.value) audioFeedback.playClick()
        }
    }

    private fun calculateEnd(startTime: String): String {
        return try {
            val parts = startTime.split(":").map { it.toInt() }
            val mins = parts[0] * 60 + parts[1] + 30
            val h = (mins / 60) % 24
            val m = mins % 60
            String.format(Locale.getDefault(), "%02d:%02d", h, m)
        } catch (e: Exception) {
            "13:00"
        }
    }

    // --- Focus / Pomodoro Timer ---
    fun setFocusMode(mode: FocusMode) {
        pauseFocus()
        _focusMode.value = mode
        _focusTimeLeft.value = mode.durationSeconds
        if (_soundEnabled.value) audioFeedback.playClick()
    }

    fun toggleFocusTimer() {
        if (_isFocusRunning.value) {
            pauseFocus()
        } else {
            startFocus()
        }
        if (_soundEnabled.value) audioFeedback.playClick()
        audioFeedback.vibrate(30)
    }

    fun resetFocusTimer() {
        pauseFocus()
        _focusTimeLeft.value = _focusMode.value.durationSeconds
        if (_soundEnabled.value) audioFeedback.playClick()
    }

    private fun startFocus() {
        _isFocusRunning.value = true
        focusTimerJob?.cancel()
        focusTimerJob = viewModelScope.launch {
            while (_isFocusRunning.value && _focusTimeLeft.value > 0) {
                delay(1000L)
                _focusTimeLeft.value -= 1
            }
            if (_focusTimeLeft.value <= 0) {
                onFocusCompleted()
            }
        }
    }

    private fun pauseFocus() {
        _isFocusRunning.value = false
        focusTimerJob?.cancel()
        focusTimerJob = null
    }

    private fun onFocusCompleted() {
        pauseFocus()
        if (_soundEnabled.value) audioFeedback.playTimerBell()
        audioFeedback.vibrate(120)
        if (_focusMode.value == FocusMode.POMODORO) {
            val mins = _focusMode.value.durationSeconds / 60
            _focusMinutesToday.value += mins
            _completedPomodorosToday.value += 1
            setFocusMode(FocusMode.SHORT_BREAK)
        } else {
            setFocusMode(FocusMode.POMODORO)
        }
    }

    // --- AI Breakdown & Note Tools ---
    fun breakdownGoalWithAi(goal: String) {
        if (goal.isBlank() || _isBreakingDown.value) return
        _isBreakingDown.value = true
        viewModelScope.launch {
            val list = repository.breakdownGoal(goal)
            _breakdownTasks.value = list
            _isBreakingDown.value = false
            if (_soundEnabled.value) audioFeedback.playCompletionSound()
        }
    }

    fun addAllBreakdownTasks() {
        viewModelScope.launch {
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            _breakdownTasks.value.forEach { t ->
                repository.insertTask(
                    TaskEntity(
                        title = t.title,
                        description = t.description,
                        priority = t.priority,
                        category = t.category,
                        dueDate = today
                    )
                )
            }
            _breakdownTasks.value = emptyList()
            if (_soundEnabled.value) audioFeedback.playCompletionSound()
            audioFeedback.vibrate(50)
        }
    }

    fun clearBreakdown() {
        _breakdownTasks.value = emptyList()
    }

    suspend fun enhanceNote(content: String, action: String): String {
        return repository.enhanceNote(content, action)
    }

    override fun onCleared() {
        super.onCleared()
        audioFeedback.release()
        voiceManager.destroy()
    }
}
