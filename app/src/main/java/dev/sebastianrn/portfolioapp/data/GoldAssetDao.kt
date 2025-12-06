package dev.sebastianrn.portfolioapp.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface GoldAssetDao {
    @Insert
    suspend fun insert(asset: GoldAsset): Long

    @Update
    suspend fun update(asset: GoldAsset)

    @Delete
    suspend fun deleteAsset(asset: GoldAsset)

    @Query("UPDATE gold_assets SET currentPrice = :newPrice WHERE id = :assetId")
    suspend fun updateCurrentPrice(assetId: Int, newPrice: Double)

    @Insert
    suspend fun insertHistory(history: PriceHistory)

    @Delete
    suspend fun deleteHistory(history: PriceHistory)

    @Query("SELECT * FROM gold_assets WHERE id = :id")
    fun getAssetById(id: Int): Flow<GoldAsset>

    @Query("SELECT * FROM gold_assets ORDER BY id DESC")
    fun getAllAssets(): Flow<List<GoldAsset>>

    @Query("SELECT * FROM price_history WHERE assetId = :assetId ORDER BY dateTimestamp DESC")
    fun getHistoryForAsset(assetId: Int): Flow<List<PriceHistory>>

    @Query("SELECT SUM(originalPrice * quantity) FROM gold_assets")
    fun getTotalInvestment(): Flow<Double?>

    @Query("SELECT * FROM price_history ORDER BY dateTimestamp ASC")
    fun getAllHistory(): Flow<List<PriceHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllAssets(assets: List<GoldAsset>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllHistory(history: List<PriceHistory>)

    @Query("DELETE FROM gold_assets")
    suspend fun clearAssets()

    @Query("DELETE FROM price_history")
    suspend fun clearHistory()

    @Transaction
    suspend fun restoreDatabase(assets: List<GoldAsset>, history: List<PriceHistory>) {
        clearHistory()
        clearAssets()
        insertAllAssets(assets)
        insertAllHistory(history)
    }

    @Query("UPDATE gold_assets SET originalPrice = originalPrice * :factor, currentPrice = currentPrice * :factor")
    suspend fun applyCurrencyFactorToAssets(factor: Double)

    @Query("UPDATE price_history SET price = price * :factor")
    suspend fun applyCurrencyFactorToHistory(factor: Double)

    @Query("UPDATE price_history SET price = price * :factor WHERE assetId = :assetId")
    suspend fun adjustHistoryForAsset(assetId: Int, factor: Double)

    @Update
    suspend fun updateHistory(history: PriceHistory)

    @Query("SELECT * FROM gold_assets WHERE id = :id")
    suspend fun getAsset(id: Int): GoldAsset?

    @Query("SELECT * FROM price_history WHERE assetId = :assetId ORDER BY dateTimestamp ASC LIMIT 1")
    suspend fun getEarliestHistory(assetId: Int): PriceHistory?

    // --- NEW METHOD ---
    @Query("SELECT * FROM price_history WHERE assetId = :assetId ORDER BY dateTimestamp DESC LIMIT 1")
    suspend fun getLatestHistory(assetId: Int): PriceHistory?
}