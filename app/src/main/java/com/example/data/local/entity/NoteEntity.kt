package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String,
    val category: String = "General",
    val colorHex: String = "#6366F1",
    val isPinned: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)
