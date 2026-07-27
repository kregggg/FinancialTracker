package com.example.financialtracker.source;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.financialtracker.database.DataAccess;
import com.example.financialtracker.database.SettingsManager;
import com.example.financialtracker.databinding.MainSummaryActivityBinding;
import com.example.financialtracker.ref.CategoryBreakdownAdapter;
import com.example.financialtracker.ref.Transaction;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainSummary extends AppCompatActivity {

    private MainSummaryActivityBinding binding;
    private SettingsManager settingsManager;
    private List<Transaction> transactions = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = MainSummaryActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        settingsManager = new SettingsManager(this);

        binding.btnBackExpenseSummary.setOnClickListener(v -> finish());

        binding.btnViewWeeklyRecords.setOnClickListener(v ->
                startActivity(new Intent(this, WeeklyRecords.class)));

        refreshSummary();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Re-pull from the DB every time this screen becomes visible — this is what
        // makes the pie chart reflect a transaction you just added on another screen.
        refreshSummary();
    }

    /**
     * Single entry point: reloads transactions from Room, then rebuilds every
     * stat, list, and the pie chart from that fresh data. Nothing on this screen
     * is left stale after a save elsewhere in the app, as long as onResume() fires.
     */
    private void refreshSummary() {
        transactions = DataAccess.getInstance(this).transactionDao().getAllTransactions();

        updateTotalsCard();
        updateExpenseBreakdown();
        updateIncomeBreakdown();
    }

    // =====================================================================
    // --- TOP CARD: Total Allowance / Expenses / Savings / Balance ---
    // =====================================================================

    private void updateTotalsCard() {
        double totalAllowance = 0;
        double totalIncome = 0;
        double totalExpenses = 0;

        for (Transaction t : transactions) {
            if ("INCOME".equalsIgnoreCase(t.getTransactionType())) {
                totalIncome += t.getAmount();
                if ("Allowance".equalsIgnoreCase(t.getCategory())) {
                    totalAllowance += t.getAmount();
                }
            } else if ("EXPENSE".equalsIgnoreCase(t.getTransactionType())) {
                totalExpenses += t.getAmount();
            }
        }

        // Savings = everything earned minus everything spent.
        // NOTE: this assumption isn't defined anywhere else in the codebase yet —
        // if "savings" is meant to mean something narrower (e.g. allowance-only),
        // this is the one line to change.
        double totalSavings = totalIncome - totalExpenses;
        double currentBalance = settingsManager.getCurrentBalance();

        binding.tvTotalAllowanceValue.setText(String.format(Locale.US, "P%.2f", totalAllowance));
        binding.tvTotalExpensesValue.setText(String.format(Locale.US, "P%.2f", totalExpenses));
        binding.tvTotalSavingsValue.setText(String.format(Locale.US, "P%.2f", totalSavings));
        binding.tvCurrentBalanceValue.setText(String.format(Locale.US, "P%.2f", currentBalance));
    }

    // =====================================================================
    // --- EXPENSES: Pie chart + breakdown list ---
    // =====================================================================

    private void updateExpenseBreakdown() {
        Map<String, Double> totalsByCategory = groupByCategory("EXPENSE");
        double grandTotal = sumValues(totalsByCategory);

        renderPieChart(binding.pieChartExpenses, totalsByCategory);
        renderBreakdownList(binding.rvExpensesBreakdown, totalsByCategory, grandTotal);
    }

    // =====================================================================
    // --- INCOME: breakdown list (no chart in this layout, list only) ---
    // =====================================================================

    private void updateIncomeBreakdown() {
        Map<String, Double> totalsByCategory = groupByCategory("INCOME");
        double grandTotal = sumValues(totalsByCategory);

        renderBreakdownList(binding.rvIncomeBreakdown, totalsByCategory, grandTotal);
    }

    // =====================================================================
    // --- SHARED HELPERS ---
    // =====================================================================

    /**
     * Groups transactions of the given type by category and sums their amounts.
     * LinkedHashMap keeps categories in first-seen order for stable chart/list ordering.
     */
    private Map<String, Double> groupByCategory(String transactionType) {
        Map<String, Double> totals = new LinkedHashMap<>();

        for (Transaction t : transactions) {
            if (!transactionType.equalsIgnoreCase(t.getTransactionType())) continue;

            String category = t.getCategory();
            double running = totals.containsKey(category) ? totals.get(category) : 0.0;
            totals.put(category, running + t.getAmount());
        }

        return totals;
    }

    private double sumValues(Map<String, Double> totals) {
        double sum = 0;
        for (double v : totals.values()) sum += v;
        return sum;
    }

    private void renderPieChart(PieChart pieChart, Map<String, Double> totalsByCategory) {
        if (totalsByCategory.isEmpty()) {
            pieChart.clear();
            pieChart.setNoDataText("No expenses recorded yet.");
            pieChart.invalidate();
            return;
        }

        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Double> entry : totalsByCategory.entrySet()) {
            // Raw amounts go in — setUsePercentValues(true) below makes the chart
            // display each slice's share of the total automatically.
            entries.add(new PieEntry((float) (double) entry.getValue(), entry.getKey()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.WHITE);

        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.getLegend().setEnabled(false); // handled by rvExpensesBreakdown instead
        pieChart.setHoleRadius(0f);
        pieChart.setTransparentCircleRadius(0f);
        pieChart.invalidate(); // required every time setData() is called, not just once
    }

    private void renderBreakdownList(androidx.recyclerview.widget.RecyclerView recyclerView,
                                     Map<String, Double> totalsByCategory, double grandTotal) {
        List<CategoryBreakdownAdapter.CategoryTotal> rows = new ArrayList<>();

        for (Map.Entry<String, Double> entry : totalsByCategory.entrySet()) {
            double amount = entry.getValue();
            double percentage = grandTotal > 0 ? (amount / grandTotal) * 100.0 : 0.0;
            rows.add(new CategoryBreakdownAdapter.CategoryTotal(entry.getKey(), amount, percentage));
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new CategoryBreakdownAdapter(rows));
    }
}