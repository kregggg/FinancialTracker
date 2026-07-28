package com.example.financialtracker.database;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

import com.example.financialtracker.ref.Transaction;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class DataBackupManager {

    public static JSONObject buildExportJson(Context context) throws JSONException {
        SettingsManager settingsManager = new SettingsManager(context);

        JSONObject settingsJson = new JSONObject();
        settingsJson.put("username", settingsManager.getUsername());
        settingsJson.put("allowanceType", settingsManager.getAllowanceType());
        settingsJson.put("daysWithClasses", settingsManager.getDaysWithClasses());
        settingsJson.put("allowanceAmount", settingsManager.getAllowanceAmount());
        settingsJson.put("startingBalance", settingsManager.getStartingBalance());
        settingsJson.put("currentBalance", settingsManager.getCurrentBalance());
        settingsJson.put("darkMode", settingsManager.isDarkModeEnabled());

        List<Transaction> transactions = DataAccess.getInstance(context).transactionDao().getAllTransactions();
        JSONArray transactionsJson = new JSONArray();
        for (Transaction t : transactions) {
            JSONObject row = new JSONObject();
            row.put("description", t.getDescription());
            row.put("amount", t.getAmount());
            row.put("category", t.getCategory());
            row.put("timestamp", t.getTimestamp());
            row.put("transactionType", t.getTransactionType());
            transactionsJson.put(row);
        }

        JSONObject root = new JSONObject();
        root.put("settings", settingsJson);
        root.put("transactions", transactionsJson);
        return root;
    }

    public static void writeToUri(Context context, Uri uri, JSONObject data) throws IOException, JSONException {
        ContentResolver resolver = context.getContentResolver();
        try (OutputStream out = resolver.openOutputStream(uri)) {
            if (out == null) throw new IOException("Could not open output stream for " + uri);
            out.write(data.toString(2).getBytes(StandardCharsets.UTF_8));
        }
    }

    public static JSONObject readFromUri(Context context, Uri uri) throws IOException, JSONException {
        StringBuilder builder = new StringBuilder();
        try (InputStream in = context.getContentResolver().openInputStream(uri)) {
            if (in == null) throw new IOException("Could not open input stream for " + uri);
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            String line;
            while ((line = reader.readLine()) != null) builder.append(line);
        }
        return new JSONObject(builder.toString());
    }

    /** Overwrites settings and REPLACES the entire transactions table. A restore, not a merge. */
    public static void applyImportedData(Context context, JSONObject root) throws JSONException {
        JSONObject settingsJson = root.getJSONObject("settings");
        new SettingsManager(context).restoreSettings(
                settingsJson.optString("username", ""),
                settingsJson.optString("allowanceType", "Weekly"),
                settingsJson.optInt("daysWithClasses", 5),
                settingsJson.optDouble("allowanceAmount", 0.0),
                settingsJson.optDouble("startingBalance", 0.0),
                settingsJson.optDouble("currentBalance", 0.0),
                settingsJson.optBoolean("darkMode", false)
        );

        JSONArray transactionsJson = root.optJSONArray("transactions");
        List<Transaction> imported = new ArrayList<>();
        if (transactionsJson != null) {
            for (int i = 0; i < transactionsJson.length(); i++) {
                JSONObject row = transactionsJson.getJSONObject(i);
                imported.add(new Transaction(
                        row.getString("description"),
                        row.getDouble("amount"),
                        row.getString("category"),
                        row.getLong("timestamp"),
                        row.getString("transactionType")
                ));
            }
        }

        DataAccess.getInstance(context).transactionDao().deleteAllTransactions();
        if (!imported.isEmpty()) {
            DataAccess.getInstance(context).transactionDao().insertAll(imported);
        }
    }
}