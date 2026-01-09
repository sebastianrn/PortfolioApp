package dev.sebastianrn.portfolioapp.viewmodel

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.google.gson.Gson
import dev.sebastianrn.portfolioapp.BuildConfig
import dev.sebastianrn.portfolioapp.data.AppDatabase
import dev.sebastianrn.portfolioapp.data.AssetType
import dev.sebastianrn.portfolioapp.data.BackupData
import dev.sebastianrn.portfolioapp.data.GoldAsset
import dev.sebastianrn.portfolioapp.data.GoldAssetDao
import dev.sebastianrn.portfolioapp.data.NetworkModule
import dev.sebastianrn.portfolioapp.data.PhiloroScrapingService
import dev.sebastianrn.portfolioapp.data.PriceHistory
import dev.sebastianrn.portfolioapp.data.ScrapedAsset
import dev.sebastianrn.portfolioapp.data.UserPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import kotlin.math.abs

data class PortfolioSummary(
    val totalValue: Double = 0.0,
    val totalProfit: Double = 0.0,
    val totalInvested: Double = 0.0
)

class GoldViewModel(
    private val application: Application,
    private val dao: GoldAssetDao,
    private val prefs: UserPreferences
) : AndroidViewModel(application) {
    private val scrapingService = PhiloroScrapingService()

    // 1. Expose Currency State
    val currentCurrency: StateFlow<String> = prefs.currency
        .stateIn(viewModelScope, SharingStarted.Lazily, "CHF")

    val allAssets: StateFlow<List<GoldAsset>> = dao.getAllAssets()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun addAsset(asset: GoldAsset) = viewModelScope.launch { dao.insert(asset) }
    fun updateAsset(asset: GoldAsset) = viewModelScope.launch { dao.update(asset) }
    fun deleteAsset(asset: GoldAsset) = viewModelScope.launch { dao.deleteAsset(asset) }

    val portfolioStats: StateFlow<PortfolioSummary> = allAssets.map { assets ->
        val value = assets.sumOf { it.totalCurrentValue }
        val profit = assets.sumOf { it.totalProfitOrLoss }
        val invested = assets.sumOf { it.purchasePrice * it.quantity }
        PortfolioSummary(value, profit, invested)
    }.stateIn(viewModelScope, SharingStarted.Lazily, PortfolioSummary())

    val portfolioCurve: StateFlow<List<Pair<Long, Double>>> = dao.getAllHistory()
        .combine(allAssets) { history, assets ->
            if (history.isEmpty() || assets.isEmpty()) return@combine emptyList()

            val assetMap = assets.associateBy { it.id }
            val latestPrices = mutableMapOf<Int, Double>()

            // Group entries by date to ensure we only have ONE point per day
            history.sortedBy { it.dateTimestamp }
                .groupBy { it.dateTimestamp }
                .map { (timestamp, entriesForDay) ->
                    // 1. Update latest known prices for all assets in these entries
                    entriesForDay.forEach { latestPrices[it.assetId] = it.price }

                    // 2. Calculate the total portfolio value using the latest known price for EVERY asset
                    val totalValue = latestPrices.entries.sumOf { (id, price) ->
                        val asset = assetMap[id]
                        (asset?.quantity ?: 0).toDouble() * price
                    }

                    timestamp to totalValue
                }
        }
        .flowOn(Dispatchers.Default) // Double ensure it's off the Main thread
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val portfolioChange: StateFlow<Pair<Double, Double>> = portfolioCurve.map { curve ->
        if (curve.size < 2) return@map 0.0 to 0.0
        val latest = curve.last().second
        val previous = curve[curve.size - 2].second
        val diff = latest - previous
        val percent = if (previous != 0.0) (diff / previous) * 100 else 0.0
        diff to percent
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0.0 to 0.0)

    fun getAssetById(id: Int): Flow<GoldAsset> = dao.getAssetById(id)
    fun getHistoryForAsset(assetId: Int): Flow<List<PriceHistory>> = dao.getHistoryForAsset(assetId)

    fun getAssetChange(assetId: Int): Flow<Pair<Double, Double>> =
        dao.getHistoryForAsset(assetId).map { history ->
            if (history.size < 2) return@map 0.0 to 0.0
            // History is usually sorted by date descending in DAO
            val latest = history[0].price
            val previous = history[1].price
            val diff = latest - previous
            val percent = if (previous != 0.0) (diff / previous) * 100 else 0.0
            diff to percent
        }

    fun setCurrency(newCode: String) {
        val oldCode = currentCurrency.value
        if (oldCode == newCode) return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        application,
                        "Converting $oldCode to $newCode...",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                val apiKey = BuildConfig.GOLD_API_KEY
                if (apiKey.isEmpty()) return@launch

                val oldPriceJob = async { NetworkModule.api.getGoldPrice(oldCode, apiKey) }
                val newPriceJob = async { NetworkModule.api.getGoldPrice(newCode, apiKey) }

                val oldResponse = oldPriceJob.await()
                val newResponse = newPriceJob.await()

                val factor = newResponse.price / oldResponse.price

                dao.applyCurrencyFactorToAssets(factor)
                dao.applyCurrencyFactorToHistory(factor)

                prefs.setCurrency(newCode)

                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        application,
                        "Converted! Rate: %.4f".format(factor),
                        Toast.LENGTH_LONG
                    ).show()
                }

            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        application,
                        "Conversion Failed. Internet required.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    fun insert(
        name: String,
        type: AssetType,
        price: Double,
        qty: Int,
        weight: Double,
        philoroId: Int
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val asset = GoldAsset(
                name = name,
                type = type,
                purchasePrice = price,
                currentSellPrice = price, // Initial sell price (approx. purchase price)
                currentBuyPrice = price,  // <--- ADD THIS: Set initial Buy Price to Purchase Price
                quantity = qty, // Ensure Int is converted to Double
                weightInGrams = weight,   // Ensure this matches your GoldAsset definition
                philoroId = philoroId          // Optional: Explicitly null for manual entries
            )
            val id = dao.insert(asset)
            dao.insertHistory(
                PriceHistory(
                    assetId = id.toInt(),
                    dateTimestamp = System.currentTimeMillis(),
                    price = price,
                    isManual = true
                )
            )
        }
    }

    fun updateAsset(
        id: Int,
        name: String,
        type: AssetType,
        originalPrice: Double,
        quantity: Int,
        weight: Double,
        philoroId: Int? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val oldAsset = dao.getAsset(id) ?: return@launch

            val oldValueFactor = oldAsset.weightInGrams
            val newValueFactor = weight
            val adjustmentFactor = if (oldValueFactor > 0) newValueFactor / oldValueFactor else 1.0

            val newCurrentPrice = oldAsset.currentSellPrice * adjustmentFactor

            val updatedAsset = oldAsset.copy(
                name = name,
                type = type,
                purchasePrice = originalPrice,
                currentSellPrice = newCurrentPrice,
                quantity = quantity, // Ensure DB gets Double
                weightInGrams = weight,
                philoroId = philoroId // <--- SAVE THE ID
            )
            dao.update(updatedAsset)

            if (abs(adjustmentFactor - 1.0) > 0.0001) {
                dao.adjustHistoryForAsset(id, adjustmentFactor)
            }

            val firstHistory = dao.getEarliestHistory(id)
            if (firstHistory != null) {
                dao.updateHistory(firstHistory.copy(price = originalPrice))
            }
        }
    }

    fun updateAllPricesFromApi() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                withContext(Dispatchers.Main) {
                    Toast.makeText(application, "Fetching Spot Price...", Toast.LENGTH_SHORT).show()
                }

                val currency = currentCurrency.value
                val apiKey = BuildConfig.GOLD_API_KEY

                if (apiKey.isEmpty()) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(application, "API Key missing!", Toast.LENGTH_LONG).show()
                    }
                    return@launch
                }

                val response = NetworkModule.api.getGoldPrice(currency, apiKey)
                val spotPricePerGram24k = response.price_gram_24k

                val assets = dao.getAllAssets().first()
                val timestamp = System.currentTimeMillis()
                var updatedCount = 0
                val coinFineness = 0.9999

                for (asset in assets) {
                    val finalPrice = spotPricePerGram24k * asset.weightInGrams * coinFineness

                    dao.insertHistory(
                        PriceHistory(
                            assetId = asset.id,
                            dateTimestamp = timestamp,
                            price = finalPrice,
                            isManual = false
                        )
                    )
                    dao.updateCurrentPrice(asset.id, finalPrice)
                    updatedCount++
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        application,
                        "Updated $updatedCount assets in $currency.",
                        Toast.LENGTH_LONG
                    ).show()
                }

            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(application, "Update Failed: ${e.message}", Toast.LENGTH_LONG)
                        .show()
                }
            }
        }
    }

    fun addDailyRate(assetId: Int, newPrice: Double, selectedDate: Long) {
        val currentTimestamp = System.currentTimeMillis()
        if (selectedDate > currentTimestamp + 60_000) return

        val finalTimestamp = mergeTimeIntoDate(selectedDate)

        viewModelScope.launch(Dispatchers.IO) {
            dao.insertHistory(
                PriceHistory(
                    assetId = assetId,
                    dateTimestamp = finalTimestamp,
                    price = newPrice,
                    isManual = true
                )
            )

            // FIX: Recalculate current price based on the LATEST history record
            refreshAssetCurrentPrice(assetId)
        }
    }

    fun updateHistoryRecord(
        historyId: Int,
        assetId: Int,
        newPrice: Double,
        newDate: Long,
        isManual: Boolean
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val updatedRecord = PriceHistory(
                historyId = historyId,
                assetId = assetId,
                dateTimestamp = newDate,
                price = newPrice,
                isManual = isManual
            )
            dao.updateHistory(updatedRecord)

            // Update original price if we edited the earliest record
            val firstHistory = dao.getEarliestHistory(assetId)
            if (firstHistory != null && firstHistory.historyId == historyId) {
                val asset = dao.getAsset(assetId)
                if (asset != null) {
                    dao.update(asset.copy(purchasePrice = newPrice))
                }
            }

            // FIX: Recalculate current price (in case we edited the latest record or changed dates)
            refreshAssetCurrentPrice(assetId)
        }
    }

    // Helper function to ensure consistency
    private suspend fun refreshAssetCurrentPrice(assetId: Int) {
        val latest = dao.getLatestHistory(assetId)
        if (latest != null) {
            dao.updateCurrentPrice(assetId, latest.price)
        }
    }

    // --- EXPORT / IMPORT ---

    suspend fun createBackupJson(): String {
        val assets = dao.getAllAssets().first()
        val history = dao.getAllHistory().first()
        val backup = BackupData(assets = assets, history = history)
        return Gson().toJson(backup)
    }

    suspend fun restoreFromBackupJson(jsonString: String): Boolean {
        return try {
            val backup = Gson().fromJson(jsonString, BackupData::class.java)
            if (backup.assets.isNotEmpty()) {
                dao.restoreDatabase(backup.assets, backup.history)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun getChartPointsForAsset(assetId: Int): StateFlow<List<Pair<Long, Double>>> {
        return dao.getHistoryForAsset(assetId)
            .map { history ->
                if (history.isEmpty()) return@map emptyList()

                // 1. Process on background thread
                val rawPoints = history.reversed().map { it.dateTimestamp to it.price }

                // 2. Downsample: If we have many points, only take a max of 100
                // to keep chart rendering performant
                if (rawPoints.size > 100) {
                    val step = rawPoints.size / 100
                    rawPoints.filterIndexed { index, _ ->
                        index % step == 0 || index == rawPoints.lastIndex
                    }
                } else {
                    rawPoints
                }
            }
            .flowOn(Dispatchers.Default) // Ensure processing is off-main
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    }

    /**
     * Triggers the programmatic generation of test data.
     * @param assetCount Number of GoldAssets to create.
     * @param historyPerAsset Number of PriceHistory records for each asset.
     */
    fun generateTestData(assetCount: Int = 10, historyPerAsset: Int = 30) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Using your existing TestDataGenerator class
                val generator = dev.sebastianrn.portfolioapp.util.TestDataGenerator(dao)
                generator.generateData(assetCount, historyPerAsset)

                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        application,
                        "Generated $assetCount assets with $historyPerAsset records each",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        application,
                        "Error generating data: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun mergeTimeIntoDate(dateMillis: Long): Long {
        val calendarDate = Calendar.getInstance().apply { timeInMillis = dateMillis }
        val calendarNow = Calendar.getInstance()
        calendarDate.set(Calendar.HOUR_OF_DAY, calendarNow.get(Calendar.HOUR_OF_DAY))
        calendarDate.set(Calendar.MINUTE, calendarNow.get(Calendar.MINUTE))
        calendarDate.set(Calendar.SECOND, calendarNow.get(Calendar.SECOND))
        return calendarDate.timeInMillis
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                val application =
                    checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])

                // Manual Dependency Injection
                val database = AppDatabase.getDatabase(application)
                val dao = database.goldAssetDao()
                val prefs = UserPreferences(application)

                return GoldViewModel(application, dao, prefs) as T
            }
        }
    }

    fun updatePricesFromScraper() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                withContext(Dispatchers.Main) {
                    Toast.makeText(getApplication(), "Updating prices...", Toast.LENGTH_SHORT)
                        .show()
                }

                // 1. Scrape the website
                val scrapedItems = scrapingService.scrapePrices()

                if (scrapedItems.isEmpty()) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            getApplication(),
                            "Scraping failed or found no items.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    return@launch
                }

                // 2. Map Scraped Data to a Map for fast lookup by ID
                val scrapedMap: Map<String, ScrapedAsset> = scrapedItems.associateBy { it.id }

                // 3. Get all local assets that have a philoroId
                val localAssets = dao.getAssetsWithPhiloroId()
                var updateCount = 0

                // 4. Update matching assets
                for (asset in localAssets) {
                    val targetId = asset.philoroId
                    val match = scrapedMap[targetId.toString()]

                    if (match != null) {
                        // Parse prices safely
                        val newBuyPrice = match.buyPrice.toDoubleOrNull() ?: 0.0
                        val newSellPrice = match.sellPrice.toDoubleOrNull() ?: 0.0

                        if (newBuyPrice > 0 && newSellPrice > 0) {
                            dao.updatePricesByPhiloroId(
                                philoroId = targetId!!,
                                sellPrice = newSellPrice, // Map Sell -> Sell
                                buyPrice = newBuyPrice    // Map Buy -> Buy
                            )
                            updateCount++
                        }
                    }
                }

                withContext(Dispatchers.Main) {
                    if (updateCount > 0) {
                        Toast.makeText(
                            getApplication(),
                            "Updated $updateCount assets successfully!",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            getApplication(),
                            "No matching assets found to update.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        getApplication(),
                        "Update Error: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    fun testScrapingService() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                withContext(Dispatchers.Main) {
                    Toast.makeText(application, "Starting Scraping...", Toast.LENGTH_SHORT).show()
                }

                val scraper = PhiloroScrapingService()
                val results = scraper.scrapePrices()

                withContext(Dispatchers.Main) {
                    if (results.isNotEmpty()) {
                        // For demonstration, show the first result in a Toast
                        val first = results.first()
                        Toast.makeText(
                            application,
                            "Found ${results.size} items. First: ${first.name} (${first.buyPrice})",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(
                            application,
                            "Scraping finished but no items found. Check Logcat.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(application, "Scraping Error: ${e.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }
}