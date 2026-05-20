package com.example.notesappandroidcompose.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.notesappandroidcompose.domain.use_case.NoteUseCases
import com.example.notesappandroidcompose.presentation.notes_list.NotesViewModel
import com.example.notesappandroidcompose.presentation.note_detail.NoteDetailViewModel

class ViewModelFactory(
    private val noteUseCases: NoteUseCases
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        return when {
            modelClass.isAssignableFrom(NotesViewModel::class.java) -> {
                NotesViewModel(noteUseCases) as T
            }
            modelClass.isAssignableFrom(NoteDetailViewModel::class.java) -> {
                val savedStateHandle = extras.createSavedStateHandle()
                NoteDetailViewModel(noteUseCases, savedStateHandle) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
