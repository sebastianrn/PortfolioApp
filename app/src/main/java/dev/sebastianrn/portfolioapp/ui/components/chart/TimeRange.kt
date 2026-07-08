package dev.sebastianrn.portfolioapp.ui.components.chart

import androidx.annotation.StringRes
import dev.sebastianrn.portfolioapp.R

/**
 * [label] is a universal range symbol and intentionally not translated;
 * [descriptorRes] is the localized "past week" style description.
 */
enum class TimeRange(
    val label: String,
    val days: Int,
    @param:StringRes val descriptorRes: Int
) {
    ONE_WEEK("1W", 7, R.string.range_past_week),
    ONE_MONTH("1M", 30, R.string.range_past_month),
    SIX_MONTHS("6M", 180, R.string.range_past_6_months),
    ONE_YEAR("1Y", 365, R.string.range_past_year),
    ALL("ALL", Int.MAX_VALUE, R.string.all_time)
}
