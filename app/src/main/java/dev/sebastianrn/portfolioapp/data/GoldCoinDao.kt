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

    // NEW: Bulk inserts for restoring backup
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllAssets(assets: List<GoldAsset>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllHistory(history: List<PriceHistory>)

    // NEW: Nuke tables before restore
    @Query("DELETE FROM gold_assets")
    suspend fun clearAssets()

    @Query("DELETE FROM price_history")
    suspend fun clearHistory()

    // NEW: Transaction to do it all atomically
    @Transaction
    suspend fun restoreDatabase(assets: List<GoldAsset>, history: List<PriceHistory>) {
        clearHistory() // History depends on Assets, delete first (or Cascade handles it)
        clearAssets()
        insertAllAssets(assets)
        insertAllHistory(history)
    }
}