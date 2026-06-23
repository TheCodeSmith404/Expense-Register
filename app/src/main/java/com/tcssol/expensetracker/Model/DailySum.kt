package com.tcssol.expensetracker.Model

import java.time.LocalDate

data class DailySum(
    val date: LocalDate,
    val totalSpent: Double,
    val totalEarned: Double
)
