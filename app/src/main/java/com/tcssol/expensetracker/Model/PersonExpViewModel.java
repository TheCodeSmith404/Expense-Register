package com.tcssol.expensetracker.Model;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.tcssol.expensetracker.Data.PersonExpDao;
import com.tcssol.expensetracker.Data.PersonExpRepository;

import java.util.ConcurrentModificationException;
import java.util.List;

public class PersonExpViewModel extends AndroidViewModel {
    public static PersonExpRepository personExpRepository;
    public final LiveData<List<PersonExp>> allExpenses;
    public final LiveData<List<PersonExp>> allExpensesGrouped;
    private final List<PersonExp> allExpensesList;

    public PersonExpViewModel(@NonNull Application application) {
        super(application);
        personExpRepository=new PersonExpRepository(application);
        this.allExpenses= personExpRepository.getAllExpenses();
        this.allExpensesList=personExpRepository.getAllExpensesList();
        this.allExpensesGrouped= personExpRepository.getAllExpensesGrouped();
    }
    public LiveData<List<PersonExp>> getAllExpensesGroupedFiltered(String month,String year){
        return personExpRepository.getAllExpensesGroupedFiltered(month,year);
    }
    public LiveData<List<PersonExp>> getAllExpensesGrouped(){
        return allExpensesGrouped;
    }
    public LiveData<List<PersonExp>> getAllExpensesFiltered(String month,String year){
        return personExpRepository.getAllExpensesFiltered(month,year);
    }

    public List<PersonExp> getAllExpensesList() {
        return allExpensesList;
    }

    public LiveData<List<PersonExp>> getAllExpenses(){return  allExpenses;}
    public static void insert(PersonExp personExp){
        personExpRepository.insert(personExp);
    }
    public static void delete(PersonExp personExp){personExpRepository.delete(personExp);}
    public PersonExpDao getPersonExpDao(){return personExpRepository.getPersonExpDao();}
}
