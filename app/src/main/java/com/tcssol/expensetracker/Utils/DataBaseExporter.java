package com.tcssol.expensetracker.Utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.google.android.material.snackbar.Snackbar;
import com.tcssol.expensetracker.Model.Expenses;
import com.tcssol.expensetracker.Model.PersonExp;
import com.tcssol.expensetracker.R;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public class DataBaseExporter {

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private static void exportData(Context context, View view, String outputString, String fileName) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            android.content.ContentValues values = new android.content.ContentValues();
            values.put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, fileName);
            if (fileName.endsWith(".csv")) {
                values.put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "text/csv");
            } else {
                values.put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "text/plain");
            }
            values.put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

            android.content.ContentResolver resolver = context.getContentResolver();
            android.net.Uri uri = resolver.insert(android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
            if (uri != null) {
                try (java.io.OutputStream os = resolver.openOutputStream(uri)) {
                    if (os != null) {
                        os.write(outputString.getBytes());
                        os.flush();
                        Snackbar.make(view, "Success! Saved to Downloads", Snackbar.LENGTH_LONG).show();
                        return;
                    }
                } catch (IOException e) {
                    Log.e("DatabaseExporter", "Error writing to MediaStore", e);
                }
            }
            Snackbar.make(view, "Export failed!", Snackbar.LENGTH_LONG).show();
        } else {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                if (context instanceof Activity) {
                    ActivityCompat.requestPermissions((Activity) context, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
                } else {
                    Log.e("DatabaseExporter", "Context is not an Activity; cannot request write permission.");
                }
                return;
            }

            try {
                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);
                FileWriter fileWriter = new FileWriter(file);
                fileWriter.write(outputString);
                fileWriter.flush();
                fileWriter.close();
                Snackbar.make(view, "Success! Saved to Downloads", Snackbar.LENGTH_LONG).show();
            } catch (IOException e) {
                Log.e("DatabaseExporter", "Error writing to file", e);
                Snackbar.make(view, "Export failed!", Snackbar.LENGTH_LONG).show();
            }
        }
    }
    public static void exportCSVExpenses(Context context,View view,List<Expenses> list){
        if (list != null && list.size() > 0) {
            // Convert data to CSV format
            StringBuilder csvData = new StringBuilder();
            csvData.append("Id,Category,Sub Category,Payment Mode,Date Created,Type,Amount,Note\n");
            Log.d("Create_Database", "Building String");
            for (Expenses data : list) {
                csvData.append(Expenses.toCsvFormat(data));
            }
            LocalDate date= LocalDate.now();
            String name="table_1_"+Converters.toString(date)+".csv";
            exportData(context,view,csvData.toString(),name);
        }else{
            Snackbar.make(view, "No Data to Export!", Snackbar.LENGTH_SHORT).show();
        }
    }
    public static void exportTxtExpenses(Context context,View view,List<Expenses> list){
        if (list != null && list.size() > 0) {
            // Convert data to CSV format
            StringBuilder csvData = new StringBuilder();
            csvData.append("Id\tCategory\tSub Category\tPayment Mode\tDate Created\tType\tAmount\tNote\n");
//            Log.d("Create_Database", "Building String");
            for (Expenses data : list) {
                csvData.append(Expenses.toTxtFormat(data));
            }
            LocalDate date= LocalDate.now();
            String name="table_1_"+Converters.toString(date)+".txt";
            exportData(context,view,csvData.toString(),name);
        }else{
            Snackbar.make(view, "No Data to Export!", Snackbar.LENGTH_SHORT).show();
        }
    }
    public static void exportTxtPersonExpenses(Context context,View view, List<PersonExp> list){
        if (list != null && list.size() > 0) {
            // Convert data to CSV format
            StringBuilder csvData = new StringBuilder();
            csvData.append("Id\tDate Created\tName\tContact Number\tPayment Mode\tPending Date\tType\tAmount\tNote\n");
//            Log.d("Create_Database", "Building String");
            for (PersonExp data : list) {
                csvData.append(PersonExp.toTxtFormat(data));
            }
            LocalDate date= LocalDate.now();
            String name="table_2_"+Converters.toString(date)+".txt";
            exportData(context,view,csvData.toString(),name);
        }else{
            Snackbar.make(view, "No Data to Export!", Snackbar.LENGTH_SHORT).show();
        }
    }
    public static void exportCsvPersonExpenses(Context context,View view, List<PersonExp> list){
        if (list != null && list.size() > 0) {
            // Convert data to CSV format
            StringBuilder csvData = new StringBuilder();
            csvData.append("Id,Date Created,Name,Contact Number,Payment Mode,Pending Date,Type,Amount,Note\n");
            Log.d("Create_Database", "Building String");
            for (PersonExp data : list) {
                csvData.append(PersonExp.toCsvFormat(data));
            }
            LocalDate date= LocalDate.now();
            String name="table_2_"+Converters.toString(date)+".csv";
            exportData(context,view,csvData.toString(),name);
        }else{
            Snackbar.make(view, "No Data to Export!", Snackbar.LENGTH_SHORT).show();
        }

    }
}
