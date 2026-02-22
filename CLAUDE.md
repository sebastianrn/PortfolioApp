# CLAUDE.md - Gold Portfolio App

## Project Overview

Android app for tracking physical gold investments (coins/bars) with real-time price updates, charts, and local data management. Built with Kotlin and Jetpack Compose.

## Build & Run Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Run unit tests
./gradlew testDebugUnitTest

# Run instrumented tests (requires emulator/device)
./gradlew connectedDebugAndroidTest

# Clean build
./gradlew clean

# Lint check
./gradlew lintDebug
```

## Project Structure

```
app/src/main/java/dev/sebastianrn/portfolioapp/
├── MainActivity.kt              # Entry point with ViewModel initialization
├── PortfolioApplication.kt      # Application class with DI container
│
├── data/
│   ├── model/                   # Room entities and DTOs
│   │   ├── GoldAsset.kt         # Asset entity (COIN/BAR)
│   │   ├── PriceHistory.kt      # Price history entity
│   │   ├── AssetType.kt         # Enum: COIN, BAR
│   │   ├── PortfolioSummary.kt  # Portfolio stats DTO
│   │   ├── HistoricalStats.kt   # Historical performance stats DTO
│   │   └── BackupData.kt        # Backup serialization DTO
│   ├── local/                   # Room database layer
│   │   ├── AppDatabase.kt       # Room database singleton
│   │   ├── GoldAssetDao.kt      # DAO with Flow queries
│   │   └── Converters.kt        # Type converters
│   ├── remote/                  # Network layer
│   │   ├── GoldApi.kt           # Retrofit interface + NetworkModule
│   │   └── PhiloroScrapingService.kt  # API for Philoro prices
│   ├── repository/
│   │   └── GoldRepository.kt    # Single source of truth
│   └── UserPreferences.kt       # DataStore currency preferences
│
├── domain/
│   └── usecase/                 # Business logic (Clean Architecture)
│       ├── CalculatePortfolioStatsUseCase.kt      # Portfolio value/profit calculation
│       ├── CalculatePortfolioCurveUseCase.kt      # Portfolio curve + daily change (grouped by minute)
│       ├── CalculateHistoricalStatsUseCase.kt     # ATH/ATL, best/worst day, drawdown, return
│       └── UpdatePricesUseCase.kt                 # Price fetching from APIs
│
├── viewmodel/
│   ├── GoldViewModel.kt         # Main ViewModel (assets, prices, stats, historicalStats, lastUpdated)
│   ├── BackupViewModel.kt       # Backup/restore operations
│   ├── ThemeViewModel.kt        # Theme management
│   └── UiEvent.kt               # Sealed class for one-time UI events
│
├── di/
│   └── AppContainer.kt          # Manual service locator (no Hilt)
│
├── ui/
│   ├── screens/
│   │   ├── MainScreen.kt        # Tab-based dashboard (Portfolio + Assets tabs)
│   │   └── DetailScreen.kt      # Asset detail view
│   ├── navigation/
│   │   └── AppNavigation.kt     # NavHost + FloatingNavBar overlay (owns tab state)
│   ├── components/
│   │   ├── common/              # Reusable atomic components
│   │   │   ├── StatItem.kt      # Stat display with optional percentage
│   │   │   ├── AppTextField.kt  # Styled text input
│   │   │   └── AddAssetFab.kt   # Floating action button
│   │   ├── bottombar/           # Bottom navigation
│   │   │   ├── FloatingNavBar.kt  # Revolut-style floating pill nav bar
│   │   │   └── MainTab.kt      # Tab enum (Portfolio, Assets)
│   │   ├── cards/               # Card components
│   │   │   ├── AssetCard.kt     # Asset list item
│   │   │   ├── AssetSummaryCard.kt  # Detail screen header
│   │   │   ├── PortfolioSummaryCard.kt  # Main dashboard card (with last updated)
│   │   │   ├── HistoricalStatsCard.kt   # ATH/ATL, best/worst day, drawdown stats
│   │   │   ├── PortfolioHistoryCard.kt  # Portfolio value history entry with trend icon
│   │   │   ├── HistoryCard.kt   # Price history item
│   │   │   ├── PerformanceCard.kt   # Performance chart wrapper
│   │   │   └── ChartCard.kt     # Alternative chart wrapper
│   │   ├── sheets/              # Bottom sheets
│   │   │   ├── AssetSheet.kt    # Add/edit asset form
│   │   │   ├── EditHistorySheet.kt  # Edit price history
│   │   │   ├── BackupSettingsSheet.kt  # Backup configuration
│   │   │   └── BackupListSheet.kt  # View/manage backups
│   │   ├── dialogs/             # Alert dialogs
│   │   │   ├── RestoreConfirmDialog.kt
│   │   │   └── DeleteConfirmDialog.kt
│   │   ├── topbar/              # App bars
│   │   │   ├── MainTopBar.kt    # Main screen top bar (dynamic title per tab)
│   │   │   └── DetailTopBar.kt  # Detail screen top bar
│   │   └── chart/               # Chart components & utilities
│   │       ├── PortfolioChart.kt    # Vico line chart
│   │       ├── TimeRange.kt         # Time range enum
│   │       ├── TimeRangeSelector.kt # Time range chips
│   │       ├── ChartDataProcessor.kt  # Data filtering/calculations
│   │       └── ChartFormatters.kt   # Axis/marker formatting
│   └── theme/
│       ├── Theme.kt             # Material3 theme definition
│       ├── Color.kt             # Color palette
│       ├── Font.kt              # Google Fonts provider + FontFamily (Inter)
│       ├── Shapes.kt            # Material3 shape definitions
│       └── Typography.kt        # Typography using GoogleSansFlexFamily
│
├── backup/
│   ├── BackupManager.kt         # Local file storage management
│   ├── BackupWorker.kt          # WorkManager periodic backup
│   ├── BackupSettings.kt        # Settings data class
│   ├── BackupFile.kt            # File metadata data class
│   └── BackupFrequency.kt       # Enum: MANUAL, DAILY, WEEKLY
│
└── util/
    ├── CurrencyUtils.kt         # Double.formatCurrency() extension
    ├── DateUtils.kt             # Date formatting utilities
    └── Constants.kt             # App-wide constants (GOLD_FINENESS_24K, etc.)
```

## Architecture

### Pattern: MVVM + Repository + UseCases

```
UI (Compose) → ViewModel → UseCase → Repository → DataSource (Room/Network)
```

- **ViewModels**: Expose `StateFlow<T>` for UI state, `Channel<UiEvent>` for one-time events
- **UseCases**: Encapsulate business logic, single responsibility, `operator fun invoke()`
- **Repository**: Abstract data sources, provide single source of truth
- **DataSources**: Room DB (local), Retrofit/Scraper (remote)

### State Management

```kotlin
// ViewModel exposes immutable state
val portfolioStats: StateFlow<PortfolioSummary> = allAssets
    .map { assets -> calculateStats(assets) }
    .stateIn(viewModelScope, SharingStarted.Lazily, PortfolioSummary())

// One-time events via Channel (decoupled from Android Toast)
sealed class UiEvent {
    data class ShowToast(val message: String) : UiEvent()
    data class ShowError(val error: Throwable) : UiEvent()
}

private val _events = Channel<UiEvent>(Channel.BUFFERED)
val events = _events.receiveAsFlow()

// UI collects events
LaunchedEffect(Unit) {
    viewModel.events.collect { event ->
        when (event) {
            is UiEvent.ShowToast -> Toast.makeText(context, event.message, ...).show()
            is UiEvent.ShowError -> // handle error
        }
    }
}
```

### DI: Manual Service Locator

```kotlin
// AppContainer.kt - Lazy initialization
class AppContainer(context: Context) {
    val database by lazy { AppDatabase.getDatabase(context) }
    val repository by lazy { GoldRepository(dao, apiService, scraper) }

    // UseCases
    val calculatePortfolioStats by lazy { CalculatePortfolioStatsUseCase() }
    val calculatePortfolioCurve by lazy { CalculatePortfolioCurveUseCase() }
    val calculateHistoricalStats by lazy { CalculateHistoricalStatsUseCase() }
    val updatePrices by lazy { UpdatePricesUseCase(repository, scraper, api) }
}
```

### Navigation

- **AppNavigation** owns `selectedTab` state and renders `FloatingNavBar` as a `Box` overlay on top of `NavHost`
- **FloatingNavBar** is a Revolut-style compact pill nav bar visible on all screens (MainScreen + DetailScreen)
- **MainScreen** receives `selectedTab` and `onTabSelected` as params; uses `Crossfade` for tab switching
- **Tabs**: `MainTab.Portfolio` (summary, chart, historical stats, portfolio history) and `MainTab.Assets` (compact summary, asset list)
- Tapping a tab from DetailScreen pops back to MainScreen via `navController.popBackStack`

## Key Dependencies

| Library | Version | Purpose |
|---------|---------|---------|
| Compose BOM | 2025.12.00 | UI framework (Material 3) |
| Compose Google Fonts | (via BOM) | Downloadable fonts (Inter) |
| Room | 2.8.4 | Local database |
| Retrofit | 3.0.0 | Network requests |
| Vico | 2.3.6 | Charts |
| DataStore | 1.2.0 | User preferences |
| WorkManager | 2.11.0 | Scheduled backups |
| Gson | (via Retrofit) | JSON serialization |

## Configuration

- **API Key**: Add `GOLD_API_KEY=your_key` to `local.properties` (git-ignored)
- **Min SDK**: 26 | **Target SDK**: 36
- **Kotlin**: 2.2.21 | **JVM Target**: 1.8
- **Java**: Requires Java 17 for Gradle builds

## Versioning

Version format: `x.y` (e.g., `0.5`, `0.6`, ... `0.9`, `1.0`)

- **Auto-increment**: Version increments automatically on release builds
- **Minor (y)**: Goes 0→9, then wraps to 0 and increments major (x)
- **VERSION_CODE**: Always increments by 1 with each release
- **Storage**: `app/version.properties`

Works with:
- `./gradlew assembleRelease`
- `./gradlew bundleRelease`
- Android Studio → Build → Generate Signed Bundle / APK

```bash
# Check current version
./gradlew printVersion
```

## Coding Conventions

### Naming

| Type | Convention | Example |
|------|------------|---------|
| Components | PascalCase, descriptive | `AssetCard`, `ChartCard` |
| Sheets | `*Sheet.kt` | `AssetSheet`, `EditHistorySheet` |
| Dialogs | `*Dialog.kt` | `RestoreConfirmDialog` |
| UseCases | `*UseCase.kt` | `CalculatePortfolioCurveUseCase` |
| ViewModels | `*ViewModel.kt` | `GoldViewModel` |
| TopBars | `*TopBar.kt` | `MainTopBar`, `DetailTopBar` |
| Tabs | `*Tab.kt` | `MainTab` |

### File Organization

- **One component per file** (file name matches primary composable)
- **Group by feature type**: `cards/`, `sheets/`, `dialogs/`, `topbar/`, `bottombar/`, `chart/`
- **Common components** in `common/` subdirectory
- **Business logic** in `domain/usecase/`

### Kotlin Style

```kotlin
// StateFlow for UI state
val uiState: StateFlow<T> = _uiState.asStateFlow()

// viewModelScope for coroutines
viewModelScope.launch(Dispatchers.IO) { ... }

// Extension functions for formatting
fun Double.formatCurrency(short: Boolean = false): String

// Data classes with copy() for immutable updates
data class PortfolioSummary(val totalValue: Double = 0.0, ...)

// UseCases with operator invoke
class CalculatePortfolioStatsUseCase {
    operator fun invoke(assets: List<GoldAsset>): PortfolioSummary { ... }
}

// Constants in dedicated file
object Constants {
    const val GOLD_FINENESS_24K = 0.9999
    const val MAX_CHART_POINTS = 100
}
```

### Threading

- **Main**: UI updates, state emission
- **IO**: Database, network, file operations
- **Default**: CPU-intensive calculations (chart data processing)

## Database Schema

```sql
-- GoldAsset
CREATE TABLE gold_assets (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    type TEXT NOT NULL,           -- 'COIN' or 'BAR'
    purchasePrice REAL NOT NULL,
    currentSellPrice REAL NOT NULL,
    currentBuyPrice REAL NOT NULL,
    quantity INTEGER NOT NULL,
    weightInGrams REAL NOT NULL,
    philoroId INTEGER NOT NULL DEFAULT 0
);

-- PriceHistory (FK with CASCADE DELETE)
CREATE TABLE price_history (
    historyId INTEGER PRIMARY KEY AUTOINCREMENT,
    assetId INTEGER NOT NULL,
    dateTimestamp INTEGER NOT NULL,
    sellPrice REAL NOT NULL,
    buyPrice REAL NOT NULL,
    isManual INTEGER NOT NULL,    -- 0=API, 1=Manual
    FOREIGN KEY (assetId) REFERENCES gold_assets(id) ON DELETE CASCADE
);
```

## Testing

### Test Structure

```
app/src/test/kotlin/dev/sebastianrn/portfolioapp/
├── domain/usecase/
│   ├── CalculatePortfolioStatsUseCaseTest.kt
│   ├── CalculatePortfolioCurveUseCaseTest.kt
│   └── UpdatePricesUseCaseTest.kt
├── viewmodel/
│   ├── GoldViewModelTest.kt
│   ├── BackupViewModelTest.kt
│   └── ThemeViewModelTest.kt
├── data/
│   ├── model/
│   │   ├── GoldAssetTest.kt
│   │   └── PortfolioSummaryTest.kt
│   └── repository/
│       └── GoldRepositoryTest.kt
├── util/
│   ├── CurrencyUtilsTest.kt
│   ├── DateUtilsTest.kt
│   └── ConstantsTest.kt
└── ui/components/chart/
    └── ChartDataProcessorTest.kt

app/src/androidTest/kotlin/dev/sebastianrn/portfolioapp/
├── data/
│   ├── GoldAssetDaoTest.kt
│   └── AppDatabaseMigrationTest.kt
└── backup/
    └── BackupManagerTest.kt
```

### Testing Libraries

| Library | Purpose |
|---------|---------|
| JUnit 4 | Test framework |
| MockK | Mocking Kotlin classes |
| Turbine | Testing Kotlin Flows |
| kotlinx-coroutines-test | Coroutine testing with `runTest` |
| Room Testing | In-memory database for DAO tests |
| AndroidX Test | Instrumented test utilities |

### Testing Patterns

#### Unit Tests (JVM)

```kotlin
// UseCase testing - no mocks needed for pure logic
class CalculatePortfolioStatsUseCaseTest {
    private val useCase = CalculatePortfolioStatsUseCase()

    @Test
    fun `returns empty summary for empty list`() {
        val result = useCase(emptyList())
        assertEquals(0.0, result.totalValue, 0.001)
    }
}

// ViewModel testing with MockK and Turbine
class GoldViewModelTest {
    @MockK private lateinit var repository: GoldRepository
    private lateinit var viewModel: GoldViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @Test
    fun `portfolioStats updates when assets change`() = runTest {
        every { repository.allAssets } returns flowOf(testAssets)
        viewModel.portfolioStats.test {
            assertEquals(expectedStats, awaitItem())
        }
    }
}
```

#### Instrumented Tests (Android)

```kotlin
// DAO testing with in-memory Room database
@RunWith(AndroidJUnit4::class)
class GoldAssetDaoTest {
    private lateinit var database: AppDatabase
    private lateinit var dao: GoldAssetDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = database.goldAssetDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertAndRetrieveAsset() = runTest {
        dao.insert(testAsset)
        dao.getAllAssets().test {
            assertEquals(listOf(testAsset), awaitItem())
        }
    }
}
```

### Test Commands

```bash
# Run all unit tests
./gradlew testDebugUnitTest

# Run specific test class
./gradlew testDebugUnitTest --tests "*.GoldViewModelTest"

# Run specific test method
./gradlew testDebugUnitTest --tests "*.GoldViewModelTest.portfolioStats updates when assets change"

# Run instrumented tests (requires emulator/device)
./gradlew connectedDebugAndroidTest

# Run specific instrumented test
./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=dev.sebastianrn.portfolioapp.data.GoldAssetDaoTest

# Generate test coverage report
./gradlew testDebugUnitTestCoverage
./gradlew createDebugCoverageReport  # For instrumented tests

# Run all tests
./gradlew test connectedAndroidTest
```

### Test Coverage Guidelines

| Layer | Coverage Target | Key Test Cases |
|-------|----------------|----------------|
| UseCases | 100% | Empty inputs, edge cases, calculations |
| ViewModels | 90%+ | State flows, events, error handling |
| Repository | 85%+ | CRUD operations, flow emissions |
| DAO | 100% | All queries, cascade deletes, transactions |
| Utilities | 100% | Formatting, edge cases, locales |

### Test Data Factories

Use consistent test data across tests:

```kotlin
// TestDataFactory.kt (in test sources)
object TestDataFactory {
    fun createGoldAsset(
        id: Int = 1,
        name: String = "Test Gold Bar",
        type: AssetType = AssetType.BAR,
        purchasePrice: Double = 1000.0,
        currentSellPrice: Double = 1100.0,
        currentBuyPrice: Double = 1150.0,
        quantity: Int = 1,
        weightInGrams: Double = 31.1,
        philoroId: Int = 0
    ) = GoldAsset(id, name, type, purchasePrice, currentSellPrice, currentBuyPrice, quantity, weightInGrams, philoroId)

    fun createPriceHistory(
        historyId: Int = 1,
        assetId: Int = 1,
        dateTimestamp: Long = System.currentTimeMillis(),
        sellPrice: Double = 1100.0,
        buyPrice: Double = 1150.0,
        isManual: Boolean = false
    ) = PriceHistory(historyId, assetId, dateTimestamp, sellPrice, buyPrice, isManual)
}
```

### Naming Conventions

- Test class: `{ClassUnderTest}Test.kt`
- Test method: Use backticks with descriptive names
  - `fun \`returns empty summary for empty list\`()`
  - `fun \`throws exception when API key is empty\`()`
- Arrange-Act-Assert pattern in test body

## Backup System

- **Storage**: Local app files (`/files/backups/`)
- **Format**: JSON (Gson serialization)
- **Filename**: `portfolio_backup_YYYY-MM-DD_HH:mm:ss.json`
- **Scheduling**: WorkManager (Daily/Weekly/Manual)
- **Retention**: Keeps last 10 backups, auto-deletes older ones
- **Sharing**: FileProvider for secure file sharing

## Typography / Font

- **Font**: Inter (via Google Downloadable Fonts), used as `GoogleSansFlexFamily`
- **Provider**: `Font.kt` defines the `GoogleFont.Provider` with GMS font certs
- **Certificates**: `res/values/font_certs.xml` (required for Google Fonts provider)
- **Usage**: All `Typography` text styles in `Typography.kt` set `fontFamily = GoogleSansFlexFamily`
- **Dependency**: `androidx.compose.ui:ui-text-google-fonts` (version managed by Compose BOM)

## Common Tasks

### Add a new component

1. Create file in appropriate subdirectory (`cards/`, `sheets/`, etc.)
2. Use package matching directory: `ui.components.cards`
3. Follow naming convention: `FeatureCard.kt` → `fun FeatureCard()`

### Add a new UseCase

1. Create in `domain/usecase/`
2. Single `operator fun invoke()` method
3. Register in `AppContainer.kt`
4. Inject via ViewModel constructor

### Add a new screen

1. Create in `ui/screens/`
2. Add route to `navigation/AppNavigation.kt`
3. Create corresponding ViewModel if needed
4. Handle UiEvents with `LaunchedEffect`

### Handle one-time UI events

```kotlin
// In ViewModel
private fun sendEvent(event: UiEvent) {
    viewModelScope.launch { _events.send(event) }
}

// In Composable
LaunchedEffect(Unit) {
    viewModel.events.collect { event ->
        when (event) {
            is UiEvent.ShowToast -> Toast.makeText(context, event.message, LENGTH_SHORT).show()
            is UiEvent.ShowError -> Toast.makeText(context, "Error: ${event.error.message}", LENGTH_LONG).show()
        }
    }
}
```
