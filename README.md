# Bedrock 📓

**Bedrock** is a high-contrast, minimalist monochrome notes editor for Android. Built with a focus on simplicity and distraction-free writing, it combines the lightweight feel of Google Keep with powerful Markdown support, structured scheduling, and local-first security.

![Bedrock Logo](app/src/main/res/drawable/ic_launcher_foreground.xml) 

## Features

- **Minimalist Design**: A pure monochrome aesthetic using Material 3, inspired by modern "Keep" style layouts with premium Pixel Launcher-style expansion animations.
- **Floating Profile Hub**: A central "Command Center" for your identity. Set your name, choose a profile picture from your gallery, and access app-wide utilities from a modern modal bottom sheet.
- **Versatile Editing Modes**:
    - **Plain Notes**: A lightweight, borderless text editing experience.
    - **Markdown Editor**: Dedicated environment with live preview, split-screen mode, and a quick formatting toolbar (H1, H2, Bold, Lists, etc.).
    - **Structured To-do Lists**: Dedicated checklist mode with auto-creation of new items via the keyboard and native task management.
- **Universal Scheduler**: Set precise Date & Time reminders for any note type. Includes system notifications that deep-link directly back to your note.
- **Reminders & Reliability Center**: A dedicated utility section in settings to manage notification importance, bypass battery restrictions for reliable delivery, and handle exact alarm permissions.
- **Material Expressive Settings**: A modern, hierarchical settings structure with status summaries and bold headers.
- **Local-First Security**: Secure sensitive notes with local SHA-256 encryption and PIN/passcode protection.
- **Organization**: Powerful tag-based categorization with a searchable management dialog and instant text filtering.
- **Backup & Restore**: Export and import your notes as JSON for cross-device manual synchronization.

## Tech Stack

- **UI**: Jetpack Compose (Material 3 Expressive)
- **Architecture**: MVVM + Repository Pattern
- **Persistence**: Room Database (SQLite v3) + DataStore (Preferences)
- **Image Loading**: Coil
- **Scheduling**: AlarmManager + BroadcastReceivers
- **Navigation**: Jetpack Navigation Compose with Shared Transitions
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

## Reliability Tips
To ensure scheduled reminders are delivered precisely even when your device is in Doze mode, go to **Profile > App Settings > Reminders & Reliability** and enable the **Battery Restriction Bypass**.

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
