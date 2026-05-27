package com.example.notesappandroidcompose.domain.model

data class Note(
    val id: Int? = null,
    val title: String,
    val content: String,
    val attachments: List<Attachment> = emptyList()
)
