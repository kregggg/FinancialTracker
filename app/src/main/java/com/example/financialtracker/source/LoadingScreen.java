package com.example.financialtracker.source;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;

import com.example.financialtracker.source.DashboardActivity;
import com.example.financialtracker.source.InitializeAccount;
import com.example.financialtracker.ref.SettingsManager;
import com.example.financialtracker.databinding.LoadingScreenActivityBinding;

public class LoadingScreen extends AppCompatActivity {

    private LoadingScreenActivityBinding binding;
    private SettingsManager settingsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup View Binding
        binding = LoadingScreenActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        settingsManager = new SettingsManager(this);

        // Check if a username exists in local storage
        String savedUsername = settingsManager.getUsername();
        boolean hasAccount = !savedUsername.isEmpty();

        // Use a Handler to create our timed delays
        Handler handler = new Handler(Looper.getMainLooper());

        // TIMER 1: After 1 second (1000 milliseconds), change the text
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (hasAccount) {
                    binding.tvWelcomeText.setText("Welcome back, " + savedUsername + "!");
                } else {
                    binding.tvWelcomeText.setText("Getting things ready...");
                }
            }
        }, 1000);

        // TIMER 2: After 2 seconds total (2000 milliseconds), jump to the correct screen
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent;

                if (hasAccount) {
                    // Route to Dashboard if they are set up
                    intent = new Intent(LoadingScreen.this, DashboardActivity.class);
                } else {
                    // Route to Initialization if they are brand new
                    intent = new Intent(LoadingScreen.this, InitializeAccount.class);
                }

                startActivity(intent);
                finish(); // Destroys the loading screen so the user can't "Back" button into it
            }
        }, 2000);
    }
}
