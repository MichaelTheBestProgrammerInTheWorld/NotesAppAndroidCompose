# My Notes - Android Compose App

A modern, feature-rich notes application built with **Jetpack Compose** and **Clean Architecture**.

## 📱 Screenshots

<p align="center">
  <img src="https://raw.githubusercontent.com/placeholder-path/main_screen.png" width="300" alt="Main Screen">
</p>

> *Note: Replace the placeholder above with actual screenshot paths from your repository.*

## ✨ Features

- **Rich Text Editing**: Format your notes with bold, italic, underline, and more using `RichEditor`.
- **Note Organization**:
    - **Pinning**: Keep up to 5 important notes at the top.
    - **Archiving**: Keep your main list clean by archiving finished tasks.
    - **Trash (Soft Delete)**: Safety first—deleted notes go to the trash before being permanently removed.
- **Advanced Sorting**: 
    - **Manual**: Drag and drop notes to your preferred order.
    - **Chronological**: Sort by newest or oldest created.
    - **Alphabetical**: Sort by title (A-Z or Z-A).
- **Flexible Views**: Switch between a clean **List view** and a modern **Staggered Grid view**.
- **Smart Search**: Quickly find notes by title, content, or even attachment names.
- **Multimedia Support**: Attach voice recordings and images to your notes.
- **Selection Mode**: Perform bulk actions like archiving or deleting multiple notes at once.

## 🏗️ Architecture

The app follows **Clean Architecture** principles to ensure scalability and testability:
- **Presentation**: Jetpack Compose UI with MVVM pattern.
- **Domain**: Pure Kotlin business logic (Use Cases & Models).
- **Data**: Room Database implementation and Repository patterns.

## 🛠️ Tech Stack

- **UI**: Jetpack Compose & Material 3
- **Local DB**: Room
- **Image Loading**: Coil
- **Editor**: RichEditor Compose
- **Concurrency**: Kotlin Coroutines & Flow
- **Navigation**: Compose Navigation

## 🚀 Getting Started

1. Clone this repository.
2. Open the project in **Android Studio (Ladybug or newer)**.
3. Sync Gradle and run the `:app` module on an emulator or physical device.

---

*Developed with ❤️ using Jetpack Compose.*
