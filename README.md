# Bedrock 📓

**Bedrock** is a high-contrast, minimalist monochrome notes editor for Android. Built with a focus on simplicity and distraction-free writing, it combines the lightweight feel of Google Keep with powerful Markdown support and local-first security.

![Bedrock Logo](app/src/main/res/drawable/ic_launcher_foreground.xml) 

## Features

- **Minimalist Design**: A pure monochrome aesthetic using Material 3, inspired by modern "Keep" style layouts.
- **Dual Editing Modes**:
    - **Plain Notes**: A lightweight, borderless text editing experience.
    - **Markdown Editor**: Dedicated environment with live preview, split-screen mode, and a quick formatting toolbar (H1, H2, Bold, Lists, etc.).
- **Interactive Checklists**: Create and manage to-do lists directly within your notes.
- **Local-First Security**: Secure sensitive notes with local SHA-256 encryption and PIN/passcode protection.
- **Offline Sync Engine**: A simulated sync engine that manages local caching and tracks synchronization status with a "cloud" backend.
- **Organization**: Powerful tag-based categorization and instant search.
- **Backup & Restore**: Export and import your notes as JSON for cross-device manual synchronization.

## Tech Stack

- **UI**: Jetpack Compose (Material 3)
- **Architecture**: MVVM + Repository Pattern
- **Persistence**: Room Database (SQLite)
- **Navigation**: Jetpack Navigation Compose
- **Asynchrony**: Kotlin Coroutines & Flow
- **Testing**: Robolectric & Roborazzi (Screenshot testing)

## Getting Started

### Prerequisites
- Android Studio Ladybug (or newer)
- Android SDK 35+
- Java 17+

### Installation
1. Clone the repository.
2. Open the project in Android Studio.
3. Sync Gradle and run the `:app` module on an emulator or physical device.

## Application ID
The project is identified by: `com.bedrock.notes`

## License
This project is for demonstration and educational purposes.
