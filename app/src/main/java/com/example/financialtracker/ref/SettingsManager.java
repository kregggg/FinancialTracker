package com.example.financialtracker.ref;

import android.content.Context;
import android.content.SharedPreferences;

public class SettingsManager {

    private final SharedPreferences prefs;

    // Unique name for the private file stored on the device
    private static final String PREFS_NAME = "user_settings";

    // Keys for storing data
    private static final String KEY_USERNAME = "key_username";
    private static final String KEY_ALLOWANCE_TYPE = "key_allowance_type";
    private static final String KEY_DAYS_WITH_CLASSES = "key_days_classes";
    private static final String KEY_ALLOWANCE_AMOUNT = "key_allowance_amount";
    private static final String KEY_STARTING_BALANCE = "key_starting_balance";
    private static final String KEY_DARK_MODE = "key_dark_mode";

    // NEW KEY: Keeps track of the live wallet balance as expenses get logged
    private static final String KEY_CURRENT_BALANCE = "key_current_balance";

    // Dynamic Quick Action Key Prefixes
    private static final String PREFIX_QA_DESC = "key_qa_desc_";
    private static final String PREFIX_QA_AMT = "key_qa_amt_";
    private static final String PREFIX_QA_TYPE = "key_qa_type_";

    // Constructor to pass the Context from an Activity
    public SettingsManager(Context context) {
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Saves all initialization and settings data to local storage.
     */
    public void saveSettings(String username, String allowanceType, int days, double allowance, double balance, boolean isDarkMode) {
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_ALLOWANCE_TYPE, allowanceType);
        editor.putInt(KEY_DAYS_WITH_CLASSES, days);
        editor.putFloat(KEY_ALLOWANCE_AMOUNT, (float) allowance);
        editor.putFloat(KEY_STARTING_BALANCE, (float) balance);
        editor.putBoolean(KEY_DARK_MODE, isDarkMode);

        // INITIALIZATION STEP: Set your wallet pool to match the start allowance amount
        editor.putFloat(KEY_CURRENT_BALANCE, (float) allowance);

        // Commit changes to permanent device memory safely
        editor.apply();
    }

    /**
     * Getters to read the data back out.
     */
    public String getUsername() {
        return prefs.getString(KEY_USERNAME, "");
    }

    public String getAllowanceType() {
        return prefs.getString(KEY_ALLOWANCE_TYPE, "Weekly");
    }

    public int getDaysWithClasses() {
        return prefs.getInt(KEY_DAYS_WITH_CLASSES, 5);
    }

    public double getAllowanceAmount() {
        return (double) prefs.getFloat(KEY_ALLOWANCE_AMOUNT, 0.0f);
    }

    public boolean isDarkModeEnabled() {
        return prefs.getBoolean(KEY_DARK_MODE, false);
    }

    // --- LIVE BALANCE MUTATION GETTERS & SETTERS ---

    /**
     * Retrieves the current available cash remaining in the wallet.
     */
    public double getCurrentBalance() {
        return (double) prefs.getFloat(KEY_CURRENT_BALANCE, 0.0f);
    }

    /**
     * Updates the running balance amount after subtracting or adding transactions.
     */
    public void updateCurrentBalance(double newBalance) {
        prefs.edit()
                .putFloat(KEY_CURRENT_BALANCE, (float) newBalance)
                .apply();
    }

    // --- QUICK ACTION LOGIC STORAGE METHODS ---

    /**
     * Saves or updates a specific macro configuration bundle.
     */
    public void saveQuickAction(int actionNumber, String description, double amount, String expenseType) {
        prefs.edit()
                .putString(PREFIX_QA_DESC + actionNumber, description)
                .putFloat(PREFIX_QA_AMT + actionNumber, (float) amount)
                .putString(PREFIX_QA_TYPE + actionNumber, expenseType)
                .apply();
    }

    /**
     * MODIFIED: Returns null if no custom description has been saved yet.
     */
    public String getQuickActionDescription(int actionNumber) {
        return prefs.getString(PREFIX_QA_DESC + actionNumber, null);
    }

    public double getQuickActionAmount(int actionNumber) {
        return (double) prefs.getFloat(PREFIX_QA_AMT + actionNumber, 0.0f);
    }

    public String getQuickActionType(int actionNumber) {
        return prefs.getString(PREFIX_QA_TYPE + actionNumber, "Transportation");
    }

    public void removeQuickAction(int actionNumber) {
        prefs.edit()
                .remove(PREFIX_QA_DESC + actionNumber)
                .remove(PREFIX_QA_AMT + actionNumber)
                .remove(PREFIX_QA_TYPE + actionNumber)
                .apply();
    }

    /**
     * Clears everything (useful if you ever want to add a 'Reset Data' button).
     */
    public void clearData() {
        prefs.edit().clear().apply();
    }
}