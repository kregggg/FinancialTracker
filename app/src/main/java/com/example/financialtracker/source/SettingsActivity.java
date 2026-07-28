package com.example.financialtracker.source;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.financialtracker.R;
import com.example.financialtracker.database.DataBackupManager;
import com.example.financialtracker.databinding.HelpInitializeActivityBinding;
import com.example.financialtracker.databinding.MainSettingsActivityBinding;
import com.example.financialtracker.databinding.QuickactionExpenseActivityBinding;
import com.example.financialtracker.database.SettingsManager;

import org.json.JSONObject;

import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {

    private MainSettingsActivityBinding binding;
    private SettingsManager settingsManager;
    private ActivityResultLauncher<String> exportLauncher;
    private ActivityResultLauncher<String[]> importLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = MainSettingsActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        settingsManager = new SettingsManager(this);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.allowance_options,
                R.layout.spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerAllowanceType.setAdapter(adapter);
        initializeValues(adapter);

        // Synchronize settings menu macro layouts with persistent cache state values
        updateQuickActionButtons();
        binding.switchDarkMode.setEnabled(false); // TODO: add dark mode

        // --- NAVIGATION & SCREEN TRANSITIONS ---

        binding.btnBackSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        binding.btnSubmitSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean valid = validateSave();
                if (valid){
                    Intent intent = new Intent(SettingsActivity.this, DashboardActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });

        binding.tvHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showHelpOverlay();
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

        // inside onCreate:
        exportLauncher = registerForActivityResult(new ActivityResultContracts.CreateDocument("application/json"), uri -> {
            if (uri != null) exportBackup(uri);
        });
        importLauncher = registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
            if (uri != null) importBackup(uri);
        });

        binding.btnExportData.setOnClickListener(v -> exportLauncher.launch("financial_tracker_backup.json"));
        binding.btnImportData.setOnClickListener(v -> importLauncher.launch(new String[]{"application/json"}));
    }

    /**
     * Refreshes settings layout labels. Defaults to "Action N" if null; otherwise uses raw text.
     */
    private void updateQuickActionButtons() {
        String desc1 = settingsManager.getQuickActionDescription(1);
        String desc2 = settingsManager.getQuickActionDescription(2);
        String desc3 = settingsManager.getQuickActionDescription(3);

        binding.btnQuickAction1.setText(desc1 != null ? desc1 : "Action 1");
        binding.btnQuickAction2.setText(desc2 != null ? desc2 : "Action 2");
        binding.btnQuickAction3.setText(desc3 != null ? desc3 : "Action 3");
    }

    /**
     * Instantiates and manages the layout transition overlay for the Quick Action Macros.
     */
    private void showQuickActionDialog(final int actionNumber) {
        Dialog dialog = new Dialog(this);

        QuickactionExpenseActivityBinding dialogBinding = QuickactionExpenseActivityBinding.inflate(getLayoutInflater());
        dialog.setContentView(dialogBinding.getRoot());

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.90);
            dialog.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        dialogBinding.tvDialogTitle.setText("Quick Action " + actionNumber);

        // Set up the category spinner adapter inside the dialog popup card layout container
        ArrayAdapter<CharSequence> dialogSpinnerAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.allowance_options, // Using allowance options or a dedicated expense array
                R.layout.spinner_item
        );
        dialogSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dialogBinding.spinnerActionType.setAdapter(dialogSpinnerAdapter);

        // DATA RESTORATION LAYER: Pre-fill input cards if configuration exists
        String savedDesc = settingsManager.getQuickActionDescription(actionNumber);
        if (savedDesc != null) {
            dialogBinding.etActionDescription.setText(savedDesc);
            dialogBinding.etActionAmount.setText(String.valueOf(settingsManager.getQuickActionAmount(actionNumber)));

            int position = dialogSpinnerAdapter.getPosition(settingsManager.getQuickActionType(actionNumber));
            if (position >= 0) {
                dialogBinding.spinnerActionType.setSelection(position);
            }
        }

        // --- DIALOG BUTTON TRANSITIONS ---

        dialogBinding.btnCancelAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialogBinding.btnRemoveAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Completely clear settings preferences matching this button sequence allocation
                settingsManager.removeQuickAction(actionNumber);
                // Refresh activity panel text components
                updateQuickActionButtons();
                dialog.dismiss();
            }
        });

        dialogBinding.btnSaveAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String descInput = dialogBinding.etActionDescription.getText().toString().trim();
                String typeInput = dialogBinding.spinnerActionType.getSelectedItem() != null ?
                        dialogBinding.spinnerActionType.getSelectedItem().toString() : "Transportation";

                // Form validation gate
                if (descInput.isEmpty()) {
                    dialogBinding.etActionDescription.setError("Description is required!");
                    dialogBinding.etActionDescription.requestFocus();
                    return;
                }

                double amtInput = 0.0;
                try {
                    amtInput = Double.parseDouble(dialogBinding.etActionAmount.getText().toString().trim());
                } catch (NumberFormatException e) {
                    // Defaults tracking variables back to 0.0 if numerical data was omitted
                }

                // Write modifications persistently, reload UI textures, and dismiss
                settingsManager.saveQuickAction(actionNumber, descInput, amtInput, typeInput);
                updateQuickActionButtons();
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    /*
    TODO:
        switch off button for dark mode
        set the initial content of the following
            - username (etUsername)
            - allowance type (spinnerAllowanceType)
            - days with classes (etDaysWithClasses)
            - allocated allowance per time frame (etSettingsAllowance)
        set action for button submit
     */

    public void initializeValues(ArrayAdapter<CharSequence> adapter){
        updateQuickActionButtons();

        binding.etUsername.setText(settingsManager.getUsername());
        binding.etDaysWithClasses.setText(String.valueOf(settingsManager.getDaysWithClasses()));
        binding.etSettingsAllowance.setText(String.format(Locale.US, "%.2f", settingsManager.getAllowanceAmount()));

        String savedAllowanceType = settingsManager.getAllowanceType();
        if (savedAllowanceType != null) {
            int pos = adapter.getPosition(savedAllowanceType);
            if (pos >= 0) binding.spinnerAllowanceType.setSelection(pos);
        }
    }

    public boolean validateSave() {
        boolean valid = true;

        // Username
        String username = binding.etUsername.getText().toString().trim();
        if (username.isEmpty()) {
            binding.etUsername.setError("Please provide a username!");
            valid = false;
        }

        // Allowance Type
        String type = binding.spinnerAllowanceType.getSelectedItem() != null ?
                binding.spinnerAllowanceType.getSelectedItem().toString() : "Weekly";

        // Days with classes
        int days = 0;
        String tempDays = binding.etDaysWithClasses.getText().toString().trim();
        if (tempDays.isEmpty()) {
            binding.etDaysWithClasses.setError("Please fill in this field!");
            valid = false;
        } else {
            try {
                days = Integer.parseInt(tempDays);
                if (days > 7) {
                    binding.etDaysWithClasses.setError("Days with classes cannot exceed 7!");
                    valid = false;
                } else if (days <= 0 && type.equalsIgnoreCase("Daily")) {
                    binding.etDaysWithClasses.setError("Days with classes is required for daily allowance.");
                    valid = false;
                }
            } catch (NumberFormatException e) {
                binding.etDaysWithClasses.setError("Days with classes cannot contain letters or special characters!");
                valid = false;
            }
        }

        // Allowance amount
        double allowance = 0;
        String tempAllowance = binding.etSettingsAllowance.getText().toString().trim();
        if (tempAllowance.isEmpty()) {
            binding.etSettingsAllowance.setError("Please fill in this field!");
            valid = false;
        } else {
            try {
                allowance = Double.parseDouble(tempAllowance);
                if (allowance < 0) {
                    binding.etSettingsAllowance.setError("Allowance cannot be a negative value!");
                    valid = false;
                }
            } catch (NumberFormatException e) {
                binding.etSettingsAllowance.setError("Please enter a valid number.");
                valid = false;
            }
        }

        if (!valid) return false;

        // Preserve both balances exactly as they are — see note below on why
        // this can't use saveSettings() the way the old code did.
        double currentBalance = settingsManager.getCurrentBalance();
        double startingBalance = settingsManager.getStartingBalance();
        settingsManager.restoreSettings(username, type, days, allowance, startingBalance, currentBalance, false);

        return true;
    }

    /**
     * Same help overlay used in InitializeAccount — the setup fields and
     * settings fields are the same data, so the same explanation applies.
     */
    private void showHelpOverlay() {
        Dialog helpDialog = new Dialog(this);

        HelpInitializeActivityBinding dialogBinding = HelpInitializeActivityBinding.inflate(getLayoutInflater());
        helpDialog.setContentView(dialogBinding.getRoot());

        if (helpDialog.getWindow() != null) {
            helpDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.90);
            helpDialog.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        dialogBinding.closeHelpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                helpDialog.dismiss();
            }
        });

        helpDialog.show();
    }

    private void exportBackup(Uri uri) {
        try {
            DataBackupManager.writeToUri(this, uri, DataBackupManager.buildExportJson(this));
            Toast.makeText(this, "Data exported successfully!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Export failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void importBackup(Uri uri) {
        new AlertDialog.Builder(this)
                .setTitle("Import Data")
                .setMessage("This will replace all current settings and transactions with this backup file. Continue?")
                .setPositiveButton("Import", (dialog, which) -> {
                    try {
                        JSONObject data = DataBackupManager.readFromUri(this, uri);
                        DataBackupManager.applyImportedData(this, data);
                        Toast.makeText(this, "Data imported successfully!", Toast.LENGTH_SHORT).show();
                        recreate(); // reload this screen so every field reflects the restored settings
                    } catch (Exception e) {
                        Toast.makeText(this, "Import failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}