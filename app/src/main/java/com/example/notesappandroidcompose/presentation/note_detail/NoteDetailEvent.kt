package com.example.notesappandroidcompose.presentation.note_detail

sealed class NoteDetailEvent {
    data class EnteredTitle(val value: String) : NoteDetailEvent()
    data class EnteredContent(val value: String) : NoteDetailEvent()
    data object SaveNote : NoteDetailEvent()
}
