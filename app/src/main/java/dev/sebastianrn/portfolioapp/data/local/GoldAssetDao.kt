package dev.sebastianrn.portfolioapp.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import dev.sebastianrn.portfolioapp.data.model.GoldAsset
import dev.sebastianrn.portfolioapp.data.model.PriceHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface GoldAssetDao {
    @Insert
    suspend fun insert(asset: GoldAsset): Long

    @Update
    suspend fun update(asset: GoldAsset)

    @Delete
    suspend fun deleteAsset(asset: GoldAsset)

    @Query("UPDATE gold_assets SET currentSellPrice = :newPrice WHERE id = :assetId")
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

    @Query("SELECT SUM(purchasePrice * quantity) FROM gold_assets")
    fun getTotalInvestment(): Flow<Double?>

    @Query("SELECT * FROM price_history ORDER BY dateTimestamp ASC")
    fun getAllHistory(): Flow<List<PriceHistory>>

    @Query("SELECT * FROM gold_assets WHERE philoroId > 0")
    suspend fun getAssetsWithPhiloroId(): List<GoldAsset>

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

    @Query("UPDATE gold_assets SET purchasePrice = purchasePrice * :factor, currentSellPrice = currentSellPrice * :factor")
    suspend fun applyCurrencyFactorToAssets(factor: Double)

    @Query("UPDATE price_history SET sellPrice = sellPrice * :factor")
    suspend fun applyCurrencyFactorToHistory(factor: Double)

    @Query("UPDATE price_history SET sellPrice = sellPrice * :factor WHERE assetId = :assetId")
    suspend fun adjustHistoryForAsset(assetId: Int, factor: Double)

    @Update
    suspend fun updateHistory(history: PriceHistory)

    @Query("UPDATE gold_assets SET currentSellPrice = :sellPrice, currentBuyPrice = :buyPrice WHERE philoroId = :philoroId")
    suspend fun updatePricesByPhiloroId(philoroId: Int, sellPrice: Double, buyPrice: Double)

    @Query("SELECT * FROM gold_assets WHERE id = :id")
    suspend fun getAsset(id: Int): GoldAsset?

    @Query("SELECT * FROM price_history WHERE assetId = :assetId ORDER BY dateTimestamp ASC LIMIT 1")
    suspend fun getEarliestHistory(assetId: Int): PriceHistory?

    @Query("SELECT * FROM price_history WHERE assetId = :assetId ORDER BY dateTimestamp DESC LIMIT 1")
    suspend fun getLatestHistory(assetId: Int): PriceHistory?

    // Non-Flow queries for backup
    @Query("SELECT * FROM gold_assets ORDER BY id DESC")
    suspend fun getAllAssetsOnce(): List<GoldAsset>

    @Query("SELECT * FROM price_history ORDER BY dateTimestamp ASC")
    suspend fun getAllHistoryOnce(): List<PriceHistory>
}