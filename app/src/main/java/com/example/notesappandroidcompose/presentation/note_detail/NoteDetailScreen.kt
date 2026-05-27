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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
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
                            onRemove = { onEvent(NoteDetailEvent.RemoveAttachment(attachment)) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = state.content,
                onValueChange = { onEvent(NoteDetailEvent.EnteredContent(it)) },
                placeholder = { Text("Content") },
                modifier = Modifier.fillMaxSize(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
        }
    }
}

@Composable
fun AttachmentItem(
    attachment: Attachment,
    onRemove: () -> Unit
) {
    val icon = when (attachment.type) {
        AttachmentType.IMAGE -> Icons.Default.Image
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
            .padding(4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = attachment.name,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        IconButton(
            onClick = onRemove,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(24.dp)
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Remove",
                modifier = Modifier.size(16.dp),
                tint = Color.Red
            )
        }
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
