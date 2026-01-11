package dev.sebastianrn.portfolioapp.util

import dev.sebastianrn.portfolioapp.data.AssetType
import dev.sebastianrn.portfolioapp.data.GoldAsset
import dev.sebastianrn.portfolioapp.data.GoldAssetDao
import dev.sebastianrn.portfolioapp.data.PriceHistory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.random.Random

class TestDataGenerator(private val dao: GoldAssetDao) {

    suspend fun generateData(assetCount: Int, historyPerAsset: Int) = withContext(Dispatchers.IO) {
        // Optional: Clear existing data first
        dao.clearHistory()
        dao.clearAssets()

        val currentTime = System.currentTimeMillis()
        val oneDayMillis = 86_400_000L

        for (i in 1..assetCount) {
            val basePrice = 1800.0 + Random.nextDouble(-200.0, 500.0)

            // 1. Create the GoldAsset
            val asset = GoldAsset(
                name = if (i % 2 == 0) "Gold Bar #$i" else "Gold Coin #$i",
                type = if (i % 2 == 0) AssetType.BAR else AssetType.COIN,
                purchasePrice = basePrice,
                currentSellPrice = basePrice, // Will be updated by history
                currentBuyPrice = basePrice+10, // Will be updated by history
                quantity = Random.nextInt(1, 10),
                weightInGrams = if (i % 2 == 0) 31.1 else 5.81,
                philoroId = 1
            )

            // 2. Insert Asset and get ID
            val assetId = dao.insert(asset).toInt()

            // 3. Generate Multiple PriceHistory records
            var lastPrice = basePrice
            for (j in 0 until historyPerAsset) {
                // Create a price walk (simulating market fluctuations)
                val priceChange = Random.nextDouble(-10.0, 15.0)
                val recordPrice = lastPrice + priceChange
                lastPrice = recordPrice

                val historyRecord = PriceHistory(
                    assetId = assetId,
                    // Distribute records back in time
                    dateTimestamp = currentTime - (historyPerAsset - j) * oneDayMillis,
                    sellPrice = recordPrice,
                    buyPrice = recordPrice+10,
                    isManual = false
                )
                dao.insertHistory(historyRecord)

                // Update the asset's current price to the latest history entry
                if (j == historyPerAsset - 1) {
                    dao.updateCurrentPrice(assetId, recordPrice)
                }
            }
        }
    }
}