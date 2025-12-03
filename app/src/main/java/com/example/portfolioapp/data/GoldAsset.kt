package com.example.portfolioapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gold_assets")
data class GoldAsset(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val type: AssetType,       // <--- NEW: COIN or BAR
    val originalPrice: Double,
    val currentPrice: Double,
    val quantity: Int,
    val weightInGrams: Double,
    val premiumPercent: Double
) {
    val totalCurrentValue: Double
        get() = currentPrice * quantity

    val totalProfitOrLoss: Double
        get() = (currentPrice - originalPrice) * quantity
}