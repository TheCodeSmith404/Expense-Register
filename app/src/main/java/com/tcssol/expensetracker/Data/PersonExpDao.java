package com.tcssol.expensetracker.Data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Database;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.tcssol.expensetracker.Model.Expenses;
import com.tcssol.expensetracker.Model.PersonExp;

import java.util.List;
@Dao
public interface PersonExpDao {
    @Insert
    void insertPersonExpense(PersonExp expenseEntry);
    @Query("DELETE FROM person_expenses")
    void deleteAll();
    @Query("SELECT * FROM person_expenses ORDER BY id DESC")
    LiveData<List<PersonExp>> getExpenses();
    @Query("SELECT id,name,sum(amount) as amount,type FROM person_expenses GROUP BY name,type ORDER BY name")
    LiveData<List<PersonExp>> getExpensesGrouped();
    @Query("SELECT * FROM person_expenses WHERE SUBSTR(date_created, 6, 2)=:month AND SUBSTR(date_created, 1, 4)=:year ORDER BY id DESC")
    LiveData<List<PersonExp>> getExpensesFiltered(String month,String year);
    @Query("SELECT id,name,sum(amount) as amount,type FROM person_expenses WHERE SUBSTR(date_created, 6, 2)=:month AND SUBSTR(date_created, 1, 4)=:year GROUP BY name,type ORDER BY name")
    LiveData<List<PersonExp>> getExpensesFilteredGrouped(String month,String year);
    @Query("SELECT * FROM person_expenses WHERE person_expenses.id=:id")
    LiveData<PersonExp> get(long id);
    @Update
    void update(PersonExp expense);
    @Delete
    void delete(PersonExp expense);
    @Query("SELECT * FROM person_expenses ORDER BY id ASC")
    List<PersonExp> getExpensesList();

}
