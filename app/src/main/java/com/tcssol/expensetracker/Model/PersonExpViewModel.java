package com.tcssol.expensetracker.Model;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.tcssol.expensetracker.Data.PersonExpDao;
import com.tcssol.expensetracker.Data.PersonExpRepository;

import java.util.List;

public class PersonExpViewModel extends AndroidViewModel {
    public static PersonExpRepository personExpRepository;
    public final LiveData<List<PersonExp>> allExpenses;
    public final LiveData<List<PersonExp>> allExpensesGrouped;

    public PersonExpViewModel(@NonNull Application application) {
        super(application);
        personExpRepository = new PersonExpRepository(application);
        this.allExpenses = personExpRepository.getAllExpenses();
        this.allExpensesGrouped = personExpRepository.getAllExpensesGrouped();
        // ✅ No synchronous DB call here
    }

    public LiveData<List<PersonExp>> getAllExpensesGroupedFiltered(String month, String year) {
        return personExpRepository.getAllExpensesGroupedFiltered(month, year);
    }

    public LiveData<List<PersonExp>> getAllExpensesGrouped() {
        return allExpensesGrouped;
    }

    public LiveData<List<PersonExp>> getAllExpensesFiltered(String month, String year) {
        return personExpRepository.getAllExpensesFiltered(month, year);
    }

    /** Fetches list on a background thread and delivers via callback. */
    public void getAllExpensesList(java.util.function.Consumer<List<PersonExp>> callback) {
        personExpRepository.getAllExpensesList(callback);
    }

    /** Synchronous – only call from a background thread. */
    public List<PersonExp> getAllExpensesListSync() {
        return personExpRepository.getAllExpensesListSync();
    }

    public LiveData<List<PersonExp>> getAllExpenses() { return allExpenses; }

    public static void insert(PersonExp personExp) {
        personExpRepository.insert(personExp);
    }

    public static void delete(PersonExp personExp) { personExpRepository.delete(personExp); }

    public PersonExpDao getPersonExpDao() { return personExpRepository.getPersonExpDao(); }
}
