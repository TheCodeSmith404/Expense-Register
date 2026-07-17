package com.tcssol.expensetracker.Adapters;

import com.tcssol.expensetracker.Model.Expenses;

public interface TransactionsClickListener {
    void onTransactionClick(Expenses expenses);
    void onTransactionLongClick(Expenses expenses, int position);
}
