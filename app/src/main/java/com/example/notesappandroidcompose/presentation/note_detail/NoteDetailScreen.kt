package com.example.notesappandroidcompose.presentation.note_detail

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import com.example.notesappandroidcompose.domain.model.Attachment
import com.example.notesappandroidcompose.domain.model.AttachmentType
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(
    state: NoteDetailState,
    eventFlow: kotlinx.coroutines.flow.SharedFlow<NoteDetailViewModel.UiEvent>,
    onEvent: (NoteDetailEvent) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var selectedAttachmentForPreview by remember { mutableStateOf<Attachment?>(null) }
    
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            uri?.let {
                val attachment = getAttachmentFromUri(context, it)
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
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.id == null) "Add Note" else "Edit Note") },
                actions = {
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
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onEvent(NoteDetailEvent.SaveNote) },
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
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {
            OutlinedTextField(
                value = state.title,
                onValueChange = { onEvent(NoteDetailEvent.EnteredTitle(it)) },
                placeholder = { Text("Title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
            
            if (state.attachments.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Attachments", style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.attachments) { attachment ->
                        AttachmentItem(
                            attachment = attachment,
                            onRemove = { onEvent(NoteDetailEvent.RemoveAttachment(attachment)) },
                            onClick = {
                                when (attachment.type) {
                                    AttachmentType.IMAGE, AttachmentType.TEXT -> {
                                        selectedAttachmentForPreview = attachment
                                    }
                                    else -> {
                                        openAttachmentExternally(context, attachment)
                                    }
                                }
                            }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Content Preview", style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(8.dp))
                state.attachments.forEach { attachment ->
                    AttachmentContentView(attachment = attachment)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = state.content,
                onValueChange = { onEvent(NoteDetailEvent.EnteredContent(it)) },
                placeholder = { Text("Content") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 200.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
        }
    }

    selectedAttachmentForPreview?.let { attachment ->
        AttachmentPreviewDialog(
            attachment = attachment,
            onDismiss = { selectedAttachmentForPreview = null }
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
        AttachmentType.PDF -> Icons.Default.Description
        AttachmentType.TEXT -> Icons.Default.Description
        AttachmentType.OTHER -> Icons.Default.InsertDriveFile
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

@Composable
fun AttachmentContentView(attachment: Attachment) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                text = attachment.name,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            when (attachment.type) {
                AttachmentType.IMAGE -> {
                    AsyncImage(
                        model = attachment.uri,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        contentScale = ContentScale.Fit
                    )
                }
                AttachmentType.TEXT -> {
                    val text = remember(attachment.uri) {
                        readTextFromUri(context, Uri.parse(attachment.uri))
                    }
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 10,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                else -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = when(attachment.type) {
                                AttachmentType.VIDEO -> Icons.Default.Movie
                                AttachmentType.AUDIO -> Icons.Default.MusicNote
                                AttachmentType.PDF -> Icons.Default.Description
                                else -> Icons.Default.InsertDriveFile
                            },
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Tap to open ${attachment.type.name.lowercase()}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AttachmentPreviewDialog(
    attachment: Attachment,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Black.copy(alpha = 0.9f)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }

                when (attachment.type) {
                    AttachmentType.IMAGE -> {
                        AsyncImage(
                            model = attachment.uri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                    AttachmentType.TEXT -> {
                        val text = remember(attachment.uri) {
                            readTextFromUri(context, Uri.parse(attachment.uri))
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
                    else -> {
                        // This shouldn't be reached if we handle it in onClick, but just in case
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No internal preview available", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

private fun openAttachmentExternally(context: Context, attachment: Attachment) {
    val uri = Uri.parse(attachment.uri)
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
