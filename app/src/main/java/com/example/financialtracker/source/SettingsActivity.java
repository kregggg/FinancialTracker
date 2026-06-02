package com.example.financialtracker.source;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import androidx.appcompat.app.AppCompatActivity;

import com.example.financialtracker.R;
import com.example.financialtracker.databinding.MainSettingsActivityBinding;
import com.example.financialtracker.databinding.ActionExpenseActivityBinding;

public class SettingsActivity extends AppCompatActivity {

    private MainSettingsActivityBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Inflate the main settings layout using View Binding
        binding = MainSettingsActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 2. Structural Spinner Setup (Ensures dropdown renders without UI text errors)
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.allowance_options, // Uses your existing options array
                R.layout.spinner_item       // Uses your custom visible item text style
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerAllowanceType.setAdapter(adapter);

        // --- NAVIGATION & SCREEN TRANSITIONS ---

        // Back Button: Closes settings to return back to the main dashboard menu
        binding.btnBackSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Submit Button: Processes validation gates and navigates forward
        binding.btnSubmitSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Put your required inputs logic guard here (e.g., if empty, return;)

                // Transition execution
                Intent intent = new Intent(SettingsActivity.this, DashboardActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // Help Option Trigger
        binding.tvHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Put transition code here to display your general help overlay if desired
            }
        });

        // --- QUICK ACTION TRIGGER TRANSITIONS ---

        binding.btnQuickAction1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showQuickActionDialog(1);
            }
        });

        binding.btnQuickAction2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showQuickActionDialog(2);
            }
        });

        binding.btnQuickAction3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showQuickActionDialog(3);
            }
        });
    }

    /**
     * Instantiates and manages the layout transition overlay for the Quick Action Macros.
     * @param actionNumber Integer distinguishing which button was targeted (1, 2, or 3).
     */
    private void showQuickActionDialog(final int actionNumber) {
        Dialog dialog = new Dialog(this);

        // Inflate the floating card macro layout
        ActionExpenseActivityBinding dialogBinding = ActionExpenseActivityBinding.inflate(getLayoutInflater());
        dialog.setContentView(dialogBinding.getRoot());

        // Format dialog window properties (90% width bounds + transparent margins)
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.90);
            dialog.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        // Dynamically alter title to state "Quick Action 1", "Quick Action 2", etc.
        dialogBinding.tvDialogTitle.setText("Quick Action " + actionNumber);

        // TODO: YOUR LOGIC - Check if macro data exists for this actionNumber and pre-fill fields here

        // --- DIALOG BUTTON TRANSITIONS ---

        // Cancel Button: Instantly closes down the overlay without modifying anything
        dialogBinding.btnCancelAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        // Remove Button: Deletes configuration data and resets UI representation back to base state
        dialogBinding.btnRemoveAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: YOUR LOGIC - Delete the database/SharedPrefs profile matching this actionNumber
                // TODO: YOUR LOGIC - Reset main layout button text back to default: "Action " + actionNumber

                dialog.dismiss();
            }
        });

        // Save Button: Captures text input specifications to cache configurations
        dialogBinding.btnSaveAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: YOUR LOGIC - Extract field data (dialogBinding.etActionDescription.getText(), etc.)
                // TODO: YOUR LOGIC - Save structural profile attributes
                // TODO: YOUR LOGIC - Change main layout button name matching your state:
                //       (e.g., if actionNumber == 1, change binding.btnQuickAction1 text to new description)

                dialog.dismiss();
            }
        });

        // Execute visual presentation transition
        dialog.show();
    }
}