package dev.sebastianrn.portfolioapp.viewmodel

import app.cash.turbine.test
import dev.sebastianrn.portfolioapp.TestDataFactory
import dev.sebastianrn.portfolioapp.data.UserPreferences
import dev.sebastianrn.portfolioapp.data.model.AssetType
import dev.sebastianrn.portfolioapp.data.model.GoldAsset
import dev.sebastianrn.portfolioapp.data.model.PriceHistory
import dev.sebastianrn.portfolioapp.data.repository.GoldRepository
import dev.sebastianrn.portfolioapp.domain.usecase.CalculateHistoricalStatsUseCase
import dev.sebastianrn.portfolioapp.domain.usecase.CalculatePortfolioCurveUseCase
import dev.sebastianrn.portfolioapp.domain.usecase.CalculatePortfolioStatsUseCase
import dev.sebastianrn.portfolioapp.domain.usecase.UpdatePricesUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for GoldViewModel.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class GoldViewModelTest {

    private lateinit var viewModel: GoldViewModel
    private lateinit var repository: GoldRepository
    private lateinit var prefs: UserPreferences
    private lateinit var calculateStats: CalculatePortfolioStatsUseCase
    private lateinit var calculateCurve: CalculatePortfolioCurveUseCase
    private lateinit var calculateHistoricalStats: CalculateHistoricalStatsUseCase
    private lateinit var updatePrices: UpdatePricesUseCase

    private val testDispatcher = StandardTestDispatcher()

    private val assetsFlow = MutableStateFlow<List<GoldAsset>>(emptyList())
    private val historyFlow = MutableStateFlow<List<PriceHistory>>(emptyList())
    private val currencyFlow = MutableStateFlow("CHF")

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        repository = mockk(relaxed = true)
        prefs = mockk(relaxed = true)
        calculateStats = CalculatePortfolioStatsUseCase()
        calculateCurve = CalculatePortfolioCurveUseCase()
        calculateHistoricalStats = CalculateHistoricalStatsUseCase()
        updatePrices = mockk(relaxed = true)

        // Setup default mocks
        every { repository.allAssets } returns assetsFlow
        every { repository.allHistory } returns historyFlow
        every { prefs.currency } returns currencyFlow

        viewModel = GoldViewModel(
            repository = repository,
            prefs = prefs,
            calculateStats = calculateStats,
            calculateCurve = calculateCurve,
            calculateHistoricalStats = calculateHistoricalStats,
            updatePrices = updatePrices
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // --- State Flow Tests ---

    @Test
    fun `allAssets emits empty list initially`() = runTest {
        viewModel.allAssets.test {
            assertEquals(emptyList<GoldAsset>(), awaitItem())
        }
    }

    @Test
    fun `allAssets updates when repository emits`() = runTest {
        val assets = listOf(TestDataFactory.createGoldAsset(id = 1))

        viewModel.allAssets.test {
            assertEquals(emptyList<GoldAsset>(), awaitItem())

            assetsFlow.value = assets
            assertEquals(assets, awaitItem())
        }
    }

    @Test
    fun `portfolioStats calculates from assets`() = runTest {
        val assets = listOf(
            TestDataFactory.createProfitableAsset(
                id = 1,
                purchasePrice = 100.0,
                currentSellPrice = 150.0,
                quantity = 2
            )
        )

        viewModel.portfolioStats.test {
            // Initial empty
            val initial = awaitItem()
            assertEquals(0.0, initial.totalValue, 0.001)

            // Update assets
            assetsFlow.value = assets
            advanceUntilIdle()

            val stats = awaitItem()
            assertEquals(300.0, stats.totalValue, 0.001) // 150 * 2
            assertEquals(100.0, stats.totalProfit, 0.001) // (150-100) * 2
            assertEquals(200.0, stats.totalInvested, 0.001) // 100 * 2
        }
    }

    @Test
    fun `currentCurrency emits default CHF`() = runTest {
        viewModel.currentCurrency.test {
            assertEquals("CHF", awaitItem())
        }
    }

    // --- Insert Asset Tests ---

    @Test
    fun `insertAsset calls repository addAsset`() = runTest {
        coEvery { repository.addAsset(any()) } returns 1L

        viewModel.insertAsset(
            name = "Test Gold",
            type = AssetType.BAR,
            purchasePrice = 1000.0,
            buyPrice = 1050.0,
            qty = 1,
            weight = 31.1,
            philoroId = 1991
        )

        advanceUntilIdle()

        coVerify { repository.addAsset(any()) }
    }

    @Test
    fun `insertAsset creates asset with correct values`() = runTest {
        coEvery { repository.addAsset(match {
            it.name == "Gold Bar" &&
            it.type == AssetType.BAR &&
            it.purchasePrice == 5000.0 &&
            it.currentSellPrice == 5000.0 &&
            it.currentBuyPrice == 5200.0 &&
            it.quantity == 2 &&
            it.weightInGrams == 100.0 &&
            it.philoroId == 1991
        }) } returns 1L

        viewModel.insertAsset(
            name = "Gold Bar",
            type = AssetType.BAR,
            purchasePrice = 5000.0,
            buyPrice = 5200.0,
            qty = 2,
            weight = 100.0,
            philoroId = 1991
        )

        advanceUntilIdle()

        // Verify addAsset was called with the expected values
        coVerify { repository.addAsset(match {
            it.name == "Gold Bar" &&
            it.type == AssetType.BAR &&
            it.purchasePrice == 5000.0
        }) }
    }

    @Test
    fun `insertAsset adds initial history record`() = runTest {
        coEvery { repository.addAsset(any()) } returns 1L

        viewModel.insertAsset(
            name = "Test",
            type = AssetType.COIN,
            purchasePrice = 500.0,
            buyPrice = 520.0,
            qty = 1,
            weight = 10.0,
            philoroId = 0
        )

        advanceUntilIdle()

        coVerify { repository.addHistory(any()) }
    }

    // --- Update Asset Tests ---

    @Test
    fun `updateAsset calls repository updateAsset`() = runTest {
        val existingAsset = TestDataFactory.createGoldAsset(id = 1)
        every { repository.getAssetById(1) } returns flowOf(existingAsset)

        viewModel.updateAsset(
            id = 1,
            name = "Updated Name",
            type = AssetType.BAR,
            purchasePrice = 1000.0,
            currentSellPrice = 1100.0,
            quantity = 2,
            weight = 31.1,
            philoroId = 0
        )

        advanceUntilIdle()

        coVerify { repository.updateAsset(any()) }
    }

    // --- Price Update Tests ---

    @Test
    fun `updateAllPricesFromApi sends toast event`() = runTest {
        coEvery { updatePrices.fromSpotPriceApi(any(), any(), any()) } returns Result.success(5)

        viewModel.events.test {
            viewModel.updateAllPricesFromApi()
            advanceUntilIdle()

            // Should receive "Fetching Spot Price..." toast first
            val event1 = awaitItem()
            assertTrue(event1 is UiEvent.ShowToast)
        }
    }

    @Test
    fun `updatePricesFromScraper calls updatePrices fromPhiloroApi`() = runTest {
        coEvery { updatePrices.fromPhiloroApi() } returns Result.success(3)

        viewModel.updatePricesFromScraper()

        advanceUntilIdle()

        coVerify { updatePrices.fromPhiloroApi() }
    }

    @Test
    fun `updateAllPricesFromApi handles error`() = runTest {
        val error = RuntimeException("Network error")
        coEvery { updatePrices.fromSpotPriceApi(any(), any(), any()) } returns Result.failure(error)

        viewModel.events.test {
            viewModel.updateAllPricesFromApi()
            advanceUntilIdle()

            // Skip the "Fetching..." toast
            awaitItem()
            // Should receive error event
            val errorEvent = awaitItem()
            assertTrue(errorEvent is UiEvent.ShowError)
            assertEquals("Network error", (errorEvent as UiEvent.ShowError).error.message)
        }
    }

    // --- Daily Rate Tests ---

    @Test
    fun `addDailyRate rejects future dates`() = runTest {
        val futureDate = System.currentTimeMillis() + 120_000 // 2 minutes in future

        viewModel.addDailyRate(
            assetId = 1,
            newSellPrice = 100.0,
            newBuyPrice = 110.0,
            selectedDate = futureDate,
            isManual = true
        )

        advanceUntilIdle()

        // Should not call repository for future dates
        coVerify(exactly = 0) { repository.addHistory(any()) }
    }

    @Test
    fun `addDailyRate accepts current date`() = runTest {
        val now = System.currentTimeMillis()

        viewModel.addDailyRate(
            assetId = 1,
            newSellPrice = 100.0,
            newBuyPrice = 110.0,
            selectedDate = now,
            isManual = true
        )

        advanceUntilIdle()

        coVerify { repository.addHistory(any()) }
    }

    @Test
    fun `addDailyRate accepts date within tolerance`() = runTest {
        val withinTolerance = System.currentTimeMillis() + 30_000 // 30 seconds in future

        // Create fresh viewmodel to avoid issues with stale coroutine context
        val testViewModel = GoldViewModel(
            repository = repository,
            prefs = prefs,
            calculateStats = calculateStats,
            calculateCurve = calculateCurve,
            calculateHistoricalStats = calculateHistoricalStats,
            updatePrices = updatePrices
        )

        testViewModel.addDailyRate(
            assetId = 1,
            newSellPrice = 100.0,
            newBuyPrice = 110.0,
            selectedDate = withinTolerance,
            isManual = true
        )

        advanceUntilIdle()

        coVerify { repository.addHistory(any()) }
    }

    // --- Backup Tests ---

    @Test
    fun `createBackupJson returns valid JSON`() = runTest {
        // Create a fresh flow and rebuild viewmodel to avoid stale state issues
        val testAssetsFlow = MutableStateFlow(listOf(TestDataFactory.createGoldAsset(id = 1)))
        val testHistoryFlow = MutableStateFlow(listOf(TestDataFactory.createPriceHistory(assetId = 1)))

        every { repository.allAssets } returns testAssetsFlow
        every { repository.allHistory } returns testHistoryFlow

        val testViewModel = GoldViewModel(
            repository = repository,
            prefs = prefs,
            calculateStats = calculateStats,
            calculateCurve = calculateCurve,
            calculateHistoricalStats = calculateHistoricalStats,
            updatePrices = updatePrices
        )

        advanceUntilIdle()

        val json = testViewModel.createBackupJson()

        assertTrue(json.contains("\"assets\""))
        assertTrue(json.contains("\"history\""))
    }

    @Test
    fun `restoreFromBackupJson with invalid JSON returns false`() = runTest {
        val result = viewModel.restoreFromBackupJson("invalid json")

        assertEquals(false, result)
    }

    @Test
    fun `restoreFromBackupJson with empty assets returns false`() = runTest {
        val emptyBackup = "{\"assets\":[],\"history\":[]}"

        val result = viewModel.restoreFromBackupJson(emptyBackup)

        assertEquals(false, result)
    }

    // --- Chart Data Tests ---

    @Test
    fun `getChartPointsForAsset returns empty for no history`() = runTest {
        every { repository.getHistoryForAsset(1) } returns flowOf(emptyList())

        val chartPoints = viewModel.getChartPointsForAsset(1)

        chartPoints.test {
            assertEquals(emptyList<Pair<Long, Double>>(), awaitItem())
        }
    }

    @Test
    fun `getHistoryForAsset returns flow from repository`() = runTest {
        val history = listOf(
            TestDataFactory.createPriceHistory(assetId = 1, sellPrice = 100.0),
            TestDataFactory.createPriceHistory(assetId = 1, sellPrice = 110.0)
        )
        every { repository.getHistoryForAsset(1) } returns flowOf(history)

        val result = viewModel.getHistoryForAsset(1)

        result.test {
            assertEquals(history, awaitItem())
            awaitComplete()
        }
    }

    // --- Portfolio Curve Tests ---

    @Test
    fun `portfolioCurve updates when history and assets change`() = runTest {
        val asset = TestDataFactory.createGoldAsset(id = 1, quantity = 1)
        val history = listOf(
            TestDataFactory.createPriceHistory(assetId = 1, dateTimestamp = 1000L, sellPrice = 100.0),
            TestDataFactory.createPriceHistory(assetId = 1, dateTimestamp = 2000L, sellPrice = 150.0)
        )

        viewModel.portfolioCurve.test {
            // Initial empty
            assertEquals(emptyList<Pair<Long, Double>>(), awaitItem())

            // Update data
            assetsFlow.value = listOf(asset)
            historyFlow.value = history
            advanceUntilIdle()

            val curve = awaitItem()
            assertEquals(2, curve.size)
            assertEquals(100.0, curve[0].second, 0.001)
            assertEquals(150.0, curve[1].second, 0.001)
        }
    }

    // --- Portfolio Change Tests ---

    @Test
    fun `portfolioChange returns zeros for empty curve`() = runTest {
        viewModel.portfolioChange.test {
            val change = awaitItem()
            assertEquals(0.0, change.first, 0.001) // absolute
            assertEquals(0.0, change.second, 0.001) // percentage
        }
    }
}
