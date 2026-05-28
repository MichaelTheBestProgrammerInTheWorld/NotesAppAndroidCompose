# NotesApp Project Plan

## Overview
A modern Android notes application built with Jetpack Compose, Material 3, and Clean Architecture principles. The app allows users to create, organize, and manage notes with rich text support and various sorting/filtering capabilities.

## Architecture
The project follows **Clean Architecture** with a layered structure:
- **Presentation Layer**: Jetpack Compose UI, ViewModels, UI State, and Events.
- **Domain Layer**: Business logic, Use Cases, Models, and Repository interfaces.
- **Data Layer**: Room Database, Repository implementations, Entities, and Mappers.

## Tech Stack
- **UI**: Jetpack Compose, Material 3
- **Navigation**: Compose Navigation
- **Asynchronous Work**: Kotlin Coroutines & Flow
- **Local Storage**: Room Database
- **Image Loading**: Coil
- **Rich Text**: RichEditor Compose
- **Dependency Injection**: Manual/Factory-based (as seen in ViewModels)

## Features

### Completed Features
- [x] **Note Management**: Create, Edit, and Save notes.
- [x] **Rich Text Support**: Support for formatted content using `RichEditor`.
- [x] **Soft Delete**: Move notes to "Trash" before permanent deletion.
- [x] **Archiving**: Hide active notes by moving them to "Archived".
- [x] **Pinning**: Pin up to 5 important notes to the top of the list.
- [x] **Searching**: Filter notes by title, content, or attachments.
- [x] **Selection Mode**: Bulk actions (delete, archive, select all).
- [x] **View Toggling**: Switch between List and Grid layouts.
- [x] **Sorting**:
    - Manual (supports drag-and-drop)
    - Newest first
    - Oldest first
    - Title Ascending (A-Z)
    - Title Descending (Z-A)
- [x] **Attachments**: Support for voice recordings and other media.

### Upcoming / Planned Features
- [ ] **Reminders**: Set time-based notifications for notes.
- [ ] **Labels/Tags**: Categorize notes beyond folders.
- [ ] **Cloud Sync**: Optional Firebase/Drive backup.
- [ ] **Themes**: Dynamic color support and custom themes.
- [ ] **Widget**: Quick access to notes from the home screen.

## Project Structure
- `app/src/main/java/com/example/notesappandroidcompose/`
    - `data/`: Local DB configuration and repository implementations.
    - `domain/`: Pure business logic (Models, Use Cases).
    - `presentation/`: UI components, Screens, and ViewModels.
        - `notes_list/`: Main screen with filtering and sorting.
        - `note_detail/`: Editing screen with rich text and attachments.
        - `navigation/`: Navigation host and screen definitions.
