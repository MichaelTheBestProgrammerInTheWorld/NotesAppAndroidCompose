package com.example.notesappandroidcompose.presentation.note_detail

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notesappandroidcompose.domain.model.Note
import com.example.notesappandroidcompose.domain.repository.NoteRepository
import com.example.notesappandroidcompose.domain.use_case.NoteUseCases
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class NoteDetailViewModel(
    private val noteUseCases: NoteUseCases,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = mutableStateOf(NoteDetailState())
    val state: State<NoteDetailState> = _state

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private var currentNoteId: Int? = null

    init {
        savedStateHandle.get<Int>("noteId")?.let { noteId ->
            if (noteId != -1) {
                viewModelScope.launch {
                    noteUseCases.getNoteById(noteId)?.also { note ->
                        currentNoteId = note.id
                        _state.value = _state.value.copy(
                            title = note.title,
                            content = note.content,
                            id = note.id
                        )
                    }
                }
            }
        }
    }

    // Since I missed GetNoteUseCase in NoteUseCases, I'll update NoteUseCases or just use repo if I pass it.
    // Actually, I'll just create the UI Event class first.

    sealed class UiEvent {
        data object SaveNote : UiEvent()
    }

    fun onEvent(event: NoteDetailEvent) {
        when (event) {
            is NoteDetailEvent.EnteredTitle -> {
                _state.value = _state.value.copy(title = event.value)
            }
            is NoteDetailEvent.EnteredContent -> {
                _state.value = _state.value.copy(content = event.value)
            }
            is NoteDetailEvent.SaveNote -> {
                viewModelScope.launch {
                    noteUseCases.saveNote(
                        Note(
                            title = state.value.title,
                            content = state.value.content,
                            id = currentNoteId
                        )
                    )
                    _eventFlow.emit(UiEvent.SaveNote)
                }
            }
        }
    }
    
    fun setNote(note: Note?) {
        note?.let {
            currentNoteId = it.id
            _state.value = _state.value.copy(
                title = it.title,
                content = it.content,
                id = it.id
            )
        }
    }
}
