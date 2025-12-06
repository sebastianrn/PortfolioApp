# Gold Portfolio 🥇

A modern Android application designed to track the value and performance of physical gold investments. Built with **Kotlin** and **Jetpack Compose**, it offers real-time price updates, interactive charts, and secure local data management.

## ✨ Key Features

* **Asset Tracking:** Manage your portfolio of Gold Coins and Bars. Track specific details like weight (g/oz), quantity, premiums, and original purchase price.
* **Live Market Data:** Fetches real-time gold spot prices via [GoldAPI](https://www.goldapi.io/).
* **Performance Charts:** Visualize your portfolio's value history and individual asset performance using interactive graphs.
* **Multi-Currency Support:** Seamlessly switch between currencies (USD, EUR, CHF, GBP, etc.). The app automatically calculates exchange rates based on the gold standard.
* **Data Security:** All data is stored locally on your device using a Room database.
* **Backup & Restore:** Export your entire portfolio history to a JSON file and restore it on any device.
* **Modern UI:** Fully responsive Material Design 3 interface with support for both **Light** and **Dark** themes.

## 🛠️ Tech Stack

* **Language:** Kotlin
* **UI:** Jetpack Compose (Material 3)
* **Architecture:** MVVM (Model-View-ViewModel)
* **Persistence:** Room Database (SQLite)
* **Networking:** Retrofit & OkHttp
* **Charting:** Vico
* **Asynchronous:** Kotlin Coroutines & Flow
* **Settings:** Jetpack DataStore

## 🚀 Getting Started

### Prerequisites
* Android Studio Ladybug or newer.
* JDK 17+.
* A free API Key from [GoldAPI.io](https://www.goldapi.io/).

### Installation

1.  **Clone the repository:**
    ```bash
    git clone [https://github.com/yourusername/GoldPortfolio.git](https://github.com/yourusername/GoldPortfolio.git)
    ```

2.  **Configure API Key:**
    For security, the API key is not stored in version control.
    * Open the `local.properties` file in the root of the project.
    * Add your key:
        ```properties
        GOLD_API_KEY=your_api_key_here
        ```

3.  **Build and Run:**
    * Open the project in Android Studio.
    * Sync Gradle files.
    * Run on an emulator or physical device (Min SDK 26).

## 📸 Screenshots

| Dashboard (Dark) | Asset Detail (Light) | Edit/Add Asset |
|:---:|:---:|:---:|
| *(Add Screenshot)* | *(Add Screenshot)* | *(Add Screenshot)* |

## 🤝 Contributing

Contributions are welcome! Please fork the repository and submit a pull request for any features, bug fixes, or enhancements.

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
