package dev.sebastianrn.portfolioapp.viewmodel

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import dev.sebastianrn.portfolioapp.BuildConfig
import dev.sebastianrn.portfolioapp.data.AppDatabase
import dev.sebastianrn.portfolioapp.data.AssetType
import dev.sebastianrn.portfolioapp.data.BackupData
import dev.sebastianrn.portfolioapp.data.GoldAsset
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

class GoldViewModel(private val application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).goldAssetDao()
    private val prefs = UserPreferences(application)

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

    val portfolioCurve: StateFlow<List<Pair<Long, Double>>> = combine(
        dao.getAllHistory(),
        allAssets
    ) { history, assets ->
        calculatePortfolioCurve(assets, history)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun getAssetById(id: Int): Flow<GoldAsset> = dao.getAssetById(id)
    fun getHistoryForAsset(assetId: Int): Flow<List<PriceHistory>> = dao.getHistoryForAsset(assetId)

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

                // 1. Fetch Gold Price in BOTH currencies to calculate the ratio
                // We use async to fetch them in parallel for speed
                val oldPriceJob = async { NetworkModule.api.getGoldPrice(oldCode, apiKey) }
                val newPriceJob = async { NetworkModule.api.getGoldPrice(newCode, apiKey) }

                val oldResponse = oldPriceJob.await()
                val newResponse = newPriceJob.await()

                // 2. Calculate Exchange Rate (New / Old)
                // Example: Gold is 2000 USD and 1800 CHF. Factor = 1.11
                val factor = newResponse.price / oldResponse.price

                // 3. Update Database (Atomic Transaction)
                dao.applyCurrencyFactorToAssets(factor)
                dao.applyCurrencyFactorToHistory(factor)

                // 4. Finally, switch the app mode
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

    // --- ACTIONS ---

    fun insert(
        name: String,
        type: AssetType,
        price: Double,
        qty: Int,
        weight: Double,
        premium: Double
    ) {
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
            // Initial record is Manual
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

                // Pass the currency variable instead of hardcoded CHF
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
            dao.updateCurrentPrice(assetId, newPrice)
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

    // --- HELPERS ---

    private fun mergeTimeIntoDate(dateMillis: Long): Long {
        val calendarDate = Calendar.getInstance().apply { timeInMillis = dateMillis }
        val calendarNow = Calendar.getInstance()
        calendarDate.set(Calendar.HOUR_OF_DAY, calendarNow.get(Calendar.HOUR_OF_DAY))
        calendarDate.set(Calendar.MINUTE, calendarNow.get(Calendar.MINUTE))
        calendarDate.set(Calendar.SECOND, calendarNow.get(Calendar.SECOND))
        return calendarDate.timeInMillis
    }

    private fun calculatePortfolioCurve(
        assets: List<GoldAsset>,
        history: List<PriceHistory>
    ): List<Pair<Long, Double>> {
        if (assets.isEmpty()) return emptyList()
        val uniqueDates = history.map { it.dateTimestamp }.toSortedSet()
        val points = mutableListOf<Pair<Long, Double>>()
        val priceMap = assets.associate { it.id to it.originalPrice }.toMutableMap()
        val qtyMap = assets.associate { it.id to it.quantity }
        val historyByDate = history.groupBy { it.dateTimestamp }

        for (date in uniqueDates) {
            historyByDate[date]?.forEach { record -> priceMap[record.assetId] = record.price }
            var totalValue = 0.0
            qtyMap.forEach { (id, qty) ->
                if (qtyMap.containsKey(id)) {
                    totalValue += (qty * (priceMap[id] ?: 0.0))
                }
            }
            points.add(date to totalValue)
        }
        return points
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
            // 1. Get current state to calculate ratios
            val oldAsset = dao.getAsset(id) ?: return@launch

            // 2. Calculate Adjustment Factor
            // Logic: New Value / Old Value
            // Value is proportional to Weight * (1 + Premium%)
            val oldValueFactor = oldAsset.weightInGrams * (1 + oldAsset.premiumPercent / 100.0)
            val newValueFactor = weight * (1 + premium / 100.0)

            val adjustmentFactor = if (oldValueFactor > 0) newValueFactor / oldValueFactor else 1.0

            // 3. Calculate new Current Price (Manual adjustment to avoid waiting for API)
            val newCurrentPrice = oldAsset.currentPrice * adjustmentFactor

            // 4. Update the Asset Entity
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

            // 5. Update History if the intrinsic value factors changed
            if (abs(adjustmentFactor - 1.0) > 0.0001) {
                dao.adjustHistoryForAsset(id, adjustmentFactor)
            }

            // If the user changed the "Bought At" price, the first history entry must match it.
            val firstHistory = dao.getEarliestHistory(id)
            if (firstHistory != null) {
                // Force the first record to match the new Original Price
                // (We ignore the adjustment factor here because this is an explicit override)
                dao.updateHistory(firstHistory.copy(price = originalPrice))
            }
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

            // Check if the record we just updated is the "First" one
            val firstHistory = dao.getEarliestHistory(assetId)

            // If the IDs match, it means we edited the earliest record
            if (firstHistory != null && firstHistory.historyId == historyId) {
                val asset = dao.getAsset(assetId)
                if (asset != null) {
                    // Update the asset's "Bought At" to match this new price
                    dao.update(asset.copy(originalPrice = newPrice))
                }
            }
        }
    }
}