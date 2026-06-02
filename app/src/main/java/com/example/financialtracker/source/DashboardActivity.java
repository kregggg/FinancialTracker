package com.example.financialtracker.source;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.example.financialtracker.ref.SettingsManager;
import com.example.financialtracker.databinding.MainDashboardActivityBinding;

public class DashboardActivity extends AppCompatActivity {

    private MainDashboardActivityBinding binding;
    private SettingsManager settingsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
            // TODO: Intent intent = new Intent(this, DailyExpensesActivity.class);
            // startActivity(intent);
        });

        binding.cardRecordIncome.setOnClickListener(v -> {
            // TODO: Handle income recording navigation
        });

        binding.cardExpensesSummary.setOnClickListener(v -> {
            // TODO: Handle historical summary navigation
        });

        binding.cardSettings.setOnClickListener(v -> {
            // TODO: Handle configuration setup modifications
        });
    }
}