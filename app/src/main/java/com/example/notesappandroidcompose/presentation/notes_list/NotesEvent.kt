package com.example.notesappandroidcompose.presentation.notes_list

import com.example.notesappandroidcompose.domain.model.Note

sealed class NotesEvent {
    data class DeleteNote(val note: Note) : NotesEvent()
    data class SoftDeleteNote(val note: Note) : NotesEvent()
    data class RestoreNote(val note: Note) : NotesEvent()
    data class ArchiveNote(val note: Note) : NotesEvent()
    data class UnarchiveNote(val note: Note) : NotesEvent()
    data object DeleteAllNotes : NotesEvent()
    data object EmptyTrash : NotesEvent()
    data class ToggleSelection(val note: Note) : NotesEvent()
    data object DeleteSelectedNotes : NotesEvent()
    data object SoftDeleteSelectedNotes : NotesEvent()
    data object ArchiveSelectedNotes : NotesEvent()
    data class ShowDeleteConfirmation(val show: Boolean) : NotesEvent()
    data class SearchNotes(val query: String) : NotesEvent()
    data class MoveNote(val fromIndex: Int, val toIndex: Int) : NotesEvent()
    data class ChangeView(val view: NotesView) : NotesEvent()
    data object ToggleGridView : NotesEvent()
}

sealed class NotesView {
    object All : NotesView()
    object Archived : NotesView()
    object Trash : NotesView()
}
