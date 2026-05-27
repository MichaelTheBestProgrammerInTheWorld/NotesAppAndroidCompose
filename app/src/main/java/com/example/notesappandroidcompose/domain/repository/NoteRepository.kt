package com.example.notesappandroidcompose.domain.repository

import com.example.notesappandroidcompose.domain.model.Note
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    fun getNotes(): Flow<List<Note>>
    suspend fun getNoteById(id: Int): Note?
    suspend fun getMaxPosition(): Int
    suspend fun insertNote(note: Note)
    suspend fun insertNotes(notes: List<Note>)
    suspend fun deleteNote(note: Note)
    suspend fun deleteNotes(notes: List<Note>)
    suspend fun deleteAllNotes()
}
