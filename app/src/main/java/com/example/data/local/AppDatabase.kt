package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.CalendarEventDao
import com.example.data.local.dao.HabitDao
import com.example.data.local.dao.NoteDao
import com.example.data.local.dao.TaskDao
import com.example.data.local.entity.CalendarEventEntity
import com.example.data.local.entity.HabitEntity
import com.example.data.local.entity.NoteEntity
import com.example.data.local.entity.TaskEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Database(
    entities = [
        TaskEntity::class,
        HabitEntity::class,
        NoteEntity::class,
        CalendarEventEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun habitDao(): HabitDao
    abstract fun noteDao(): NoteDao
    abstract fun calendarEventDao(): CalendarEventDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "famous_assistant_database"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                populateInitialData(getDatabase(context))
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun populateInitialData(db: AppDatabase) {
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            // Initial Tasks
            db.taskDao().insertTasks(
                listOf(
                    TaskEntity(
                        title = "Definir objetivos de la semana",
                        description = "Priorizar proyectos clave y establecer hitos ejecutivos.",
                        priority = "alta",
                        category = "Trabajo",
                        dueDate = today,
                        status = "en_progreso",
                        isCompleted = false
                    ),
                    TaskEntity(
                        title = "Sesión de enfoque Pomodoro (50 min)",
                        description = "Avanzar en el entregable principal sin interrupciones ni redes.",
                        priority = "alta",
                        category = "Estudio",
                        dueDate = today,
                        status = "pendiente",
                        isCompleted = false
                    ),
                    TaskEntity(
                        title = "Revisión y contestación de correos",
                        description = "Procesar bandeja de entrada ejecutiva a Inbox Cero.",
                        priority = "media",
                        category = "Trabajo",
                        dueDate = today,
                        status = "pendiente",
                        isCompleted = false
                    ),
                    TaskEntity(
                        title = "30 minutos de lectura o ejercicio",
                        description = "Caminar al aire libre o avanzar 1 capítulo del libro.",
                        priority = "baja",
                        category = "Salud",
                        dueDate = today,
                        status = "completada",
                        isCompleted = true
                    )
                )
            )

            // Initial Habits
            db.habitDao().insertHabits(
                listOf(
                    HabitEntity(
                        name = "Beber 2L de agua",
                        category = "Salud",
                        colorHex = "#06B6D4",
                        targetPerWeek = 7,
                        streak = 5,
                        completedDatesCsv = today
                    ),
                    HabitEntity(
                        name = "Meditación matutina (10 min)",
                        category = "Bienestar",
                        colorHex = "#8B5CF6",
                        targetPerWeek = 5,
                        streak = 3,
                        completedDatesCsv = ""
                    ),
                    HabitEntity(
                        name = "Planificación diaria nocturna",
                        category = "Productividad",
                        colorHex = "#10B981",
                        targetPerWeek = 7,
                        streak = 12,
                        completedDatesCsv = today
                    ),
                    HabitEntity(
                        name = "Lectura de 20 páginas",
                        category = "Aprendizaje",
                        colorHex = "#F59E0B",
                        targetPerWeek = 6,
                        streak = 4,
                        completedDatesCsv = ""
                    )
                )
            )

            // Initial Notes
            db.noteDao().insertNotes(
                listOf(
                    NoteEntity(
                        title = "Regla 80/20 (Principio de Pareto)",
                        content = "El 80% de tus resultados provienen del 20% de tus esfuerzos enfocados. Identifica tus tareas de alto impacto temprano en el día.",
                        category = "Estrategia",
                        colorHex = "#6366F1",
                        isPinned = true
                    ),
                    NoteEntity(
                        title = "Ideas para proyectos del trimestre",
                        content = "1. Automatización de reportes semanales\n2. Optimización del flujo de trabajo diario\n3. Implementación de bloques de descanso activo",
                        category = "Proyectos",
                        colorHex = "#0EA5E9",
                        isPinned = false
                    )
                )
            )

            // Initial Calendar Events
            db.calendarEventDao().insertEvents(
                listOf(
                    CalendarEventEntity(
                        title = "Reunión Estratégica Trimestral",
                        date = today,
                        startTime = "10:00",
                        endTime = "11:00",
                        location = "Sala de Juntas B / Google Meet",
                        description = "Revisión de KPIs y planificación de metas."
                    ),
                    CalendarEventEntity(
                        title = "Llamada con Proveedor de Insumos",
                        date = today,
                        startTime = "15:30",
                        endTime = "16:00",
                        location = "Llamada Telefónica",
                        description = "Ajuste de tiempos de logística e inventario."
                    ),
                    CalendarEventEntity(
                        title = "Revisión de Presupuesto y Finanzas",
                        date = today,
                        startTime = "17:00",
                        endTime = "18:00",
                        location = "Oficina Principal",
                        description = "Análisis de costos operativos."
                    )
                )
            )
        }
    }
}
