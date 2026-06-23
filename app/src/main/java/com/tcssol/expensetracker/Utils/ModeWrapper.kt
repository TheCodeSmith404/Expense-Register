package com.tcssol.expensetracker.Utils

import androidx.room.ColumnInfo

data class ModeWrapper(
    @ColumnInfo(name = "Mode")
    var name: String,
    
    @ColumnInfo(name = "Count")
    var perentage: Double
)
