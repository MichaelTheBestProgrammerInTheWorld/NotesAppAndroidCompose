package com.example.notesappandroidcompose.domain.model

data class Attachment(
    val uri: String,
    val type: AttachmentType,
    val name: String
)

enum class AttachmentType {
    IMAGE, VIDEO, AUDIO, PDF, TEXT, VOICE, OTHER
}
