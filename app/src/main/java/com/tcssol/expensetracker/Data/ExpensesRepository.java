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
    private final LiveData<List<ModeWrapper>> modeDist;

    public ExpensesRepository(Application application) {
        ExpensesDatabase database = ExpensesDatabase.getDatabase(application);
        this.expenseDao = database.expenseDao();
        this.allExpenses = expenseDao.getExpenses();
        this.groupedExpenses = expenseDao.getGroupedItems();
        this.modeDist = expenseDao.getModeDist();
    }

    /** Returns all expenses synchronously for export – call from a background Executor only. */
    public List<Expenses> getAllExpensesList() {
        return expenseDao.getExpensesAllList();
    }

    public LiveData<List<Expenses>> getGroupedExpenses() {
        return groupedExpenses;
    }

    public LiveData<List<Expenses>> getAllExpenses() {
        return allExpenses;
    }

    public void insert(Expenses expenses) {
        ExpensesDatabase.databaseWriterExecutor.execute(() -> expenseDao.insertExpense(expenses));
    }

    public LiveData<Expenses> get(long id) {
        return expenseDao.get(id);
    }

    public void update(Expenses expenses) {
        ExpensesDatabase.databaseWriterExecutor.execute(() -> expenseDao.update(expenses));
    }

    public void delete(Expenses expenses) {
        ExpensesDatabase.databaseWriterExecutor.execute(() -> expenseDao.delete(expenses));
    }

    public ExpenseDao getExpenseDao() {
        return expenseDao;
    }

    public LiveData<List<Expenses>> getMonthlyExpenses(LocalDate date) {
        String year = String.valueOf(date.getYear());
        String month = String.valueOf(date.getMonthValue());
        if (month.length() == 1) month = "0" + month;
        Log.d("expd", year + "" + month);
        return expenseDao.getMonth(month, year);
    }

    public LiveData<List<Expenses>> getGroupedMonthlyExpenses(LocalDate date) {
        String year = String.valueOf(date.getYear());
        String month = String.valueOf(date.getMonthValue());
        if (month.length() == 1) month = "0" + month;
        Log.d("expd", year + "" + month);
        return expenseDao.getGroupedMonth(month, year);
    }

    /**
     * Returns MutableLiveData with [earned, spend, received, given] summaries.
     * The DAO aggregate queries must also be called on a background thread,
     * so we post the result via MutableLiveData after an Executor call.
     */
    public MutableLiveData<List<Integer>> getFrag3DataFiltered(int montht, int yearl) {
        Log.d("expdao", montht + " data received in repo " + yearl);
        String month = montht < 10 ? "0" + montht : String.valueOf(montht);
        String year = String.valueOf(yearl);
        MutableLiveData<List<Integer>> result = new MutableLiveData<>();
        ExpensesDatabase.databaseWriterExecutor.execute(() -> {
            List<Integer> list = new ArrayList<>();
            list.add(expenseDao.getSumEarnedF(month, year));
            list.add(expenseDao.getSumSpendF(month, year));
            list.add(expenseDao.getSumReceivedF(month, year));
            list.add(expenseDao.getSumGivenF(month, year));
            result.postValue(list);
        });
        return result;
    }

    public MutableLiveData<List<Integer>> getFrag3Data() {
        MutableLiveData<List<Integer>> result = new MutableLiveData<>();
        ExpensesDatabase.databaseWriterExecutor.execute(() -> {
            List<Integer> list = new ArrayList<>();
            list.add(expenseDao.getSumEarned());
            list.add(expenseDao.getSumSpend());
            list.add(expenseDao.getSumReceived());
            list.add(expenseDao.getSumGiven());
            result.postValue(list);
        });
        return result;
    }

    public List<Expenses> getSubCats(String category, boolean type) {
        // Called from Executor thread in ViewModel – safe.
        return expenseDao.getSubCats(category, type);
    }

    public List<Expenses> getSubCatsF(int montht, String year, String category, boolean type) {
        String month = montht < 10 ? "0" + montht : String.valueOf(montht);
        // Called from Executor thread in ViewModel – safe.
        return expenseDao.getSubsCatsF(month, year, category, type);
    }

    public LiveData<List<ModeWrapper>> getModeDist() {
        return modeDist;
    }

    // --- v2.0 additions ---

    public LiveData<List<Expenses>> searchExpenses(String query) {
        return expenseDao.searchExpenses(query);
    }

    public LiveData<Double> getNetBalance(String month, String year) {
        return expenseDao.getNetBalance(month, year);
    }

    /** Returns monthly spend total – call from a background Executor only. */
    public double getMonthSpend(String month, String year) {
        return expenseDao.getMonthSpend(month, year);
    }
}
