package com.tcssol.expensetracker.Adapters;

import com.tcssol.expensetracker.Model.Expenses;

public interface CategoriesClickListener {
    void onCategoryClick(Expenses expenses);
    void onCategoryLongClick(Expenses expenses);
}
