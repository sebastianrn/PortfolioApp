package com.example.portfolioapp.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "price_history",
    foreignKeys = [ForeignKey(
        entity = GoldAsset::class, // Updated reference
        parentColumns = ["id"],
        childColumns = ["assetId"], // Renamed for clarity
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["assetId"])]
)
data class PriceHistory(
    @PrimaryKey(autoGenerate = true) val historyId: Int = 0,
    val assetId: Int, // Renamed from coinId
    val dateTimestamp: Long,
    val price: Double
)