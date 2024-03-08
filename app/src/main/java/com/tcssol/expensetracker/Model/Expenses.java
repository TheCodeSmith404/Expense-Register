package com.tcssol.expensetracker.Model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;


import com.tcssol.expensetracker.Utils.Converters;

import java.time.LocalDate;
@Entity(tableName="expenses_table")
public class Expenses {
    @PrimaryKey(autoGenerate=true)
    private long id;
    @ColumnInfo(name="date_created")
    private LocalDate dateCreated;
    private String category;
    @ColumnInfo(name="sub_category")
    private String subCategory;
    private String mode;
    private double amount;
    private boolean type;

    @Ignore
    public Expenses(String category,String subCategory){
        this.category=category;
        this.subCategory=subCategory;
    }

    public Expenses(LocalDate dateCreated, String category, String subCategory,String mode, double amount, boolean type) {
        this.dateCreated = dateCreated;
        this.category = category;
        this.subCategory = subCategory;
        this.mode=mode;
        this.amount = amount;
        this.type = type;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public LocalDate getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(LocalDate dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSubCategory() {
        return subCategory;
    }

    public void setSubCategory(String subCategory) {
        this.subCategory = subCategory;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public boolean isType() {
        return type;
    }

    public void setType(boolean type) {
        this.type = type;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public static String toCsvFormat(Expenses expenses){
        String typeString=String.valueOf(expenses.isType());
        String temp= String.format("%d,%s,%s,%s,%s,%s,%s\n", expenses.getId(), expenses.getCategory(), expenses.getSubCategory(),expenses.getMode(), Converters.toString(expenses.getDateCreated()), typeString, expenses.getAmount());
        return temp;
    }
    public static String toTxtFormat(Expenses expenses){
        String typeString=String.valueOf(expenses.isType());
        String temp= String.format("%d\t%s\t%s\t%s\t%s\t%s\t%s\n", expenses.getId(), expenses.getCategory(), expenses.getSubCategory(),expenses.getMode(), Converters.toString(expenses.getDateCreated()), typeString, expenses.getAmount());
        return temp;
    }
}
