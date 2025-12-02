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

    // --- NEW: Get Specific Coin ---
    fun getCoinById(id: Int): Flow<GoldCoin> = dao.getCoinById(id)

    fun getHistoryForCoin(coinId: Int): Flow<List<PriceHistory>> = dao.getHistoryForCoin(coinId)

    fun insert(name: String, price: Double, qty: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            val coin = GoldCoin(name = name, originalPrice = price, currentPrice = price, quantity = qty)
            val id = dao.insert(coin)
            dao.insertHistory(PriceHistory(coinId = id.toInt(), dateTimestamp = System.currentTimeMillis(), price = price))
        }
    }

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