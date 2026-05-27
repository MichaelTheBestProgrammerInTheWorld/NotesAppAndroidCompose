package com.example.notesappandroidcompose.presentation.note_detail

import com.example.notesappandroidcompose.domain.model.Attachment

sealed class NoteDetailEvent {
    data class EnteredTitle(val value: String) : NoteDetailEvent()
    data class EnteredContent(val value: String) : NoteDetailEvent()
    data class ContentChanged(val html: String) : NoteDetailEvent()
    data class AddAttachment(val attachment: Attachment) : NoteDetailEvent()
    data class ToggleRecording(val context: android.content.Context) : NoteDetailEvent()
    data class RemoveAttachment(val attachment: Attachment) : NoteDetailEvent()
    data object SaveNote : NoteDetailEvent()
}
