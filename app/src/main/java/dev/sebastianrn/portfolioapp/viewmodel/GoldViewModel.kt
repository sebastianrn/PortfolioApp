package dev.sebastianrn.portfolioapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import dev.sebastianrn.portfolioapp.BuildConfig
import dev.sebastianrn.portfolioapp.data.model.AssetType
import dev.sebastianrn.portfolioapp.data.model.BackupData
import dev.sebastianrn.portfolioapp.data.model.GoldAsset
import dev.sebastianrn.portfolioapp.data.model.PriceHistory
import dev.sebastianrn.portfolioapp.data.model.HistoricalStats
import dev.sebastianrn.portfolioapp.data.model.PortfolioSummary
import dev.sebastianrn.portfolioapp.data.UserPreferences
import dev.sebastianrn.portfolioapp.data.repository.GoldRepository
import dev.sebastianrn.portfolioapp.domain.usecase.CalculateHistoricalStatsUseCase
import dev.sebastianrn.portfolioapp.domain.usecase.CalculatePortfolioCurveUseCase
import dev.sebastianrn.portfolioapp.domain.usecase.CalculatePortfolioStatsUseCase
import dev.sebastianrn.portfolioapp.domain.usecase.UpdatePricesUseCase
import dev.sebastianrn.portfolioapp.util.Constants
import dev.sebastianrn.portfolioapp.util.mergeTimeIntoDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Main ViewModel for the Gold Portfolio app.
 *
 * Responsibilities:
 * - Expose UI state as StateFlow
 * - Delegate business logic to UseCases
 * - Handle one-time events via UiEvent Channel
 */
class GoldViewModel(
    private val repository: GoldRepository,
    private val prefs: UserPreferences,
    private val calculateStats: CalculatePortfolioStatsUseCase,
    private val calculateCurve: CalculatePortfolioCurveUseCase,
    private val calculateHistoricalStats: CalculateHistoricalStatsUseCase,
    private val updatePrices: UpdatePricesUseCase
) : ViewModel() {

    // One-time UI events channel
    private val _events = Channel<UiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    // Currency preference
    val currentCurrency: StateFlow<String> = prefs.currency
        .stateIn(viewModelScope, SharingStarted.Lazily, "CHF")

    // All assets from database
    val allAssets: StateFlow<List<GoldAsset>> = repository.allAssets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All price history from database
    val allHistory: StateFlow<List<PriceHistory>> = repository.allHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Portfolio statistics (delegated to UseCase)
    val portfolioStats: StateFlow<PortfolioSummary> = allAssets
        .map { assets -> calculateStats(assets) }
        .stateIn(viewModelScope, SharingStarted.Lazily, PortfolioSummary())

    // Timestamp of the most recent price history entry
    val lastUpdated: StateFlow<Long?> = allHistory
        .map { history -> history.maxByOrNull { it.dateTimestamp }?.dateTimestamp }
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    // Portfolio value curve over time (delegated to UseCase)
    val portfolioCurve: StateFlow<List<Pair<Long, Double>>> = allHistory
        .combine(allAssets) { history, assets ->
            calculateCurve(history, assets)
        }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Daily change calculation (delegated to UseCase)
    val portfolioChange: StateFlow<Pair<Double, Double>> = portfolioCurve
        .map { curve -> calculateCurve.calculateDailyChange(curve) }
        .stateIn(viewModelScope, SharingStarted.Lazily, 0.0 to 0.0)

    // Historical performance stats (delegated to UseCase)
    val historicalStats: StateFlow<HistoricalStats> = portfolioCurve
        .map { curve -> calculateHistoricalStats(curve) }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.Lazily, HistoricalStats())

    // --- Asset Operations ---

    fun insertAsset(
        name: String,
        type: AssetType,
        purchasePrice: Double,
        buyPrice: Double,
        qty: Int,
        weight: Double,
        philoroId: Int
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val asset = GoldAsset(
                name = name,
                type = type,
                purchasePrice = purchasePrice,
                currentSellPrice = purchasePrice,
                currentBuyPrice = buyPrice,
                quantity = qty,
                weightInGrams = weight,
                philoroId = philoroId
            )
            val id = repository.addAsset(asset)

            addDailyRate(
                assetId = id.toInt(),
                newSellPrice = purchasePrice,
                newBuyPrice = buyPrice,
                selectedDate = System.currentTimeMillis(),
                isManual = true
            )
        }
    }

    fun updateAsset(
        id: Int,
        name: String,
        type: AssetType,
        purchasePrice: Double,
        currentSellPrice: Double,
        quantity: Int,
        weight: Double,
        philoroId: Int
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val oldAsset = repository.getAssetById(id).first()

            val updatedAsset = oldAsset.copy(
                name = name,
                type = type,
                purchasePrice = purchasePrice,
                currentSellPrice = currentSellPrice,
                quantity = quantity,
                weightInGrams = weight,
                philoroId = philoroId
            )
            repository.updateAsset(updatedAsset)

            val firstHistory = repository.getEarliestHistory(id)
            if (firstHistory != null) {
                repository.updateHistory(firstHistory.copy(sellPrice = purchasePrice))
            }
        }
    }

    // --- Price Update Operations ---

    fun updateAllPricesFromApi() {
        viewModelScope.launch(Dispatchers.IO) {
            sendEvent(UiEvent.ShowToast("Fetching Spot Price..."))

            val currency = currentCurrency.value
            val apiKey = BuildConfig.GOLD_API_KEY

            updatePrices.fromSpotPriceApi(currency, apiKey, allAssets.value)
                .onSuccess { count ->
                    sendEvent(UiEvent.ShowToast("Updated $count assets in $currency."))
                }
                .onFailure { error ->
                    sendEvent(UiEvent.ShowError(error))
                }
        }
    }

    fun updatePricesFromScraper() {
        viewModelScope.launch(Dispatchers.IO) {
            sendEvent(UiEvent.ShowToast("Updating prices via API..."))

            updatePrices.fromPhiloroApi()
                .onSuccess { count ->
                    if (count > 0) {
                        sendEvent(UiEvent.ShowToast("Updated $count assets via API!"))
                    } else {
                        sendEvent(UiEvent.ShowToast("No Philoro assets to update."))
                    }
                }
                .onFailure { error ->
                    sendEvent(UiEvent.ShowError(error))
                }
        }
    }

    // --- History Operations ---

    fun addDailyRate(
        assetId: Int,
        newSellPrice: Double,
        newBuyPrice: Double,
        selectedDate: Long,
        isManual: Boolean
    ) {
        val currentTimestamp = System.currentTimeMillis()
        if (selectedDate > currentTimestamp + Constants.FUTURE_DATE_TOLERANCE_MS) return

        val finalTimestamp = mergeTimeIntoDate(selectedDate)

        viewModelScope.launch(Dispatchers.IO) {
            repository.addHistory(
                PriceHistory(
                    assetId = assetId,
                    dateTimestamp = finalTimestamp,
                    sellPrice = newSellPrice,
                    buyPrice = newBuyPrice,
                    isManual = isManual
                )
            )
        }
    }

    fun updateHistoryRecord(
        historyId: Int,
        assetId: Int,
        newSellPrice: Double,
        newBuyPrice: Double,
        newDate: Long,
        isManual: Boolean
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val updatedRecord = PriceHistory(
                historyId = historyId,
                assetId = assetId,
                dateTimestamp = newDate,
                sellPrice = newSellPrice,
                buyPrice = newBuyPrice,
                isManual = isManual
            )
            repository.updateHistory(updatedRecord)

            // Update original price if we edited the earliest record
            val firstHistory = repository.getEarliestHistory(assetId)
            if (firstHistory != null && firstHistory.historyId == historyId) {
                val asset = repository.getAssetById(assetId).first()
                repository.updateAsset(asset.copy(purchasePrice = newSellPrice))
            }

            refreshAssetCurrentPrice(assetId)
        }
    }

    // --- Backup Operations ---

    fun createBackupJson(): String {
        val backup = BackupData(assets = allAssets.value, history = allHistory.value)
        return Gson().toJson(backup)
    }

    suspend fun restoreFromBackupJson(jsonString: String): Boolean {
        return try {
            val backup = Gson().fromJson(jsonString, BackupData::class.java)
            if (backup.assets.isNotEmpty()) {
                repository.restoreDatabase(backup.assets, backup.history)
                for (asset in backup.assets) {
                    refreshAssetCurrentPrice(asset.id)
                }
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // --- Chart Data ---

    fun getChartPointsForAsset(assetId: Int): StateFlow<List<Pair<Long, Double>>> {
        return repository.getHistoryForAsset(assetId)
            .map { history ->
                if (history.isEmpty()) return@map emptyList()

                val rawPoints = history.reversed().map { it.dateTimestamp to it.sellPrice }

                // Downsample if too many points
                if (rawPoints.size > Constants.MAX_CHART_POINTS) {
                    val step = rawPoints.size / Constants.MAX_CHART_POINTS
                    rawPoints.filterIndexed { index, _ ->
                        index % step == 0 || index == rawPoints.lastIndex
                    }
                } else {
                    rawPoints
                }
            }
            .flowOn(Dispatchers.Default)
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    }

    // --- Data Access ---

    fun getAssetById(id: Int): Flow<GoldAsset> = repository.getAssetById(id)

    fun getHistoryForAsset(id: Int): Flow<List<PriceHistory>> = repository.getHistoryForAsset(id)

    // --- Private Helpers ---

    private suspend fun refreshAssetCurrentPrice(assetId: Int) {
        val latest = repository.getLatestHistory(assetId)
        if (latest != null) {
            repository.updateCurrentPrice(assetId, latest.sellPrice)
        }
    }

    private fun sendEvent(event: UiEvent) {
        viewModelScope.launch {
            _events.send(event)
        }
    }
}
