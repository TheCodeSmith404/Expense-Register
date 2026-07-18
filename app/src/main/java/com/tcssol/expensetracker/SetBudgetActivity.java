package com.tcssol.expensetracker;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.tcssol.expensetracker.Model.CategoryConfig;
import com.tcssol.expensetracker.Model.ExpenseViewModel;
import com.tcssol.expensetracker.Utils.WorkwithJSONStrings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SetBudgetActivity extends AppCompatActivity {

    private ExpenseViewModel expenseViewModel;
    private RecyclerView rvBudgets;
    private BudgetAdapter adapter;
    private List<BudgetItem> budgetItems = new ArrayList<>();
    private Map<String, Double> currentBudgets = new HashMap<>();
    private Map<String, Boolean> currentFixedState = new HashMap<>();

    public static class BudgetItem {
        public String displayName;
        public String configKey;
        public boolean isSubcategory;
        public double enteredBudget = 0.0;

        public BudgetItem(String displayName, String configKey, boolean isSubcategory) {
            this.displayName = displayName;
            this.configKey = configKey;
            this.isSubcategory = isSubcategory;
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_budget);

        Toolbar toolbar = findViewById(R.id.toolbarSetBudget);
        toolbar.setTitle("Configure Budgets");
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        Drawable drawable = toolbar.getNavigationIcon();
        if (drawable != null) {
            drawable = DrawableCompat.wrap(drawable);
            DrawableCompat.setTint(drawable, Color.WHITE);
            toolbar.setNavigationIcon(drawable);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        rvBudgets = findViewById(R.id.rvBudgets);
        rvBudgets.setLayoutManager(new LinearLayoutManager(this));

        // Load categories & subcategories
        SharedPreferences sharedPreferences = getSharedPreferences("com.tcs.expensetracker.stored_categories", MODE_PRIVATE);
        String storedCategories = sharedPreferences.getString("STORED_CATEGORIES", getResources().getString(R.string.category_subcategory));
        WorkwithJSONStrings jsonStrings = new WorkwithJSONStrings(storedCategories);

        List<String> categories = jsonStrings.getList("_elementlist");
        for (String category : categories) {
            // Add parent category
            budgetItems.add(new BudgetItem(category, category, false));
            // Add subcategories
            List<String> subcategories = jsonStrings.getList(category);
            if (subcategories != null) {
                for (String sub : subcategories) {
                    budgetItems.add(new BudgetItem(sub, category + "::" + sub, true));
                }
            }
        }

        adapter = new BudgetAdapter();
        rvBudgets.setAdapter(adapter);

        expenseViewModel = new ViewModelProvider(this).get(ExpenseViewModel.class);
        expenseViewModel.getAllCategoryConfigs().observe(this, configs -> {
            if (configs != null) {
                for (CategoryConfig config : configs) {
                    currentBudgets.put(config.getCategoryName(), config.getBudget());
                    currentFixedState.put(config.getCategoryName(), config.isFixed());
                }
                // Pre-populate budget item amounts from DB
                for (BudgetItem item : budgetItems) {
                    if (currentBudgets.containsKey(item.configKey)) {
                        item.enteredBudget = currentBudgets.get(item.configKey);
                    }
                }
                adapter.notifyDataSetChanged();
            }
        });

        MaterialButton btnSave = findViewById(R.id.btnSaveBudgets);
        btnSave.setOnClickListener(v -> {
            // Save all items to DB
            for (BudgetItem item : budgetItems) {
                boolean isFixed = currentFixedState.containsKey(item.configKey) && currentFixedState.get(item.configKey);
                CategoryConfig config = new CategoryConfig(item.configKey, isFixed);
                config.setBudget(item.enteredBudget);
                expenseViewModel.insertCategoryConfig(config);
            }
            Toast.makeText(this, "Budgets saved successfully", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private class BudgetAdapter extends RecyclerView.Adapter<BudgetAdapter.ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_budget_config, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            BudgetItem item = budgetItems.get(position);
            holder.tvName.setText(item.displayName);
            
            // Subcategory visual indent
            int paddingStart = item.isSubcategory ? 48 : 8;
            float density = getResources().getDisplayMetrics().density;
            holder.tvName.setPadding((int)(paddingStart * density), holder.tvName.getPaddingTop(), holder.tvName.getPaddingRight(), holder.tvName.getPaddingBottom());
            
            if (item.isSubcategory) {
                holder.tvName.setTextAppearance(androidx.appcompat.R.style.TextAppearance_AppCompat_Body1);
            } else {
                holder.tvName.setTextAppearance(androidx.appcompat.R.style.TextAppearance_AppCompat_Menu);
            }

            // Remove existing watcher if any to prevent recycling triggers
            if (holder.textWatcher != null) {
                holder.etValue.removeTextChangedListener(holder.textWatcher);
            }

            if (item.enteredBudget > 0) {
                holder.etValue.setText(String.format(Locale.getDefault(), "%.0f", item.enteredBudget));
            } else {
                holder.etValue.setText("");
            }

            holder.textWatcher = new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override
                public void afterTextChanged(Editable s) {
                    String str = s.toString().trim();
                    if (str.isEmpty()) {
                        item.enteredBudget = 0.0;
                    } else {
                        try {
                            item.enteredBudget = Double.parseDouble(str);
                        } catch (NumberFormatException e) {
                            item.enteredBudget = 0.0;
                        }
                    }
                }
            };
            holder.etValue.addTextChangedListener(holder.textWatcher);
        }

        @Override
        public int getItemCount() {
            return budgetItems.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName;
            TextInputEditText etValue;
            TextWatcher textWatcher;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvBudgetItemName);
                etValue = itemView.findViewById(R.id.etBudgetValue);
            }
        }
    }
}
