package com.example.portfolioapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.portfolioapp.data.AppDatabase
import com.example.portfolioapp.data.GoldCoin
import com.example.portfolioapp.data.PriceHistory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

class GoldViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).goldCoinDao()

    // --- Flows for UI ---
    val allCoins: StateFlow<List<GoldCoin>> = dao.getAllCoins()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val totalInvestment: StateFlow<Double> = dao.getTotalInvestment()
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.Lazily, 0.0)

    val portfolioCurve: StateFlow<List<Pair<Long, Double>>> = combine(
        dao.getAllHistory(),
        allCoins
    ) { history, coins ->
        calculatePortfolioCurve(coins, history)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // --- Operations ---

    fun insert(name: String, price: Double, qty: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            val coin = GoldCoin(name = name, originalPrice = price, currentPrice = price, quantity = qty)
            val id = dao.insert(coin)
            // Initial History
            dao.insertHistory(PriceHistory(coinId = id.toInt(), dateTimestamp = System.currentTimeMillis(), price = price))
        }
    }

    /**
     * Adds a daily rate with validation and time merging.
     */
    fun addDailyRate(coinId: Int, newPrice: Double, selectedDate: Long) {
        val currentTimestamp = System.currentTimeMillis()

        // 1. Validation: Prevent Future Dates (with 1 minute buffer for system clock diffs)
        if (selectedDate > currentTimestamp + 60_000) {
            // In a real app, you might emit an error event here.
            // For now, we simply ignore the future date or clamp it to "now".
            return
        }

        // 2. Merge Time: The DatePicker returns midnight (00:00).
        // We attach the CURRENT time (HH:mm:ss) to that date so multiple records
        // on the same day can be distinguished and sorted correctly.
        val finalTimestamp = mergeTimeIntoDate(selectedDate)

        viewModelScope.launch(Dispatchers.IO) {
            dao.insertHistory(PriceHistory(coinId = coinId, dateTimestamp = finalTimestamp, price = newPrice))

            // We usually treat the newly added record as the "Current Price"
            dao.updateCurrentPrice(coinId, newPrice)
        }
    }

    fun getHistoryForCoin(coinId: Int): Flow<List<PriceHistory>> = dao.getHistoryForCoin(coinId)

    /**
     * Helper to merge the "Hour/Minute/Second" from NOW into the selected DATE.
     */
    private fun mergeTimeIntoDate(dateMillis: Long): Long {
        val calendarDate = Calendar.getInstance().apply { timeInMillis = dateMillis }
        val calendarNow = Calendar.getInstance()

        calendarDate.set(Calendar.HOUR_OF_DAY, calendarNow.get(Calendar.HOUR_OF_DAY))
        calendarDate.set(Calendar.MINUTE, calendarNow.get(Calendar.MINUTE))
        calendarDate.set(Calendar.SECOND, calendarNow.get(Calendar.SECOND))

        return calendarDate.timeInMillis
    }

    // --- Calculation Logic ---

    private fun calculatePortfolioCurve(coins: List<GoldCoin>, history: List<PriceHistory>): List<Pair<Long, Double>> {
        if (coins.isEmpty()) return emptyList()

        val uniqueDates = (history.map { it.dateTimestamp } + System.currentTimeMillis()).toSortedSet()
        val points = mutableListOf<Pair<Long, Double>>()

        val priceMap = coins.associate { it.id to it.originalPrice }.toMutableMap()
        val qtyMap = coins.associate { it.id to it.quantity }

        val historyByDate = history.groupBy { it.dateTimestamp }

        for (date in uniqueDates) {
            historyByDate[date]?.forEach { record ->
                priceMap[record.coinId] = record.price
            }

            var totalValue = 0.0
            qtyMap.forEach { (id, qty) ->
                val price = priceMap[id] ?: 0.0
                totalValue += (qty * price)
            }
            points.add(date to totalValue)
        }
        return points
    }
}