package com.example.notesappandroidcompose.domain.use_case

import com.example.notesappandroidcompose.domain.model.Note
import com.example.notesappandroidcompose.domain.repository.NoteRepository

class SaveNotesUseCase(
    private val repository: NoteRepository
) {
    suspend operator fun invoke(notes: List<Note>) {
        repository.insertNotes(notes)
    }
}
