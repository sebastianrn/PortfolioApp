package dev.sebastianrn.portfolioapp.backup

import com.google.gson.Gson
import dev.sebastianrn.portfolioapp.data.model.BackupData
import dev.sebastianrn.portfolioapp.data.model.GoldAsset
import dev.sebastianrn.portfolioapp.data.model.PriceHistory

/**
 * Centralized serialization/deserialization for backup data.
 * Avoids scattered Gson usage across ViewModels.
 */
object BackupSerializer {
    private val gson = Gson()

    fun serialize(assets: List<GoldAsset>, history: List<PriceHistory>): String {
        val backupData = BackupData(assets = assets, history = history)
        return gson.toJson(backupData)
    }

    fun deserialize(json: String): BackupData? {
        return try {
            gson.fromJson(json, BackupData::class.java)
        } catch (e: Exception) {
            null
        }
    }
}
