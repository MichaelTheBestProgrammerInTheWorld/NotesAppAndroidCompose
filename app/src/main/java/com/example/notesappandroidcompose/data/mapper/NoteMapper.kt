package com.example.notesappandroidcompose.data.mapper

import com.example.notesappandroidcompose.data.local.NoteEntity
import com.example.notesappandroidcompose.domain.model.Note

fun NoteEntity.toNote(): Note {
    return Note(
        id = id,
        title = title,
        content = content
    )
}

fun Note.toNoteEntity(): NoteEntity {
    return NoteEntity(
        id = id,
        title = title,
        content = content
    )
}
