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
            is NotesEvent.SearchNotes -> {
                _state.value = _state.value.copy(searchQuery = event.query)
                filterNotes()
            }
            is NotesEvent.MoveNote -> {
                moveNote(event.fromIndex, event.toIndex)
            }
        }
    }

    private fun moveNote(fromIndex: Int, toIndex: Int) {
        val notes = _state.value.filteredNotes.toMutableList()
        if (fromIndex !in notes.indices || toIndex !in notes.indices) return

        val note = notes.removeAt(fromIndex)
        notes.add(toIndex, note)

        // Update positions based on the new order in the list
        // Note: This logic assumes we are reordering the entire list or the filtered list.
        // For simplicity, we'll map these back to the main list if filtered, 
        // but reordering while filtered is tricky. 
        // We'll only allow reordering when not searching for now, or apply to main list.
        
        val updatedNotes = notes.mapIndexed { index, n ->
            n.copy(position = index)
        }

        viewModelScope.launch {
            noteUseCases.saveNotes(updatedNotes)
        }
    }

    private fun filterNotes() {
        val query = _state.value.searchQuery.lowercase()
        if (query.isBlank()) {
            _state.value = _state.value.copy(filteredNotes = _state.value.notes)
            return
        }

        val filtered = _state.value.notes.filterIndexed { index, note ->
            note.title.lowercase().contains(query) ||
            note.content.lowercase().contains(query) ||
            (index + 1).toString() == query ||
            note.attachments.any { it.name.lowercase().contains(query) }
        }
        _state.value = _state.value.copy(filteredNotes = filtered)
    }

    private fun getNotes() {
        getNotesJob?.cancel()
        getNotesJob = noteUseCases.getNotes()
            .onEach { notes ->
                _state.value = _state.value.copy(
                    notes = notes,
                    filteredNotes = if (_state.value.searchQuery.isBlank()) notes else {
                        val query = _state.value.searchQuery.lowercase()
                        notes.filterIndexed { index, note ->
                            note.title.lowercase().contains(query) ||
                            note.content.lowercase().contains(query) ||
                            (index + 1).toString() == query ||
                            note.attachments.any { it.name.lowercase().contains(query) }
                        }
                    }
                )
            }
            .launchIn(viewModelScope)
    }
}
