package com.tcssol.expensetracker.Adapters;

public interface OnEditItemClickListner {
    void onEditTextViewClick(String category);
    void onCategoryOptionsClick(String category, android.view.View anchorView);
    void addItem();
}
