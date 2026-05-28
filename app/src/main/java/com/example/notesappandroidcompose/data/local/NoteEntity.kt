package com.example.notesappandroidcompose.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.notesappandroidcompose.domain.model.Attachment

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    val title: String,
    val content: String,
    val attachments: List<Attachment>,
    val timestamp: Long,
    val position: Int,
    val isPinned: Boolean = false,
    val isArchived: Boolean = false,
    val isDeleted: Boolean = false
)
