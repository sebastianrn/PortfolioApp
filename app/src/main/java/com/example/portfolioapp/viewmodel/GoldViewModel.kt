package com.example.portfolioapp.viewmodel

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.portfolioapp.data.AppDatabase
import com.example.portfolioapp.data.AssetType
import com.example.portfolioapp.data.GoldAsset
import com.example.portfolioapp.data.NetworkModule
import com.example.portfolioapp.data.PriceHistory
import kotlinx.coroutines.Dispatchers
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

data class PortfolioSummary(
    val totalValue: Double = 0.0,
    val totalProfit: Double = 0.0,
    val totalInvested: Double = 0.0
)

class GoldViewModel(private val application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).goldAssetDao()

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

    // UPDATED: Now takes AssetType
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
            dao.insertHistory(PriceHistory(assetId = id.toInt(), dateTimestamp = System.currentTimeMillis(), price = price))
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

                // TODO: Replace with your API Key
                val apiKey = "***REMOVED***"
                val response = NetworkModule.api.getGoldPrice("CHF", apiKey)
                val spotPricePerGram24k = response.price_gram_24k

                val assets = dao.getAllAssets().first()
                val timestamp = System.currentTimeMillis()
                var updatedCount = 0
                val coinFineness = 0.9999

                for (asset in assets) {
                    val intrinsicValue = spotPricePerGram24k * asset.weightInGrams * coinFineness
                    val premiumMultiplier = 1 + (asset.premiumPercent / 100.0)
                    val finalPrice = intrinsicValue * premiumMultiplier

                    dao.insertHistory(PriceHistory(assetId = asset.id, dateTimestamp = timestamp, price = finalPrice))
                    dao.updateCurrentPrice(asset.id, finalPrice)
                    updatedCount++
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(application, "Updated $updatedCount assets.", Toast.LENGTH_LONG).show()
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
            dao.insertHistory(PriceHistory(assetId = assetId, dateTimestamp = finalTimestamp, price = newPrice))
            dao.updateCurrentPrice(assetId, newPrice)
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

    private fun calculatePortfolioCurve(assets: List<GoldAsset>, history: List<PriceHistory>): List<Pair<Long, Double>> {
        if (assets.isEmpty()) return emptyList()
        val uniqueDates = (history.map { it.dateTimestamp } + System.currentTimeMillis()).toSortedSet()
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
}