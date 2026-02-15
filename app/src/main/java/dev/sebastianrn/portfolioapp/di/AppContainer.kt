package dev.sebastianrn.portfolioapp.di

import android.content.Context
import dev.sebastianrn.portfolioapp.data.local.AppDatabase
import dev.sebastianrn.portfolioapp.data.remote.NetworkModule
import dev.sebastianrn.portfolioapp.data.remote.PhiloroScrapingService
import dev.sebastianrn.portfolioapp.data.repository.GoldRepository
import dev.sebastianrn.portfolioapp.domain.usecase.CalculatePortfolioCurveUseCase
import dev.sebastianrn.portfolioapp.domain.usecase.CalculatePortfolioStatsUseCase
import dev.sebastianrn.portfolioapp.domain.usecase.CalculateHistoricalStatsUseCase
import dev.sebastianrn.portfolioapp.domain.usecase.UpdatePricesUseCase

class AppContainer(context: Context) {

    private val database = AppDatabase.getDatabase(context)

    // Network layer
    private val apiService = NetworkModule.api
    private val scraper = PhiloroScrapingService()

    // Repository - single source of truth
    val repository: GoldRepository by lazy {
        GoldRepository(
            dao = database.goldAssetDao(),
            apiService = apiService,
            scraper = scraper
        )
    }

    // UseCases - business logic layer
    val calculatePortfolioStats: CalculatePortfolioStatsUseCase by lazy {
        CalculatePortfolioStatsUseCase()
    }

    val calculatePortfolioCurve: CalculatePortfolioCurveUseCase by lazy {
        CalculatePortfolioCurveUseCase()
    }

    val calculateHistoricalStats: CalculateHistoricalStatsUseCase by lazy {
        CalculateHistoricalStatsUseCase()
    }

    val updatePrices: UpdatePricesUseCase by lazy {
        UpdatePricesUseCase(
            repository = repository,
            scrapingService = scraper,
            goldApiService = apiService
        )
    }
}
