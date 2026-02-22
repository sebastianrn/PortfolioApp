package dev.sebastianrn.portfolioapp.data.repository

import dev.sebastianrn.portfolioapp.data.remote.GoldApiService
import dev.sebastianrn.portfolioapp.data.model.GoldAsset
import dev.sebastianrn.portfolioapp.data.local.GoldAssetDao
import dev.sebastianrn.portfolioapp.data.remote.PhiloroScrapingService
import dev.sebastianrn.portfolioapp.data.model.PriceHistory
import kotlinx.coroutines.flow.Flow

class GoldRepository(
    private val dao: GoldAssetDao,
    private val apiService: GoldApiService,
    private val scraper: PhiloroScrapingService
) {

    // --- Database Access (Flows) ---
    // These update automatically when the DB changes
    val allAssets: Flow<List<GoldAsset>> = dao.getAllAssets()
    val allHistory: Flow<List<PriceHistory>> = dao.getAllHistory()

    fun getAssetById(id: Int): Flow<GoldAsset> = dao.getAssetById(id)
    fun getHistoryForAsset(id: Int): Flow<List<PriceHistory>> = dao.getHistoryForAsset(id)

    // --- Database Operations (Suspend) ---
    suspend fun addAsset(asset: GoldAsset): Long = dao.insert(asset)
    suspend fun updateAsset(asset: GoldAsset) = dao.update(asset)
    suspend fun getAssetWithPhiloroId(): List<GoldAsset> = dao.getAssetsWithPhiloroId()
    suspend fun updatePricesByPhiloroId(philoroId: Int, sellPrice: Double, buyPrice: Double) = dao.updatePricesByPhiloroId(philoroId,sellPrice, buyPrice)
    suspend fun deleteAsset(asset: GoldAsset) = dao.deleteAsset(asset)
    suspend fun getEarliestHistory(assetId: Int): PriceHistory? = dao.getEarliestHistory(assetId)
    suspend fun getLatestHistory(assetId: Int): PriceHistory? = dao.getLatestHistory(assetId)
    suspend fun updateHistory(history: PriceHistory) = dao.updateHistory(history)
    suspend fun insertHistory(history: PriceHistory) = dao.insertHistory(history)
    suspend fun updateCurrentPrice(assetId: Int, newPrice: Double) = dao.updateCurrentPrice(assetId, newPrice)
    suspend fun restoreDatabase(assets: List<GoldAsset>, history: List<PriceHistory>)  = dao.restoreDatabase(assets, history)


    // Non-Flow queries for backup operations
    suspend fun getAllAssetsOnce(): List<GoldAsset> = dao.getAllAssetsOnce()
    suspend fun getAllHistoryOnce(): List<PriceHistory> = dao.getAllHistoryOnce()

    suspend fun addHistory(history: PriceHistory) {
        dao.insertHistory(history)
        // We can enforce logic here: updating the asset's current price whenever history is added
        dao.updateCurrentPrice(history.assetId, history.sellPrice)
    }
}