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
    // 1. Expose Currency State
    val currentCurrency: StateFlow<String> = prefs.currency
        .stateIn(viewModelScope, SharingStarted.Lazily, "CHF")

    val allAssets: StateFlow<List<GoldAsset>> = dao.getAllAssets()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val portfolioStats: StateFlow<PortfolioSummary> = allAssets.map { assets ->
        val value = assets.sumOf { it.totalCurrentValue }
        val profit = assets.sumOf { it.totalProfitOrLoss }
        val invested = assets.sumOf { it.originalPrice * it.quantity }
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

    fun getAssetById(id: Int): Flow<GoldAsset> = dao.getAssetById(id)
    fun getHistoryForAsset(assetId: Int): Flow<List<PriceHistory>> = dao.getHistoryForAsset(assetId)

    // --- CURRENCY ---
    fun setCurrency(newCode: String) {
        val oldCode = currentCurrency.value
        if (oldCode == newCode) return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                withContext(Dispatchers.Main) {
                    Toast.makeText(application, "Converting $oldCode to $newCode...", Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(application, "Converted! Rate: %.4f".format(factor), Toast.LENGTH_LONG).show()
                }

            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(application, "Conversion Failed. Internet required.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // --- ACTIONS ---

    fun insert(name: String, type: AssetType, price: Double, qty: Int, weight: Double, premium: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            val asset = GoldAsset(
                name = name,
                type = type,
                originalPrice = price,
                currentPrice = price,
                quantity = qty,
                weightInGrams = weight,
                premiumPercent = premium
            )
            val id = dao.insert(asset)
            dao.insertHistory(PriceHistory(
                assetId = id.toInt(),
                dateTimestamp = System.currentTimeMillis(),
                price = price,
                isManual = true
            ))
        }
    }

    fun updateAsset(
        id: Int,
        name: String,
        type: AssetType,
        originalPrice: Double,
        quantity: Int,
        weight: Double,
        premium: Double
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val oldAsset = dao.getAsset(id) ?: return@launch

            val oldValueFactor = oldAsset.weightInGrams * (1 + oldAsset.premiumPercent / 100.0)
            val newValueFactor = weight * (1 + premium / 100.0)
            val adjustmentFactor = if (oldValueFactor > 0) newValueFactor / oldValueFactor else 1.0

            val newCurrentPrice = oldAsset.currentPrice * adjustmentFactor

            val updatedAsset = oldAsset.copy(
                name = name,
                type = type,
                originalPrice = originalPrice,
                currentPrice = newCurrentPrice,
                quantity = quantity,
                weightInGrams = weight,
                premiumPercent = premium
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

    fun deleteAsset(asset: GoldAsset) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.deleteAsset(asset)
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
                    val intrinsicValue = spotPricePerGram24k * asset.weightInGrams * coinFineness
                    val premiumMultiplier = 1 + (asset.premiumPercent / 100.0)
                    val finalPrice = intrinsicValue * premiumMultiplier

                    dao.insertHistory(PriceHistory(
                        assetId = asset.id,
                        dateTimestamp = timestamp,
                        price = finalPrice,
                        isManual = false
                    ))
                    dao.updateCurrentPrice(asset.id, finalPrice)
                    updatedCount++
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(application, "Updated $updatedCount assets in $currency.", Toast.LENGTH_LONG).show()
                }

            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(application, "Update Failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun addDailyRate(assetId: Int, newPrice: Double, selectedDate: Long) {
        val currentTimestamp = System.currentTimeMillis()
        if (selectedDate > currentTimestamp + 60_000) return

        val finalTimestamp = mergeTimeIntoDate(selectedDate)

        viewModelScope.launch(Dispatchers.IO) {
            dao.insertHistory(PriceHistory(
                assetId = assetId,
                dateTimestamp = finalTimestamp,
                price = newPrice,
                isManual = true
            ))

            // FIX: Recalculate current price based on the LATEST history record
            refreshAssetCurrentPrice(assetId)
        }
    }

    fun updateHistoryRecord(historyId: Int, assetId: Int, newPrice: Double, newDate: Long, isManual: Boolean) {
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
                    dao.update(asset.copy(originalPrice = newPrice))
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
                    Toast.makeText(application, "Generated $assetCount assets with $historyPerAsset records each", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(application, "Error generating data: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // --- HELPERS ---

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
                val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])

                // Manual Dependency Injection
                val database = AppDatabase.getDatabase(application)
                val dao = database.goldAssetDao()
                val prefs = UserPreferences(application)

                return GoldViewModel(application, dao, prefs) as T
            }
        }
    }
}