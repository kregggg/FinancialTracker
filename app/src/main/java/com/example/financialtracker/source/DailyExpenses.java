package com.example.financialtracker.source;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.financialtracker.database.DataAccess;
import com.example.financialtracker.databinding.DailyExpensesActivityBinding;
import com.example.financialtracker.databinding.RecordExpenseActivityBinding; // Generated from your new overlay XML
import com.example.financialtracker.database.SettingsManager;
import com.example.financialtracker.ref.Transaction;
import com.example.financialtracker.ref.TransactionAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DailyExpenses extends AppCompatActivity {

    private DailyExpensesActivityBinding binding;
    private SettingsManager settingsManager;
    private TransactionAdapter transactionAdapter;
    private ArrayList<Transaction> expenseList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DailyExpensesActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        settingsManager = new SettingsManager(this);

        // FIX 1: Always set up your LayoutManager and Adapter BEFORE loading data panels
        binding.rvExpenses.setLayoutManager(new LinearLayoutManager(this));
        transactionAdapter = new TransactionAdapter(expenseList);
        binding.rvExpenses.setAdapter(transactionAdapter);

        // Now it is safe to load data because the adapter is ready!
        updateStatsPanel();
        updateTransactionTable();

        // --- NAVIGATION & SCREEN TRANSITIONS ---
        binding.btnBack.setOnClickListener(v -> finish());

        binding.btnCalendar.setOnClickListener(v -> {
            long weekStartMillis = getDatesOfSameWeek(System.currentTimeMillis()).get(0);
            Intent intent = new Intent(this, DayOverview.class);
            intent.putExtra(DayOverview.EXTRA_WEEK_START, weekStartMillis);
            startActivity(intent);
        });

        binding.btnAddExpense.setOnClickListener(v -> showRecordExpenseDialog());
    }

    /**
     * Instantiates and manages the layout transition overlay for adding new custom daily expenses.
     */
    private void showRecordExpenseDialog() {
        Dialog dialog = new Dialog(this);

        // Inflate the Record Expense XML overlay using structural layout binding
        RecordExpenseActivityBinding dialogBinding = RecordExpenseActivityBinding.inflate(getLayoutInflater());
        dialog.setContentView(dialogBinding.getRoot());

        // Format dialog window properties (90% width bounds + transparent layout margins)
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.90);
            dialog.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        // --- DYNAMIC DATA SYNC ---
        // Dynamically rename Quick Action button labels to match what was configured in Settings
        bindQuickActionButton(dialogBinding.btnAction1, 1, dialogBinding);
        bindQuickActionButton(dialogBinding.btnAction2, 2, dialogBinding);
        bindQuickActionButton(dialogBinding.btnAction3, 3, dialogBinding);

        // --- OVERLAY MACRO INTERACTIVE TRIGGERS ---

        dialogBinding.btnAction1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                quickAction(1, dialogBinding);
            }
        });

        dialogBinding.btnAction2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                quickAction(2, dialogBinding);
            }
        });

        dialogBinding.btnAction3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                quickAction(3, dialogBinding);
            }
        });

        // --- SUBMIT & DISMISSAL BUTTON TRANSITIONS ---

        // Cancel Button: Instantly closes down the overlay layout without altering anything
        dialogBinding.btnCancelExpense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        // Submit Button: Processes verification checking gates and handles entry addition
        dialogBinding.btnSubmitExpense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean validAmount = false, validDesc = false;
                String descInput = dialogBinding.etExpenseDescription.getText().toString().trim();
                String amountStr = dialogBinding.etExpenseAmount.getText().toString().trim();
                double amount = 0;

                // Validation checking guard for empty fields
                if (descInput.isEmpty()) {
                    dialogBinding.etExpenseDescription.setError("Expense description is required!");
                    dialogBinding.etExpenseDescription.requestFocus();
                    return;
                } else {
                    validDesc = true;
                }

                if (amountStr.isEmpty()) {
                    dialogBinding.etExpenseAmount.setError("Please supply transaction amount price!");
                    dialogBinding.etExpenseAmount.requestFocus();
                    return;
                } else {
                    try {
                        amount = Double.parseDouble(amountStr);
                        validAmount = true;
                    } catch (NumberFormatException e){
                        dialogBinding.etExpenseAmount.setError("Amount cannot contain any  letters or special characters.");
                    }
                }

                if (validDesc && validAmount){
                    long currentTime = System.currentTimeMillis();

                    String categoryInput = dialogBinding.spinnerExpenseType.getSelectedItem().toString();

                    // 1. Package data into the Transaction blueprint model
                    Transaction newExpense = new Transaction(
                            descInput,
                            amount,
                            categoryInput,
                            currentTime,
                            "EXPENSE"
                    );

                    // 2. Call your consolidated DataAccess instance to write to DB and update wallet preferences
                    DataAccess.getInstance(DailyExpenses.this).addTransactionAndUpdateBalance(newExpense, DailyExpenses.this);

                    // 3. Sync UI display elements on the parent screen
                    // Update the wallet balance display string immediately
                    double updatedBalance = DataAccess.getInstance(DailyExpenses.this).transactionDao().getTotalAmountByTypeSince("INCOME", 0); // placeholder or call settingsManager

                    updateStatsPanel();
                    updateTransactionTable();

                    dialog.dismiss(); // Clean finish overlay animation transition
                }
            }
        });

        // Render layout overlay display view
        dialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStatsPanel();
        updateTransactionTable();
    }

    private void quickAction(int num, RecordExpenseActivityBinding dialogBinding) {
        String desc = settingsManager.getQuickActionDescription(num);

        // CLEANER CHECK: Since it returns null when unassigned, just verify if it's not null!
        if (desc != null) {
            double amount = settingsManager.getQuickActionAmount(num);
            String type = settingsManager.getQuickActionType(num);

            dialogBinding.etExpenseDescription.setText(desc);
            dialogBinding.etExpenseAmount.setText(String.valueOf(amount));

            if (dialogBinding.spinnerExpenseType.getAdapter() != null) {
                // FIX: Cast to ArrayAdapter<CharSequence> instead of ArrayAdapter<?>
                ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) dialogBinding.spinnerExpenseType.getAdapter();

                int spinnerPosition = adapter.getPosition(type);
                if (spinnerPosition >= 0) {
                    dialogBinding.spinnerExpenseType.setSelection(spinnerPosition);
                }
            }
        } else {
            // Fallback Toast notification if the slot has no configuration values
            Toast.makeText(
                    DailyExpenses.this,
                    "No action saved for Action " + num + ". Create a Quick action in the settings.",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    /**
     * Checks if a transaction's timestamp falls within today's calendar date.
     */
    private boolean isTransactionFromToday(long transactionTimestamp) {
        java.util.Calendar today = java.util.Calendar.getInstance();

        java.util.Calendar txDate = java.util.Calendar.getInstance();
        txDate.setTimeInMillis(transactionTimestamp);

        return (today.get(java.util.Calendar.YEAR) == txDate.get(java.util.Calendar.YEAR) &&
                today.get(java.util.Calendar.DAY_OF_YEAR) == txDate.get(java.util.Calendar.DAY_OF_YEAR));
    }

    public List<Transaction> getTransactionsToday(transactionTypes types){
        List<Transaction> all = getAllTransactions();
        List<Transaction> today = new ArrayList<>();

        for (Transaction transac : all){
            if (isTransactionFromToday(transac.getTimestamp())){
                if (types.equals(transactionTypes.ALL)){
                    today.add(transac);
                } else if (types.equals(transactionTypes.EXPENSE)){
                    if (transac.getTransactionType().equalsIgnoreCase("EXPENSE")){
                        today.add(transac);
                    }
                } else if (types.equals(transactionTypes.INCOME)){
                    if (transac.getTransactionType().equalsIgnoreCase("INCOME")){
                        today.add(transac);
                    }
                }
            }
        }

        return today;
    }

    public void updateStatsPanel() {
        // Fetch from database once to maximize processing speed performance
        List<Transaction> allTransactions = getAllTransactions();

        // Get the exact start of this week (Monday at 12:00:00 AM)
        Calendar cal = Calendar.getInstance();
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long startOfWeekMillis = cal.getTimeInMillis();

        // Get the exact end of this week (Sunday at 11:59:59 PM)
        cal.add(Calendar.DAY_OF_WEEK, 7);
        long endOfWeekMillis = cal.getTimeInMillis();

        double spentToday = 0;
        double spentWeek = 0;
        double savedWeek = 0;

        // Parse everything inside a single local memory loop step
        for (Transaction transaction : allTransactions) {
            long txTime = transaction.getTimestamp();
            boolean isExpense = "EXPENSE".equalsIgnoreCase(transaction.getTransactionType());
            boolean isIncome = "INCOME".equalsIgnoreCase(transaction.getTransactionType());

            // 1. Calculate Today's stats
            if (isTransactionFromToday(txTime) && isExpense) {
                spentToday += transaction.getAmount();
            }

            // 2. Calculate Week's stats
            if (txTime >= startOfWeekMillis && txTime < endOfWeekMillis) {
                if (isExpense) {
                    spentWeek += transaction.getAmount();
                } else if (isIncome) {
                    savedWeek += transaction.getAmount();
                }
            }
        }

        // Final balance calculation
        savedWeek -= spentWeek;

        // Assign uniform layout display values with currency indicator text
        binding.tvTotalSpentToday.setText(String.format(Locale.US, "P%,.2f", spentToday));
        binding.tvTotalSpentWeek.setText(String.format(Locale.US, "P%,.2f", spentWeek));
        binding.tvTotalWeeklySavings.setText(String.format(Locale.US, "P%,.2f", savedWeek));
    }

    public double getSpentByDate(Long date){
        List<Transaction> temp = getTransactionByDate(date);
        double amount = 0;

        for (Transaction transaction : temp){
            if (transaction.getTransactionType().equalsIgnoreCase("EXPENSE")){
                amount += transaction.getAmount();
            }
        }

        return amount;
    }

    public double getIncomeDate(Long date){
        List<Transaction> temp = getTransactionByDate(date);
        double amount = 0;

        for (Transaction transaction : temp){
            if (transaction.getTransactionType().equalsIgnoreCase("INCOME")){
                amount += transaction.getAmount();
            }
        }

        return amount;
    }

    /**
     * Takes a database timestamp and returns a list of all 7 calendar dates for that specific week.
     * (Assumes the week starts on Monday).
     */
    public List<Long> getDatesOfSameWeek(long targetTimestamp) {
        List<Long> weekDates = new ArrayList<>();

        // 1. Create a Calendar instance and set it to the given timestamp
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(targetTimestamp);

        // 2. Adjust the calendar so Monday is considered the first day of the week
        calendar.setFirstDayOfWeek(Calendar.MONDAY);

        // 3. Snap the calendar backwards to the Monday of this exact week
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

        // 4. Loop 7 times to capture the timestamp of each day, then move forward one day
        for (int i = 0; i < 7; i++) {
            weekDates.add(calendar.getTimeInMillis());

            // Push the calendar forward by 1 day for the next loop iteration
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        return weekDates;
    }

    public void updateTransactionTable() {
        List<Transaction> today = getTransactionsToday(transactionTypes.ALL);

        // Feed the parsed array data straight to your adapter
        if (transactionAdapter != null) {
            transactionAdapter.updateData(today);
        }
    }

    public String getTimeFromTimestamp(long timestamp) {
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        Date date = new Date(timestamp);
        return timeFormat.format(date);
    }

    public String getFormattedDateFromTimestamp(long timestamp) {
        // Define how you want the date to look (e.g., "June 3, 2026" or "06/03/2026")
        SimpleDateFormat formatter = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());

        // Convert the millisecond timestamp into a Date object
        Date date = new Date(timestamp);

        // Return the pretty text string
        return formatter.format(date);
    }

    public List<Transaction> getAllTransactions(){
        return DataAccess.getInstance(this).transactionDao().getAllTransactions();
    }

    public List<Transaction> getTransactionByDate(Long date){
        return DataAccess.getInstance(this).transactionDao().getTransactionByDate(date);
    }

    public enum transactionTypes{
        EXPENSE,
        INCOME,
        ALL
    }

    private void bindQuickActionButton(com.google.android.material.button.MaterialButton button, int actionNumber, RecordExpenseActivityBinding dialogBinding) {
        String description = settingsManager.getQuickActionDescription(actionNumber);

        if (description == null) {
            button.setVisibility(View.GONE);
        } else {
            button.setVisibility(View.VISIBLE);
            button.setText(description);
            button.setOnClickListener(v -> quickAction(actionNumber, dialogBinding));
        }
    }
}