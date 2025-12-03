package com.example.portfolioapp.viewmodel

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.portfolioapp.data.AppDatabase
import com.example.portfolioapp.data.GoldCoin
import com.example.portfolioapp.data.NetworkModule
import com.example.portfolioapp.data.PriceHistory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

data class PortfolioSummary(
    val totalValue: Double = 0.0,
    val totalProfit: Double = 0.0,
    val totalInvested: Double = 0.0
)

class GoldViewModel(private val application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).goldCoinDao()

    // UI State Flows
    val allCoins: StateFlow<List<GoldCoin>> = dao.getAllCoins()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val portfolioStats: StateFlow<PortfolioSummary> = allCoins.map { coins ->
        val value = coins.sumOf { it.totalCurrentValue }
        val profit = coins.sumOf { it.totalProfitOrLoss }
        val invested = coins.sumOf { it.originalPrice * it.quantity }
        PortfolioSummary(value, profit, invested)
    }.stateIn(viewModelScope, SharingStarted.Lazily, PortfolioSummary())

    val portfolioCurve: StateFlow<List<Pair<Long, Double>>> = combine(
        dao.getAllHistory(),
        allCoins
    ) { history, coins ->
        calculatePortfolioCurve(coins, history)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // --- DB Operations ---

    fun getCoinById(id: Int): Flow<GoldCoin> = dao.getCoinById(id)
    fun getHistoryForCoin(coinId: Int): Flow<List<PriceHistory>> = dao.getHistoryForCoin(coinId)

    fun insert(name: String, price: Double, qty: Int, weight: Double, premium: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            val coin = GoldCoin(
                name = name,
                originalPrice = price,
                currentPrice = price,
                quantity = qty,
                weightInGrams = weight,
                premiumPercent = premium
            )
            val id = dao.insert(coin)
            dao.insertHistory(PriceHistory(coinId = id.toInt(), dateTimestamp = System.currentTimeMillis(), price = price))
        }
    }

    // --- API UPDATE LOGIC ---
    fun updateAllPricesFromApi() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 1. Notify User
                withContext(Dispatchers.Main) {
                    Toast.makeText(application, "Fetching Spot Price...", Toast.LENGTH_SHORT).show()
                }

                // 2. Fetch Spot Price (Raw Gold Value 24k/100%)
                val apiKey = "***REMOVED***"
                val response = NetworkModule.api.getGoldPrice("CHF", apiKey)
                val spotPricePerGram24k = response.price_gram_24k

                Log.d("GoldApp", "Spot Price (100% Pure): $spotPricePerGram24k CHF/g")

                // 3. Update ALL coins
                val coins = dao.getAllCoins().first()
                val timestamp = System.currentTimeMillis()
                var updatedCount = 0

                // 4. The "999.9" Fineness Factor
                // The API gives us the price for 100% pure gold (1.000).
                // Most investment coins (Vreneli, Philharmoniker) are 99.99% pure.
                val coinFineness = 0.9999

                for (coin in coins) {
                    // Step A: Calculate Intrinsic Material Value
                    // (Spot Price * Weight) * Fineness
                    val intrinsicValue = spotPricePerGram24k * coin.weightInGrams * coinFineness

                    // Step B: Apply Dealer Premium
                    // Value * (1 + Premium%)
                    val premiumMultiplier = 1 + (coin.premiumPercent / 100.0)
                    val finalPrice = intrinsicValue * premiumMultiplier

                    // Save
                    dao.insertHistory(PriceHistory(coinId = coin.id, dateTimestamp = timestamp, price = finalPrice))
                    dao.updateCurrentPrice(coin.id, finalPrice)
                    updatedCount++
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(application, "Updated $updatedCount coins. Spot: ${String.format("%.2f", spotPricePerGram24k)} CHF/g", Toast.LENGTH_LONG).show()
                }

            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(application, "Update Failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // Manual Update
    fun addDailyRate(coinId: Int, newPrice: Double, selectedDate: Long) {
        val currentTimestamp = System.currentTimeMillis()
        if (selectedDate > currentTimestamp + 60_000) return

        val finalTimestamp = mergeTimeIntoDate(selectedDate)

        viewModelScope.launch(Dispatchers.IO) {
            dao.insertHistory(PriceHistory(coinId = coinId, dateTimestamp = finalTimestamp, price = newPrice))
            dao.updateCurrentPrice(coinId, newPrice)
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

    private fun calculatePortfolioCurve(coins: List<GoldCoin>, history: List<PriceHistory>): List<Pair<Long, Double>> {
        if (coins.isEmpty()) return emptyList()
        val uniqueDates = (history.map { it.dateTimestamp } + System.currentTimeMillis()).toSortedSet()
        val points = mutableListOf<Pair<Long, Double>>()
        val priceMap = coins.associate { it.id to it.originalPrice }.toMutableMap()
        val qtyMap = coins.associate { it.id to it.quantity }
        val historyByDate = history.groupBy { it.dateTimestamp }

        for (date in uniqueDates) {
            historyByDate[date]?.forEach { record -> priceMap[record.coinId] = record.price }
            var totalValue = 0.0
            qtyMap.forEach { (id, qty) -> totalValue += (qty * (priceMap[id] ?: 0.0)) }
            points.add(date to totalValue)
        }
        return points
    }
}