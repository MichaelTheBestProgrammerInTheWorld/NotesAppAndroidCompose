package com.example.notesappandroidcompose.presentation.notes_list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay
import com.example.notesappandroidcompose.domain.model.Note
import com.example.notesappandroidcompose.presentation.notes_list.components.NoteItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesListScreen(
    state: NotesState,
    onEvent: (NotesEvent) -> Unit,
    onNoteClick: (Note) -> Unit,
    onAddNoteClick: () -> Unit
) {
    val lazyListState = rememberLazyListState()
    var draggedItemIndex by remember { mutableStateOf<Int?>(null) }
    var draggingOffset by remember { mutableFloatStateOf(0f) }
    var draggingItemInitialOffset by remember { mutableFloatStateOf(0f) }
    val haptic = LocalHapticFeedback.current
    
    val currentOnEvent by rememberUpdatedState(onEvent)

    // Auto-scroll logic when dragging near edges
    LaunchedEffect(draggedItemIndex) {
        if (draggedItemIndex != null) {
            while (draggedItemIndex != null) {
                val layoutInfo = lazyListState.layoutInfo
                val viewportHeight = layoutInfo.viewportEndOffset.toFloat()
                val currentTop = draggingItemInitialOffset + draggingOffset
                
                val scrollAmount = when {
                    currentTop < 100f -> -15f
                    currentTop > viewportHeight - 150f -> 15f
                    else -> 0f
                }
                
                if (scrollAmount != 0f) {
                    val canScroll = if (scrollAmount > 0) lazyListState.canScrollForward else lazyListState.canScrollBackward
                    if (canScroll) {
                        lazyListState.scrollBy(scrollAmount)
                        draggingOffset += scrollAmount
                        draggingItemInitialOffset -= scrollAmount
                    }
                }
                delay(16)
            }
        }
    }

    Scaffold(
        topBar = {
            Column {
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
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = { onEvent(NotesEvent.SearchNotes(it)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    placeholder = { Text("Search title, content, index or file name...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (state.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onEvent(NotesEvent.SearchNotes("")) }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear search")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }
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
            if (state.filteredNotes.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = if (state.searchQuery.isEmpty()) "No notes yet. Tap + to add one!" else "No results found for \"${state.searchQuery}\"",
                        color = Color.Gray
                    )
                }
            } else {
                LazyColumn(
                    state = lazyListState,
                    userScrollEnabled = draggedItemIndex == null,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp)
                ) {
                    itemsIndexed(state.filteredNotes, key = { _, note -> note.id ?: 0 }) { index, note ->
                        val isSelected = state.selectedNotes.contains(note)
                        
                        val isDragging = draggedItemIndex == index
                        val itemModifier = Modifier
                            .fillMaxWidth()
                            .then(
                                if (isDragging) {
                                    Modifier
                                        .zIndex(1f)
                                        .graphicsLayer {
                                            translationY = draggingOffset
                                        }
                                } else {
                                    Modifier.animateItem()
                                }
                            )
                            .pointerInput(state.searchQuery) {
                                if (state.searchQuery.isNotEmpty()) return@pointerInput
                                detectDragGesturesAfterLongPress(
                                    onDragStart = { _ ->
                                        lazyListState.layoutInfo.visibleItemsInfo
                                            .find { it.index == index }
                                            ?.also { item ->
                                                draggedItemIndex = index
                                                draggingItemInitialOffset = item.offset.toFloat()
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            }
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        draggingOffset += dragAmount.y
                                        
                                        val currentOffset = draggingItemInitialOffset + draggingOffset
                                        val overItem = lazyListState.layoutInfo.visibleItemsInfo
                                            .firstOrNull { item ->
                                                currentOffset.toInt() in item.offset..(item.offset + item.size)
                                            }
                                        
                                        if (overItem != null && overItem.index != draggedItemIndex) {
                                            draggedItemIndex?.let { fromIndex ->
                                                currentOnEvent(NotesEvent.MoveNote(fromIndex, overItem.index))
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                draggedItemIndex = overItem.index
                                                draggingItemInitialOffset = overItem.offset.toFloat()
                                                draggingOffset = 0f
                                            }
                                        }
                                    },
                                    onDragEnd = {
                                        draggedItemIndex = null
                                        draggingOffset = 0f
                                    },
                                    onDragCancel = {
                                        draggedItemIndex = null
                                        draggingOffset = 0f
                                    }
                                )
                            }
                        
                        // Find original index for display
                        val originalIndex = state.notes.indexOf(note)
                        
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
                            modifier = itemModifier,
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
                                index = originalIndex,
                                isSelected = isSelected,
                                modifier = Modifier.clickable {
                                    if (state.isSelectionMode) {
                                        onEvent(NotesEvent.ToggleSelection(note))
                                    } else {
                                        onNoteClick(note)
                                    }
                                }
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
