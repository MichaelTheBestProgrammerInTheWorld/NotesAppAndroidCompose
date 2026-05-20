package com.example.notesappandroidcompose.presentation.notes_list

import com.example.notesappandroidcompose.domain.model.Note

data class NotesState(
    val notes: List<Note> = emptyList(),
    val selectedNotes: Set<Note> = emptySet(),
    val isSelectionMode: Boolean = false,
    val showDeleteConfirmation: Boolean = false
)
