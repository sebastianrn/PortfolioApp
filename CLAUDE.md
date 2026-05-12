# Gold Portfolio 🥇

A modern Android application designed to track the value and performance of physical gold investments. Built with **Kotlin** and **Jetpack Compose**, it offers real-time price updates, interactive charts, and secure local data management.

## ✨ Key Features

* **Asset Tracking:** Manage your portfolio of Gold Coins and Bars. Track specific details like weight (g/oz), quantity, premiums, and original purchase price.
* **Live Market Data:** Fetches real-time gold spot prices via [GoldAPI](https://www.goldapi.io/).
* **Performance Charts:** Visualize your portfolio's value history and individual asset performance using interactive graphs (Vico).
* **Data Security:** All data is stored locally on your device using a Room database.
* **Backup & Restore:** Export your entire portfolio history to a JSON file and restore it on any device.
* **Modern UI:** Fully responsive Material Design 3 interface with support for both **Light** and **Dark** themes.

## Architecture (Updated May 2026)

- **Clean Architecture + MVVM** with Use Cases
- Repository as single source of truth
- Jetpack Compose + StateFlow + `collectAsStateWithLifecycle()`
- Manual DI via `AppContainer` (Hilt migration planned)

## Recent Improvements (PR #11)

- Fixed worst-day calculation bug in `CalculateHistoricalStatsUseCase`
- Adopted `collectAsStateWithLifecycle()` in UI screens for better lifecycle awareness
- Added `safeCall<T>` wrapper in `GoldRepository` for consistent error handling
- Improved ViewModel safety and documentation

## Tech Stack

* **Language:** Kotlin
* **UI:** Jetpack Compose (Material 3)
* **Architecture:** MVVM (Model-View-ViewModel) + Clean Architecture
* **Persistence:** Room Database (SQLite)
* **Networking:** Retrofit & OkHttp
* **Charting:** Vico
* **Asynchronous:** Kotlin Coroutines & Flow
* **Settings:** Jetpack DataStore

## Getting Started

### Prerequisites

* Android Studio Ladybug or newer.
* JDK 17+.
* A free API Key from [GoldAPI.io](https://www.goldapi.io/).

### Installation

1. Clone the repository
2. Add your `GOLD_API_KEY` to `local.properties`
3. Build and run (Min SDK 26)

See full README.md for details.