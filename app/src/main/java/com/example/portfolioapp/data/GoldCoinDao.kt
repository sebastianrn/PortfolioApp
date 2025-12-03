package com.example.portfolioapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import kotlinx.coroutines.flow.Flow

@Dao
interface GoldCoinDao {
    @Insert
    suspend fun insert(coin: GoldCoin): Long

    @Update
    suspend fun update(coin: GoldCoin)

    @Query("UPDATE gold_coins SET currentPrice = :newPrice WHERE id = :coinId")
    suspend fun updateCurrentPrice(coinId: Int, newPrice: Double)

    @Insert
    suspend fun insertHistory(history: PriceHistory)

    @Delete
    suspend fun deleteHistory(history: PriceHistory)

    @Query("SELECT * FROM gold_coins WHERE id = :id")
    fun getCoinById(id: Int): Flow<GoldCoin>

    @Query("SELECT * FROM gold_coins ORDER BY id DESC")
    fun getAllCoins(): Flow<List<GoldCoin>>

    @Query("SELECT * FROM price_history WHERE coinId = :coinId ORDER BY dateTimestamp DESC")
    fun getHistoryForCoin(coinId: Int): Flow<List<PriceHistory>>

    @Query("SELECT SUM(originalPrice * quantity) FROM gold_coins")
    fun getTotalInvestment(): Flow<Double?>

    @Query("SELECT * FROM price_history ORDER BY dateTimestamp ASC")
    fun getAllHistory(): Flow<List<PriceHistory>>
}