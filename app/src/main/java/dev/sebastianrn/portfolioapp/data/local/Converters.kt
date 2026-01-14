package dev.sebastianrn.portfolioapp.data.local

import androidx.room.TypeConverter
import dev.sebastianrn.portfolioapp.data.model.AssetType

class Converters {
    @TypeConverter
    fun fromAssetType(value: AssetType): String = value.name

    @TypeConverter
    fun toAssetType(value: String): AssetType = AssetType.valueOf(value)
}