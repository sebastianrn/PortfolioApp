package com.example.portfolioapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gold_coins")
data class GoldCoin(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val originalPrice: Double, // Price PER COIN
    val currentPrice: Double,  // Price PER COIN
    val quantity: Double       // Number of coins
) {
    // Helper properties (computed on the fly)
    val totalCurrentValue: Double
        get() = currentPrice * quantity

    val totalProfitOrLoss: Double
        get() = (currentPrice - originalPrice) * quantity
}