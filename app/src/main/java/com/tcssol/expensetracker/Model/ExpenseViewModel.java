package com.tcssol.expensetracker.Model;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.tcssol.expensetracker.Data.ExpenseDao;
import com.tcssol.expensetracker.Data.ExpensesDatabase;
import com.tcssol.expensetracker.Data.ExpensesRepository;
import com.tcssol.expensetracker.Model.DailySum;
import com.tcssol.expensetracker.Utils.ModeWrapper;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.Future;

public class ExpenseViewModel extends AndroidViewModel {
    public static ExpensesRepository repository;
    public final LiveData<List<Expenses>> allExpenses;
    public final LiveData<List<Expenses>> allExpensesGrouped;
    public final LiveData<List<ModeWrapper>> modeDist;
    public final LiveData<List<CategoryConfig>> allCategoryConfigs;

    // v2.0: Frag3 summary data – lazy via getFrag3Data() to avoid sync access
    private MutableLiveData<List<Integer>> _frag3Data;
    public LiveData<List<Integer>> frag3Data;

    public ExpenseViewModel(@NonNull Application application) {
        super(application);
        repository = new ExpensesRepository(application);
        this.allExpenses = repository.getAllExpenses();
        this.allExpensesGrouped = repository.getGroupedExpenses();
        this.modeDist = repository.getModeDist();
        this.allCategoryConfigs = repository.getAllCategoryConfigs();

        // Trigger initial summary fetch asynchronously
        _frag3Data = repository.getFrag3Data();
        frag3Data = _frag3Data;
    }

    public LiveData<List<CategoryConfig>> getAllCategoryConfigs() {
        return allCategoryConfigs;
    }

    public void insertCategoryConfig(CategoryConfig config) {
        repository.insertCategoryConfig(config);
    }

    /**
     * Returns sub-category breakdown. Must be called from a background thread.
     * Callers in Fragment1 already consumed this via a PopupWindow, which we now
     * protect by posting via the Executor.
     */
    public List<Expenses> getSubCatsF(int month, int year, String category, boolean type) {
        if (month > 0 && year > 0)
            return repository.getSubCatsF(month, String.valueOf(year), category, type);
        else
            return repository.getSubCats(category, type);
    }

    /** Export list – must be called from background thread (Executor) only. */
    public List<Expenses> getAllExpensesList() {
        return repository.getAllExpensesList();
    }

    public LiveData<List<Expenses>> getAllExpensesGrouped() { return allExpensesGrouped; }
    public LiveData<List<Expenses>> getAllExpenses() { return allExpenses; }

    public LiveData<List<Expenses>> getAllExpensesGroupedMonthly(LocalDate date) {
        return repository.getGroupedMonthlyExpenses(date);
    }

    public LiveData<List<Expenses>> getAllExpensesMonthly(LocalDate date) {
        return repository.getMonthlyExpenses(date);
    }

    public MutableLiveData<List<Integer>> getFrag3DataFiltered(int month, int year) {
        return repository.getFrag3DataFiltered(month, year);
    }

    public MutableLiveData<List<Integer>> getFrag3Data() {
        return repository.getFrag3Data();
    }

    public static void insert(Expenses task, Context context) {
        repository.insert(task);
    }

    public LiveData<Expenses> get(long id) { return repository.get(id); }

    public static void update(Expenses expenses) { repository.update(expenses); }

    public static void delete(Expenses expenses) { repository.delete(expenses); }

    public ExpenseDao getExpenseDao() { return repository.getExpenseDao(); }

    public LiveData<List<ModeWrapper>> getModeDist() { return modeDist; }

    // --- v2.0 additions ---

    /** Full-text search across category, subcategory, note, mode. Returns LiveData. */
    public LiveData<List<Expenses>> search(String query) {
        return repository.searchExpenses(query);
    }

    /** Net balance (earned - spent) for a given month/year as LiveData. */
    public LiveData<Double> getNetBalance(String month, String year) {
        return repository.getNetBalance(month, year);
    }

    /** Total Active Balance (Lifetime) as LiveData. */
    public LiveData<Double> getTotalNetBalance() {
        return repository.getTotalNetBalance();
    }

    /** Daily sums for the bar chart. */
    public LiveData<List<DailySum>> getDailySums(int month, int year) {
        return repository.getDailySums(month, year);
    }

    /**
     * Monthly spend total for budget check – runs on background Executor.
     * Delivers result via the callback on main thread.
     */
    public void getMonthSpendAsync(String month, String year, MonthSpendCallback callback) {
        ExpensesDatabase.databaseWriterExecutor.execute(() -> {
            double spend = repository.getMonthSpend(month, year);
            // Post back to main thread is handled by observer of callback in activity
            callback.onResult(spend);
        });
    }

    public interface MonthSpendCallback {
        void onResult(double totalSpend);
    }
}
