package com.tcssol.expensetracker.Utils

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.tcssol.expensetracker.Model.Expenses
import com.tcssol.expensetracker.Model.PersonExp
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.time.LocalDate

object DataBaseExporter {

    private const val REQUEST_EXTERNAL_STORAGE = 1
    private val PERMISSIONS_STORAGE = arrayOf(
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    private fun exportData(context: Context, view: View?, outputString: String, fileName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                if (fileName.endsWith(".csv")) {
                    put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
                } else {
                    put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
                }
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }

            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
            if (uri != null) {
                try {
                    resolver.openOutputStream(uri)?.use { os ->
                        os.write(outputString.toByteArray())
                        os.flush()
                        
                        // Fallback to Toast if Compose view snackbar is not available
                        (context as? Activity)?.runOnUiThread {
                            Toast.makeText(context, "Success! Saved to Downloads", Toast.LENGTH_LONG).show()
                        }
                        return
                    }
                } catch (e: IOException) {
                    Log.e("DatabaseExporter", "Error writing to MediaStore", e)
                }
            }
            (context as? Activity)?.runOnUiThread {
                Toast.makeText(context, "Export failed!", Toast.LENGTH_LONG).show()
            }
        } else {
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
            ) {
                if (context is Activity) {
                    ActivityCompat.requestPermissions(context, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE)
                } else {
                    Log.e("DatabaseExporter", "Context is not an Activity; cannot request write permission.")
                }
                return
            }

            try {
                val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)
                val fileWriter = FileWriter(file)
                fileWriter.write(outputString)
                fileWriter.flush()
                fileWriter.close()
                
                (context as? Activity)?.runOnUiThread {
                    Toast.makeText(context, "Success! Saved to Downloads", Toast.LENGTH_LONG).show()
                }
            } catch (e: IOException) {
                Log.e("DatabaseExporter", "Error writing to file", e)
                (context as? Activity)?.runOnUiThread {
                    Toast.makeText(context, "Export failed!", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    @JvmStatic
    fun exportCSVExpenses(context: Context, view: View?, list: List<Expenses>?) {
        if (!list.isNullOrEmpty()) {
            val csvData = StringBuilder()
            csvData.append("Id,Category,Sub Category,Payment Mode,Date Created,Type,Amount,Note\n")
            for (data in list) {
                csvData.append(Expenses.toCsvFormat(data))
            }
            val date = LocalDate.now()
            val name = "table_1_${Converters.toString(date)}.csv"
            exportData(context, view, csvData.toString(), name)
        } else {
            Toast.makeText(context, "No Data to Export!", Toast.LENGTH_SHORT).show()
        }
    }

    @JvmStatic
    fun exportTxtExpenses(context: Context, view: View?, list: List<Expenses>?) {
        if (!list.isNullOrEmpty()) {
            val csvData = StringBuilder()
            csvData.append("Id\tCategory\tSub Category\tPayment Mode\tDate Created\tType\tAmount\tNote\n")
            for (data in list) {
                csvData.append(Expenses.toTxtFormat(data))
            }
            val date = LocalDate.now()
            val name = "table_1_${Converters.toString(date)}.txt"
            exportData(context, view, csvData.toString(), name)
        } else {
            Toast.makeText(context, "No Data to Export!", Toast.LENGTH_SHORT).show()
        }
    }

    @JvmStatic
    fun exportTxtPersonExpenses(context: Context, view: View?, list: List<PersonExp>?) {
        if (!list.isNullOrEmpty()) {
            val csvData = StringBuilder()
            csvData.append("Id\tDate Created\tName\tContact Number\tPayment Mode\tPending Date\tType\tAmount\tNote\n")
            for (data in list) {
                csvData.append(PersonExp.toTxtFormat(data))
            }
            val date = LocalDate.now()
            val name = "table_2_${Converters.toString(date)}.txt"
            exportData(context, view, csvData.toString(), name)
        } else {
            Toast.makeText(context, "No Data to Export!", Toast.LENGTH_SHORT).show()
        }
    }

    @JvmStatic
    fun exportCsvPersonExpenses(context: Context, view: View?, list: List<PersonExp>?) {
        if (!list.isNullOrEmpty()) {
            val csvData = StringBuilder()
            csvData.append("Id,Date Created,Name,Contact Number,Payment Mode,Pending Date,Type,Amount,Note\n")
            for (data in list) {
                csvData.append(PersonExp.toCsvFormat(data))
            }
            val date = LocalDate.now()
            val name = "table_2_${Converters.toString(date)}.csv"
            exportData(context, view, csvData.toString(), name)
        } else {
            Toast.makeText(context, "No Data to Export!", Toast.LENGTH_SHORT).show()
        }
    }
}
