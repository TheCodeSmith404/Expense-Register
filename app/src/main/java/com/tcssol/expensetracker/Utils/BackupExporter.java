package com.tcssol.expensetracker.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.tcssol.expensetracker.Model.CategoryConfig;
import com.tcssol.expensetracker.Model.EarningsHistory;
import com.tcssol.expensetracker.Model.Expenses;
import com.tcssol.expensetracker.Model.PersonExp;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class BackupExporter {

    private static final String PREF_NAME = "TimeViewPrefs";

    public interface ProgressListener {
        void onProgress(int progress, String step);
    }

    public static void exportBackup(
            Context context,
            List<Expenses> expenses,
            List<CategoryConfig> configs,
            List<EarningsHistory> histories,
            List<PersonExp> personExps,
            OutputStream outputStream,
            ProgressListener listener
    ) throws Exception {
        if (listener != null) listener.onProgress(10, "Serializing Transaction Records...");

        JSONObject backupJson = new JSONObject();
        backupJson.put("version", 1);

        // 1. Serialize Expenses
        JSONArray expensesArray = new JSONArray();
        for (Expenses exp : expenses) {
            JSONObject expJson = new JSONObject();
            expJson.put("amount", exp.getAmount());
            expJson.put("category", exp.getCategory());
            expJson.put("subCategory", exp.getSubCategory());
            expJson.put("mode", exp.getMode());
            expJson.put("type", exp.isType());
            expJson.put("note", exp.getNote());
            if (exp.getDateCreated() != null) {
                expJson.put("dateCreated", exp.getDateCreated().toString());
            }
            expensesArray.put(expJson);
        }
        backupJson.put("expenses", expensesArray);

        if (listener != null) listener.onProgress(35, "Serializing Category Budgets...");

        // 2. Serialize CategoryConfig
        JSONArray configsArray = new JSONArray();
        for (CategoryConfig cfg : configs) {
            JSONObject cfgJson = new JSONObject();
            cfgJson.put("categoryName", cfg.getCategoryName());
            cfgJson.put("isFixed", cfg.isFixed());
            cfgJson.put("budget", cfg.getBudget());
            configsArray.put(cfgJson);
        }
        backupJson.put("categoryConfigs", configsArray);

        if (listener != null) listener.onProgress(60, "Serializing Hardwork Progression Logs...");

        // 3. Serialize EarningsHistory
        JSONArray historiesArray = new JSONArray();
        for (EarningsHistory hist : histories) {
            JSONObject histJson = new JSONObject();
            histJson.put("timestamp", hist.getTimestamp());
            histJson.put("monthlyEarnings", hist.getMonthlyEarnings());
            histJson.put("daysWorked", hist.getDaysWorked());
            histJson.put("hoursWorked", hist.getHoursWorked());
            histJson.put("hourlyRate", hist.getHourlyRate());
            historiesArray.put(histJson);
        }
        backupJson.put("earningsHistory", historiesArray);

        if (listener != null) listener.onProgress(80, "Serializing Peer-to-Peer Loans...");

        // 4. Serialize PersonExp
        JSONArray personArray = new JSONArray();
        for (PersonExp pExp : personExps) {
            JSONObject pJson = new JSONObject();
            pJson.put("amount", pExp.getAmount());
            pJson.put("type", pExp.getType());
            pJson.put("name", pExp.getName());
            pJson.put("contactNumber", pExp.getContactNumber());
            pJson.put("mode", pExp.getMode());
            pJson.put("hasDate", pExp.getHasDate());
            pJson.put("note", pExp.getNote());
            if (pExp.getDateCreated() != null) {
                pJson.put("dateCreated", pExp.getDateCreated().toString());
            }
            if (pExp.getPendingDate() != null) {
                pJson.put("pendingDate", pExp.getPendingDate().toString());
            }
            personArray.put(pJson);
        }
        backupJson.put("personExpenses", personArray);

        if (listener != null) listener.onProgress(90, "Saving Preference Settings...");

        // 5. Serialize SharedPreferences
        JSONObject prefsJson = new JSONObject();
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        Map<String, ?> allEntries = prefs.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            prefsJson.put(entry.getKey(), entry.getValue());
        }
        backupJson.put("preferences", prefsJson);

        if (listener != null) listener.onProgress(95, "Compiling Backup Package...");

        // Zip compress into outputStream
        ZipOutputStream zos = new ZipOutputStream(outputStream);
        ZipEntry ze = new ZipEntry("backup.json");
        zos.putNextEntry(ze);

        byte[] jsonBytes = backupJson.toString().getBytes("UTF-8");
        zos.write(jsonBytes);
        zos.closeEntry();
        zos.finish();
        zos.close();

        if (listener != null) listener.onProgress(100, "Backup Successful!");
    }

    public static class BackupData {
        public int version;
        public List<Expenses> expenses;
        public List<CategoryConfig> categoryConfigs;
        public List<EarningsHistory> earningsHistory;
        public List<PersonExp> personExpenses;
        public JSONObject preferences;
    }

    public static BackupData importBackup(InputStream inputStream, ProgressListener listener) throws Exception {
        if (listener != null) listener.onProgress(10, "Extracting Backup Archive...");

        ZipInputStream zis = new ZipInputStream(inputStream);
        ZipEntry ze = zis.getNextEntry();
        if (ze == null || !ze.getName().equals("backup.json")) {
            throw new FileNotFoundException("Invalid backup package: backup.json not found");
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int count;
        while ((count = zis.read(buffer)) != -1) {
            baos.write(buffer, 0, count);
        }
        zis.closeEntry();
        zis.close();

        if (listener != null) listener.onProgress(40, "Parsing Backup Records...");

        String jsonStr = baos.toString("UTF-8");
        JSONObject backupJson = new JSONObject(jsonStr);

        BackupData data = new BackupData();
        data.version = backupJson.optInt("version", 1);

        // 1. Deserialize Expenses
        data.expenses = new java.util.ArrayList<>();
        JSONArray expensesArray = backupJson.optJSONArray("expenses");
        if (expensesArray != null) {
            for (int i = 0; i < expensesArray.length(); i++) {
                JSONObject expJson = expensesArray.getJSONObject(i);
                LocalDate date = null;
                if (expJson.has("dateCreated")) {
                    date = LocalDate.parse(expJson.getString("dateCreated"));
                }
                Expenses exp = new Expenses(
                        date,
                        expJson.getString("category"),
                        expJson.getString("subCategory"),
                        expJson.getString("mode"),
                        expJson.getDouble("amount"),
                        expJson.getBoolean("type")
                );
                if (expJson.has("note")) {
                    exp.setNote(expJson.optString("note", null));
                }
                data.expenses.add(exp);
            }
        }

        if (listener != null) listener.onProgress(60, "Parsing Categories and Budgets...");

        // 2. Deserialize CategoryConfig
        data.categoryConfigs = new java.util.ArrayList<>();
        JSONArray configsArray = backupJson.optJSONArray("categoryConfigs");
        if (configsArray != null) {
            for (int i = 0; i < configsArray.length(); i++) {
                JSONObject cfgJson = configsArray.getJSONObject(i);
                CategoryConfig cfg = new CategoryConfig(
                        cfgJson.getString("categoryName"),
                        cfgJson.getBoolean("isFixed")
                );
                cfg.setBudget(cfgJson.optDouble("budget", 0.0));
                data.categoryConfigs.add(cfg);
            }
        }

        // 3. Deserialize EarningsHistory
        data.earningsHistory = new java.util.ArrayList<>();
        JSONArray historiesArray = backupJson.optJSONArray("earningsHistory");
        if (historiesArray != null) {
            for (int i = 0; i < historiesArray.length(); i++) {
                JSONObject histJson = historiesArray.getJSONObject(i);
                EarningsHistory hist = new EarningsHistory(
                        histJson.getLong("timestamp"),
                        histJson.getDouble("monthlyEarnings"),
                        histJson.getInt("daysWorked"),
                        histJson.getDouble("hoursWorked"),
                        histJson.getDouble("hourlyRate")
                );
                data.earningsHistory.add(hist);
            }
        }

        if (listener != null) listener.onProgress(85, "Parsing P2P Loan Accounts...");

        // 4. Deserialize PersonExp
        data.personExpenses = new java.util.ArrayList<>();
        JSONArray personArray = backupJson.optJSONArray("personExpenses");
        if (personArray != null) {
            for (int i = 0; i < personArray.length(); i++) {
                JSONObject pJson = personArray.getJSONObject(i);
                LocalDate date = pJson.has("dateCreated") ? LocalDate.parse(pJson.getString("dateCreated")) : null;
                LocalDate pendingDate = pJson.has("pendingDate") ? LocalDate.parse(pJson.getString("pendingDate")) : null;

                PersonExp pExp = new PersonExp(
                        date,
                        pJson.getBoolean("type"),
                        pJson.getString("name"),
                        pJson.optString("contactNumber", ""),
                        pJson.optString("mode", "Cash"),
                        pJson.optBoolean("hasDate", false),
                        pendingDate,
                        pJson.getDouble("amount"),
                        pJson.optString("note", "")
                );
                data.personExpenses.add(pExp);
            }
        }

        // 5. Deserialize preferences
        data.preferences = backupJson.optJSONObject("preferences");

        if (listener != null) listener.onProgress(100, "Extraction Complete!");
        return data;
    }
}
