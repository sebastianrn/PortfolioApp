package dev.sebastianrn.portfolioapp.ui.components.chart

enum class TimeRange(val label: String, val days: Int, val descriptor: String) {
    ONE_WEEK("1W", 7, "past week"),
    ONE_MONTH("1M", 30, "past month"),
    SIX_MONTHS("6M", 180, "past 6 months"),
    ONE_YEAR("1Y", 365, "past year"),
    ALL("ALL", Int.MAX_VALUE, "all time")
}
