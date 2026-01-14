package dev.sebastianrn.portfolioapp.data.model

data class BackupData(
    val version: Int = 1,
    val timestamp: Long = System.currentTimeMillis(),
    val assets: List<GoldAsset>,
    val history: List<PriceHistory>
)