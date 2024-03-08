package com.tcssol.expensetracker.Model;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.tcssol.expensetracker.Data.ExpenseDao;
import com.tcssol.expensetracker.Data.ExpensesRepository;
import com.tcssol.expensetracker.Utils.ModeWrapper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ExpenseViewModel extends AndroidViewModel {
    public static ExpensesRepository repository;
    public final LiveData<List<Expenses>> allExpenses;
    public final LiveData<List<Expenses>> allExpensesGrouped;
    public final List<Expenses> allExpensesList;
    public final LiveData<List<ModeWrapper>> modeDist;
    public volatile List<Expenses> temp;


    public ExpenseViewModel(@NonNull Application application) {
        super(application);
        repository=new ExpensesRepository(application);
        this.allExpenses = repository.getAllExpenses();
        this.allExpensesGrouped= repository.getGroupedExpenses();
        this.allExpensesList= repository.getAllExpensesList();
        this.modeDist=repository.getModeDist();
    }
    public List<Expenses> getSubCatsF(int month,int year,String category,boolean type){
        if(month>0&&year>0)
            return repository.getSubCatsF(month,String.valueOf(year),category,type);
        else
            return repository.getSubCats(category,type);
//        List<Expenses> list=new ArrayList<>();
//        Runnable runnable=new Runnable() {
//            @Override
//            public void run() {
//                if(month>0&&year>0)
//                    temp=repository.getSubCatsF(month,String.valueOf(year),category,type);
//                else
//                    temp=repository.getSubCats(category,type);
//                Log.d("BackEnd",temp.toString());
//
//            }
//        };
//
//        Handler handler=new Handler();
//        handler.post(runnable);
//        Log.d("BackEnd",temp.toString());
//        return list;

    }

    public List<Expenses> getAllExpensesList() {
        return allExpensesList;
    }

    public LiveData<List<Expenses>> getAllExpensesGrouped() {
        return allExpensesGrouped;
    }

    public LiveData<List<Expenses>> getAllExpenses(){return allExpenses;}
    public LiveData<List<Expenses>> getAllExpensesGroupedMonthly(LocalDate date){
        return repository.getGroupedMonthlyExpenses(date);
    }
    public LiveData<List<Expenses>> getAllExpensesMonthly(LocalDate date){
        return repository.getMonthlyExpenses(date);
    }
    public MutableLiveData<List<Integer>> getFrag3DataFiltered(int month, int year){
        return repository.getFrag3DataFiltered(month,year);
    }
    public MutableLiveData<List<Integer>> getFrag3Data(){
        return repository.getFrag3Data();

    }
    public static void insert(Expenses task,Context context){
        repository.insert(task);}
    public LiveData<Expenses> get(long id){return repository.get(id);}
    public static void update(Expenses expenses){ repository.update(expenses);}
    public static void delete(Expenses expenses){repository.delete(expenses);}
    public ExpenseDao getExpenseDao() {
        return repository.getExpenseDao();
    }
    public LiveData<List<ModeWrapper>> getModeDist(){
        return modeDist;
    }
}
