package com.example.financialtracker.source;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.financialtracker.databinding.DailyExpensesActivityBinding;
import com.example.financialtracker.databinding.RecordExpenseActivityBinding; // Generated from your new overlay XML
import com.example.financialtracker.ref.SettingsManager;

public class DailyExpenses extends AppCompatActivity {

    private DailyExpensesActivityBinding binding;
    private SettingsManager settingsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DailyExpensesActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        settingsManager = new SettingsManager(this);

        binding.rvExpenses.setLayoutManager(new LinearLayoutManager(this));

        // --- NAVIGATION & SCREEN TRANSITIONS ---

        binding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        binding.btnCalendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Put transitional calendar picker dialog initialization code here
            }
        });

        // UPDATED: Triggers the dialog modal overlay transition layout instead of opening a new Activity
        binding.btnAddExpense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRecordExpenseDialog();
            }
        });
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
        dialogBinding.btnAction1.setText(settingsManager.getQuickActionDescription(1));
        dialogBinding.btnAction2.setText(settingsManager.getQuickActionDescription(2));
        dialogBinding.btnAction3.setText(settingsManager.getQuickActionDescription(3));

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
                String descInput = dialogBinding.etExpenseDescription.getText().toString().trim();
                String amountStr = dialogBinding.etExpenseAmount.getText().toString().trim();

                // Validation checking guard for empty fields
                if (descInput.isEmpty()) {
                    dialogBinding.etExpenseDescription.setError("Expense description is required!");
                    dialogBinding.etExpenseDescription.requestFocus();
                    return;
                }

                if (amountStr.isEmpty()) {
                    dialogBinding.etExpenseAmount.setError("Please supply transaction amount price!");
                    dialogBinding.etExpenseAmount.requestFocus();
                    return;
                }

                // TODO: YOUR LOGIC - Parse fields, append transaction to list, subtract cost from current wallet balance:
                //       double expenseCost = Double.parseDouble(amountStr);
                //       double updatedBalance = settingsManager.getCurrentBalance() - expenseCost;
                //       settingsManager.updateCurrentBalance(updatedBalance);

                // TODO: YOUR LOGIC - Notify adapter dataset updates or recalculate metrics on dashboard summary card

                dialog.dismiss(); // Clean finish overlay animation transition
            }
        });

        // Render layout overlay display view
        dialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // TODO: Refresh dashboard totals and data arrays
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
}