package dev.sebastianrn.portfolioapp.domain.usecase

import dev.sebastianrn.portfolioapp.data.model.PriceHistory
import dev.sebastianrn.portfolioapp.data.remote.GoldApiService
import dev.sebastianrn.portfolioapp.data.remote.PhiloroScrapingService
import dev.sebastianrn.portfolioapp.data.repository.GoldRepository
import dev.sebastianrn.portfolioapp.util.Constants

/**
 * Use case for updating asset prices from external sources.
 * Handles both spot price API and Philoro scraping service.
 */
class UpdatePricesUseCase(
    private val repository: GoldRepository,
    private val scrapingService: PhiloroScrapingService,
    private val goldApiService: GoldApiService
) {

    /**
     * Update prices from the gold spot price API.
     * @return Result with number of updated assets on success, or error on failure.
     */
    suspend fun fromSpotPriceApi(
        currency: String,
        apiKey: String,
        assets: List<dev.sebastianrn.portfolioapp.data.model.GoldAsset>
    ): Result<Int> {
        return try {
            if (apiKey.isEmpty()) {
                return Result.failure(IllegalStateException("API Key missing!"))
            }

            val response = goldApiService.getGoldPrice(currency, apiKey)
            val spotPricePerGram24k = response.price_gram_24k
            val timestamp = System.currentTimeMillis()
            var updatedCount = 0

            for (asset in assets) {
                val finalPrice = spotPricePerGram24k * asset.weightInGrams * Constants.GOLD_FINENESS_24K

                repository.insertHistory(
                    PriceHistory(
                        assetId = asset.id,
                        dateTimestamp = timestamp,
                        sellPrice = finalPrice,
                        buyPrice = 0.0,
                        isManual = false
                    )
                )
                repository.updateCurrentPrice(asset.id, finalPrice)
                updatedCount++
            }

            Result.success(updatedCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Update prices from Philoro scraping service.
     * @return Result with number of updated assets on success, or error on failure.
     */
    suspend fun fromPhiloroApi(): Result<Int> {
        return try {
            // Get all local assets that have a philoroId
            val localAssets = repository.getAssetWithPhiloroId()

            if (localAssets.isEmpty()) {
                return Result.success(0)
            }

            // Extract IDs (SKUs)
            val skus = localAssets.map { it.philoroId.toString() }

            // Fetch from API
            val scrapedItems = scrapingService.fetchPrices(skus)

            if (scrapedItems.isEmpty()) {
                return Result.failure(IllegalStateException("API returned no data"))
            }

            // Map Scraped Data for fast lookup
            val scrapedMap = scrapedItems.associateBy { it.id }
            var updateCount = 0

            // Update matching assets
            for (asset in localAssets) {
                val targetId = asset.philoroId.toString()
                val match = scrapedMap[targetId]

                if (match != null) {
                    val newBuyPrice = match.buyPrice.toDoubleOrNull() ?: 0.0
                    val newSellPrice = match.sellPrice.toDoubleOrNull() ?: 0.0

                    if (newBuyPrice > 0) {
                        repository.updatePricesByPhiloroId(
                            philoroId = asset.philoroId,
                            sellPrice = newSellPrice,
                            buyPrice = newBuyPrice
                        )
                        updateCount++

                        repository.addHistory(
                            PriceHistory(
                                assetId = asset.id,
                                dateTimestamp = System.currentTimeMillis(),
                                sellPrice = newSellPrice,
                                buyPrice = newBuyPrice,
                                isManual = false
                            )
                        )
                    }
                }
            }

            Result.success(updateCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
