package com.example.task_manager.util

import com.example.task_manager.util.GlobalKeys.DATE_FORMAT
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Locale

object GlobalMethods {

    fun formatDate(date: LocalDateTime): String {
        val utilDate = java.util.Date.from(date.atZone(ZoneId.systemDefault()).toInstant())
        val dateFormat = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
        return dateFormat.format(utilDate)
    }
}