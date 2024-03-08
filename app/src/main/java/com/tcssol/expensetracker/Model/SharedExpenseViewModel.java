package com.tcssol.expensetracker.Model;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.tcssol.expensetracker.Utils.Wrapped;

import java.util.ArrayList;
import java.util.List;

public class SharedExpenseViewModel extends ViewModel {
    private static MutableLiveData<Wrapped> Object=new MutableLiveData<Wrapped>();
    private static final MutableLiveData<String> category=new MutableLiveData<>();
    private static final MutableLiveData<String> subCategory=new MutableLiveData<>();
    private static final MutableLiveData<String> medium=new MutableLiveData<>();
    private static boolean change=false;

    public static boolean getChange() {
        return change;
    }

    public static void setChange(boolean change) {
        SharedExpenseViewModel.change = change;
    }



    public static void setObject(MutableLiveData<Wrapped> object) {
        Object = object;
    }

    public MutableLiveData<String> getCategory() {
        return category;
    }

    public void setCategory(String text) {
        category.setValue(text);
    }

    public MutableLiveData<String> getSubCategory() {
        return subCategory;
    }

    public void setSubCategory(String text) {
        subCategory.setValue(text);
    }

    public MutableLiveData<String> getMedium() {
        return medium;
    }

    public void setMedium(String text) {
        medium.setValue(text);
    }

    public LiveData<Wrapped> getObject() {
        return Object;
    }

    public void setObject(Wrapped wrap) {
        Log.d("SEVM","Inside Sevm");
        Object.setValue(wrap);
        Log.d("SEVM","Inside Sevm value"+Object.toString());
    }
}
