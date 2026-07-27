package com.example.financialtracker.source;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.financialtracker.database.DataAccess;
import com.example.financialtracker.databinding.WeeklyRecordsActivityBinding;
import com.example.financialtracker.ref.Transaction;
import com.example.financialtracker.ref.WeekRecord;
import com.example.financialtracker.ref.WeeklyRecordAdapter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class WeeklyRecords extends AppCompatActivity {

    private WeeklyRecordsActivityBinding binding;
    private static final long MILLIS_PER_WEEK = 7L * 24 * 60 * 60 * 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = WeeklyRecordsActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnBackWeekly.setOnClickListener(v -> finish());

        loadWeeklyRecords();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadWeeklyRecords(); // reflect any expense/income added since this screen was last open
    }

    private void loadWeeklyRecords() {
        List<Transaction> transactions = DataAccess.getInstance(this).transactionDao().getAllTransactions();

        List<WeekRecord> weeks = buildWeekList(transactions);

        binding.rvWeeklyRecords.setLayoutManager(new LinearLayoutManager(this));
        binding.rvWeeklyRecords.setAdapter(new WeeklyRecordAdapter(weeks, week -> {
            Intent intent = new Intent(WeeklyRecords.this, DayOverview.class);
            intent.putExtra(DayOverview.EXTRA_WEEK_START, week.weekStartMillis);
            startActivity(intent);
        }));
    }

    /**
     * Builds one WeekRecord per calendar week from the earliest transaction on record
     * up through the current week — including weeks with zero transactions, so a
     * 5-week-old account always shows 5 cards, not just the weeks that happen to have data.
     * Returned newest-first, matching how the rest of the app orders transactions.
     */
    private List<WeekRecord> buildWeekList(List<Transaction> transactions) {
        List<WeekRecord> weeks = new ArrayList<>();

        if (transactions.isEmpty()) {
            return weeks; // brand-new account, no records yet — empty list, no crash
        }

        long earliestTimestamp = Long.MAX_VALUE;
        for (Transaction t : transactions) {
            if (t.getTimestamp() < earliestTimestamp) {
                earliestTimestamp = t.getTimestamp();
            }
        }

        long earliestWeekStart = getStartOfWeekMillis(earliestTimestamp);
        long currentWeekStart = getStartOfWeekMillis(System.currentTimeMillis());

        int totalWeeks = (int) ((currentWeekStart - earliestWeekStart) / MILLIS_PER_WEEK) + 1;

        for (int i = 0; i < totalWeeks; i++) {
            long weekStart = earliestWeekStart + (i * MILLIS_PER_WEEK);
            long weekEnd = weekStart + MILLIS_PER_WEEK - 1; // last millisecond of Sunday
            weeks.add(new WeekRecord(i + 1, weekStart, weekEnd));
        }

        // Newest week first
        java.util.Collections.reverse(weeks);
        return weeks;
    }

    /** Same Monday-rollback logic used in RecordMenu's allowance week check, kept local
     *  here since this codebase doesn't currently have a shared date-utils class. */
    private long getStartOfWeekMillis(long timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
            cal.add(Calendar.DAY_OF_MONTH, -1);
        }
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }
}