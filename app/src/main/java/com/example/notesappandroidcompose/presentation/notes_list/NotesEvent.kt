package com.example.notesappandroidcompose.presentation.notes_list

import com.example.notesappandroidcompose.domain.model.Note

sealed class NotesEvent {
    data class DeleteNote(val note: Note) : NotesEvent()
    data object DeleteAllNotes : NotesEvent()
    data class ToggleSelection(val note: Note) : NotesEvent()
    data object DeleteSelectedNotes : NotesEvent()
    data class ShowDeleteConfirmation(val show: Boolean) : NotesEvent()
    data class SearchNotes(val query: String) : NotesEvent()
}
