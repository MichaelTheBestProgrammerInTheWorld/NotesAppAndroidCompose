package com.example.notesappandroidcompose.presentation.notes_list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.notesappandroidcompose.domain.model.Note
import com.example.notesappandroidcompose.presentation.notes_list.components.NoteItem

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun NotesListScreen(
    state: NotesState,
    onEvent: (NotesEvent) -> Unit,
    onNoteClick: (Note) -> Unit,
    onAddNoteClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Notes") },
                actions = {
                    if (state.isSelectionMode) {
                        IconButton(onClick = { onEvent(NotesEvent.ShowDeleteConfirmation(true)) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete selected")
                        }
                    } else if (state.notes.isNotEmpty()) {
                        IconButton(onClick = { onEvent(NotesEvent.ShowDeleteConfirmation(true)) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete all")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddNoteClick,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Note", tint = Color.White)
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            if (state.notes.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "No notes yet. Tap + to add one!", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp)
                ) {
                    itemsIndexed(state.notes, key = { _, note -> note.id ?: 0 }) { index, note ->
                        val isSelected = state.selectedNotes.contains(note)
                        
                        // Swipe to delete logic
                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = {
                                if (it == SwipeToDismissBoxValue.EndToStart) {
                                    onEvent(NotesEvent.DeleteNote(note))
                                    true
                                } else false
                            }
                        )

                        SwipeToDismissBox(
                            state = dismissState,
                            backgroundContent = {
                                val color = when (dismissState.dismissDirection) {
                                    SwipeToDismissBoxValue.EndToStart -> Color.Red.copy(alpha = 0.6f)
                                    else -> Color.Transparent
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(color)
                                        .padding(horizontal = 20.dp),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White)
                                }
                            },
                            enableDismissFromStartToEnd = false
                        ) {
                            NoteItem(
                                note = note,
                                index = index,
                                isSelected = isSelected,
                                modifier = Modifier.combinedClickable(
                                    onClick = {
                                        if (state.isSelectionMode) {
                                            onEvent(NotesEvent.ToggleSelection(note))
                                        } else {
                                            onNoteClick(note)
                                        }
                                    },
                                    onLongClick = {
                                        onEvent(NotesEvent.ToggleSelection(note))
                                    }
                                ),
                                onNoteClick = {}
                            )
                        }
                    }
                }
            }
        }

        if (state.showDeleteConfirmation) {
            AlertDialog(
                onDismissRequest = { onEvent(NotesEvent.ShowDeleteConfirmation(false)) },
                title = { Text(if (state.isSelectionMode) "Delete Selected?" else "Delete All Notes?") },
                text = { Text("Are you sure you want to delete ${if (state.isSelectionMode) "the selected" else "all"} notes? This action cannot be undone.") },
                confirmButton = {
                    TextButton(onClick = {
                        if (state.isSelectionMode) {
                            onEvent(NotesEvent.DeleteSelectedNotes)
                        } else {
                            onEvent(NotesEvent.DeleteAllNotes)
                        }
                        onEvent(NotesEvent.ShowDeleteConfirmation(false))
                    }) {
                        Text("Delete", color = Color.Red)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { onEvent(NotesEvent.ShowDeleteConfirmation(false)) }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
