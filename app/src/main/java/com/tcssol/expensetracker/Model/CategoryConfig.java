package com.tcssol.expensetracker.Model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "category_config_table")
public class CategoryConfig {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "category_name")
    private String categoryName;

    @ColumnInfo(name = "is_fixed")
    private boolean isFixed;

    @ColumnInfo(name = "budget")
    private double budget;

    public CategoryConfig(@NonNull String categoryName, boolean isFixed) {
        this.categoryName = categoryName;
        this.isFixed = isFixed;
        this.budget = 0.0;
    }

    @NonNull
    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(@NonNull String categoryName) {
        this.categoryName = categoryName;
    }

    public boolean isFixed() {
        return isFixed;
    }

    public void setFixed(boolean fixed) {
        isFixed = fixed;
    }

    public double getBudget() {
        return budget;
    }

    public void setBudget(double budget) {
        this.budget = budget;
    }
}
