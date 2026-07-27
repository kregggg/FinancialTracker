package com.example.financialtracker.source;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.example.financialtracker.database.DataAccess;
import com.example.financialtracker.databinding.DayOverviewActivityBinding;
import com.example.financialtracker.ref.Transaction;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class DayOverview extends AppCompatActivity {

    public static final String EXTRA_WEEK_START = "weekStartMillis";

    private DayOverviewActivityBinding binding;
    private long weekStartMillis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DayOverviewActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        weekStartMillis = getIntent().getLongExtra(EXTRA_WEEK_START, -1);
        if (weekStartMillis == -1) {
            finish(); // safety net — this screen has no meaning without a week to show
            return;
        }

        binding.btnBackWeekly.setOnClickListener(v -> finish());

        setupDayClickListeners();
        loadDayTotals();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDayTotals();
    }

    private void loadDayTotals() {
        List<Transaction> transactions = DataAccess.getInstance(this).transactionDao().getAllTransactions();

        // One bucket per day-of-week, Monday first, matching this screen's Mon->Sun card order
        double[] income = new double[7];
        double[] expenses = new double[7];

        for (Transaction t : transactions) {
            if (t.getTimestamp() < weekStartMillis || t.getTimestamp() >= weekStartMillis + (7L * 24 * 60 * 60 * 1000)) {
                continue; // not part of this week
            }

            int dayIndex = getMondayBasedDayIndex(t.getTimestamp());

            if ("INCOME".equalsIgnoreCase(t.getTransactionType())) {
                income[dayIndex] += t.getAmount();
            } else if ("EXPENSE".equalsIgnoreCase(t.getTransactionType())) {
                expenses[dayIndex] += t.getAmount();
            }
        }

        bindDay(binding.tvMondayValues, income[0], expenses[0]);
        bindDay(binding.tvTuesdayValues, income[1], expenses[1]);
        bindDay(binding.tvWednesdayValues, income[2], expenses[2]);
        bindDay(binding.tvThursdayValues, income[3], expenses[3]);
        bindDay(binding.tvFridayValues, income[4], expenses[4]);
        bindDay(binding.tvSaturdayValues, income[5], expenses[5]);
        bindDay(binding.tvSundayValues, income[6], expenses[6]);
    }

    private void bindDay(android.widget.TextView view, double income, double expenses) {
        view.setText(String.format(Locale.US, "Income: Php %.2f | Expenses: Php %.2f", income, expenses));
    }

    /** Monday = 0 ... Sunday = 6, so results line up with cardMonday...cardSunday in order. */
    private int getMondayBasedDayIndex(long timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        int calendarDay = cal.get(Calendar.DAY_OF_WEEK); // Sunday=1, Monday=2, ..., Saturday=7
        return (calendarDay == Calendar.SUNDAY) ? 6 : calendarDay - 2;
    }

    private void setupDayClickListeners() {
        binding.cardMonday.setOnClickListener(v -> openDailySummary(0));
        binding.cardTuesday.setOnClickListener(v -> openDailySummary(1));
        binding.cardWednesday.setOnClickListener(v -> openDailySummary(2));
        binding.cardThursday.setOnClickListener(v -> openDailySummary(3));
        binding.cardFriday.setOnClickListener(v -> openDailySummary(4));
        binding.cardSaturday.setOnClickListener(v -> openDailySummary(5));
        binding.cardSunday.setOnClickListener(v -> openDailySummary(6));
    }

    private void openDailySummary(int dayOffsetFromMonday) {
        long dayStartMillis = weekStartMillis + (dayOffsetFromMonday * 24L * 60 * 60 * 1000);

        Intent intent = new Intent(this, DailySummary.class);
        intent.putExtra(DailySummary.EXTRA_DAY_START, dayStartMillis);
        startActivity(intent);
    }
}