package com.example.financialtracker.source;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.financialtracker.database.DataAccess;
import com.example.financialtracker.databinding.DailySummaryActivityBinding;
import com.example.financialtracker.ref.Transaction;
import com.example.financialtracker.ref.TransactionAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DailySummary extends AppCompatActivity {

    public static final String EXTRA_DAY_START = "dayStartMillis";
    private static final long MILLIS_PER_DAY = 24L * 60 * 60 * 1000;

    private DailySummaryActivityBinding binding;
    private long dayStartMillis;
    private TransactionAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DailySummaryActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dayStartMillis = getIntent().getLongExtra(EXTRA_DAY_START, -1);
        if (dayStartMillis == -1) {
            finish();
            return;
        }

        binding.btnBackRecord.setOnClickListener(v -> finish());
        binding.btnReturn.setOnClickListener(v -> finish());

        adapter = new TransactionAdapter(new ArrayList<>());
        binding.rvRecordTransactionsList.setLayoutManager(new LinearLayoutManager(this));
        binding.rvRecordTransactionsList.setAdapter(adapter);

        loadDayDetails();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDayDetails();
    }

    private void loadDayDetails() {
        List<Transaction> all = DataAccess.getInstance(this).transactionDao().getAllTransactions();

        List<Transaction> dayTransactions = new ArrayList<>();
        double income = 0;
        double expenses = 0;

        for (Transaction t : all) {
            if (t.getTimestamp() < dayStartMillis || t.getTimestamp() >= dayStartMillis + MILLIS_PER_DAY) {
                continue;
            }
            dayTransactions.add(t);

            if ("INCOME".equalsIgnoreCase(t.getTransactionType())) {
                income += t.getAmount();
            } else if ("EXPENSE".equalsIgnoreCase(t.getTransactionType())) {
                expenses += t.getAmount();
            }
        }

        // Latest to oldest, as requested
        dayTransactions.sort((a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy (EEE)", Locale.US);
        binding.tvRecordDate.setText(dateFormat.format(new Date(dayStartMillis)));
        binding.tvRecordIncome.setText(String.format(Locale.US, "P%.2f", income));
        binding.tvRecordExpenses.setText(String.format(Locale.US, "P%.2f", expenses));
        binding.tvRecordSavings.setText(String.format(Locale.US, "P%.2f", income - expenses));

        adapter.updateData(dayTransactions);
    }
}