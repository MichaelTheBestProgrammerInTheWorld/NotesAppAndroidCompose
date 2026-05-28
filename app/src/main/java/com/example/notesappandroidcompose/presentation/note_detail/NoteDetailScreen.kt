package com.example.notesappandroidcompose.presentation.note_detail

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import android.widget.Toast
import com.example.notesappandroidcompose.domain.model.Attachment
import com.example.notesappandroidcompose.domain.model.AttachmentType
import com.mohamedrejeb.richeditor.model.RichTextState
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditor
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditorDefaults
import kotlinx.coroutines.flow.collectLatest
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material.icons.filled.FormatStrikethrough
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.core.net.toUri

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun NoteDetailScreen(
    state: NoteDetailState,
    eventFlow: kotlinx.coroutines.flow.SharedFlow<NoteDetailViewModel.UiEvent>,
    onEvent: (NoteDetailEvent) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var previewIndex by remember { mutableIntStateOf(-1) }
    val richTextState = rememberRichTextState()
    val voicePlayer = remember { VoicePlayer(context) }

    DisposableEffect(Unit) {
        onDispose {
            voicePlayer.stop()
        }
    }

    // Initial load of content
    LaunchedEffect(state.id) {
        if (state.id != null) {
            richTextState.setHtml(state.content)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                onEvent(NoteDetailEvent.ToggleRecording(context))
            }
        }
    )

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments(),
        onResult = { uris ->
            uris.forEach { uri ->
                val attachment = getAttachmentFromUri(context, uri)
                attachment?.let { at ->
                    onEvent(NoteDetailEvent.AddAttachment(at))
                }
            }
        }
    )

    LaunchedEffect(key1 = true) {
        eventFlow.collectLatest { event ->
            when (event) {
                is NoteDetailViewModel.UiEvent.SaveNote -> {
                    onNavigateBack()
                }
                is NoteDetailViewModel.UiEvent.ShowError -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.id == null) "Add Note" else "Edit Note") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.Close, contentDescription = "Cancel")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val hasPermission = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED
                        
                        if (hasPermission) {
                            onEvent(NoteDetailEvent.ToggleRecording(context))
                        } else {
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    }) {
                        Icon(
                            imageVector = if (state.isRecording) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = "Voice Note",
                            tint = if (state.isRecording) Color.Red else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = {
                        launcher.launch(arrayOf(
                            "image/*",
                            "video/*",
                            "audio/*",
                            "application/pdf",
                            "text/*"
                        ))
                    }) {
                        Icon(Icons.Default.AttachFile, contentDescription = "Attach File")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    onEvent(NoteDetailEvent.ContentChanged(richTextState.toHtml()))
                    onEvent(NoteDetailEvent.SaveNote)
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Check, contentDescription = "Save Note", tint = Color.White)
            }
        }
    ) { padding ->
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = state.title,
                onValueChange = { onEvent(NoteDetailEvent.EnteredTitle(it)) },
                placeholder = { Text("Title", style = MaterialTheme.typography.headlineSmall) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                textStyle = MaterialTheme.typography.headlineSmall,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                )
            )
            
            if (state.attachments.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    items(state.attachments) { attachment ->
                        AttachmentItem(
                            attachment = attachment,
                            onRemove = { onEvent(NoteDetailEvent.RemoveAttachment(attachment)) },
                            onClick = {
                                when (attachment.type) {
                                    AttachmentType.IMAGE, AttachmentType.TEXT, AttachmentType.PDF, AttachmentType.VOICE -> {
                                        previewIndex = state.attachments.indexOf(attachment)
                                    }
                                    else -> {
                                        openAttachmentExternally(context, attachment)
                                    }
                                }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            RichTextToolbar(state = richTextState)
            RichTextEditor(
                state = richTextState,
                placeholder = { Text("Content") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 300.dp),
                colors = RichTextEditorDefaults.richTextEditorColors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    containerColor = Color.Transparent,
                )
            )
        }
    }

    if (previewIndex != -1) {
        AttachmentPagerDialog(
            attachments = state.attachments,
            initialIndex = previewIndex,
            onDismiss = {
                voicePlayer.stop()
                previewIndex = -1
            },
            voicePlayer = voicePlayer
        )
    }
}

@Composable
fun AttachmentItem(
    attachment: Attachment,
    onRemove: () -> Unit,
    onClick: () -> Unit
) {
    val icon = when (attachment.type) {
        AttachmentType.IMAGE -> null // We'll show the image itself
        AttachmentType.VIDEO -> Icons.Default.Movie
        AttachmentType.AUDIO -> Icons.Default.MusicNote
        AttachmentType.VOICE -> Icons.Default.Mic
        AttachmentType.PDF -> Icons.Default.Description
        AttachmentType.TEXT -> Icons.Default.Description
        AttachmentType.OTHER -> Icons.AutoMirrored.Filled.InsertDriveFile
    }

    Box(
        modifier = Modifier
            .size(100.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { onClick() }
            .padding(4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (attachment.type == AttachmentType.IMAGE) {
                AsyncImage(
                    model = attachment.uri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(4.dp)),
                    contentScale = ContentScale.Crop
                )
            } else if (attachment.type == AttachmentType.VOICE) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Voice Note",
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp)
                )
            } else {
                icon?.let {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = attachment.name,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
        IconButton(
            onClick = onRemove,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(24.dp)
                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Remove",
                modifier = Modifier.size(16.dp),
                tint = Color.White
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AttachmentPagerDialog(
    attachments: List<Attachment>,
    initialIndex: Int,
    onDismiss: () -> Unit,
    voicePlayer: VoicePlayer
) {
    val pagerState = rememberPagerState(
        initialPage = initialIndex,
        pageCount = { attachments.size }
    )
    val context = LocalContext.current

    LaunchedEffect(pagerState.currentPage) {
        voicePlayer.stop()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Black.copy(alpha = 0.95f)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    val attachment = attachments[page]
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        when (attachment.type) {
                            AttachmentType.IMAGE -> {
                                AsyncImage(
                                    model = attachment.uri,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp),
                                    contentScale = ContentScale.Fit
                                )
                            }
                            AttachmentType.TEXT -> {
                                val text = remember(attachment.uri) {
                                    readTextFromUri(context, attachment.uri.toUri())
                                }
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 24.dp, vertical = 64.dp)
                                        .verticalScroll(rememberScrollState())
                                ) {
                                    Text(
                                        text = text,
                                        color = Color.White,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                            AttachmentType.VOICE -> {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.Mic,
                                        contentDescription = null,
                                        modifier = Modifier.size(64.dp),
                                        tint = Color.White
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text("Voice Recording", color = Color.White)
                                    Button(onClick = { voicePlayer.play(attachment.uri.toUri()) }) {
                                        Text("Play Recording")
                                    }
                                }
                            }
                            else -> {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = when(attachment.type) {
                                            AttachmentType.VIDEO -> Icons.Default.Movie
                                            AttachmentType.AUDIO -> Icons.Default.MusicNote
                                            AttachmentType.PDF -> Icons.Default.Description
                                            else -> Icons.AutoMirrored.Filled.InsertDriveFile
                                        },
                                        contentDescription = null,
                                        modifier = Modifier.size(64.dp),
                                        tint = Color.White
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(attachment.name, color = Color.White)
                                    Button(onClick = { openAttachmentExternally(context, attachment) }) {
                                        Text("Open Externally")
                                    }
                                }
                            }
                        }
                    }
                }

                // Close Button
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
                
                // Page Indicator
                if (attachments.size > 1) {
                    Text(
                        text = "${pagerState.currentPage + 1} / ${attachments.size}",
                        color = Color.White,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 32.dp),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}


@Composable
fun RichTextToolbar(
    state: RichTextState,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        item {
            ControlIcon(
                imageVector = Icons.Default.FormatBold,
                isSelected = state.currentSpanStyle.fontWeight == androidx.compose.ui.text.font.FontWeight.Bold,
                onClick = { state.toggleSpanStyle(androidx.compose.ui.text.SpanStyle(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)) }
            )
        }
        item {
            ControlIcon(
                imageVector = Icons.Default.FormatItalic,
                isSelected = state.currentSpanStyle.fontStyle == androidx.compose.ui.text.font.FontStyle.Italic,
                onClick = { state.toggleSpanStyle(androidx.compose.ui.text.SpanStyle(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)) }
            )
        }
        item {
            ControlIcon(
                imageVector = Icons.Default.FormatUnderlined,
                isSelected = state.currentSpanStyle.textDecoration?.contains(androidx.compose.ui.text.style.TextDecoration.Underline) == true,
                onClick = { state.toggleSpanStyle(androidx.compose.ui.text.SpanStyle(textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline)) }
            )
        }
        item {
            ControlIcon(
                imageVector = Icons.Default.FormatStrikethrough,
                isSelected = state.currentSpanStyle.textDecoration?.contains(androidx.compose.ui.text.style.TextDecoration.LineThrough) == true,
                onClick = { state.toggleSpanStyle(androidx.compose.ui.text.SpanStyle(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough)) }
            )
        }
        item {
            ControlIcon(
                imageVector = Icons.AutoMirrored.Filled.FormatListBulleted,
                isSelected = state.isUnorderedList,
                onClick = { state.toggleUnorderedList() }
            )
        }
        item {
            ControlIcon(
                imageVector = Icons.Default.FormatListNumbered,
                isSelected = state.isOrderedList,
                onClick = { state.toggleOrderedList() }
            )
        }
    }
}

@Composable
fun ControlIcon(
    imageVector: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
        ),
        modifier = Modifier.size(36.dp)
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
    }
}

private fun openAttachmentExternally(context: Context, attachment: Attachment) {
    val uri = attachment.uri.toUri()
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, context.contentResolver.getType(uri))
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        // Handle error (e.g., no app to open this file)
    }
}

private fun readTextFromUri(context: Context, uri: Uri): String {
    return try {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            inputStream.bufferedReader().use { it.readText() }
        } ?: "Could not read file"
    } catch (e: Exception) {
        "Error: ${e.message}"
    }
}

private fun getAttachmentFromUri(context: Context, uri: Uri): Attachment? {
    val contentResolver = context.contentResolver
    
    // Take persistable permission to access the file later
    try {
        val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION
        contentResolver.takePersistableUriPermission(uri, takeFlags)
    } catch (e: Exception) {
        e.printStackTrace()
    }

    val mimeType = contentResolver.getType(uri)
    val attachmentType = when {
        mimeType?.startsWith("image/") == true -> AttachmentType.IMAGE
        mimeType?.startsWith("video/") == true -> AttachmentType.VIDEO
        mimeType?.startsWith("audio/") == true -> AttachmentType.AUDIO
        mimeType == "application/pdf" -> AttachmentType.PDF
        mimeType?.startsWith("text/") == true -> AttachmentType.TEXT
        else -> AttachmentType.OTHER
    }

    var name = "Unknown"
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (cursor.moveToFirst()) {
            name = cursor.getString(nameIndex)
        }
    }

    return Attachment(uri.toString(), attachmentType, name)
}
