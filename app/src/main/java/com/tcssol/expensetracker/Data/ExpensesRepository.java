package com.tcssol.expensetracker.Data;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.tcssol.expensetracker.Model.Expenses;
import com.tcssol.expensetracker.Utils.ModeWrapper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ExpensesRepository {
    private final ExpenseDao expenseDao;
    private final LiveData<List<Expenses>> allExpenses;
    private final LiveData<List<Expenses>> groupedExpenses;
    private final List<Expenses> allExpensesList;
    private final LiveData<List<ModeWrapper>> modeDist;
    private Application application;


    public ExpensesRepository(Application application) {
        ExpensesDatabase database=ExpensesDatabase.getDatabase(application);
        this.expenseDao = database.expenseDao();
        this.allExpenses= expenseDao.getExpenses();
        this.groupedExpenses=expenseDao.getGroupedItems();
        this.allExpensesList=expenseDao.getExpensesAllList();
        this.modeDist=expenseDao.getModeDist();
        this.application=application;

    }

    public List<Expenses> getAllExpensesList() {
        return allExpensesList;
    }

    public LiveData<List<Expenses>> getGroupedExpenses() {
        return groupedExpenses;
    }

    public LiveData<List<Expenses>> getAllExpenses(){return allExpenses;}

    public void insert(Expenses expenses){
        ExpensesDatabase.databaseWriterExecutor.execute(()->expenseDao.insertExpense(expenses));
    }
    public LiveData<Expenses> get(long id){return expenseDao.get(id);}

    public void update(Expenses expenses){
        ExpensesDatabase.databaseWriterExecutor.execute(()->expenseDao.update(expenses));
    }
    public void delete(Expenses expenses){
        ExpensesDatabase.databaseWriterExecutor.execute(()->expenseDao.delete(expenses));
    }
    public ExpenseDao getExpenseDao(){return expenseDao;}
    public LiveData<List<Expenses>> getMonthlyExpenses(LocalDate date) {
        String year=String.valueOf(date.getYear());
        String month=String.valueOf(date.getMonthValue());
        if(month.length()==1){
            month="0"+month;
        }
        Log.d("expd",year+""+month);
        return expenseDao.getMonth(month,year);
    }

    public LiveData<List<Expenses>> getGroupedMonthlyExpenses(LocalDate date) {
        String year=String.valueOf(date.getYear());
        String month=String.valueOf(date.getMonthValue());
        if(month.length()==1){
            month="0"+month;
        }
        Log.d("expd",year+""+month);
        return expenseDao.getGroupedMonth(month,year);
    }
    public MutableLiveData<List<Integer>> getFrag3DataFiltered(int montht, int yearl){
        Log.d("expdao",montht+" data received in repo "+yearl);
        String month=String.valueOf(montht);
        if(month.length()==1){
            month="0"+montht;
        }
        String year=String.valueOf(yearl);
        Log.d("expdao",month+" dat converted in repo "+year);
        List<Integer> list=new ArrayList<>();
        list.add(expenseDao.getSumEarnedF(month,year));
        list.add(expenseDao.getSumSpendF(month,year));
        list.add(expenseDao.getSumReceivedF(month,year));
        list.add(expenseDao.getSumGivenF(month,year));
        return new MutableLiveData<>(list);

    }
    public MutableLiveData<List<Integer>> getFrag3Data(){
        List<Integer> list=new ArrayList<>();
        list.add(expenseDao.getSumEarned());
        list.add(expenseDao.getSumSpend());
        list.add(expenseDao.getSumReceived());
        list.add(expenseDao.getSumGiven());
        return new MutableLiveData<>(list);
    }

    public List<Expenses> getSubCats(String category,boolean type) {
        return expenseDao.getSubCats(category,type);
    }

    public List<Expenses> getSubCatsF(int montht, String year,String category,boolean type) {
        String month=String.valueOf(montht);
        if(month.length()==1){
            month="0"+montht;
        }
        return expenseDao.getSubsCatsF(month,year,category,type);

    }
    public LiveData<List<ModeWrapper>> getModeDist(){
        return modeDist;
    }
}
