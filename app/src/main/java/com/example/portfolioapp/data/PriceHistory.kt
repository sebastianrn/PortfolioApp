package com.example.portfolioapp.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "price_history",
    foreignKeys = [ForeignKey(
        entity = GoldCoin::class,
        parentColumns = ["id"],
        childColumns = ["coinId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class PriceHistory(
    @PrimaryKey(autoGenerate = true) val historyId: Int = 0,
    val coinId: Int,
    val dateTimestamp: Long,
    val price: Double
)