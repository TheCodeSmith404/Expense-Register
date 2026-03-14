package com.tcssol.expensetracker;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.drawable.DrawableCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class Settings extends AppCompatActivity {
    private Toolbar toolbar;
    private TextInputEditText editBudget;
    private MaterialButton btnSaveBudget;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        toolbar = findViewById(R.id.materialToolbar);
        toolbar.setSubtitle("Settings");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Drawable drawable = toolbar.getNavigationIcon();
        if (drawable != null) {
            drawable = DrawableCompat.wrap(drawable);
            DrawableCompat.setTint(drawable, Color.WHITE);
            toolbar.setNavigationIcon(drawable);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        editBudget = findViewById(R.id.editBudget);
        btnSaveBudget = findViewById(R.id.btnSaveBudget);

        prefs = getSharedPreferences("com.tcs.expensetracker.stored_categories", Context.MODE_PRIVATE);

        // Pre-fill saved budget
        float savedBudget = prefs.getFloat("MONTHLY_BUDGET", 0f);
        if (savedBudget > 0) {
            editBudget.setText(String.valueOf((int) savedBudget));
        }

        btnSaveBudget.setOnClickListener(v -> {
            String input = editBudget.getText() != null ? editBudget.getText().toString().trim() : "";
            float budget = 0f;
            if (!input.isEmpty()) {
                try {
                    budget = Float.parseFloat(input);
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            prefs.edit().putFloat("MONTHLY_BUDGET", budget).apply();
            Toast.makeText(this, budget > 0
                    ? "Budget set to ₹" + (int) budget
                    : "Budget cleared", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}