package com.example.financialtracker.source;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.financialtracker.R;
import com.example.financialtracker.database.DataAccess;
import com.example.financialtracker.database.SettingsManager;
import com.example.financialtracker.databinding.AdditionalIncomeActivityBinding;
import com.example.financialtracker.databinding.NoticeReceivedActivityBinding;
import com.example.financialtracker.databinding.RecordIncomeActivityBinding;
import com.example.financialtracker.ref.Transaction;
import com.example.financialtracker.databinding.ReceiveAllowanceActivityBinding;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class RecordMenu extends AppCompatActivity {

    private RecordIncomeActivityBinding binding;
    private SettingsManager settingsManager;
    private List<Transaction> transactions = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate the layout using the correct binding class for record_income_activity.xml
        binding = RecordIncomeActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize persistent storage manager
        settingsManager = new SettingsManager(this);
        populateTransactions();

        // --- NAVIGATION & CLICK LISTENERS ---

        binding.btnBackRecordIncome.setOnClickListener(v -> finish());

        binding.cardRecordAllowance.setOnClickListener(v -> {
            processAllowanceLogic();
        });

        binding.cardAdditionalIncome.setOnClickListener(v -> {
            processAdditionalIncomeLogic();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        populateTransactions();
    }

    private void processAllowanceLogic() {
        if (allowanceClaimed()) {
            showNoticeReceivedDialog();
        } else {
            showReceiveAllowanceDialog();
        }
    }

    private void processAdditionalIncomeLogic() {
        showAdditionalIncomeDialog();
    }

    private void showNoticeReceivedDialog() {
        Dialog dialog = new Dialog(this);
        NoticeReceivedActivityBinding dialogBinding = NoticeReceivedActivityBinding.inflate(getLayoutInflater());
        dialog.setContentView(dialogBinding.getRoot());
        setupOverlayWindow(dialog);

        dialogBinding.tvNoticeTitle.setText("Allowance Already Claimed");
        dialogBinding.tvNoticeMessage.setText(
                "You've already received your allowance for this period. Record it again anyway?");

        dialogBinding.btnCancelNotice.setOnClickListener(v -> dialog.dismiss());
        dialogBinding.btnProceedNotice.setOnClickListener(v -> {
            dialog.dismiss();
            showReceiveAllowanceDialog();
        });

        dialog.show();
    }

    private void showReceiveAllowanceDialog() {
        Dialog dialog = new Dialog(this);
        ReceiveAllowanceActivityBinding dialogBinding = ReceiveAllowanceActivityBinding.inflate(getLayoutInflater());
        dialog.setContentView(dialogBinding.getRoot());
        setupOverlayWindow(dialog);

        dialogBinding.etAllowanceAmount.setText(String.format(Locale.US, "%.2f", settingsManager.getAllowanceAmount()));

        dialogBinding.btnCancelAllowance.setOnClickListener(v -> dialog.dismiss());
        dialogBinding.btnSaveAllowance.setOnClickListener(v -> {
            String amountStr = dialogBinding.etAllowanceAmount.getText().toString().trim();

            if (amountStr.isEmpty()) {
                dialogBinding.etAllowanceAmount.setError("Amount is required.");
                return;
            }

            double amount;
            try {
                amount = Double.parseDouble(amountStr);
            } catch (NumberFormatException e) {
                dialogBinding.etAllowanceAmount.setError("Enter a valid number.");
                return;
            }

            Transaction allowanceTransaction = new Transaction(
                    "Allowance",
                    amount,
                    "Allowance", // must match income_sources[0] — allowanceClaimed() filters on this category
                    System.currentTimeMillis(),
                    "INCOME"
            );

            onTransactionRecorded(allowanceTransaction);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showAdditionalIncomeDialog() {
        Dialog dialog = new Dialog(this);
        AdditionalIncomeActivityBinding dialogBinding = AdditionalIncomeActivityBinding.inflate(getLayoutInflater());
        dialog.setContentView(dialogBinding.getRoot());
        setupOverlayWindow(dialog);

        // 1. Fetch sources and filter out "Allowance" (index 0) so it doesn't show in Additional Income
        String[] allSources = getResources().getStringArray(R.array.income_sources);
        List<String> filteredSources = new ArrayList<>();
        for (int i = 1; i < allSources.length; i++) { // Start at index 1 to skip Allowance
            filteredSources.add(allSources[i]);
        }

        // 2. Set up adapter forcing black text and a clean white background for the dropdown menu
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_item,
                filteredSources
        ) {
            @Override
            public android.view.View getView(int position, android.view.View convertView, android.view.ViewGroup parent) {
                android.view.View view = super.getView(position, convertView, parent);
                if (view instanceof TextView) {
                    ((TextView) view).setTextColor(Color.BLACK);
                }
                return view;
            }

            @Override
            public android.view.View getDropDownView(int position, android.view.View convertView, android.view.ViewGroup parent) {
                android.view.View view = super.getDropDownView(position, convertView, parent);
                view.setBackgroundColor(Color.WHITE); // Force item background to white
                if (view instanceof TextView) {
                    ((TextView) view).setTextColor(Color.BLACK); // Force item text to black
                }
                return view;
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dialogBinding.spinnerSource.setAdapter(adapter);

        dialogBinding.btnCancelAdditional.setOnClickListener(v -> dialog.dismiss());
        dialogBinding.btnSaveAdditional.setOnClickListener(v -> {
            String desc = dialogBinding.etDescription.getText().toString().trim();
            String amountStr = dialogBinding.etAmount.getText().toString().trim();

            if (desc.isEmpty()) {
                dialogBinding.etDescription.setError("Description is required.");
                return;
            }
            if (amountStr.isEmpty()) {
                dialogBinding.etAmount.setError("Amount is required.");
                return;
            }

            double amount;
            try {
                amount = Double.parseDouble(amountStr);
            } catch (NumberFormatException e) {
                dialogBinding.etAmount.setError("Enter a valid number.");
                return;
            }

            // 3. Null-safe check before assigning the selected item to source
            Object selectedObject = dialogBinding.spinnerSource.getSelectedItem();
            if (selectedObject == null) {
                Toast.makeText(this, "Please select a category source.", Toast.LENGTH_SHORT).show();
                return;
            }
            String source = selectedObject.toString();

            Transaction additionalIncome = new Transaction(
                    desc, amount, source, System.currentTimeMillis(), "INCOME"
            );

            onTransactionRecorded(additionalIncome);
            dialog.dismiss();
        });

        dialog.show();
    }

    /**
     * The one place that actually touches the database. Overlays only build
     * a Transaction object and pass it here — they never call DataAccess directly.
     */
    private void onTransactionRecorded(Transaction transaction) {
        DataAccess.getInstance(this).addTransactionAndUpdateBalance(transaction, this);

        // Refresh in-memory list immediately, so a second dialog opened in this
        // same session (e.g. user reopens "Get Allowance") sees the up-to-date state.
        populateTransactions();

        Toast.makeText(this, transaction.getDescription() + " recorded!", Toast.LENGTH_SHORT).show();
    }

    private void setupOverlayWindow(Dialog dialog) {
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.90);
            dialog.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    public boolean isTodayMonday() {
        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        return dayOfWeek == Calendar.MONDAY;
    }

    public void populateTransactions(){
        transactions = DataAccess.getInstance(this).transactionDao().getAllTransactions();
    }

    public boolean allowanceClaimed(){
        List<Transaction> basis = new ArrayList<>();
        String allowanceType = settingsManager.getAllowanceType();
        String[] match = getResources().getStringArray(R.array.allowance_options);
        String allowance = getResources().getStringArray(R.array.income_sources)[0];

        if  (allowanceType.equalsIgnoreCase(match[0])){ // DAILY
            for (Transaction transac : transactions) {
                if (isTransactionFromToday(transac.getTimestamp())){
                    basis.add(transac);
                }
            }

            for (Transaction today : basis){
                if (today.getTransactionType().equalsIgnoreCase("INCOME") && today.getCategory().equalsIgnoreCase(allowance)){
                    return true;
                }
            }
        } else if (allowanceType.equalsIgnoreCase(match[1])) { // WEEKLY
            for (Transaction transac : transactions) {
                if (isTransactionFromThisWeek(transac.getTimestamp())){
                    basis.add(transac);
                }
            }

            for (Transaction today : basis){
                if (today.getTransactionType().equalsIgnoreCase("INCOME") && today.getCategory().equalsIgnoreCase(allowance)){
                    return true;
                }
            }
        } else if (allowanceType.equalsIgnoreCase(match[2])){ // MONTHLY
            for (Transaction transac : transactions) {
                if (isTransactionFromThisMonth(transac.getTimestamp())){
                    basis.add(transac);
                }
            }

            for (Transaction today : basis){
                if (today.getTransactionType().equalsIgnoreCase("INCOME") && today.getCategory().equalsIgnoreCase(allowance)){
                    return true;
                }
            }
        }

        return false;
    }

    private boolean isTransactionFromToday(long transactionTimestamp) {
        java.util.Calendar today = java.util.Calendar.getInstance();

        java.util.Calendar txDate = java.util.Calendar.getInstance();
        txDate.setTimeInMillis(transactionTimestamp);

        return (today.get(java.util.Calendar.YEAR) == txDate.get(java.util.Calendar.YEAR) &&
                today.get(java.util.Calendar.DAY_OF_YEAR) == txDate.get(java.util.Calendar.DAY_OF_YEAR));
    }

    /**
     * Rolls a timestamp back to midnight of that week's Monday.
     * Used as a stable "week bucket" that correctly handles year boundaries.
     */
    private long getStartOfWeekMillis(long timestamp) {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        cal.setFirstDayOfWeek(java.util.Calendar.MONDAY);

        // Roll back day-by-day until we hit Monday
        while (cal.get(java.util.Calendar.DAY_OF_WEEK) != java.util.Calendar.MONDAY) {
            cal.add(java.util.Calendar.DAY_OF_MONTH, -1);
        }

        // Zero out the time so only the date matters
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);

        return cal.getTimeInMillis();
    }

    private boolean isTransactionFromThisWeek(long transactionTimestamp) {
        long todayWeekStart = getStartOfWeekMillis(System.currentTimeMillis());
        long txWeekStart = getStartOfWeekMillis(transactionTimestamp);
        return todayWeekStart == txWeekStart;
    }

    private boolean isTransactionFromThisMonth(long transactionTimestamp) {
        java.util.Calendar today = java.util.Calendar.getInstance();
        java.util.Calendar txDate = java.util.Calendar.getInstance();
        txDate.setTimeInMillis(transactionTimestamp);

        return (today.get(java.util.Calendar.YEAR) == txDate.get(java.util.Calendar.YEAR) &&
                today.get(java.util.Calendar.MONTH) == txDate.get(java.util.Calendar.MONTH));
    }
}