package com.example.notesappandroidcompose.domain.use_case

data class NoteUseCases(
    val getNotes: GetNotesUseCase,
    val deleteNote: DeleteNoteUseCase,
    val saveNote: SaveNoteUseCase,
    val getNoteById: GetNoteByIdUseCase
)
