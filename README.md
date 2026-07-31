# Bedrock 📓

**Bedrock** is a high-contrast, minimalist monochrome notes editor for Android. Built with a focus on simplicity and distraction-free writing, it combines the lightweight feel of Google Keep with powerful Markdown support, structured scheduling, and local-first security.

![Bedrock Logo](app/src/main/res/drawable/ic_launcher_foreground.xml) 

## Features

- **Minimalist Design**: A pure monochrome aesthetic using Material 3, inspired by modern "Keep" style layouts with premium Pixel Launcher-style expansion animations.
- **Adaptive Workspace**: Intelligent layout that automatically scales from a single-pane mobile view to a **Two-pane Desktop/Tablet view**. Enjoy a persistent sidebar for your notes and filters on larger screens.
- **Multi-Note Tabs**: Open multiple notes simultaneously! Manage them via a modern horizontal tab bar on desktops or a quick-access "Tabs" switcher on mobile.
- **Floating Profile Hub**: A central "Command Center" for your identity. Set your name, choose a permanent profile picture, and access app-wide utilities from a modern modal bottom sheet.
- **In-Note Search**: Find specific words or phrases instantly with live visual highlighting across all editor and viewer modes.
- **Versatile Editing Modes**:
    - **Plain Notes**: A lightweight, borderless text editing experience.
    - **Markdown Editor**: Dedicated environment with live preview, split-screen mode, and a swipeable formatting toolbar (H1, H2, Bold, Lists, etc.).
    - **Structured To-do Lists**: Dedicated checklist mode with auto-creation of new items via the keyboard and native task management.
- **Rich Image Support**: Insert images from your gallery and adjust them directly in the text with resizable handles and reorderable move controls.
- **Universal Scheduler**: Set precise Date & Time reminders for any note type. Includes system notifications that deep-link directly back to your note.
- **Safe Trash Bin**: Notes moved to trash are kept for **30 days** before being automatically deleted, providing a safe grace period for your data.
- **Reminders & Reliability Center**: A dedicated utility section in settings to manage notification importance, bypass battery restrictions for reliable delivery, and handle exact alarm permissions.
- **Material Expressive Settings**: A modern, card-based grouped settings structure with status summaries and bold headers.
- **Master Password Security**: Secure sensitive notes and backups with a full alphanumeric Master Password and local SHA-256 encryption.
- **Encrypted Backup Hub**: Create secure local backups of your entire note library, encrypted with your Master Password and saved to your public `Download/Bedrock` folder.
- **Developer Mode**: Hidden in-app debugging tools activated via a secret 7-tap sequence, featuring automated log generation and crash recovery.
- **Organization**: Powerful tag-based categorization with a searchable management dialog and instant text filtering.
- **Backup & Restore**: Export and import your notes as JSON for cross-device manual synchronization. Remote Cloud Sync is currently a work-in-progress (WIP).

## Tech Stack

- **UI**: Jetpack Compose (Material 3 Expressive)
- **Architecture**: MVVM + Repository Pattern
- **Persistence**: Room Database (SQLite v5) + DataStore (Preferences)
- **Adaptive Layout**: Material 3 Adaptive Layouts + WindowSizeClass
- **Image Loading**: Coil
- **Scheduling**: AlarmManager + BroadcastReceivers
- **Storage**: MediaStore API (Scoped Storage) for privacy-compliant public folder access
- **Security**: SHA-256 Hashing + XOR Content Obfuscation
- **Navigation**: Jetpack Navigation Compose with Shared Transitions
- **Asynchrony**: Kotlin Coroutines & Flow
- **Error Handling**: Custom Crash Catcher with persistent logging
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

## Reliability Tips
To ensure scheduled reminders are delivered precisely even when your device is in Doze mode, go to **Profile Hub > App Settings > Reminders & Reliability** and enable the **Battery Restriction Bypass**.

## Application ID
The project is identified by: `com.bedrock.notes`

## License

Copyright 2026 @isaiahscape

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
