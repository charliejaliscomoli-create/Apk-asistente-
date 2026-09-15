package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val category: String = "Salud",
    val colorHex: String = "#6366F1",
    val targetPerWeek: Int = 7,
    val streak: Int = 0,
    val completedDatesCsv: String = "" // comma-separated YYYY-MM-DD dates
)
