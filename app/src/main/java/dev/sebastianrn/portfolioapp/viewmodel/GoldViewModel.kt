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
                    entriesForDay.forEach { latestPrices[it.assetId] = it.sellPrice }

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
        if (curve.isEmpty()) return@map 0.0 to 0.0

        // 1. Get current values (Latest point)
        val currentPoint = curve.last()
        val currentValue = currentPoint.second
        val currentTimestamp = currentPoint.first

        // 2. Find the "Start of Day" timestamp (Midnight today relative to the current point)
        val cal = Calendar.getInstance().apply {
            timeInMillis = currentTimestamp
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfDay = cal.timeInMillis

        // 3. Find the last point recorded BEFORE today started
        // This effectively finds "Yesterday's Closing Price" (or the last known price before today)
        val previousPoint = curve.lastOrNull { it.first < startOfDay }

        if (previousPoint != null) {
            val previousValue = previousPoint.second
            val diff = currentValue - previousValue
            val percent = if (previousValue != 0.0) (diff / previousValue) * 100 else 0.0
            diff to percent
        } else {
            // If no history exists before today (e.g. new user), show 0 change
            0.0 to 0.0
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0.0 to 0.0)

    fun getAssetById(id: Int): Flow<GoldAsset> = dao.getAssetById(id)
    fun getHistoryForAsset(assetId: Int): Flow<List<PriceHistory>> = dao.getHistoryForAsset(assetId)

    fun getAssetChange(assetId: Int): Flow<Pair<Double, Double>> =
        dao.getHistoryForAsset(assetId).map { history ->
            if (history.size < 2) return@map 0.0 to 0.0
            // History is usually sorted by date descending in DAO
            val latest = history[0].sellPrice
            val previous = history[1].sellPrice
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

    fun insertAsset(
        name: String,
        type: AssetType,
        purchasePrice: Double,
        buyPrice: Double,
        qty: Int,
        weight: Double,
        philoroId: Int
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val asset = GoldAsset(
                name = name,
                type = type,
                purchasePrice = purchasePrice,
                currentSellPrice = purchasePrice,
                currentBuyPrice = buyPrice,
                quantity = qty,
                weightInGrams = weight,
                philoroId = philoroId
            )
            val id = dao.insert(asset)

            addDailyRate(
                assetId = id.toInt(),
                newSellPrice = purchasePrice,
                newBuyPrice = buyPrice,
                System.currentTimeMillis(),
                true
            )
        }
    }

    fun updateAsset(
        id: Int,
        name: String,
        type: AssetType,
        purchasePrice: Double,
        currentSellPrice: Double,
        quantity: Int,
        weight: Double,
        philoroId: Int
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val oldAsset = dao.getAsset(id) ?: return@launch

            val updatedAsset = oldAsset.copy(
                name = name,
                type = type,
                purchasePrice = purchasePrice,
                currentSellPrice = currentSellPrice,
                quantity = quantity,
                weightInGrams = weight,
                philoroId = philoroId
            )
            dao.update(updatedAsset)

            val firstHistory = dao.getEarliestHistory(id)
            if (firstHistory != null) {
                dao.updateHistory(firstHistory.copy(sellPrice = purchasePrice))
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
                            sellPrice = finalPrice,
                            buyPrice = 0.0,
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

    fun addDailyRate(
        assetId: Int,
        newSellPrice: Double,
        newBuyPrice: Double,
        selectedDate: Long,
        isManual: Boolean
    ) {
        val currentTimestamp = System.currentTimeMillis()
        if (selectedDate > currentTimestamp + 60_000) return

        val finalTimestamp = mergeTimeIntoDate(selectedDate)

        viewModelScope.launch(Dispatchers.IO) {
            dao.insertHistory(
                PriceHistory(
                    assetId = assetId,
                    dateTimestamp = finalTimestamp,
                    sellPrice = newSellPrice,
                    buyPrice = newBuyPrice,
                    isManual = isManual
                )
            )

            // FIX: Recalculate current price based on the LATEST history record
            refreshAssetCurrentPrice(assetId)
        }
    }

    fun updateHistoryRecord(
        historyId: Int,
        assetId: Int,
        newSellPrice: Double,
        newBuyPrice: Double,
        newDate: Long,
        isManual: Boolean
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val updatedRecord = PriceHistory(
                historyId = historyId,
                assetId = assetId,
                dateTimestamp = newDate,
                sellPrice = newSellPrice,
                buyPrice = newBuyPrice,
                isManual = isManual
            )
            dao.updateHistory(updatedRecord)

            // Update original price if we edited the earliest record
            val firstHistory = dao.getEarliestHistory(assetId)
            if (firstHistory != null && firstHistory.historyId == historyId) {
                val asset = dao.getAsset(assetId)
                if (asset != null) {
                    dao.update(asset.copy(purchasePrice = newSellPrice))
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
            dao.updateCurrentPrice(assetId, latest.sellPrice)
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
                for (asset in backup.assets) {
                    refreshAssetCurrentPrice(asset.id)
                }
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
                val rawPoints = history.reversed().map { it.dateTimestamp to it.sellPrice }

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
                    Toast.makeText(getApplication(), "Updating prices via API...", Toast.LENGTH_SHORT)
                        .show()
                }

                // 1. Get all local assets that have a philoroId
                val localAssets = dao.getAssetsWithPhiloroId()
                val timestamp = System.currentTimeMillis()

                if (localAssets.isEmpty()) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(getApplication(), "No Philoro assets to update.", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                // 2. Extract IDs (SKUs)
                val skus = localAssets.mapNotNull { it.philoroId?.toString() }

                // 3. Fetch from API
                // Changed from 'scrapePrices()' to 'fetchPrices(skus)'
                val scrapedItems = scrapingService.fetchPrices(skus)

                if (scrapedItems.isEmpty()) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            getApplication(),
                            "API returned no data.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    return@launch
                }

                // 4. Map Scraped Data to a Map for fast lookup
                val scrapedMap = scrapedItems.associateBy { it.id } // it.id is the SKU

                var updateCount = 0

                // 5. Update matching assets
                for (asset in localAssets) {
                    val targetId = asset.philoroId.toString()
                    val match = scrapedMap[targetId]

                    if (match != null) {
                        // Parse prices (They are already cleaned strings from our Service)
                        val newBuyPrice = match.buyPrice.toDoubleOrNull() ?: 0.0
                        val newSellPrice = match.sellPrice.toDoubleOrNull() ?: 0.0

                        if (newBuyPrice > 0) {
                            dao.updatePricesByPhiloroId(
                                philoroId = asset.philoroId!!,
                                sellPrice = newSellPrice,
                                buyPrice = newBuyPrice
                            )
                            updateCount++

                            addDailyRate(
                                asset.id,
                                newSellPrice,
                                newBuyPrice,
                                System.currentTimeMillis(),
                                false
                            )
                        }
                    }
                }

                withContext(Dispatchers.Main) {
                    if (updateCount > 0) {
                        Toast.makeText(
                            getApplication(),
                            "Updated $updateCount assets via API!",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            getApplication(),
                            "Prices retrieved but no local assets matched.",
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
}