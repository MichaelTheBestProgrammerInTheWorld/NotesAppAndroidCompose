package com.example.notesappandroidcompose.presentation.notes_list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
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
import kotlinx.coroutines.launch
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
    val staggeredGridState = rememberLazyStaggeredGridState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    var draggedItemIndex by remember { mutableStateOf<Int?>(null) }
    var draggingOffset by remember { mutableFloatStateOf(0f) }
    var draggingItemInitialOffset by remember { mutableFloatStateOf(0f) }
    
    val currentOnEvent by rememberUpdatedState(onEvent)
    val currentIsSelectionMode by rememberUpdatedState(state.isSelectionMode)
    val currentSearchQuery by rememberUpdatedState(state.searchQuery)

    // Auto-scroll logic when dragging near edges
    LaunchedEffect(draggedItemIndex != null) {
        if (draggedItemIndex != null && !state.isGridView) {
            while (true) {
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

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(modifier = Modifier.height(16.dp))
                NavigationDrawerItem(
                    label = { Text("All Notes") },
                    selected = state.currentView == NotesView.All,
                    onClick = {
                        onEvent(NotesEvent.ChangeView(NotesView.All))
                        scope.launch { drawerState.close() }
                    },
                    icon = { Icon(Icons.AutoMirrored.Filled.Note, contentDescription = null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    label = { Text("Archived") },
                    selected = state.currentView == NotesView.Archived,
                    onClick = {
                        onEvent(NotesEvent.ChangeView(NotesView.Archived))
                        scope.launch { drawerState.close() }
                    },
                    icon = { Icon(Icons.Default.Archive, contentDescription = null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    label = { Text("Trash") },
                    selected = state.currentView == NotesView.Trash,
                    onClick = {
                        onEvent(NotesEvent.ChangeView(NotesView.Trash))
                        scope.launch { drawerState.close() }
                    },
                    icon = { Icon(Icons.Default.Delete, contentDescription = null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                Column {
                    TopAppBar(
                        title = {
                            Text(
                                when (state.currentView) {
                                    NotesView.All -> "My Notes"
                                    NotesView.Archived -> "Archived"
                                    NotesView.Trash -> "Trash"
                                }
                            )
                        },
                        navigationIcon = {
                            if (state.isSelectionMode) {
                                IconButton(onClick = { onEvent(NotesEvent.ClearSelection) }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear Selection")
                                }
                            } else {
                                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                    Icon(Icons.Default.Menu, contentDescription = "Menu")
                                }
                            }
                        },
                        actions = {
                            if (state.isSelectionMode) {
                                when (state.currentView) {
                                    NotesView.Trash -> {
                                        IconButton(onClick = { onEvent(NotesEvent.DeleteSelectedNotes) }) {
                                            Icon(Icons.Default.DeleteForever, contentDescription = "Delete forever")
                                        }
                                        val firstSelectedNote = state.selectedNotes.firstOrNull()
                                        IconButton(onClick = { 
                                            firstSelectedNote?.let { onEvent(NotesEvent.RestoreNote(it)) }
                                        }, enabled = firstSelectedNote != null) {
                                             Icon(Icons.Default.RestoreFromTrash, contentDescription = "Restore")
                                        }
                                    }
                                    else -> {
                                        IconButton(onClick = { onEvent(NotesEvent.ArchiveSelectedNotes) }) {
                                            Icon(Icons.Default.Archive, contentDescription = "Archive")
                                        }
                                        IconButton(onClick = { onEvent(NotesEvent.SoftDeleteSelectedNotes) }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Move to trash")
                                        }
                                    }
                                }
                            } else {
                                IconButton(onClick = { 
                                    // I need to add ToggleGridView to NotesEvent
                                    onEvent(NotesEvent.ToggleGridView)
                                }) {
                                    Icon(
                                        imageVector = if (state.isGridView) Icons.AutoMirrored.Filled.ViewList else Icons.Default.GridView,
                                        contentDescription = "Toggle View"
                                    )
                                }
                                if (state.currentView == NotesView.Trash) {
                                    IconButton(onClick = { onEvent(NotesEvent.ShowDeleteConfirmation(true)) }) {
                                        Icon(Icons.Default.DeleteForever, contentDescription = "Empty Trash")
                                    }
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            titleContentColor = MaterialTheme.colorScheme.onPrimary,
                            actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
                            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                    OutlinedTextField(
                        value = state.searchQuery,
                        onValueChange = { onEvent(NotesEvent.SearchNotes(it)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        placeholder = { Text("Search...") },
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
                if (state.currentView == NotesView.All) {
                    FloatingActionButton(
                        onClick = onAddNoteClick,
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Note", tint = Color.White)
                    }
                }
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding)) {
                if (state.filteredNotes.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = if (state.searchQuery.isEmpty()) "Empty" else "No results found",
                            color = Color.Gray
                        )
                    }
                } else if (state.isGridView) {
                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Fixed(2),
                        state = staggeredGridState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalItemSpacing = 8.dp
                    ) {
                        itemsIndexed(state.filteredNotes, key = { _, note -> note.id ?: 0 }) { _, note ->
                            NoteItem(
                                note = note,
                                index = state.notes.indexOf(note),
                                isSelected = state.selectedNotes.contains(note),
                                modifier = Modifier.clickable {
                                    if (state.isSelectionMode) {
                                        onEvent(NotesEvent.ToggleSelection(note))
                                    } else {
                                        onNoteClick(note)
                                    }
                                },
                                onPinClick = { onEvent(NotesEvent.TogglePin(note)) },
                                onSelectClick = { onEvent(NotesEvent.ToggleSelection(note)) }
                            )
                        }
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
                            val currentNote by rememberUpdatedState(note)
                            val currentIndex by rememberUpdatedState(index)
                            val currentIsSelected by rememberUpdatedState(isSelected)
                            
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
                                .pointerInput(Unit) {
                                    detectDragGesturesAfterLongPress(
                                        onDragStart = { _ ->
                                            val isSearching = currentSearchQuery.isNotEmpty()
                                            val isMultiSelect = state.selectedNotes.size > 1
                                            
                                            if (isSearching || isMultiSelect || state.currentView != NotesView.All) {
                                                currentOnEvent(NotesEvent.ToggleSelection(currentNote))
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                return@detectDragGesturesAfterLongPress
                                            }

                                            if (!currentIsSelected) {
                                                currentOnEvent(NotesEvent.ToggleSelection(currentNote))
                                            }

                                            lazyListState.layoutInfo.visibleItemsInfo
                                                .find { it.index == currentIndex }
                                                ?.also { item ->
                                                    draggedItemIndex = currentIndex
                                                    draggingItemInitialOffset = item.offset.toFloat()
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                }
                                        },
                                        onDrag = { change, dragAmount ->
                                            if (currentSearchQuery.isEmpty() && state.currentView == NotesView.All) {
                                                change.consume()
                                                draggingOffset += dragAmount.y
                                                
                                                val draggingItem = lazyListState.layoutInfo.visibleItemsInfo
                                                    .find { it.index == draggedItemIndex }
                                                val draggingItemSize = draggingItem?.size ?: 0
                                                val currentCenter = draggingItemInitialOffset + draggingOffset + draggingItemSize / 2
                                                
                                                val overItem = lazyListState.layoutInfo.visibleItemsInfo
                                                    .firstOrNull { item ->
                                                        currentCenter.toInt() in item.offset..(item.offset + item.size)
                                                    }
                                                
                                                if (overItem != null && overItem.index != draggedItemIndex) {
                                                    draggedItemIndex?.let { fromIndex ->
                                                        currentOnEvent(NotesEvent.MoveNote(fromIndex, overItem.index))
                                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                        
                                                        val prevItemOffset = draggingItemInitialOffset
                                                        draggedItemIndex = overItem.index
                                                        draggingItemInitialOffset = overItem.offset.toFloat()
                                                        draggingOffset += prevItemOffset - draggingItemInitialOffset
                                                    }
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
                            
                            val dismissState = rememberSwipeToDismissBoxState(
                                confirmValueChange = {
                                    if (it == SwipeToDismissBoxValue.EndToStart) {
                                        onEvent(NotesEvent.SoftDeleteNote(note))
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
                                    index = state.notes.indexOf(note),
                                    isSelected = isSelected,
                                    modifier = Modifier.clickable {
                                        if (state.isSelectionMode) {
                                            onEvent(NotesEvent.ToggleSelection(note))
                                        } else {
                                            onNoteClick(note)
                                        }
                                    },
                                    onPinClick = { onEvent(NotesEvent.TogglePin(note)) },
                                    onSelectClick = { onEvent(NotesEvent.ToggleSelection(note)) }
                                )
                            }
                        }
                    }
                }
            }

            if (state.showDeleteConfirmation) {
                AlertDialog(
                    onDismissRequest = { onEvent(NotesEvent.ShowDeleteConfirmation(false)) },
                    title = { Text(if (state.currentView == NotesView.Trash) "Empty Trash?" else "Delete Selected?") },
                    text = { Text("Are you sure? This action cannot be undone.") },
                    confirmButton = {
                        TextButton(onClick = {
                            if (state.currentView == NotesView.Trash) {
                                onEvent(NotesEvent.EmptyTrash)
                            } else {
                                onEvent(NotesEvent.DeleteSelectedNotes)
                            }
                            onEvent(NotesEvent.ShowDeleteConfirmation(false))
                        }) {
                            Text("Confirm", color = Color.Red)
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
}
