package dev.sebastianrn.portfolioapp.domain.usecase

import dev.sebastianrn.portfolioapp.TestDataFactory
import dev.sebastianrn.portfolioapp.data.model.PriceHistory
import dev.sebastianrn.portfolioapp.data.remote.GoldApiService
import dev.sebastianrn.portfolioapp.data.remote.GoldPriceResponse
import dev.sebastianrn.portfolioapp.data.remote.PhiloroScrapingService
import dev.sebastianrn.portfolioapp.data.remote.ScrapedAsset
import dev.sebastianrn.portfolioapp.data.repository.GoldRepository
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for UpdatePricesUseCase.
 * Tests price update logic from spot price API and Philoro API.
 */
class UpdatePricesUseCaseTest {

    private lateinit var useCase: UpdatePricesUseCase
    private lateinit var repository: GoldRepository
    private lateinit var scrapingService: PhiloroScrapingService
    private lateinit var goldApiService: GoldApiService

    @Before
    fun setup() {
        repository = mockk(relaxed = true)
        scrapingService = mockk(relaxed = true)
        goldApiService = mockk(relaxed = true)
        useCase = UpdatePricesUseCase(repository, scrapingService, goldApiService)
    }

    // --- fromSpotPriceApi() Tests ---

    @Test
    fun `fromSpotPriceApi with empty API key returns failure`() = runTest {
        val assets = listOf(TestDataFactory.createGoldAsset())

        val result = useCase.fromSpotPriceApi("CHF", "", assets)

        assertTrue(result.isFailure)
        assertEquals("API Key missing!", result.exceptionOrNull()?.message)
    }

    @Test
    fun `fromSpotPriceApi with empty assets returns success with zero count`() = runTest {
        val response = GoldPriceResponse(
            price = 2000.0,
            price_gram_24k = 64.50,
            price_gram_22k = 59.0,
            price_gram_21k = 56.0,
            price_gram_20k = 53.0,
            price_gram_18k = 48.0
        )
        coEvery { goldApiService.getGoldPrice(any(), any()) } returns response

        val result = useCase.fromSpotPriceApi("CHF", "test-api-key", emptyList())

        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull())
    }

    @Test
    fun `fromSpotPriceApi calculates price based on weight and fineness`() = runTest {
        val asset = TestDataFactory.createGoldAsset(
            id = 1,
            weightInGrams = 31.1, // 1 oz
            quantity = 1
        )
        val response = GoldPriceResponse(
            price = 2000.0,
            price_gram_24k = 64.50, // CHF per gram
            price_gram_22k = 59.0,
            price_gram_21k = 56.0,
            price_gram_20k = 53.0,
            price_gram_18k = 48.0
        )
        coEvery { goldApiService.getGoldPrice(any(), any()) } returns response

        val historySlot = slot<PriceHistory>()
        coEvery { repository.insertHistory(capture(historySlot)) } just Runs

        val result = useCase.fromSpotPriceApi("CHF", "test-api-key", listOf(asset))

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull())

        // Verify calculated price: 64.50 * 31.1 * 0.9999 = ~2005.72
        val expectedPrice = 64.50 * 31.1 * 0.9999
        assertEquals(expectedPrice, historySlot.captured.sellPrice, 0.01)
    }

    @Test
    fun `fromSpotPriceApi updates all assets`() = runTest {
        val assets = listOf(
            TestDataFactory.createGoldAsset(id = 1, weightInGrams = 10.0),
            TestDataFactory.createGoldAsset(id = 2, weightInGrams = 20.0),
            TestDataFactory.createGoldAsset(id = 3, weightInGrams = 30.0)
        )
        val response = GoldPriceResponse(
            price = 2000.0,
            price_gram_24k = 64.50,
            price_gram_22k = 59.0,
            price_gram_21k = 56.0,
            price_gram_20k = 53.0,
            price_gram_18k = 48.0
        )
        coEvery { goldApiService.getGoldPrice(any(), any()) } returns response

        val result = useCase.fromSpotPriceApi("CHF", "test-api-key", assets)

        assertTrue(result.isSuccess)
        assertEquals(3, result.getOrNull())
        coVerify(exactly = 3) { repository.insertHistory(any()) }
        coVerify(exactly = 3) { repository.updateCurrentPrice(any(), any()) }
    }

    @Test
    fun `fromSpotPriceApi handles API exception`() = runTest {
        coEvery { goldApiService.getGoldPrice(any(), any()) } throws RuntimeException("Network error")
        val assets = listOf(TestDataFactory.createGoldAsset())

        val result = useCase.fromSpotPriceApi("CHF", "test-api-key", assets)

        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)
    }

    @Test
    fun `fromSpotPriceApi passes correct currency to API`() = runTest {
        val response = GoldPriceResponse(
            price = 2000.0,
            price_gram_24k = 64.50,
            price_gram_22k = 59.0,
            price_gram_21k = 56.0,
            price_gram_20k = 53.0,
            price_gram_18k = 48.0
        )
        coEvery { goldApiService.getGoldPrice(any(), any()) } returns response

        useCase.fromSpotPriceApi("EUR", "test-api-key", listOf(TestDataFactory.createGoldAsset()))

        coVerify { goldApiService.getGoldPrice("EUR", "test-api-key") }
    }

    @Test
    fun `fromSpotPriceApi sets isManual to false for history`() = runTest {
        val asset = TestDataFactory.createGoldAsset(id = 1)
        val response = GoldPriceResponse(
            price = 2000.0,
            price_gram_24k = 64.50,
            price_gram_22k = 59.0,
            price_gram_21k = 56.0,
            price_gram_20k = 53.0,
            price_gram_18k = 48.0
        )
        coEvery { goldApiService.getGoldPrice(any(), any()) } returns response

        val historySlot = slot<PriceHistory>()
        coEvery { repository.insertHistory(capture(historySlot)) } just Runs

        useCase.fromSpotPriceApi("CHF", "test-api-key", listOf(asset))

        assertEquals(false, historySlot.captured.isManual)
    }

    // --- fromPhiloroApi() Tests ---

    @Test
    fun `fromPhiloroApi with no philoro assets returns success with zero`() = runTest {
        coEvery { repository.getAssetWithPhiloroId() } returns emptyList()

        val result = useCase.fromPhiloroApi()

        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull())
    }

    @Test
    fun `fromPhiloroApi returns failure when API returns no data`() = runTest {
        val asset = TestDataFactory.createGoldBar(id = 1, philoroId = 1991)
        coEvery { repository.getAssetWithPhiloroId() } returns listOf(asset)
        coEvery { scrapingService.fetchPrices(any()) } returns emptyList()

        val result = useCase.fromPhiloroApi()

        assertTrue(result.isFailure)
        assertEquals("API returned no data", result.exceptionOrNull()?.message)
    }

    @Test
    fun `fromPhiloroApi updates matching assets`() = runTest {
        val asset = TestDataFactory.createGoldBar(id = 1, philoroId = 1991)
        val scrapedAsset = ScrapedAsset(
            id = "1991",
            name = "Gold Bar 100g",
            description = "Weight: 100g",
            weight = "100g",
            buyPrice = "5500.00",
            sellPrice = "5200.00"
        )

        coEvery { repository.getAssetWithPhiloroId() } returns listOf(asset)
        coEvery { scrapingService.fetchPrices(listOf("1991")) } returns listOf(scrapedAsset)

        val result = useCase.fromPhiloroApi()

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull())
        coVerify { repository.updatePricesByPhiloroId(1991, 5200.00, 5500.00) }
        coVerify { repository.addHistory(any()) }
    }

    @Test
    fun `fromPhiloroApi skips assets with zero buy price`() = runTest {
        val asset = TestDataFactory.createGoldBar(id = 1, philoroId = 1991)
        val scrapedAsset = ScrapedAsset(
            id = "1991",
            name = "Gold Bar 100g",
            description = "Weight: 100g",
            weight = "100g",
            buyPrice = "0.0",
            sellPrice = "5200.00"
        )

        coEvery { repository.getAssetWithPhiloroId() } returns listOf(asset)
        coEvery { scrapingService.fetchPrices(any()) } returns listOf(scrapedAsset)

        val result = useCase.fromPhiloroApi()

        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull())
        coVerify(exactly = 0) { repository.updatePricesByPhiloroId(any(), any(), any()) }
    }

    @Test
    fun `fromPhiloroApi handles multiple assets`() = runTest {
        val assets = listOf(
            TestDataFactory.createGoldBar(id = 1, philoroId = 1991),
            TestDataFactory.createGoldBar(id = 2, philoroId = 2000)
        )
        val scrapedAssets = listOf(
            ScrapedAsset("1991", "Bar 1", "", "100g", "5500.00", "5200.00"),
            ScrapedAsset("2000", "Bar 2", "", "50g", "2800.00", "2600.00")
        )

        coEvery { repository.getAssetWithPhiloroId() } returns assets
        coEvery { scrapingService.fetchPrices(listOf("1991", "2000")) } returns scrapedAssets

        val result = useCase.fromPhiloroApi()

        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull())
    }

    @Test
    fun `fromPhiloroApi skips non-matching assets`() = runTest {
        val asset = TestDataFactory.createGoldBar(id = 1, philoroId = 1991)
        val scrapedAsset = ScrapedAsset(
            id = "9999", // Different ID
            name = "Other Bar",
            description = "",
            weight = "100g",
            buyPrice = "5500.00",
            sellPrice = "5200.00"
        )

        coEvery { repository.getAssetWithPhiloroId() } returns listOf(asset)
        coEvery { scrapingService.fetchPrices(any()) } returns listOf(scrapedAsset)

        val result = useCase.fromPhiloroApi()

        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull())
    }

    @Test
    fun `fromPhiloroApi handles API exception`() = runTest {
        val asset = TestDataFactory.createGoldBar(id = 1, philoroId = 1991)
        coEvery { repository.getAssetWithPhiloroId() } returns listOf(asset)
        coEvery { scrapingService.fetchPrices(any()) } throws RuntimeException("Network error")

        val result = useCase.fromPhiloroApi()

        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)
    }

    @Test
    fun `fromPhiloroApi handles invalid price strings`() = runTest {
        val asset = TestDataFactory.createGoldBar(id = 1, philoroId = 1991)
        val scrapedAsset = ScrapedAsset(
            id = "1991",
            name = "Gold Bar",
            description = "",
            weight = "100g",
            buyPrice = "invalid",
            sellPrice = "also_invalid"
        )

        coEvery { repository.getAssetWithPhiloroId() } returns listOf(asset)
        coEvery { scrapingService.fetchPrices(any()) } returns listOf(scrapedAsset)

        val result = useCase.fromPhiloroApi()

        // Should succeed but not update (buy price would be 0)
        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull())
    }

    @Test
    fun `fromPhiloroApi sets isManual to false for history`() = runTest {
        val asset = TestDataFactory.createGoldBar(id = 1, philoroId = 1991)
        val scrapedAsset = ScrapedAsset(
            id = "1991",
            name = "Gold Bar",
            description = "",
            weight = "100g",
            buyPrice = "5500.00",
            sellPrice = "5200.00"
        )

        coEvery { repository.getAssetWithPhiloroId() } returns listOf(asset)
        coEvery { scrapingService.fetchPrices(any()) } returns listOf(scrapedAsset)

        val historySlot = slot<PriceHistory>()
        coEvery { repository.addHistory(capture(historySlot)) } just Runs

        useCase.fromPhiloroApi()

        assertEquals(false, historySlot.captured.isManual)
    }
}
