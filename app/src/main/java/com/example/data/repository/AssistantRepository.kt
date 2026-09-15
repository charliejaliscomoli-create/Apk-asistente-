package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.entity.CalendarEventEntity
import com.example.data.local.entity.HabitEntity
import com.example.data.local.entity.NoteEntity
import com.example.data.local.entity.TaskEntity
import com.example.data.remote.AssistantAction
import com.example.data.remote.AssistantResponse
import com.example.data.remote.GeminiService
import com.example.data.remote.GeneratedTask
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AssistantRepository(private val database: AppDatabase) {
    val allTasks: Flow<List<TaskEntity>> = database.taskDao().getAllTasks()
    val allHabits: Flow<List<HabitEntity>> = database.habitDao().getAllHabits()
    val allNotes: Flow<List<NoteEntity>> = database.noteDao().getAllNotes()
    val allEvents: Flow<List<CalendarEventEntity>> = database.calendarEventDao().getAllEvents()

    suspend fun insertTask(task: TaskEntity): Long = database.taskDao().insertTask(task)
    suspend fun updateTask(task: TaskEntity) = database.taskDao().updateTask(task)
    suspend fun deleteTask(id: Long) = database.taskDao().deleteTaskById(id)

    suspend fun toggleTaskStatus(task: TaskEntity) {
        val nextCompleted = !task.isCompleted
        val nextStatus = if (nextCompleted) "completada" else "pendiente"
        database.taskDao().updateTask(
            task.copy(
                isCompleted = nextCompleted,
                status = nextStatus
            )
        )
    }

    suspend fun insertHabit(habit: HabitEntity): Long = database.habitDao().insertHabit(habit)
    suspend fun updateHabit(habit: HabitEntity) = database.habitDao().updateHabit(habit)
    suspend fun deleteHabit(id: Long) = database.habitDao().deleteHabitById(id)

    suspend fun toggleHabitDate(habit: HabitEntity, dateStr: String) {
        val currentDates = habit.completedDatesCsv.split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .toMutableSet()

        if (currentDates.contains(dateStr)) {
            currentDates.remove(dateStr)
        } else {
            currentDates.add(dateStr)
        }

        // Recalculate streak counting backwards from today
        var streak = 0
        val cal = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        while (true) {
            val dStr = sdf.format(cal.time)
            if (currentDates.contains(dStr)) {
                streak++
                cal.add(Calendar.DAY_OF_YEAR, -1)
            } else {
                break
            }
        }

        database.habitDao().updateHabit(
            habit.copy(
                completedDatesCsv = currentDates.joinToString(","),
                streak = streak
            )
        )
    }

    suspend fun insertNote(note: NoteEntity): Long = database.noteDao().insertNote(note)
    suspend fun updateNote(note: NoteEntity) = database.noteDao().updateNote(note)
    suspend fun deleteNote(id: Long) = database.noteDao().deleteNoteById(id)
    suspend fun toggleNotePin(note: NoteEntity) {
        database.noteDao().updateNote(note.copy(isPinned = !note.isPinned))
    }

    suspend fun insertEvent(event: CalendarEventEntity): Long = database.calendarEventDao().insertEvent(event)
    suspend fun deleteEvent(id: Long) = database.calendarEventDao().deleteEventById(id)

    suspend fun processAssistantCommand(prompt: String): Pair<AssistantResponse, String?> {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val pendingTasks = database.taskDao().getPendingTasks()
        val todayEvents = database.calendarEventDao().getEventsForDate(today)

        val pendingPreview = pendingTasks.take(3).joinToString(", ") { it.title }
        val eventsPreview = todayEvents.joinToString(", ") { "${it.startTime} - ${it.title}" }

        val response = GeminiService.processCommand(prompt, pendingPreview, eventsPreview)
        var detailFeedback: String? = null

        when (val action = response.action) {
            is AssistantAction.CreateTask -> {
                database.taskDao().insertTask(
                    TaskEntity(
                        title = action.title,
                        category = action.category,
                        priority = action.priority,
                        dueDate = today,
                        status = "pendiente"
                    )
                )
                detailFeedback = "Tarea creada en ${action.category}: ${action.title}"
            }
            is AssistantAction.CompleteTask -> {
                val match = database.taskDao().findPendingTaskByTitle(action.taskTitle)
                if (match != null) {
                    database.taskDao().updateTask(
                        match.copy(
                            isCompleted = true,
                            status = "completada"
                        )
                    )
                    detailFeedback = "Tarea completada: ${match.title}"
                } else {
                    detailFeedback = "No se encontró pendiente para: ${action.taskTitle}"
                }
            }
            is AssistantAction.GetPendingTasks -> {
                detailFeedback = if (pendingTasks.isEmpty()) {
                    "No tienes tareas pendientes."
                } else {
                    "Pendientes (${pendingTasks.size}): ${pendingTasks.take(4).joinToString(", ") { it.title }}"
                }
            }
            is AssistantAction.AddNote -> {
                database.noteDao().insertNote(
                    NoteEntity(
                        title = action.title,
                        content = action.content,
                        category = action.category
                    )
                )
                detailFeedback = "Nota guardada: ${action.title}"
            }
            is AssistantAction.SetTimer -> {
                detailFeedback = "Temporizador de ${action.seconds}s iniciado."
            }
            is AssistantAction.CreateEvent -> {
                database.calendarEventDao().insertEvent(
                    CalendarEventEntity(
                        title = action.title,
                        date = today,
                        startTime = action.time,
                        endTime = calculateEndTime(action.time, 30),
                        description = action.description
                    )
                )
                detailFeedback = "Evento agendado: ${action.title} a las ${action.time}"
            }
            is AssistantAction.GetAgenda -> {
                detailFeedback = if (todayEvents.isEmpty()) {
                    "No tienes compromisos en agenda hoy."
                } else {
                    "Agenda hoy (${todayEvents.size}): " + todayEvents.joinToString(" | ") { "${it.startTime} ${it.title}" }
                }
            }
            is AssistantAction.GeneralReply -> {
                detailFeedback = null
            }
        }

        return Pair(response, detailFeedback)
    }

    suspend fun breakdownGoal(goal: String): List<GeneratedTask> {
        return GeminiService.breakdownGoal(goal)
    }

    suspend fun enhanceNote(content: String, action: String): String {
        return GeminiService.enhanceNote(content, action)
    }

    private fun calculateEndTime(startTime: String, addMinutes: Int): String {
        return try {
            val parts = startTime.split(":").map { it.toInt() }
            val totalMins = parts[0] * 60 + parts[1] + addMinutes
            val endH = (totalMins / 60) % 24
            val endM = totalMins % 60
            String.format(Locale.getDefault(), "%02d:%02d", endH, endM)
        } catch (e: Exception) {
            "13:00"
        }
    }
}
