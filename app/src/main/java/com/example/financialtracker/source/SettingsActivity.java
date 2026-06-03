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
import com.example.financialtracker.ref.SettingsManager;

public class SettingsActivity extends AppCompatActivity {

    private MainSettingsActivityBinding binding;
    private SettingsManager settingsManager;

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

        // Synchronize settings menu macro layouts with persistent cache state values
        updateQuickActionButtons();

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
                // Optional TODO: Map and validate your user modifications here before saving
                Intent intent = new Intent(SettingsActivity.this, DashboardActivity.class);
                startActivity(intent);
                finish();
            }
        });

        binding.tvHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Help overlay panel initialization
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

        ActionExpenseActivityBinding dialogBinding = ActionExpenseActivityBinding.inflate(getLayoutInflater());
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
}