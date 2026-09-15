package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val priority: String = "media", // "alta", "media", "baja"
    val category: String = "Trabajo",
    val dueDate: String = "",
    val status: String = "pendiente", // "pendiente", "en_progreso", "completada"
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
