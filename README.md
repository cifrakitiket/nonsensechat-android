# NonsenseChat Android

![Platform](https://img.shields.io/badge/platform-Android-green)
![Kotlin](https://img.shields.io/badge/Kotlin-1.9-blue)
![Min SDK](https://img.shields.io/badge/minSdk-24-orange)
![Architecture](https://img.shields.io/badge/architecture-MVVM-blueviolet)
![License](https://img.shields.io/badge/license-MIT-lightgrey)

NonsenseChat — современное Android-приложение для обмена сообщениями с элементами генерации абсурдного (nonsense) контента.  
Проект демонстрирует использование современной Android-архитектуры, best practices и чистого кода.

---

## ✨ Основные возможности

- 💬 Обмен сообщениями
- 🤖 Генерация nonsense-сообщений
- 🧠 Асинхронная обработка данных
- 📜 История диалогов
- 🎨 UI на базе Material Design 3
- ⚡ Реактивная архитектура на основе Flow

---

## 🏗 Архитектура

Приложение построено по принципам:

- **MVVM**
- **Clean Architecture**
- **Repository pattern**
- **Single Source of Truth**
- **Unidirectional Data Flow**

### Структура проекта
presentation/ → UI, ViewModels
domain/ → UseCases, бизнес-логика
data/ → Repository, API, источники данных
di/ → Dependency Injection

text


### Принципы

- Разделение ответственности
- Высокая тестируемость
- Минимальная связанность модулей
- Масштабируемость

---

## 🛠 Технологический стек

- **Kotlin**
- **Coroutines + Flow**
- **Jetpack ViewModel**
- **Navigation Component**
- **Material Design 3**
- **Hilt / Dagger** (если используется DI)
- **Retrofit / Ktor** (если используется API)
- **Room** (если используется локальное хранилище)
- **Gradle**

---

## 📦 Сборка и запуск

### Требования

- Android Studio (последняя стабильная версия)
- JDK 17+
- Android SDK 24+

### Установка

```bash
git clone https://github.com/cifrakitiket/nonsensechat-android.git
cd nonsensechat-android
Откройте проект в Android Studio и запустите на устройстве или эмуляторе.

⚙️ Конфигурация
Если требуется API-ключ:

Создайте local.properties:

properties

API_KEY=your_api_key_here
BASE_URL=https://your.api.url/
🧪 Тестирование
Unit-тесты:

Bash

./gradlew test
Инструментальные тесты:

Bash

./gradlew connectedAndroidTest
🚀 Roadmap
 Поддержка нескольких чатов
 Offline режим
 Темная тема
 Push-уведомления
 Расширенная генерация сообщений
🤝 Contributing
Fork репозиторий
Создайте feature-ветку
Сделайте commit изменений
Откройте Pull Request
📄 Лицензия
Distributed under the MIT License. See LICENSE for details.

👤 Автор
cifrakitiket
GitHub: https://github.com/cifrakitiket
