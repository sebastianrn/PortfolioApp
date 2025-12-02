package com.example.portfolioapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gold_coins")
data class GoldCoin(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val originalPrice: Double,
    val currentPrice: Double,
    val quantity: Int
) {
    // Math automatically works (Int * Double = Double)
    val totalCurrentValue: Double
        get() = currentPrice * quantity

    val totalProfitOrLoss: Double
        get() = (currentPrice - originalPrice) * quantity
}