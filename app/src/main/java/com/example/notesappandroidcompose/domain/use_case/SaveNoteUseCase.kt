package com.example.notesappandroidcompose.domain.use_case

import com.example.notesappandroidcompose.domain.model.Note
import com.example.notesappandroidcompose.domain.repository.NoteRepository

class SaveNoteUseCase(
    private val repository: NoteRepository
) {
    suspend operator fun invoke(note: Note) {
        if (note.title.isBlank() && note.content.isBlank()) return
        repository.insertNote(note)
    }
}
