package com.tcssol.expensetracker.Data;

import android.view.Display;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Ignore;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.tcssol.expensetracker.Model.Expenses;
import com.tcssol.expensetracker.Utils.ModeWrapper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Dao
public interface ExpenseDao {

    @Insert
    void insertExpense(Expenses expenseEntry);

    @Query("DELETE FROM expenses_table")
    void deleteAll();
    @Query("SELECT * FROM expenses_table ORDER BY id DESC")
    LiveData<List<Expenses>> getExpenses();
    @Query("SELECT id, category, SUM(amount) AS amount, type, GROUP_CONCAT(DISTINCT sub_category) AS sub_category FROM EXPENSES_TABLE GROUP BY category, type")
    LiveData<List<Expenses>> getGroupedItems();

    @Query("SELECT * FROM EXPENSES_TABLE WHERE expenses_table.id=:id")
    LiveData<Expenses> get(long id);
//    @Query("UPDATE task_table SET orderId = :position WHERE task_id = :taskId")
//    void updateTaskOrder(int taskId, int position);

    @Update
    void update(Expenses expense);

    @Delete
    void delete(Expenses expense);
    @Query("SELECT * FROM EXPENSES_TABLE WHERE SUBSTR(date_created, 6, 2)=:month AND SUBSTR(date_created, 1, 4)=:year ORDER BY id DESC")
    LiveData<List<Expenses>> getMonth(String  month,String year);
    @Query("SELECT id, category, SUM(amount) AS amount, type, GROUP_CONCAT(DISTINCT sub_category) AS sub_category,date_created FROM (SELECT * FROM EXPENSES_TABLE WHERE SUBSTR(date_created, 6, 2)=:month AND SUBSTR(date_created, 1, 4)=:year)  GROUP BY category, type")
    LiveData<List<Expenses>> getGroupedMonth(String  month,String year);
    @Query("SELECT SUM(amount) FROM (SELECT * FROM EXPENSES_TABLE WHERE type=false) WHERE category NOT IN(\"Money Received\",\"Money Given\")")
    Integer getSumSpend();
    @Query("SELECT SUM(amount) FROM (SELECT * FROM EXPENSES_TABLE WHERE type=true) WHERE category NOT IN(\"Money Received\",\"Money Given\")")
    Integer getSumEarned();
    @Query("SELECT SUM(amount) FROM expenses_table WHERE category =\"Money Given\"")
    Integer getSumGiven();
    @Query("SELECT SUM(amount) FROM expenses_table WHERE category =\"Money Received\"")
    Integer getSumReceived();
    @Query("SELECT SUM(amount) FROM (SELECT * FROM EXPENSES_TABLE WHERE SUBSTR(date_created, 6, 2)=:month AND SUBSTR(date_created, 1, 4)=:year AND type=false AND category NOT IN(\"Money Received\",\"Money Given\") ) ")
    Integer getSumSpendF(String month,String year);
    @Query("SELECT SUM(amount) FROM (SELECT * FROM EXPENSES_TABLE WHERE SUBSTR(date_created, 6, 2)=:month AND SUBSTR(date_created, 1, 4)=:year AND type=true AND category NOT IN(\"Money Received\",\"Money Given\") ) ")
    Integer getSumEarnedF(String month,String year);
    @Query("SELECT SUM(amount) FROM (SELECT * FROM EXPENSES_TABLE WHERE SUBSTR(date_created,6,2)=:month AND SUBSTR(date_created,1,4)=:year AND category=\"Money Given\")")
    Integer getSumGivenF(String month,String year);
    @Query("SELECT SUM(amount) FROM (SELECT * FROM EXPENSES_TABLE WHERE SUBSTR(date_created,6,2)=:month AND SUBSTR(date_created,1,4)=:year AND category=\"Money Received\")")
    Integer getSumReceivedF(String month,String year);
    @Query("SELECT * FROM expenses_table ORDER BY id ASC")
    List<Expenses> getExpensesAllList();
    @Query("SELECT id,category,sub_category,SUM(amount) as amount,type from expenses_table where category=:category AND type=:type GROUP BY sub_category")
    List<Expenses> getSubCats(String category,boolean type);
@Query("SELECT id,category,sub_category,SUM(amount) as amount,type from(SELECT * FROM EXPENSES_TABLE WHERE SUBSTR(date_created, 6, 2)=:month AND SUBSTR(date_created, 1, 4)=:year)where category=:category AND type=:type GROUP BY sub_category")
    List<Expenses> getSubsCatsF(String month, String year, String category,boolean type);
    @Query("SELECT mode as Mode,CAST(COUNT(mode) AS REAL)*100/ (Select count(mode) AS count from expenses_table) as Count from expenses_table GROUP BY mode")
    LiveData<List<ModeWrapper>> getModeDist();

//    @Ignore
//    default MutableLiveData<List<Integer>> giveResult(String month, String year){
//        List<Integer> list=new ArrayList<>();
//        list.add(getSumEarnedF(month,year));
//        list.add(getSumSpendF(month,year));
//        list.add(getSumReceivedF(month,year));
//        list.add(getSumGivenF(month,year));
//        return new MutableLiveData<>(list);
//
//    }
}
