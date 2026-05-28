package com.example.notesappandroidcompose.domain.model

data class Note(
    val id: Int? = null,
    val title: String,
    val content: String,
    val attachments: List<Attachment> = emptyList(),
    val timestamp: Long,
    val position: Int = 0,
    val isPinned: Boolean = false,
    val isArchived: Boolean = false,
    val isDeleted: Boolean = false
)
