package dev.sebastianrn.portfolioapp.util

import java.util.Calendar

fun mergeTimeIntoDate(dateMillis: Long): Long {
    val calendarDate = Calendar.getInstance().apply { timeInMillis = dateMillis }
    val calendarNow = Calendar.getInstance()
    calendarDate.set(Calendar.HOUR_OF_DAY, calendarNow.get(Calendar.HOUR_OF_DAY))
    calendarDate.set(Calendar.MINUTE, calendarNow.get(Calendar.MINUTE))
    calendarDate.set(Calendar.SECOND, calendarNow.get(Calendar.SECOND))
    return calendarDate.timeInMillis
}
