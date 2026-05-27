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

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.notesappandroidcompose.domain.model.Attachment
import com.example.notesappandroidcompose.domain.model.AttachmentType
import java.io.File

class NoteDetailViewModel(
    private val noteUseCases: NoteUseCases,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    companion object {
        val NOTE_USE_CASES_KEY = object : androidx.lifecycle.viewmodel.CreationExtras.Key<NoteUseCases> {}

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val savedStateHandle = createSavedStateHandle()
                val noteUseCases = this[NOTE_USE_CASES_KEY] as NoteUseCases
                NoteDetailViewModel(
                    noteUseCases = noteUseCases,
                    savedStateHandle = savedStateHandle
                )
            }
        }
    }

    private val _state = mutableStateOf(NoteDetailState())
    val state: State<NoteDetailState> = _state

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private var currentNoteId: Int? = null
    private var voiceRecorder: VoiceRecorder? = null
    private var currentRecordingFile: File? = null

    init {
        savedStateHandle.get<Int>("noteId")?.let { noteId ->
            if (noteId != -1) {
                viewModelScope.launch {
                    noteUseCases.getNoteById(noteId)?.also { note ->
                        currentNoteId = note.id
                        _state.value = _state.value.copy(
                            title = note.title,
                            content = note.content,
                            id = note.id,
                            attachments = note.attachments
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
            is NoteDetailEvent.ContentChanged -> {
                _state.value = _state.value.copy(content = event.html)
            }
            is NoteDetailEvent.AddAttachment -> {
                _state.value = _state.value.copy(
                    attachments = _state.value.attachments + event.attachment
                )
            }
            is NoteDetailEvent.ToggleRecording -> {
                if (voiceRecorder == null) {
                    voiceRecorder = VoiceRecorder(event.context)
                }
                
                if (_state.value.isRecording) {
                    voiceRecorder?.stop()
                    _state.value = _state.value.copy(isRecording = false)
                    currentRecordingFile?.let { file ->
                        val attachment = Attachment(
                            uri = android.net.Uri.fromFile(file).toString(),
                            type = AttachmentType.VOICE,
                            name = "Voice Note ${System.currentTimeMillis()}"
                        )
                        onEvent(NoteDetailEvent.AddAttachment(attachment))
                    }
                } else {
                    val file = File(event.context.cacheDir, "recording_${System.currentTimeMillis()}.m4a")
                    currentRecordingFile = file
                    voiceRecorder?.start(file)
                    _state.value = _state.value.copy(isRecording = true)
                }
            }
            is NoteDetailEvent.RemoveAttachment -> {
                _state.value = _state.value.copy(
                    attachments = _state.value.attachments.filter { it.uri != event.attachment.uri }
                )
            }
            is NoteDetailEvent.SaveNote -> {
                viewModelScope.launch {
                    noteUseCases.saveNote(
                        Note(
                            title = state.value.title,
                            content = state.value.content,
                            id = currentNoteId,
                            attachments = state.value.attachments,
                            timestamp = System.currentTimeMillis()
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
                id = it.id,
                attachments = it.attachments
            )
        }
    }
}
