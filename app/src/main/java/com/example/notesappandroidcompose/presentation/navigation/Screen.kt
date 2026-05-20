package com.example.notesappandroidcompose.presentation.navigation

sealed class Screen(val route: String) {
    data object NotesListScreen : Screen("notes_list_screen")
    data object NoteDetailScreen : Screen("note_detail_screen")
}
