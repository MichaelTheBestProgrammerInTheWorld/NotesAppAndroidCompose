package com.example.notesappandroidcompose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.room.Room
import com.example.notesappandroidcompose.data.local.NoteDatabase
import com.example.notesappandroidcompose.data.repository.NoteRepositoryImpl
import com.example.notesappandroidcompose.domain.use_case.*
import com.example.notesappandroidcompose.presentation.navigation.Screen
import com.example.notesappandroidcompose.presentation.note_detail.NoteDetailScreen
import com.example.notesappandroidcompose.presentation.note_detail.NoteDetailViewModel
import com.example.notesappandroidcompose.presentation.notes_list.NotesListScreen
import com.example.notesappandroidcompose.presentation.notes_list.NotesViewModel
import androidx.lifecycle.viewmodel.MutableCreationExtras
import com.example.notesappandroidcompose.presentation.theme.NotesAppAndroidComposeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Manual Dependency Injection for simplicity in this task
        val db = Room.databaseBuilder(
            applicationContext,
            NoteDatabase::class.java,
            NoteDatabase.DATABASE_NAME
        ).addMigrations(NoteDatabase.MIGRATION_5_6)
            .build()
        
        val repository = NoteRepositoryImpl(db.noteDao)
        val noteUseCases = NoteUseCases(
            getNotes = GetNotesUseCase(repository),
            deleteNote = DeleteNoteUseCase(repository),
            saveNote = SaveNoteUseCase(repository),
            saveNotes = SaveNotesUseCase(repository),
            getNoteById = GetNoteByIdUseCase(repository)
        )

        enableEdgeToEdge()
        setContent {
            NotesAppAndroidComposeTheme {
                val navController = rememberNavController()
                
                NavHost(
                    navController = navController,
                    startDestination = Screen.NotesListScreen.route
                ) {
                    composable(route = Screen.NotesListScreen.route) { backStackEntry ->
                        val viewModel: NotesViewModel = viewModel(
                            factory = NotesViewModel.Factory,
                            extras = MutableCreationExtras(backStackEntry.defaultViewModelCreationExtras).apply {
                                set(NotesViewModel.NOTE_USE_CASES_KEY, noteUseCases)
                            }
                        )
                        NotesListScreen(
                            state = viewModel.state.value,
                            onEvent = viewModel::onEvent,
                            onNoteClick = { note ->
                                navController.navigate(
                                    Screen.NoteDetailScreen.route + "?noteId=${note.id}"
                                )
                            },
                            onAddNoteClick = {
                                navController.navigate(Screen.NoteDetailScreen.route)
                            }
                        )
                    }
                    composable(
                        route = Screen.NoteDetailScreen.route + "?noteId={noteId}",
                        arguments = listOf(
                            navArgument(name = "noteId") {
                                type = NavType.IntType
                                defaultValue = -1
                            }
                        )
                    ) { backStackEntry ->
                        val viewModel: NoteDetailViewModel = viewModel(
                            factory = NoteDetailViewModel.Factory,
                            extras = MutableCreationExtras(backStackEntry.defaultViewModelCreationExtras).apply {
                                set(NoteDetailViewModel.NOTE_USE_CASES_KEY, noteUseCases)
                            }
                        )
                        NoteDetailScreen(
                            state = viewModel.state.value,
                            eventFlow = viewModel.eventFlow,
                            onEvent = viewModel::onEvent,
                            onNavigateBack = {
                                navController.navigateUp()
                            }
                        )
                    }
                }
            }
        }
    }
}
