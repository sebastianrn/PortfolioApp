package dev.sebastianrn.portfolioapp.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.sebastianrn.portfolioapp.util.TestDataGenerator
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class GoldAssetDaoTest {
    private lateinit var db: AppDatabase
    private lateinit var dao: GoldAssetDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        // Use an in-memory DB so we don't affect the real app data
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        dao = db.goldAssetDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun generateLargeDataset() = runBlocking {
        val generator = TestDataGenerator(dao)
        // Configure your counts here
        generator.generateData(assetCount = 15, historyPerAsset = 100)
    }

    @Test
    fun insertAndReadAsset() = runBlocking {
        val asset = GoldAsset(
            name = "Test Coin",
            type = AssetType.COIN,
            originalPrice = 100.0,
            currentPrice = 110.0,
            quantity = 1,
            weightInGrams = 31.1,
            premiumPercent = 5.0
        )
        dao.insert(asset)

        val assets = dao.getAllAssets().first()
        assertEquals(1, assets.size)
        assertEquals("Test Coin", assets[0].name)
    }

    @Test
    fun insertHistoryAndCheckForeignKeys() = runBlocking {
        // 1. Insert Parent (Asset)
        val asset = GoldAsset(
            name = "Parent",
            type = AssetType.BAR,
            originalPrice = 0.0,
            currentPrice = 0.0,
            quantity = 1,
            weightInGrams = 0.0,
            premiumPercent = 0.0
        )
        val assetId = dao.insert(asset).toInt()

        // 2. Insert Child (History)
        val history = PriceHistory(assetId = assetId, dateTimestamp = 1000L, price = 200.0)
        dao.insertHistory(history)

        val historyList = dao.getHistoryForAsset(assetId).first()
        assertEquals(1, historyList.size)
    }

    @Test
    fun deleteAssetCascadesToHistory() = runBlocking {
        // 1. Create Asset & History
        val assetId = dao.insert(
            GoldAsset(
                name = "To Delete",
                type = AssetType.COIN,
                originalPrice = 0.0,
                currentPrice = 0.0,
                quantity = 1,
                weightInGrams = 0.0,
                premiumPercent = 0.0
            )
        ).toInt()
        dao.insertHistory(PriceHistory(assetId = assetId, dateTimestamp = 1L, price = 100.0))

        // 2. Verify creation
        var historyList = dao.getHistoryForAsset(assetId).first()
        assertEquals(1, historyList.size)

        // 3. Delete Asset
        val assetObj = dao.getAssetById(assetId).first()
        dao.deleteAsset(assetObj)

        // 4. Verify History is GONE (Cascade delete)
        historyList = dao.getHistoryForAsset(assetId).first()
        assertTrue(historyList.isEmpty())
    }
}