package dev.sebastianrn.portfolioapp.data.model

data class HistoricalStats(
    val allTimeHigh: Double = 0.0,
    val allTimeHighDate: Long = 0L,
    val allTimeLow: Double = 0.0,
    val allTimeLowDate: Long = 0L,
    val bestDayAbsolute: Double = 0.0,
    val bestDayPercent: Double = 0.0,
    val bestDayDate: Long = 0L,
    val worstDayAbsolute: Double = 0.0,
    val worstDayPercent: Double = 0.0,
    val worstDayDate: Long = 0L,
    val maxDrawdownPercent: Double = 0.0,
    val totalReturnPercent: Double = 0.0
)
