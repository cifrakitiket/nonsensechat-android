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
