package com.example.financialtracker.source;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter; // Make sure this is imported
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.financialtracker.R; // Make sure to import your R class
import com.example.financialtracker.database.SettingsManager;
import com.example.financialtracker.databinding.InitializeAccountActivityBinding;
import com.example.financialtracker.databinding.HelpInitializeActivityBinding;

public class InitializeAccount extends AppCompatActivity {

    private SettingsManager settingsManager;
    private InitializeAccountActivityBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Inflate the main layout using View Binding
        binding = InitializeAccountActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        settingsManager = new SettingsManager(this);

        // --- THE MISSING SPINNER FIX ---
        // Grab your array from XML and link it to your custom black-text layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.allowance_options,
                R.layout.spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.allowanceTypeSelection.setAdapter(adapter);
        // -------------------------------

        // 2. Set up Submit Button click listener
        binding.submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserInitializationData();
            }
        });

        // 3. Set up Help Button click listener to show the overlay
        binding.helpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showHelpOverlay();
            }
        });
    }

    private void saveUserInitializationData() {
        boolean validUser = false, validDays = false, validAllowance = false, validBalance = false;

        // Username
        String usernameInput = binding.useerNameField.getText().toString().trim();
        if (usernameInput.isEmpty()){
            binding.useerNameField.setError("Username is required to create your profile!");
            binding.useerNameField.requestFocus();
            return;
        } else {
            validUser = true;
        }

        // Allowance type [Daily, Weekly, Monthly]
        String selectedType = binding.allowanceTypeSelection.getSelectedItem() != null ?
                binding.allowanceTypeSelection.getSelectedItem().toString() : "Weekly";

        // Days of classes [0-7]
        int daysInput = 5;
        String tempDays = binding.daysField.getText().toString();
        if (tempDays.isEmpty() && selectedType.equals(getResources().getStringArray(R.array.allowance_options)[0])){
            binding.daysField.setError("Number of days with classes are required for daily allowance.");
        }

        try {
            daysInput = Integer.parseInt(tempDays);

            if (daysInput > 7) {
                binding.daysField.setError("Days with classes cannot exceed 7 days");
            } else if (daysInput < 0){
                binding.daysField.setError("Days with classes cannot have a negative value");
            } else {
                validDays = true;
            }
        } catch (NumberFormatException e) {
            binding.daysField.setError("Days with classes cannot contain letters or special characters");
        }

        // Allowance to Receive
        double allowanceInput = 0.0;
        String tempAllowance = binding.allowanceField.getText().toString();
        if (tempAllowance.isEmpty()){
            binding.allowanceField.setError("Allowance to receive is required to create your profile!");
        }

        try {
            allowanceInput = Double.parseDouble(tempAllowance);

            if (allowanceInput < 0){
                binding.allowanceField.setError("Allowance to receive cannot be a negative number");
            } else {
                validAllowance = true;
            }
        } catch (NumberFormatException e) {
            binding.allowanceField.setError("Allowance to receive cannot be contain letters or special characters");
        }

        // Current Balance
        double balance = 0;
        String tempBalance = binding.startingBalanceField.getText().toString();
        if (tempBalance.isEmpty()){
            binding.startingBalanceField.setError("Starting Balance is required to create your profile. Set to 0 if necessary.");
        }

        try {
            balance = Double.parseDouble(tempBalance);

            if (balance < 0){
                binding.startingBalanceField.setError("Starting Balance cannot be a negative value!");
            } else {
                validBalance = true;
            }
        } catch (NumberFormatException e){
            binding.startingBalanceField.setError("Starting Balance cannot contain any letters or special characters.");
        }

        if (validUser && validAllowance && validBalance && validDays){
            // Save into SharedPreferences
            settingsManager.saveSettings(
                    usernameInput,
                    selectedType,
                    daysInput,
                    allowanceInput,
                    balance,
                    false // Dark mode default false
            );

            Toast.makeText(this, "Account setup finalized!", Toast.LENGTH_SHORT).show();

            // Navigate to the Dashboard
            navigateToDashboard();
        }
    }

    private void navigateToDashboard() {
        Intent intent = new Intent(this, DashboardActivity.class);
        startActivity(intent);
        finish(); // Closes the setup screen so they can't hit the back button to return
    }

    /**
     * Creates and displays the Help Dialog Overlay
     */
    private void showHelpOverlay() {
        // Create the dialog
        Dialog helpDialog = new Dialog(this);

        // Inflate the help XML layout using its binding class
        HelpInitializeActivityBinding dialogBinding = HelpInitializeActivityBinding.inflate(getLayoutInflater());
        helpDialog.setContentView(dialogBinding.getRoot());

        if (helpDialog.getWindow() != null) {
            helpDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            // --- THE WIDTH FIX ---
            // Calculate exactly 90% of the screen width
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.90);

            // Apply the new width instead of MATCH_PARENT
            helpDialog.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
            // ---------------------
        }

        // Wire up the close button
        dialogBinding.closeHelpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                helpDialog.dismiss(); // Closes the overlay
            }
        });

        // Show the overlay on the screen
        helpDialog.show();
    }
}