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


    private static void exportData(Context context,View view, String OutputString,String fileName) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) context, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
            return;
        }

        // Get database instance
//        Toast.makeText(context, "Starting export", Toast.LENGTH_LONG).show();
        Log.d("Create_Database","Inside DatabaseExporter");
        Log.d("Create_Database", String.valueOf(OutputString!=null));
//        Log.d("Create_Database","Size List"+list.size());





            // Write CSV data to file
        try {
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);
            Log.d("Create_Database", "Creating File");
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(OutputString);
            fileWriter.flush();
            fileWriter.close();
            Log.d("Create_Database", "Success");
        } catch (IOException e) {
            e.printStackTrace();
        }
        Snackbar.make(view, "Success! Write Complete", Snackbar.LENGTH_SHORT).show();

    }
    public static void exportCSVExpenses(Context context,View view,List<Expenses> list){
        if (list != null && list.size() > 0) {
            // Convert data to CSV format
            StringBuilder csvData = new StringBuilder();
            csvData.append("Id,Category,Sub Category,Payment Mode,Date Created,Type,Amount\n");
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
            csvData.append("Id\tCategory\tSub Category\tPayment Mode\tDate Created\tType,Amount\n");
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
            csvData.append("Id\tDate Created\tName\tContact Number\tPayment Mode\tPending Date\tType\tAmount\n");
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
            csvData.append("Id,Date Created,Name,Contact Number,Payment Mode,Pending Date,Type,Amount\n");
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
