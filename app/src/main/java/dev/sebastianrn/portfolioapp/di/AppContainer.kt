package dev.sebastianrn.portfolioapp.di

import android.content.Context
import dev.sebastianrn.portfolioapp.data.AppDatabase
import dev.sebastianrn.portfolioapp.data.NetworkModule
import dev.sebastianrn.portfolioapp.data.PhiloroScrapingService
import dev.sebastianrn.portfolioapp.data.repository.GoldRepository

class AppContainer(context: Context) {

    private val database = AppDatabase.getDatabase(context)

    // We access the properties directly since you defined them as objects/classes
    private val apiService = NetworkModule.api
    private val scraper = PhiloroScrapingService()

    // 'by lazy' ensures we only create it when needed, and only once.
    val repository: GoldRepository by lazy {
        GoldRepository(
            dao = database.goldAssetDao(),
            apiService = apiService,
            scraper = scraper
        )
    }
}