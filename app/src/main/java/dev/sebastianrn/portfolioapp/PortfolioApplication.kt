package dev.sebastianrn.portfolioapp

import android.app.Application
import dev.sebastianrn.portfolioapp.di.AppContainer

class PortfolioApplication : Application() {

    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}