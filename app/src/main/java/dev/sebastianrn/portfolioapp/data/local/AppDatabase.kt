package dev.sebastianrn.portfolioapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import dev.sebastianrn.portfolioapp.data.Converters
import dev.sebastianrn.portfolioapp.data.model.GoldAsset
import dev.sebastianrn.portfolioapp.data.model.PriceHistory

@Database(
    entities = [GoldAsset::class, PriceHistory::class],
    version = 6, // Version Bump
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun goldAssetDao(): GoldAssetDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gold_portfolio_db"
                )
                    .fallbackToDestructiveMigration(false)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}