package com.tcssol.expensetracker.Model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.tcssol.expensetracker.Utils.Converters
import java.time.LocalDate

@Entity(tableName = "person_expenses")
data class PersonExp(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    
    @ColumnInfo(name = "date_created")
    var dateCreated: LocalDate,
    
    var type: Boolean,
    
    var name: String,
    
    var amount: Double,
    
    @ColumnInfo(name = "contact_number")
    var contactNumber: String,
    
    var mode: String,
    
    @ColumnInfo(name = "has_date")
    var hasDate: Boolean,
    
    @ColumnInfo(name = "pending_date")
    var pendingDate: LocalDate?,
    
    var note: String? = null
) {
    companion object {
        @JvmStatic
        fun toCsvFormat(data: PersonExp): String {
            val typeString = data.type.toString()
            val amountString = data.amount.toString()
            val noteStr = data.note?.replace(",", ";") ?: ""
            return String.format(
                "%d,%s,%s,%s,%s,%s,%s,%s,%s\n",
                data.id,
                Converters.toString(data.dateCreated),
                data.name,
                data.contactNumber,
                data.mode,
                Converters.toString(data.pendingDate),
                typeString,
                amountString,
                noteStr
            )
        }

        @JvmStatic
        fun toTxtFormat(data: PersonExp): String {
            val typeString = data.type.toString()
            val amountString = data.amount.toString()
            val noteStr = data.note ?: ""
            return String.format(
                "%d\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\n",
                data.id,
                Converters.toString(data.dateCreated),
                data.name,
                data.contactNumber,
                data.mode,
                Converters.toString(data.pendingDate),
                typeString,
                amountString,
                noteStr
            )
        }
    }
}
