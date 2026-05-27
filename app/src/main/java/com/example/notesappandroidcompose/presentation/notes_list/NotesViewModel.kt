package com.example.notesappandroidcompose.presentation.notes_list

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notesappandroidcompose.domain.use_case.NoteUseCases
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory

class NotesViewModel(
    private val noteUseCases: NoteUseCases
) : ViewModel() {

    companion object {
        val NOTE_USE_CASES_KEY = object : androidx.lifecycle.viewmodel.CreationExtras.Key<NoteUseCases> {}

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val noteUseCases = this[NOTE_USE_CASES_KEY] as NoteUseCases
                NotesViewModel(noteUseCases = noteUseCases)
            }
        }
    }

    private val _state = mutableStateOf(NotesState())
    val state: State<NotesState> = _state

    private var getNotesJob: Job? = null

    init {
        getNotes()
    }

    fun onEvent(event: NotesEvent) {
        when (event) {
            is NotesEvent.DeleteNote -> {
                viewModelScope.launch {
                    noteUseCases.deleteNote(event.note)
                }
            }
            is NotesEvent.DeleteAllNotes -> {
                viewModelScope.launch {
                    // Logic to delete all notes would usually be in a use case
                    // For simplicity, we'll assume deleteNote can handle list or add a new use case
                    _state.value.notes.let { notes ->
                        noteUseCases.deleteNote(notes)
                    }
                }
            }
            is NotesEvent.ToggleSelection -> {
                val selected = _state.value.selectedNotes.toMutableSet()
                if (selected.contains(event.note)) {
                    selected.remove(event.note)
                } else {
                    selected.add(event.note)
                }
                _state.value = _state.value.copy(
                    selectedNotes = selected,
                    isSelectionMode = selected.isNotEmpty()
                )
            }
            is NotesEvent.DeleteSelectedNotes -> {
                viewModelScope.launch {
                    noteUseCases.deleteNote(_state.value.selectedNotes.toList())
                    _state.value = _state.value.copy(
                        selectedNotes = emptySet(),
                        isSelectionMode = false
                    )
                }
            }
            is NotesEvent.ShowDeleteConfirmation -> {
                _state.value = _state.value.copy(showDeleteConfirmation = event.show)
            }
        }
    }

    private fun getNotes() {
        getNotesJob?.cancel()
        getNotesJob = noteUseCases.getNotes()
            .onEach { notes ->
                _state.value = _state.value.copy(
                    notes = notes
                )
            }
            .launchIn(viewModelScope)
    }
}
