🚀 NonsenseChat Android
Platform
Kotlin
Min SDK
Architecture
CI
License

NonsenseChat is a modern Android chat application demonstrating Clean Architecture, reactive state management, and scalable project structure.

This project was built as a showcase of Android development best practices and production-level code organization.

📱 About The Project
NonsenseChat is a messaging application with experimental “nonsense” message generation.

The main goal of the project is to demonstrate:

Scalable architecture
Clean separation of concerns
Reactive UI
Maintainable and testable codebase
Modern Android stack
✨ Features
💬 Real-time message interaction
🤖 Generated "nonsense" responses
🧠 Reactive state handling with Flow
🎨 Material Design 3 UI
🏗 Modular architecture
✅ Unit-testable business logic
🔄 Asynchronous processing via Coroutines
🏗 Architecture
The project follows:

Clean Architecture
MVVM
Repository Pattern
Single Source of Truth
Unidirectional Data Flow
Layered Structure
text

presentation/   → UI, ViewModels, State
domain/         → UseCases, business models
data/           → Repository implementations, API, local storage
di/             → Dependency Injection
Architecture Principles
Clear dependency direction (UI → Domain → Data)
Domain layer independent from Android framework
Highly testable business logic
Separation between state and UI
🛠 Tech Stack
Kotlin
Coroutines + Flow
Jetpack ViewModel
Navigation Component
Material Design 3
Hilt (Dependency Injection)
Retrofit / Ktor (API layer)
Room (Local persistence)
Gradle (KTS if used)
🧪 Testing
The project supports:

✅ Unit tests (domain & business logic)
✅ Coroutine testing
✅ ViewModel testing
Run tests:

Bash

./gradlew test
Instrumentation tests:

Bash

./gradlew connectedAndroidTest
🚀 Getting Started
Requirements
Android Studio (latest stable)
JDK 17+
Android SDK 24+
Installation
Bash

git clone https://github.com/cifrakitiket/nonsensechat-android.git
cd nonsensechat-android
Open the project in Android Studio and run on an emulator or device.

⚙️ Configuration
If API keys are required:

Create local.properties:

properties

API_KEY=your_api_key_here
BASE_URL=https://your.api.url/
🔁 Continuous Integration
CI is configured using GitHub Actions.

Workflow includes:

Project build
Unit tests execution
Gradle cache optimization
Example workflow file:

.github/workflows/android.yml

YAML

name: Android CI

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
      - uses: actions/checkout@v3

      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          distribution: temurin
          java-version: 17

      - name: Grant permission for gradlew
        run: chmod +x gradlew

      - name: Build project
        run: ./gradlew build

      - name: Run unit tests
        run: ./gradlew test
🎯 Why This Project
This project demonstrates:

Ability to design scalable Android architecture
Strong understanding of MVVM and Clean Architecture
Knowledge of asynchronous programming (Coroutines)
Proper state management
Writing maintainable and readable code
CI integration
Production-ready structure
Suitable for showcasing Android Middle+ level skills.

📌 Roadmap
 Multi-chat support
 Offline mode
 Dark theme
 Push notifications
 Modularization into separate Gradle modules
 Compose migration (optional future improvement)
🤝 Contributing
Contributions are welcome.

Fork the repository
Create feature branch
Commit changes
Open Pull Request
📄 License
Distributed under the MIT License.
See LICENSE for more information.

👤 Author
cifrakitiket
GitHub: https://github.com/cifrakitiket

