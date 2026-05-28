package com.example.notesappandroidcompose.presentation.notes_list

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notesappandroidcompose.domain.model.Note
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
            is NotesEvent.SoftDeleteNote -> {
                viewModelScope.launch {
                    noteUseCases.saveNotes(listOf(event.note.copy(isDeleted = true, isArchived = false)))
                }
            }
            is NotesEvent.RestoreNote -> {
                viewModelScope.launch {
                    noteUseCases.saveNotes(listOf(event.note.copy(isDeleted = false)))
                }
            }
            is NotesEvent.ArchiveNote -> {
                viewModelScope.launch {
                    noteUseCases.saveNotes(listOf(event.note.copy(isArchived = true, isDeleted = false)))
                }
            }
            is NotesEvent.UnarchiveNote -> {
                viewModelScope.launch {
                    noteUseCases.saveNotes(listOf(event.note.copy(isArchived = false)))
                }
            }
            is NotesEvent.DeleteAllNotes -> {
                viewModelScope.launch {
                    _state.value.notes.let { notes ->
                        noteUseCases.deleteNote(notes)
                    }
                }
            }
            is NotesEvent.EmptyTrash -> {
                viewModelScope.launch {
                    val trashNotes = _state.value.notes.filter { it.isDeleted }
                    noteUseCases.deleteNote(trashNotes)
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
            is NotesEvent.SelectAll -> {
                _state.value = _state.value.copy(
                    selectedNotes = _state.value.filteredNotes.toSet(),
                    isSelectionMode = _state.value.filteredNotes.isNotEmpty()
                )
            }
            is NotesEvent.ClearSelection -> {
                _state.value = _state.value.copy(
                    selectedNotes = emptySet(),
                    isSelectionMode = false
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
            is NotesEvent.SoftDeleteSelectedNotes -> {
                viewModelScope.launch {
                    val updated = _state.value.selectedNotes.map { it.copy(isDeleted = true, isArchived = false) }
                    noteUseCases.saveNotes(updated)
                    _state.value = _state.value.copy(
                        selectedNotes = emptySet(),
                        isSelectionMode = false
                    )
                }
            }
            is NotesEvent.ArchiveSelectedNotes -> {
                viewModelScope.launch {
                    val updated = _state.value.selectedNotes.map { it.copy(isArchived = true, isDeleted = false) }
                    noteUseCases.saveNotes(updated)
                    _state.value = _state.value.copy(
                        selectedNotes = emptySet(),
                        isSelectionMode = false
                    )
                }
            }
            is NotesEvent.TogglePin -> {
                val pinnedCount = _state.value.notes.count { it.isPinned }
                if (!event.note.isPinned && pinnedCount >= 5) {
                    // Maximum of 5 notes can be pinned
                    return
                }
                viewModelScope.launch {
                    noteUseCases.saveNotes(listOf(event.note.copy(isPinned = !event.note.isPinned)))
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
            is NotesEvent.ChangeView -> {
                _state.value = _state.value.copy(currentView = event.view)
                filterNotes()
            }
            is NotesEvent.ChangeSort -> {
                _state.value = _state.value.copy(sortOption = event.sortOption)
                filterNotes()
            }
            is NotesEvent.ToggleGridView -> {
                _state.value = _state.value.copy(isGridView = !_state.value.isGridView)
            }
        }
    }

    private fun moveNote(fromIndex: Int, toIndex: Int) {
        if (_state.value.sortOption != SortOption.Manual) return

        val currentNotes = _state.value.filteredNotes
        if (fromIndex !in currentNotes.indices || toIndex !in currentNotes.indices) return

        val fromNote = currentNotes[fromIndex]
        val toNote = currentNotes[toIndex]

        // Restrict drag-and-drop so pinned notes stay within pinned group
        // and unpinned notes stay within unpinned group.
        if (fromNote.isPinned != toNote.isPinned) return

        val newList = currentNotes.toMutableList()
        val note = newList.removeAt(fromIndex)
        newList.add(toIndex, note)

        val updatedNotes = newList.mapIndexed { index, n ->
            n.copy(position = index)
        }

        // Immediate UI update to ensure smooth drag-and-drop
        // We only update the list we are currently looking at
        _state.value = _state.value.copy(
            filteredNotes = updatedNotes
        )

        viewModelScope.launch {
            noteUseCases.saveNotes(updatedNotes)
        }
    }

    private fun filterNotes() {
        val query = _state.value.searchQuery.lowercase()
        val currentView = _state.value.currentView
        val sortOption = _state.value.sortOption
        
        val baseNotes = when (currentView) {
            is NotesView.All -> _state.value.notes.filter { !it.isArchived && !it.isDeleted }
            is NotesView.Archived -> _state.value.notes.filter { it.isArchived && !it.isDeleted }
            is NotesView.Trash -> _state.value.notes.filter { it.isDeleted }
        }

        val pinnedNotes = baseNotes.filter { it.isPinned }.sortedBy { it.position }
        val unpinnedNotes = baseNotes.filter { !it.isPinned }

        val sortedUnpinnedNotes = when (sortOption) {
            SortOption.Manual -> unpinnedNotes.sortedBy { it.position }
            SortOption.Newest -> unpinnedNotes.sortedByDescending { it.timestamp }
            SortOption.Oldest -> unpinnedNotes.sortedBy { it.timestamp }
            SortOption.TitleAsc -> unpinnedNotes.sortedBy { it.title.lowercase() }
            SortOption.TitleDesc -> unpinnedNotes.sortedByDescending { it.title.lowercase() }
        }

        val combinedNotes = pinnedNotes + sortedUnpinnedNotes

        if (query.isBlank()) {
            _state.value = _state.value.copy(filteredNotes = combinedNotes)
            return
        }

        val filtered = combinedNotes.filterIndexed { index, note ->
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
                _state.value = _state.value.copy(notes = notes)
                filterNotes()
            }
            .launchIn(viewModelScope)
    }
}
