package dev.sebastianrn.portfolioapp.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.sebastianrn.portfolioapp.data.local.AppDatabase
import dev.sebastianrn.portfolioapp.data.local.GoldAssetDao
import dev.sebastianrn.portfolioapp.data.model.AssetType
import dev.sebastianrn.portfolioapp.data.model.GoldAsset
import dev.sebastianrn.portfolioapp.data.model.PriceHistory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

/**
 * Instrumented tests for GoldAssetDao.
 * Tests database operations with an in-memory Room database.
 */
@RunWith(AndroidJUnit4::class)
class GoldAssetDaoTest {
    private lateinit var db: AppDatabase
    private lateinit var dao: GoldAssetDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.goldAssetDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    // --- Helper Functions ---

    private fun createTestAsset(
        id: Int = 0,
        name: String = "Test Gold",
        type: AssetType = AssetType.BAR,
        purchasePrice: Double = 1000.0,
        currentSellPrice: Double = 1100.0,
        currentBuyPrice: Double = 1150.0,
        quantity: Int = 1,
        weightInGrams: Double = 31.1,
        philoroId: Int = 0
    ) = GoldAsset(
        id = id,
        name = name,
        type = type,
        purchasePrice = purchasePrice,
        currentSellPrice = currentSellPrice,
        currentBuyPrice = currentBuyPrice,
        quantity = quantity,
        weightInGrams = weightInGrams,
        philoroId = philoroId
    )

    private fun createTestHistory(
        historyId: Int = 0,
        assetId: Int = 1,
        dateTimestamp: Long = System.currentTimeMillis(),
        sellPrice: Double = 1100.0,
        buyPrice: Double = 1150.0,
        isManual: Boolean = false
    ) = PriceHistory(
        historyId = historyId,
        assetId = assetId,
        dateTimestamp = dateTimestamp,
        sellPrice = sellPrice,
        buyPrice = buyPrice,
        isManual = isManual
    )

    // --- Asset CRUD Tests ---

    @Test
    fun insertAndReadAsset() = runBlocking {
        val asset = createTestAsset(name = "Gold Bar 100g")

        val insertedId = dao.insert(asset)

        val assets = dao.getAllAssets().first()
        assertEquals(1, assets.size)
        assertEquals("Gold Bar 100g", assets[0].name)
        assertEquals(insertedId.toInt(), assets[0].id)
    }

    @Test
    fun insertMultipleAssets() = runBlocking {
        val asset1 = createTestAsset(name = "Asset 1")
        val asset2 = createTestAsset(name = "Asset 2")
        val asset3 = createTestAsset(name = "Asset 3")

        dao.insert(asset1)
        dao.insert(asset2)
        dao.insert(asset3)

        val assets = dao.getAllAssets().first()
        assertEquals(3, assets.size)
    }

    @Test
    fun updateAsset() = runBlocking {
        val asset = createTestAsset(name = "Original Name")
        val id = dao.insert(asset).toInt()

        val updatedAsset = asset.copy(id = id, name = "Updated Name", purchasePrice = 2000.0)
        dao.update(updatedAsset)

        val result = dao.getAssetById(id).first()
        assertEquals("Updated Name", result.name)
        assertEquals(2000.0, result.purchasePrice, 0.001)
    }

    @Test
    fun deleteAsset() = runBlocking {
        val asset = createTestAsset(name = "To Delete")
        val id = dao.insert(asset).toInt()

        val insertedAsset = dao.getAssetById(id).first()
        dao.deleteAsset(insertedAsset)

        val assets = dao.getAllAssets().first()
        assertTrue(assets.isEmpty())
    }

    @Test
    fun getAssetById() = runBlocking {
        val asset1 = createTestAsset(name = "Asset 1")
        val asset2 = createTestAsset(name = "Asset 2")
        val id1 = dao.insert(asset1).toInt()
        val id2 = dao.insert(asset2).toInt()

        val result = dao.getAssetById(id2).first()

        assertEquals("Asset 2", result.name)
        assertEquals(id2, result.id)
    }

    @Test
    fun getAsset() = runBlocking {
        val asset = createTestAsset(name = "Test Asset")
        val id = dao.insert(asset).toInt()

        val result = dao.getAsset(id)

        assertNotNull(result)
        assertEquals("Test Asset", result?.name)
    }

    @Test
    fun getAssetReturnsNullForNonexistent() = runBlocking {
        val result = dao.getAsset(999)

        assertNull(result)
    }

    // --- History CRUD Tests ---

    @Test
    fun insertAndReadHistory() = runBlocking {
        val assetId = dao.insert(createTestAsset()).toInt()
        val history = createTestHistory(assetId = assetId, sellPrice = 1200.0)

        dao.insertHistory(history)

        val historyList = dao.getHistoryForAsset(assetId).first()
        assertEquals(1, historyList.size)
        assertEquals(1200.0, historyList[0].sellPrice, 0.001)
    }

    @Test
    fun updateHistory() = runBlocking {
        val assetId = dao.insert(createTestAsset()).toInt()
        val history = createTestHistory(assetId = assetId, sellPrice = 1000.0)
        dao.insertHistory(history)

        val inserted = dao.getHistoryForAsset(assetId).first()[0]
        val updated = inserted.copy(sellPrice = 1500.0)
        dao.updateHistory(updated)

        val result = dao.getHistoryForAsset(assetId).first()[0]
        assertEquals(1500.0, result.sellPrice, 0.001)
    }

    @Test
    fun deleteHistory() = runBlocking {
        val assetId = dao.insert(createTestAsset()).toInt()
        val history = createTestHistory(assetId = assetId)
        dao.insertHistory(history)

        val inserted = dao.getHistoryForAsset(assetId).first()[0]
        dao.deleteHistory(inserted)

        val result = dao.getHistoryForAsset(assetId).first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun getHistoryForAssetOrderedByTimestamp() = runBlocking {
        val assetId = dao.insert(createTestAsset()).toInt()
        dao.insertHistory(createTestHistory(assetId = assetId, dateTimestamp = 1000L, sellPrice = 100.0))
        dao.insertHistory(createTestHistory(assetId = assetId, dateTimestamp = 3000L, sellPrice = 300.0))
        dao.insertHistory(createTestHistory(assetId = assetId, dateTimestamp = 2000L, sellPrice = 200.0))

        val history = dao.getHistoryForAsset(assetId).first()

        // Should be ordered by dateTimestamp DESC
        assertEquals(3, history.size)
        assertEquals(300.0, history[0].sellPrice, 0.001) // newest first
        assertEquals(200.0, history[1].sellPrice, 0.001)
        assertEquals(100.0, history[2].sellPrice, 0.001) // oldest last
    }

    // --- Cascade Delete Tests ---

    @Test
    fun deleteAssetCascadesToHistory() = runBlocking {
        val assetId = dao.insert(createTestAsset()).toInt()
        dao.insertHistory(createTestHistory(assetId = assetId, sellPrice = 100.0))
        dao.insertHistory(createTestHistory(assetId = assetId, sellPrice = 200.0))

        // Verify history exists
        var historyList = dao.getHistoryForAsset(assetId).first()
        assertEquals(2, historyList.size)

        // Delete asset
        val asset = dao.getAssetById(assetId).first()
        dao.deleteAsset(asset)

        // Verify history is also deleted (cascade)
        historyList = dao.getHistoryForAsset(assetId).first()
        assertTrue(historyList.isEmpty())
    }

    // --- Philoro ID Tests ---

    @Test
    fun getAssetsWithPhiloroId() = runBlocking {
        dao.insert(createTestAsset(name = "With Philoro", philoroId = 1991))
        dao.insert(createTestAsset(name = "Without Philoro", philoroId = 0))
        dao.insert(createTestAsset(name = "Another With Philoro", philoroId = 2000))

        val result = dao.getAssetsWithPhiloroId()

        assertEquals(2, result.size)
        assertTrue(result.all { it.philoroId > 0 })
    }

    @Test
    fun updatePricesByPhiloroId() = runBlocking {
        val assetId = dao.insert(createTestAsset(philoroId = 1991, currentSellPrice = 1000.0, currentBuyPrice = 1100.0)).toInt()

        dao.updatePricesByPhiloroId(1991, 1500.0, 1600.0)

        val result = dao.getAssetById(assetId).first()
        assertEquals(1500.0, result.currentSellPrice, 0.001)
        assertEquals(1600.0, result.currentBuyPrice, 0.001)
    }

    // --- Price Update Tests ---

    @Test
    fun updateCurrentPrice() = runBlocking {
        val assetId = dao.insert(createTestAsset(currentSellPrice = 1000.0)).toInt()

        dao.updateCurrentPrice(assetId, 1500.0)

        val result = dao.getAssetById(assetId).first()
        assertEquals(1500.0, result.currentSellPrice, 0.001)
    }

    // --- History Query Tests ---

    @Test
    fun getEarliestHistory() = runBlocking {
        val assetId = dao.insert(createTestAsset()).toInt()
        dao.insertHistory(createTestHistory(assetId = assetId, dateTimestamp = 3000L, sellPrice = 300.0))
        dao.insertHistory(createTestHistory(assetId = assetId, dateTimestamp = 1000L, sellPrice = 100.0)) // Earliest
        dao.insertHistory(createTestHistory(assetId = assetId, dateTimestamp = 2000L, sellPrice = 200.0))

        val result = dao.getEarliestHistory(assetId)

        assertNotNull(result)
        assertEquals(1000L, result?.dateTimestamp)
        assertEquals(100.0, result?.sellPrice ?: 0.0, 0.001)
    }

    @Test
    fun getEarliestHistoryReturnsNullWhenEmpty() = runBlocking {
        val assetId = dao.insert(createTestAsset()).toInt()

        val result = dao.getEarliestHistory(assetId)

        assertNull(result)
    }

    @Test
    fun getLatestHistory() = runBlocking {
        val assetId = dao.insert(createTestAsset()).toInt()
        dao.insertHistory(createTestHistory(assetId = assetId, dateTimestamp = 1000L, sellPrice = 100.0))
        dao.insertHistory(createTestHistory(assetId = assetId, dateTimestamp = 3000L, sellPrice = 300.0)) // Latest
        dao.insertHistory(createTestHistory(assetId = assetId, dateTimestamp = 2000L, sellPrice = 200.0))

        val result = dao.getLatestHistory(assetId)

        assertNotNull(result)
        assertEquals(3000L, result?.dateTimestamp)
        assertEquals(300.0, result?.sellPrice ?: 0.0, 0.001)
    }

    @Test
    fun getLatestHistoryReturnsNullWhenEmpty() = runBlocking {
        val assetId = dao.insert(createTestAsset()).toInt()

        val result = dao.getLatestHistory(assetId)

        assertNull(result)
    }

    // --- Bulk Operations Tests ---

    @Test
    fun insertAllAssets() = runBlocking {
        val assets = listOf(
            createTestAsset(id = 1, name = "Asset 1"),
            createTestAsset(id = 2, name = "Asset 2"),
            createTestAsset(id = 3, name = "Asset 3")
        )

        dao.insertAllAssets(assets)

        val result = dao.getAllAssets().first()
        assertEquals(3, result.size)
    }

    @Test
    fun insertAllHistory() = runBlocking {
        val assetId = dao.insert(createTestAsset()).toInt()
        val history = listOf(
            createTestHistory(historyId = 1, assetId = assetId, sellPrice = 100.0),
            createTestHistory(historyId = 2, assetId = assetId, sellPrice = 200.0),
            createTestHistory(historyId = 3, assetId = assetId, sellPrice = 300.0)
        )

        dao.insertAllHistory(history)

        val result = dao.getHistoryForAsset(assetId).first()
        assertEquals(3, result.size)
    }

    @Test
    fun clearAssets() = runBlocking {
        dao.insert(createTestAsset(name = "Asset 1"))
        dao.insert(createTestAsset(name = "Asset 2"))

        dao.clearAssets()

        val result = dao.getAllAssets().first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun clearHistory() = runBlocking {
        val assetId = dao.insert(createTestAsset()).toInt()
        dao.insertHistory(createTestHistory(assetId = assetId))
        dao.insertHistory(createTestHistory(assetId = assetId))

        dao.clearHistory()

        val result = dao.getAllHistory().first()
        assertTrue(result.isEmpty())
    }

    // --- Restore Database Tests ---

    @Test
    fun restoreDatabase() = runBlocking {
        // Add initial data
        val initialAssetId = dao.insert(createTestAsset(name = "Initial")).toInt()
        dao.insertHistory(createTestHistory(assetId = initialAssetId))

        // Prepare restore data
        val restoreAssets = listOf(
            createTestAsset(id = 10, name = "Restored 1"),
            createTestAsset(id = 20, name = "Restored 2")
        )
        val restoreHistory = listOf(
            createTestHistory(historyId = 100, assetId = 10, sellPrice = 1000.0),
            createTestHistory(historyId = 200, assetId = 20, sellPrice = 2000.0)
        )

        dao.restoreDatabase(restoreAssets, restoreHistory)

        // Verify old data is gone
        val assets = dao.getAllAssets().first()
        assertEquals(2, assets.size)
        assertTrue(assets.none { it.name == "Initial" })

        // Verify new data exists
        assertTrue(assets.any { it.name == "Restored 1" })
        assertTrue(assets.any { it.name == "Restored 2" })

        val history = dao.getAllHistory().first()
        assertEquals(2, history.size)
    }

    // --- Currency Factor Tests ---

    @Test
    fun applyCurrencyFactorToAssets() = runBlocking {
        dao.insert(createTestAsset(purchasePrice = 100.0, currentSellPrice = 110.0))
        dao.insert(createTestAsset(purchasePrice = 200.0, currentSellPrice = 220.0))

        dao.applyCurrencyFactorToAssets(1.1) // 10% increase

        val assets = dao.getAllAssets().first()
        assertEquals(110.0, assets[0].purchasePrice, 0.01)
        assertEquals(121.0, assets[0].currentSellPrice, 0.01)
        assertEquals(220.0, assets[1].purchasePrice, 0.01)
        assertEquals(242.0, assets[1].currentSellPrice, 0.01)
    }

    @Test
    fun applyCurrencyFactorToHistory() = runBlocking {
        val assetId = dao.insert(createTestAsset()).toInt()
        dao.insertHistory(createTestHistory(assetId = assetId, sellPrice = 100.0))
        dao.insertHistory(createTestHistory(assetId = assetId, sellPrice = 200.0))

        dao.applyCurrencyFactorToHistory(2.0) // Double all prices

        val history = dao.getAllHistory().first()
        assertTrue(history.all { it.sellPrice == 200.0 || it.sellPrice == 400.0 })
    }

    @Test
    fun adjustHistoryForAsset() = runBlocking {
        val assetId1 = dao.insert(createTestAsset()).toInt()
        val assetId2 = dao.insert(createTestAsset()).toInt()
        dao.insertHistory(createTestHistory(assetId = assetId1, sellPrice = 100.0))
        dao.insertHistory(createTestHistory(assetId = assetId2, sellPrice = 100.0))

        dao.adjustHistoryForAsset(assetId1, 2.0) // Only adjust asset 1

        val history1 = dao.getHistoryForAsset(assetId1).first()
        val history2 = dao.getHistoryForAsset(assetId2).first()

        assertEquals(200.0, history1[0].sellPrice, 0.001)
        assertEquals(100.0, history2[0].sellPrice, 0.001) // Unchanged
    }

    // --- Non-Flow Query Tests (for backup) ---

    @Test
    fun getAllAssetsOnce() = runBlocking {
        dao.insert(createTestAsset(name = "Asset 1"))
        dao.insert(createTestAsset(name = "Asset 2"))

        val result = dao.getAllAssetsOnce()

        assertEquals(2, result.size)
    }

    @Test
    fun getAllHistoryOnce() = runBlocking {
        val assetId = dao.insert(createTestAsset()).toInt()
        dao.insertHistory(createTestHistory(assetId = assetId, dateTimestamp = 2000L))
        dao.insertHistory(createTestHistory(assetId = assetId, dateTimestamp = 1000L))

        val result = dao.getAllHistoryOnce()

        assertEquals(2, result.size)
        // Should be ordered by timestamp ASC
        assertEquals(1000L, result[0].dateTimestamp)
        assertEquals(2000L, result[1].dateTimestamp)
    }

    // --- Total Investment Query Tests ---

    @Test
    fun getTotalInvestment() = runBlocking {
        dao.insert(createTestAsset(purchasePrice = 100.0, quantity = 2)) // 200
        dao.insert(createTestAsset(purchasePrice = 500.0, quantity = 1)) // 500

        val result = dao.getTotalInvestment().first()

        assertEquals(700.0, result ?: 0.0, 0.001)
    }

    @Test
    fun getTotalInvestmentReturnsNullWhenEmpty() = runBlocking {
        val result = dao.getTotalInvestment().first()

        assertNull(result)
    }
}
