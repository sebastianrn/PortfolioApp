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

class GoldViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).goldCoinDao()

    // Expose Flows directly to UI
    val allCoins: StateFlow<List<GoldCoin>> = dao.getAllCoins()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val totalInvestment: StateFlow<Double> = dao.getTotalInvestment()
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.Lazily, 0.0)

    // Portfolio Curve Calculation
    // We combine the history flow and coins flow to calculate the graph points
    val portfolioCurve: StateFlow<List<Pair<Long, Double>>> = combine(
        dao.getAllHistory(),
        allCoins
    ) { history, coins ->
        calculatePortfolioCurve(coins, history)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun insert(name: String, price: Double, qty: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            val coin = GoldCoin(name = name, originalPrice = price, currentPrice = price, quantity = qty)
            val id = dao.insert(coin)
            // Initial History
            dao.insertHistory(PriceHistory(coinId = id.toInt(), dateTimestamp = System.currentTimeMillis(), price = price))
        }
    }

    fun addDailyRate(coinId: Int, newPrice: Double, date: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.insertHistory(PriceHistory(coinId = coinId, dateTimestamp = date, price = newPrice))
            dao.updateCurrentPrice(coinId, newPrice)
        }
    }

    fun getHistoryForCoin(coinId: Int): Flow<List<PriceHistory>> = dao.getHistoryForCoin(coinId)

    // Logic to Calculate Graph Points
    private fun calculatePortfolioCurve(coins: List<GoldCoin>, history: List<PriceHistory>): List<Pair<Long, Double>> {
        if (coins.isEmpty()) return emptyList()

        val uniqueDates = (history.map { it.dateTimestamp } + System.currentTimeMillis()).toSortedSet()
        val points = mutableListOf<Pair<Long, Double>>()

        // Map: CoinID -> Current Price (Starts with original prices)
        val priceMap = coins.associate { it.id to it.originalPrice }.toMutableMap()
        val qtyMap = coins.associate { it.id to it.quantity }

        // Group history by date
        val historyByDate = history.groupBy { it.dateTimestamp }

        for (date in uniqueDates) {
            // Update known prices for this date
            historyByDate[date]?.forEach { record ->
                priceMap[record.coinId] = record.price
            }

            // Sum Total Value
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