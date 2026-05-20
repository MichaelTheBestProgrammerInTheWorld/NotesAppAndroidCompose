package com.example.notesappandroidcompose.domain.use_case

import com.example.notesappandroidcompose.domain.model.Note
import com.example.notesappandroidcompose.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow

class GetNotesUseCase(
    private val repository: NoteRepository
) {
    operator fun invoke(): Flow<List<Note>> {
        return repository.getNotes()
    }
}
