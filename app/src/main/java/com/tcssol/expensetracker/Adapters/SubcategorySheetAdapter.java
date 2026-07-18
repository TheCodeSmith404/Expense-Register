package com.tcssol.expensetracker.Adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.tcssol.expensetracker.Model.Expenses;
import com.tcssol.expensetracker.R;

import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SubcategorySheetAdapter extends RecyclerView.Adapter<SubcategorySheetAdapter.ViewHolder> {

    private final List<Expenses> subcategories;
    private final Context context;
    private final Map<String, Double> subcategoryBudgets;
    private final OnSubcategoryBudgetChangeListener listener;

    public interface OnSubcategoryBudgetChangeListener {
        void onBudgetChanged(String subcategoryConfigKey, double newBudget);
    }

    public SubcategorySheetAdapter(List<Expenses> subcategories, Context context, Map<String, Double> subcategoryBudgets, OnSubcategoryBudgetChangeListener listener) {
        this.subcategories = subcategories;
        this.context = context;
        this.subcategoryBudgets = subcategoryBudgets != null ? subcategoryBudgets : new HashMap<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sheet_subcategory, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Expenses expenses = subcategories.get(position);
        holder.tvName.setText(expenses.getSubCategory());

        String symbol = Currency.getInstance(Locale.getDefault()).getSymbol();
        holder.tvSpent.setText(symbol + String.format(Locale.getDefault(), "%,.0f", expenses.getAmount()));

        String subKey = expenses.getCategory() + "::" + expenses.getSubCategory();
        Double budgetLimitObj = subcategoryBudgets.get(subKey);
        double budgetLimit = budgetLimitObj != null ? budgetLimitObj : 0.0;

        if (budgetLimit > 0) {
            double remaining = budgetLimit - expenses.getAmount();
            if (remaining >= 0) {
                holder.tvBudget.setText("Budget: " + symbol + String.format(Locale.getDefault(), "%,.0f", budgetLimit) + " (" + symbol + String.format(Locale.getDefault(), "%,.0f", remaining) + " left)");
                holder.tvBudget.setTextColor(ContextCompat.getColor(context, R.color.colorSecondary));
            } else {
                holder.tvBudget.setText("Budget: " + symbol + String.format(Locale.getDefault(), "%,.0f", budgetLimit) + " (" + symbol + String.format(Locale.getDefault(), "%,.0f", -remaining) + " over)");
                holder.tvBudget.setTextColor(ContextCompat.getColor(context, R.color.red));
            }
            holder.tvBudget.setVisibility(View.VISIBLE);
        } else {
            holder.tvBudget.setVisibility(View.GONE);
        }

        holder.btnEdit.setOnClickListener(v -> {
            EditText input = new EditText(context);
            input.setInputType(InputType.TYPE_CLASS_NUMBER);
            if (budgetLimit > 0) {
                input.setText(String.format(Locale.getDefault(), "%.0f", budgetLimit));
            }
            input.setSelection(input.getText().length());

            new MaterialAlertDialogBuilder(context)
                    .setTitle("Set Subcategory Budget")
                    .setMessage("Limit for: " + expenses.getSubCategory())
                    .setView(input)
                    .setPositiveButton("Save", (dialog, which) -> {
                        String text = input.getText().toString().trim();
                        double newBudget = 0.0;
                        if (!text.isEmpty()) {
                            try {
                                newBudget = Double.parseDouble(text);
                            } catch (NumberFormatException e) {
                                Toast.makeText(context, "Invalid amount", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }
                        listener.onBudgetChanged(subKey, newBudget);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return subcategories.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        TextView tvBudget;
        TextView tvSpent;
        ImageButton btnEdit;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvSubcategoryName);
            tvBudget = itemView.findViewById(R.id.tvSubcategoryBudget);
            tvSpent = itemView.findViewById(R.id.tvSubcategorySpent);
            btnEdit = itemView.findViewById(R.id.btnEditSubcategoryBudget);
        }
    }
}
