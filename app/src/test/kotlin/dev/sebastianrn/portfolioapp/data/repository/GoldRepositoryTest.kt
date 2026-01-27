package dev.sebastianrn.portfolioapp.data.repository

import app.cash.turbine.test
import dev.sebastianrn.portfolioapp.TestDataFactory
import dev.sebastianrn.portfolioapp.data.local.GoldAssetDao
import dev.sebastianrn.portfolioapp.data.model.GoldAsset
import dev.sebastianrn.portfolioapp.data.model.PriceHistory
import dev.sebastianrn.portfolioapp.data.remote.GoldApiService
import dev.sebastianrn.portfolioapp.data.remote.PhiloroScrapingService
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for GoldRepository.
 */
class GoldRepositoryTest {

    private lateinit var repository: GoldRepository
    private lateinit var dao: GoldAssetDao
    private lateinit var apiService: GoldApiService
    private lateinit var scraper: PhiloroScrapingService

    @Before
    fun setup() {
        dao = mockk(relaxed = true)
        apiService = mockk(relaxed = true)
        scraper = mockk(relaxed = true)
        repository = GoldRepository(dao, apiService, scraper)
    }

    // --- Flow Delegation Tests ---

    @Test
    fun `allAssets delegates to dao getAllAssets`() = runTest {
        val assets = listOf(TestDataFactory.createGoldAsset(id = 1))
        every { dao.getAllAssets() } returns flowOf(assets)
        // Recreate repository after setting up the mock
        val testRepository = GoldRepository(dao, apiService, scraper)

        testRepository.allAssets.test {
            assertEquals(assets, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `allHistory delegates to dao getAllHistory`() = runTest {
        val history = listOf(TestDataFactory.createPriceHistory(historyId = 1))
        every { dao.getAllHistory() } returns flowOf(history)
        // Recreate repository after setting up the mock
        val testRepository = GoldRepository(dao, apiService, scraper)

        testRepository.allHistory.test {
            assertEquals(history, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `getAssetById delegates to dao getAssetById`() = runTest {
        val asset = TestDataFactory.createGoldAsset(id = 5)
        every { dao.getAssetById(5) } returns flowOf(asset)

        repository.getAssetById(5).test {
            assertEquals(asset, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `getHistoryForAsset delegates to dao getHistoryForAsset`() = runTest {
        val history = listOf(
            TestDataFactory.createPriceHistory(historyId = 1, assetId = 3),
            TestDataFactory.createPriceHistory(historyId = 2, assetId = 3)
        )
        every { dao.getHistoryForAsset(3) } returns flowOf(history)

        repository.getHistoryForAsset(3).test {
            assertEquals(history, awaitItem())
            awaitComplete()
        }
    }

    // --- Suspend Operation Tests ---

    @Test
    fun `addAsset calls dao insert and returns id`() = runTest {
        val asset = TestDataFactory.createGoldAsset()
        coEvery { dao.insert(asset) } returns 42L

        val result = repository.addAsset(asset)

        assertEquals(42L, result)
        coVerify { dao.insert(asset) }
    }

    @Test
    fun `updateAsset calls dao update`() = runTest {
        val asset = TestDataFactory.createGoldAsset(id = 1)

        repository.updateAsset(asset)

        coVerify { dao.update(asset) }
    }

    @Test
    fun `deleteAsset calls dao deleteAsset`() = runTest {
        val asset = TestDataFactory.createGoldAsset(id = 1)

        repository.deleteAsset(asset)

        coVerify { dao.deleteAsset(asset) }
    }

    @Test
    fun `getAssetWithPhiloroId calls dao getAssetsWithPhiloroId`() = runTest {
        val assets = listOf(
            TestDataFactory.createGoldBar(id = 1, philoroId = 1991),
            TestDataFactory.createGoldBar(id = 2, philoroId = 2000)
        )
        coEvery { dao.getAssetsWithPhiloroId() } returns assets

        val result = repository.getAssetWithPhiloroId()

        assertEquals(assets, result)
        coVerify { dao.getAssetsWithPhiloroId() }
    }

    @Test
    fun `updatePricesByPhiloroId calls dao updatePricesByPhiloroId`() = runTest {
        repository.updatePricesByPhiloroId(1991, 5200.0, 5500.0)

        coVerify { dao.updatePricesByPhiloroId(1991, 5200.0, 5500.0) }
    }

    @Test
    fun `getEarliestHistory calls dao getEarliestHistory`() = runTest {
        val history = TestDataFactory.createPriceHistory(historyId = 1, assetId = 5)
        coEvery { dao.getEarliestHistory(5) } returns history

        val result = repository.getEarliestHistory(5)

        assertEquals(history, result)
        coVerify { dao.getEarliestHistory(5) }
    }

    @Test
    fun `getLatestHistory calls dao getLatestHistory`() = runTest {
        val history = TestDataFactory.createPriceHistory(historyId = 10, assetId = 5)
        coEvery { dao.getLatestHistory(5) } returns history

        val result = repository.getLatestHistory(5)

        assertEquals(history, result)
        coVerify { dao.getLatestHistory(5) }
    }

    @Test
    fun `updateHistory calls dao updateHistory`() = runTest {
        val history = TestDataFactory.createPriceHistory(historyId = 1)

        repository.updateHistory(history)

        coVerify { dao.updateHistory(history) }
    }

    @Test
    fun `insertHistory calls dao insertHistory`() = runTest {
        val history = TestDataFactory.createPriceHistory()

        repository.insertHistory(history)

        coVerify { dao.insertHistory(history) }
    }

    @Test
    fun `updateCurrentPrice calls dao updateCurrentPrice`() = runTest {
        repository.updateCurrentPrice(5, 1500.0)

        coVerify { dao.updateCurrentPrice(5, 1500.0) }
    }

    @Test
    fun `restoreDatabase calls dao restoreDatabase`() = runTest {
        val assets = listOf(TestDataFactory.createGoldAsset())
        val history = listOf(TestDataFactory.createPriceHistory())

        repository.restoreDatabase(assets, history)

        coVerify { dao.restoreDatabase(assets, history) }
    }

    // --- addHistory Tests (Business Logic) ---

    @Test
    fun `addHistory inserts history and updates current price`() = runTest {
        val history = TestDataFactory.createPriceHistory(
            assetId = 5,
            sellPrice = 1200.0
        )

        repository.addHistory(history)

        coVerify { dao.insertHistory(history) }
        coVerify { dao.updateCurrentPrice(5, 1200.0) }
    }

    @Test
    fun `addHistory updates asset price to match history sellPrice`() = runTest {
        val priceSlot = slot<Double>()
        val assetIdSlot = slot<Int>()
        coEvery { dao.updateCurrentPrice(capture(assetIdSlot), capture(priceSlot)) } just Runs

        val history = TestDataFactory.createPriceHistory(
            assetId = 10,
            sellPrice = 9999.99
        )

        repository.addHistory(history)

        assertEquals(10, assetIdSlot.captured)
        assertEquals(9999.99, priceSlot.captured, 0.001)
    }

    // --- Null Return Tests ---

    @Test
    fun `getEarliestHistory returns null when no history exists`() = runTest {
        coEvery { dao.getEarliestHistory(999) } returns null

        val result = repository.getEarliestHistory(999)

        assertEquals(null, result)
    }

    @Test
    fun `getLatestHistory returns null when no history exists`() = runTest {
        coEvery { dao.getLatestHistory(999) } returns null

        val result = repository.getLatestHistory(999)

        assertEquals(null, result)
    }

    // --- Empty List Tests ---

    @Test
    fun `getAssetWithPhiloroId returns empty list when none exist`() = runTest {
        coEvery { dao.getAssetsWithPhiloroId() } returns emptyList()

        val result = repository.getAssetWithPhiloroId()

        assertEquals(emptyList<GoldAsset>(), result)
    }

    @Test
    fun `allAssets flow emits empty list when no assets`() = runTest {
        every { dao.getAllAssets() } returns flowOf(emptyList())
        // Recreate repository after setting up the mock
        val testRepository = GoldRepository(dao, apiService, scraper)

        testRepository.allAssets.test {
            assertEquals(emptyList<GoldAsset>(), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `allHistory flow emits empty list when no history`() = runTest {
        every { dao.getAllHistory() } returns flowOf(emptyList())
        // Recreate repository after setting up the mock
        val testRepository = GoldRepository(dao, apiService, scraper)

        testRepository.allHistory.test {
            assertEquals(emptyList<PriceHistory>(), awaitItem())
            awaitComplete()
        }
    }
}
