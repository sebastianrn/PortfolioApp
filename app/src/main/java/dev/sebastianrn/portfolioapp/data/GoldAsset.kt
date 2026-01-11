package dev.sebastianrn.portfolioapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gold_assets")
data class GoldAsset(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val type: AssetType,
    val purchasePrice: Double,
    val currentSellPrice: Double,
    val currentBuyPrice: Double,
    val quantity: Int,
    val weightInGrams: Double,
    val philoroId: Int
) {
    val totalCurrentValue: Double
        get() = currentSellPrice * quantity

    val totalProfitOrLoss: Double
        get() = (currentSellPrice - purchasePrice) * quantity
}