package dev.sebastianrn.portfolioapp.ui.components.chart

enum class TimeRange(val label: String, val days: Int) {
    ONE_WEEK("1W", 7),
    ONE_MONTH("1M", 30),
    SIX_MONTHS("6M", 180),
    ONE_YEAR("1Y", 365),
    ALL("ALL", Int.MAX_VALUE)
}
