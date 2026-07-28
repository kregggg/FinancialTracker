package com.example.financialtracker.source;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.example.financialtracker.database.SettingsManager;
import com.example.financialtracker.databinding.MainDashboardActivityBinding;

public class Dashboard extends AppCompatActivity {

    private MainDashboardActivityBinding binding;
    private SettingsManager settingsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // in DashboardActivity.onCreate (or any single early entry point), after setContentView:
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            getWindow().getDecorView().setForceDarkAllowed(true);
        }

        // 1. Inflate the layout using View Binding
        binding = MainDashboardActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 2. Initialize your data manager
        settingsManager = new SettingsManager(this);

        // 3. Call a method to setup your dashboard UI
        setupDashboard();
    }

    private void setupDashboard() {
        // 1. Personalize the header message using the saved username
        String username = settingsManager.getUsername();
        binding.tvWelcomeMessage.setText("Welcome, " + username);

        // 2. Set up navigation click listeners for each of your new menu cards
        binding.cardDailyExpenses.setOnClickListener(v -> {
            Intent intent = new Intent(this, DailyExpenses.class);
            startActivity(intent);
        });

        binding.cardRecordIncome.setOnClickListener(v ->
                startActivity(new Intent(this, RecordMenu.class)));

        binding.cardExpensesSummary.setOnClickListener(v ->
                startActivity(new Intent(this, MainSummary.class)));

        binding.cardSettings.setOnClickListener(v -> {
            Intent intent = new Intent(this, Settings.class);
            startActivity(intent);
        });
    }
}