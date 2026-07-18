package com.tcssol.expensetracker.Adapters;

import android.content.Context;
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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

public class TransactionsAdapter extends RecyclerView.Adapter<TransactionsAdapter.ViewHolder> {
    public List<Expenses> expensesList;
    public List<Expenses> expensesListNew;
    public final Context mContext;
    private final ExpenseDao expenseDao;
    private final TransactionsClickListener clickListener;

    public TransactionsAdapter(Context mContext, ExpenseDao expenseDao, List<Expenses> expensesList, TransactionsClickListener clickListener) {
        this.mContext = mContext;
        this.expenseDao = expenseDao;
        this.expensesList = expensesList;
        if (expensesList != null && !expensesList.isEmpty()) {
            this.expensesListNew = getNewList(expensesList);
        } else {
            this.expensesListNew = new ArrayList<>();
        }
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == 1) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fragment4item, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fragment4date, parent, false);
        }
        return new ViewHolder(view, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int color;
        Expenses expenses = expensesListNew.get(position);
        String symbol = Currency.getInstance(Locale.getDefault()).getSymbol();

        if (com.tcssol.expensetracker.Utils.TimeViewManager.isTimeViewMode()) {
            if (expenses.getCategory().equals("_*_")) {
                LocalDate date = expenses.getDateCreated();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy");
                holder.date.setText(date.format(formatter));
                holder.amount.setText(com.tcssol.expensetracker.Utils.TimeViewManager.formatAmount(-expenses.getAmount(), false));
            } else {
                if (expenses.isType()) {
                    holder.amount.setText(com.tcssol.expensetracker.Utils.TimeViewManager.formatAmount(expenses.getAmount(), true));
                    color = ContextCompat.getColor(mContext, R.color.income);
                } else {
                    holder.amount.setText(com.tcssol.expensetracker.Utils.TimeViewManager.formatAmount(-expenses.getAmount(), false));
                    color = ContextCompat.getColor(mContext, R.color.expense);
                }
                holder.mode.setText(expenses.getMode());
                holder.amount.setTextColor(color);
                holder.category.setText(expenses.getCategory());
                holder.subCategory.setText(expenses.getSubCategory());

                if (expenses.getNote() != null && !expenses.getNote().isEmpty()) {
                    holder.note.setText(expenses.getNote());
                    holder.note.setVisibility(View.VISIBLE);
                } else {
                    holder.note.setVisibility(View.GONE);
                }
            }
        } else {
            if (expenses.getCategory().equals("_*_")) {
                LocalDate date = expenses.getDateCreated();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy");
                holder.date.setText(date.format(formatter));
                holder.amount.setText("-" + symbol + String.format(Locale.getDefault(), "%,.2f", expenses.getAmount()));
            } else {
                if (expenses.isType()) {
                    holder.amount.setText(symbol + String.format(Locale.getDefault(), "%,.2f", expenses.getAmount()));
                    color = ContextCompat.getColor(mContext, R.color.income);
                } else {
                    holder.amount.setText("-" + symbol + String.format(Locale.getDefault(), "%,.2f", expenses.getAmount()));
                    color = ContextCompat.getColor(mContext, R.color.expense);
                }
                holder.mode.setText(expenses.getMode());
                holder.amount.setTextColor(color);
                holder.category.setText(expenses.getCategory());
                holder.subCategory.setText(expenses.getSubCategory());

                if (expenses.getNote() != null && !expenses.getNote().isEmpty()) {
                    holder.note.setText(expenses.getNote());
                    holder.note.setVisibility(View.VISIBLE);
                } else {
                    holder.note.setVisibility(View.GONE);
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return expensesListNew == null ? 0 : expensesListNew.size();
    }

    @Override
    public int getItemViewType(int position) {
        Expenses temp = expensesListNew.get(position);
        if (temp.getCategory().equals("_*_"))
            return 0;
        else
            return 1;
    }

    public List<Expenses> getNewList(List<Expenses> list) {
        if (list == null || list.isEmpty()) return new ArrayList<>();
        List<Expenses> ret = new ArrayList<>();
        
        LocalDate currentGroupDate = null;
        int currentHeaderIndex = -1;
        double currentGroupSpendSum = 0;
        
        for (Expenses expense : list) {
            LocalDate date = expense.getDateCreated();
            if (currentGroupDate == null || !date.isEqual(currentGroupDate)) {
                if (currentHeaderIndex != -1) {
                    ret.get(currentHeaderIndex).setAmount(currentGroupSpendSum);
                }
                
                currentGroupDate = date;
                currentHeaderIndex = ret.size();
                Expenses header = new Expenses(date, "_*_", null, null, 0, false);
                ret.add(header);
                currentGroupSpendSum = 0;
            }
            
            ret.add(expense);
            if (!expense.isType()) {
                currentGroupSpendSum += expense.getAmount();
            }
        }
        
        if (currentHeaderIndex != -1) {
            ret.get(currentHeaderIndex).setAmount(currentGroupSpendSum);
        }
        
        return ret;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView category;
        public TextView subCategory;
        public TextView amount;
        public TextView mode;
        public TextView note;
        public TextView date;

        public ViewHolder(@NonNull View itemView, int viewType) {
            super(itemView);
            if (viewType == 1) {
                category = itemView.findViewById(R.id.frag4category_text);
                subCategory = itemView.findViewById(R.id.frag4sub_category_txt);
                amount = itemView.findViewById(R.id.frag4amount_txt);
                mode = itemView.findViewById(R.id.frag4Mode);
                note = itemView.findViewById(R.id.frag4note_txt);
                itemView.setOnClickListener(this);
            } else {
                amount = itemView.findViewById(R.id.amount_txt);
                date = itemView.findViewById(R.id.textViewDate);
            }
        }

        @Override
        public void onClick(View v) {
            if (getAdapterPosition() != RecyclerView.NO_POSITION) {
                Expenses data = expensesListNew.get(getAdapterPosition());
                clickListener.onTransactionClick(data);
            }
        }
    }
}
