package com.tcssol.expensetracker.Model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.tcssol.expensetracker.Utils.Converters
import java.time.LocalDate

@Entity(tableName = "expenses_table")
data class Expenses(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    
    @ColumnInfo(name = "date_created")
    var dateCreated: LocalDate,
    
    var category: String,
    
    @ColumnInfo(name = "sub_category")
    var subCategory: String,
    
    var mode: String,
    
    var amount: Double,
    
    var type: Boolean,
    
    @ColumnInfo(name = "note")
    var note: String? = null
) {
    // Secondary constructors for backward compatibility & easy creation
    @Ignore
    constructor(category: String, subCategory: String) : this(
        dateCreated = LocalDate.now(),
        category = category,
        subCategory = subCategory,
        mode = "Cash",
        amount = 0.0,
        type = false
    )

    @Ignore
    constructor(
        dateCreated: LocalDate,
        category: String,
        subCategory: String,
        mode: String,
        amount: Double,
        type: Boolean
    ) : this(
        id = 0,
        dateCreated = dateCreated,
        category = category,
        subCategory = subCategory,
        mode = mode,
        amount = amount,
        type = type,
        note = null
    )

    companion object {
        @JvmStatic
        fun toCsvFormat(expenses: Expenses): String {
            val typeString = expenses.type.toString()
            val noteStr = expenses.note?.replace(",", ";") ?: ""
            return String.format(
                "%d,%s,%s,%s,%s,%s,%s,%s\n",
                expenses.id,
                expenses.category,
                expenses.subCategory,
                expenses.mode,
                Converters.toString(expenses.dateCreated),
                typeString,
                expenses.amount,
                noteStr
            )
        }

        @JvmStatic
        fun toTxtFormat(expenses: Expenses): String {
            val typeString = expenses.type.toString()
            val noteStr = expenses.note ?: ""
            return String.format(
                "%d\t%s\t%s\t%s\t%s\t%s\t%s\t%s\n",
                expenses.id,
                expenses.category,
                expenses.subCategory,
                expenses.mode,
                Converters.toString(expenses.dateCreated),
                typeString,
                expenses.amount,
                noteStr
            )
        }
    }
}
