package com.tcssol.expensetracker.Adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.tcssol.expensetracker.Data.ExpenseDao;
import com.tcssol.expensetracker.Model.Expenses;
import com.tcssol.expensetracker.R;

import java.util.Currency;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class CategoriesAdapter extends RecyclerView.Adapter<CategoriesAdapter.ViewHolder> {
    public List<Expenses> expensesList;
    public final Context mContext;
    private final ExpenseDao expenseDao;
    private final CategoriesClickListener clickListener;
    private Set<String> fixedCategories = new HashSet<>();
    private java.util.Map<String, Double> categoryBudgets = new java.util.HashMap<>();

    public void setCategoryBudgets(java.util.Map<String, Double> budgets) {
        this.categoryBudgets = budgets != null ? budgets : new java.util.HashMap<>();
        notifyDataSetChanged();
    }

    public CategoriesAdapter(List<Expenses> expensesList, Context mContext, ExpenseDao expenseDao, CategoriesClickListener clickListener) {
        this.mContext = mContext;
        this.expenseDao = expenseDao;
        this.expensesList = expensesList;
        this.clickListener = clickListener;
    }

    public void setFixedCategories(Set<String> fixedCategories) {
        this.fixedCategories = fixedCategories != null ? fixedCategories : new HashSet<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment1item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Expenses expenses = expensesList.get(position);
        
        String categoryName = expenses.getCategory();
        boolean isFixed = fixedCategories.contains(categoryName);
        holder.category.setText(categoryName + (isFixed ? " (Fixed)" : ""));
        
        holder.subCategory.setText(expenses.getSubCategory());
        String symbol = Currency.getInstance(Locale.getDefault()).getSymbol();

        if (com.tcssol.expensetracker.Utils.TimeViewManager.isTimeViewMode()) {
            if (expenses.isType()) {
                int incomeColor = ContextCompat.getColor(mContext, R.color.income);
                holder.amount.setTextColor(incomeColor);
                holder.amount.setText(com.tcssol.expensetracker.Utils.TimeViewManager.formatAmount(expenses.getAmount(), true));
                if (holder.dot != null)
                    holder.dot.setBackgroundTintList(ColorStateList.valueOf(incomeColor));
            } else {
                int expenseColor = ContextCompat.getColor(mContext, R.color.expense);
                holder.amount.setTextColor(expenseColor);
                holder.amount.setText(com.tcssol.expensetracker.Utils.TimeViewManager.formatAmount(-expenses.getAmount(), false));
                if (holder.dot != null)
                    holder.dot.setBackgroundTintList(ColorStateList.valueOf(expenseColor));
            }
        } else {
            if (expenses.isType()) {
                int incomeColor = ContextCompat.getColor(mContext, R.color.income);
                holder.amount.setTextColor(incomeColor);
                holder.amount.setText(symbol + String.format(Locale.getDefault(), "%,.2f", expenses.getAmount()));
                if (holder.dot != null)
                    holder.dot.setBackgroundTintList(ColorStateList.valueOf(incomeColor));
            } else {
                int expenseColor = ContextCompat.getColor(mContext, R.color.expense);
                holder.amount.setTextColor(expenseColor);
                holder.amount.setText("-" + symbol + String.format(Locale.getDefault(), "%,.2f", expenses.getAmount()));
                if (holder.dot != null)
                    holder.dot.setBackgroundTintList(ColorStateList.valueOf(expenseColor));
            }
        }

        if (expenses.getNote() != null && !expenses.getNote().isEmpty()) {
            holder.note.setText(expenses.getNote());
            holder.note.setVisibility(View.VISIBLE);
        } else {
            holder.note.setVisibility(View.GONE);
        }

        Double budgetLimitObj = categoryBudgets.get(categoryName);
        double budgetLimit = budgetLimitObj != null ? budgetLimitObj : 0.0;
        if (holder.tvBudget != null) {
            if (budgetLimit > 0) {
                double spentVal = expenses.getAmount();
                double remaining = budgetLimit - spentVal;
                if (com.tcssol.expensetracker.Utils.TimeViewManager.isTimeViewMode()) {
                    String limitStr = com.tcssol.expensetracker.Utils.TimeViewManager.formatAmount(budgetLimit, false);
                    if (remaining >= 0) {
                        String remStr = com.tcssol.expensetracker.Utils.TimeViewManager.formatAmount(remaining, false);
                        holder.tvBudget.setText("Budget: " + limitStr + " (" + remStr + " left)");
                        holder.tvBudget.setTextColor(ContextCompat.getColor(mContext, R.color.colorSecondary));
                    } else {
                        String overStr = com.tcssol.expensetracker.Utils.TimeViewManager.formatAmount(-remaining, false);
                        holder.tvBudget.setText("Budget: " + limitStr + " (" + overStr + " over)");
                        holder.tvBudget.setTextColor(ContextCompat.getColor(mContext, R.color.red));
                    }
                } else {
                    if (remaining >= 0) {
                        holder.tvBudget.setText("Budget: " + symbol + String.format(Locale.getDefault(), "%,.0f", budgetLimit) + " (" + symbol + String.format(Locale.getDefault(), "%,.0f", remaining) + " left)");
                        holder.tvBudget.setTextColor(ContextCompat.getColor(mContext, R.color.colorSecondary));
                    } else {
                        holder.tvBudget.setText("Budget: " + symbol + String.format(Locale.getDefault(), "%,.0f", budgetLimit) + " (" + symbol + String.format(Locale.getDefault(), "%,.0f", -remaining) + " over)");
                        holder.tvBudget.setTextColor(ContextCompat.getColor(mContext, R.color.red));
                    }
                }
                holder.tvBudget.setVisibility(View.VISIBLE);
            } else {
                holder.tvBudget.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return expensesList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        public TextView category;
        public TextView subCategory;
        public TextView amount;
        public TextView note;
        public TextView tvBudget;
        public View dot;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            category = itemView.findViewById(R.id.category_text);
            subCategory = itemView.findViewById(R.id.sub_category_txt);
            amount = itemView.findViewById(R.id.amount_txt);
            note = itemView.findViewById(R.id.note_txt);
            tvBudget = itemView.findViewById(R.id.tvCategoryItemBudget);
            dot = itemView.findViewById(R.id.categoryDot);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (getAdapterPosition() != RecyclerView.NO_POSITION) {
                clickListener.onCategoryClick(expensesList.get(getAdapterPosition()));
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if (getAdapterPosition() != RecyclerView.NO_POSITION) {
                clickListener.onCategoryLongClick(expensesList.get(getAdapterPosition()));
                return true;
            }
            return false;
        }
    }
}
