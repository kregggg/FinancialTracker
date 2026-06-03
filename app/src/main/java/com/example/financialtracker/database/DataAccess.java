package com.example.financialtracker.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

// Make sure these match your actual package structures!
import com.example.financialtracker.ref.Transaction;

@Database(entities = {Transaction.class}, version = 1, exportSchema = false)
public abstract class DataAccess extends RoomDatabase {

    private static DataAccess instance;

    public abstract TransactionDAO transactionDao();

    public static synchronized DataAccess getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            DataAccess.class,
                            "financial_tracker_db"
                    )
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries() // Allows smooth UI thread testing
                    .build();
        }
        return instance;
    }

    // =====================================================================
    // --- COMBINED OPERATIONS: HANDLES BOTH DATABASE & WALLET BALANCE ---
    // =====================================================================

    /**
     * Adds a transaction to the database AND updates the live wallet balance automatically.
     */
    public void addTransactionAndUpdateBalance(Transaction transaction, Context context) {
        // 1. Save to Database
        transactionDao().insertTransaction(transaction);

        // 2. Adjust Balance in Settings
        SettingsManager settingsManager = new SettingsManager(context);
        double currentBalance = settingsManager.getCurrentBalance();

        if ("EXPENSE".equals(transaction.getTransactionType())) {
            currentBalance -= transaction.getAmount();
        } else if ("INCOME".equals(transaction.getTransactionType())) {
            currentBalance += transaction.getAmount();
        }

        settingsManager.updateCurrentBalance(currentBalance);
    }

    /**
     * Deletes a transaction and REVERSES its effect on the wallet balance.
     */
    public void deleteTransactionAndUpdateBalance(Transaction transaction, Context context) {
        // 1. Delete from Database
        transactionDao().deleteTransaction(transaction);

        // 2. Reverse the balance effect in Settings
        SettingsManager settingsManager = new SettingsManager(context);
        double currentBalance = settingsManager.getCurrentBalance();

        if ("EXPENSE".equals(transaction.getTransactionType())) {
            currentBalance += transaction.getAmount(); // Refund the expense
        } else if ("INCOME".equals(transaction.getTransactionType())) {
            currentBalance -= transaction.getAmount(); // Revoke the income
        }

        settingsManager.updateCurrentBalance(currentBalance);
    }

    /**
     * Updates an existing transaction and calculates the math difference for the wallet balance.
     * @param transaction The updated transaction object.
     * @param oldAmount The amount this transaction cost BEFORE the user modified it.
     */
    public void updateTransactionAndUpdateBalance(Transaction transaction, double oldAmount, Context context) {
        // 1. Update Database
        transactionDao().updateTransaction(transaction);

        // 2. Calculate balance difference in Settings
        SettingsManager settingsManager = new SettingsManager(context);
        double currentBalance = settingsManager.getCurrentBalance();

        if ("EXPENSE".equals(transaction.getTransactionType())) {
            currentBalance += oldAmount; // Refund the old expense cost
            currentBalance -= transaction.getAmount(); // Subtract the new modified cost
        } else if ("INCOME".equals(transaction.getTransactionType())) {
            currentBalance -= oldAmount; // Remove the old income amount
            currentBalance += transaction.getAmount(); // Add the new modified income
        }

        settingsManager.updateCurrentBalance(currentBalance);
    }

}