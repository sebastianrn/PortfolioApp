package dev.sebastianrn.portfolioapp.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "price_history",
    foreignKeys = [ForeignKey(
        entity = GoldAsset::class,
        parentColumns = ["id"],
        childColumns = ["assetId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["assetId"])]
)
data class PriceHistory(
    @PrimaryKey(autoGenerate = true) val historyId: Int = 0,
    val assetId: Int,
    val dateTimestamp: Long,
    val sellPrice: Double,
    val buyPrice: Double,
    val isManual: Boolean = true
)