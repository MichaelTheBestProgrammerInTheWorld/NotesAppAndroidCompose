package com.example.notesappandroidcompose.presentation.note_detail

import com.example.notesappandroidcompose.domain.model.Attachment

data class NoteDetailState(
    val title: String = "",
    val content: String = "",
    val id: Int? = null,
    val attachments: List<Attachment> = emptyList()
)
