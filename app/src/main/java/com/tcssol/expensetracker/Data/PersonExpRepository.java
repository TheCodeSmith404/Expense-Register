package com.tcssol.expensetracker.Data;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.tcssol.expensetracker.Model.Expenses;
import com.tcssol.expensetracker.Model.PersonExp;

import java.util.List;
public class PersonExpRepository {
    private final PersonExpDao personExpDao;
    private final LiveData<List<PersonExp>> allExpenses;
    private final List<PersonExp> allExpensesList;
    private final LiveData<List<PersonExp>> allExpensesGrouped;
    public PersonExpRepository(Application application){
        PersonExpDatabase database=PersonExpDatabase.getDatabase(application);
        this.personExpDao=database.personExpDao();
        this.allExpenses=personExpDao.getExpenses();
        this.allExpensesList=personExpDao.getExpensesList();
        this.allExpensesGrouped= personExpDao.getExpensesGrouped();
    }

    public List<PersonExp> getAllExpensesList() {
        return allExpensesList;
    }
    public LiveData<List<PersonExp>> getAllExpensesGrouped(){
        return allExpensesGrouped;
    }
    public LiveData<List<PersonExp>> getAllExpensesGroupedFiltered(String month,String year){
        if(month.length()==1){
            month="0"+month;
        }
        return personExpDao.getExpensesFilteredGrouped(month,year);
    }
    public LiveData<List<PersonExp>> getAllExpensesFiltered(String month,String year){
        if(month.length()==1){
            month="0"+month;
        }
        return personExpDao.getExpensesFiltered(month,year);
    }

    public LiveData<List<PersonExp>> getAllExpenses(){return allExpenses;}
    public void insert(PersonExp personExp){
        PersonExpDatabase.databaseWriterExecutor.execute(()->personExpDao.insertPersonExpense(personExp));
    }
    public LiveData<PersonExp> get(long id){return personExpDao.get(id);}

    public void update(PersonExp exp){
        PersonExpDatabase.databaseWriterExecutor.execute(()->personExpDao.update(exp));
    }
    public void delete(PersonExp exp){
        PersonExpDatabase.databaseWriterExecutor.execute(()->personExpDao.delete(exp));
    }
    public PersonExpDao getPersonExpDao(){return personExpDao;}

}
