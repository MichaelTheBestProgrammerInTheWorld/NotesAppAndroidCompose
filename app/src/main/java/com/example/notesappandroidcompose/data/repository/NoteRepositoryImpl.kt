package com.example.notesappandroidcompose.data.repository

import com.example.notesappandroidcompose.data.local.NoteDao
import com.example.notesappandroidcompose.data.mapper.toNote
import com.example.notesappandroidcompose.data.mapper.toNoteEntity
import com.example.notesappandroidcompose.domain.model.Note
import com.example.notesappandroidcompose.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class NoteRepositoryImpl(
    private val dao: NoteDao
) : NoteRepository {

    override fun getNotes(): Flow<List<Note>> {
        return dao.getNotes().map { entities ->
            entities.map { it.toNote() }
        }
    }

    override suspend fun getNoteById(id: Int): Note? {
        return dao.getNoteById(id)?.toNote()
    }

    override suspend fun insertNote(note: Note) {
        dao.insertNote(note.toNoteEntity())
    }

    override suspend fun deleteNote(note: Note) {
        dao.deleteNote(note.toNoteEntity())
    }

    override suspend fun deleteNotes(notes: List<Note>) {
        dao.deleteNotes(notes.map { it.toNoteEntity() })
    }

    override suspend fun deleteAllNotes() {
        dao.deleteAllNotes()
    }
}
