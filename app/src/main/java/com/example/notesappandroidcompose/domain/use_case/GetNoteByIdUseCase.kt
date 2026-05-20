package com.example.notesappandroidcompose.domain.use_case

import com.example.notesappandroidcompose.domain.model.Note
import com.example.notesappandroidcompose.domain.repository.NoteRepository

class GetNoteByIdUseCase(
    private val repository: NoteRepository
) {
    suspend operator fun invoke(id: Int): Note? {
        return repository.getNoteById(id)
    }
}
