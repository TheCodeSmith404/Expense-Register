package com.tcssol.expensetracker.Data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.tcssol.expensetracker.Model.DailySum;
import com.tcssol.expensetracker.Model.Expenses;
import com.tcssol.expensetracker.Model.MonthlySum;
import com.tcssol.expensetracker.Utils.ModeWrapper;

import java.util.List;

@Dao
public interface ExpenseDao {

    @Insert
    void insertExpense(Expenses expenseEntry);

    @Query("DELETE FROM expenses_table")
    void deleteAll();

    @Query("SELECT * FROM expenses_table ORDER BY id DESC")
    LiveData<List<Expenses>> getExpenses();

    @SuppressWarnings(androidx.room.RoomWarnings.CURSOR_MISMATCH)
    @Query("SELECT id, category, SUM(amount) AS amount, type, GROUP_CONCAT(DISTINCT sub_category) AS sub_category FROM EXPENSES_TABLE GROUP BY category, type")
    LiveData<List<Expenses>> getGroupedItems();

    @Query("SELECT * FROM EXPENSES_TABLE WHERE expenses_table.id=:id")
    LiveData<Expenses> get(long id);

    @Update
    void update(Expenses expense);

    @Delete
    void delete(Expenses expense);

    @Query("SELECT * FROM EXPENSES_TABLE WHERE SUBSTR(date_created, 6, 2)=:month AND SUBSTR(date_created, 1, 4)=:year ORDER BY id DESC")
    LiveData<List<Expenses>> getMonth(String month, String year);

    @SuppressWarnings(androidx.room.RoomWarnings.CURSOR_MISMATCH)
    @Query("SELECT id, category, SUM(amount) AS amount, type, GROUP_CONCAT(DISTINCT sub_category) AS sub_category,date_created FROM (SELECT * FROM EXPENSES_TABLE WHERE SUBSTR(date_created, 6, 2)=:month AND SUBSTR(date_created, 1, 4)=:year)  GROUP BY category, type")
    LiveData<List<Expenses>> getGroupedMonth(String month, String year);

    // Aggregate queries (must be called from a background thread / Executor)
    @Query("SELECT SUM(amount) FROM (SELECT * FROM EXPENSES_TABLE WHERE type=false)")
    Integer getSumSpend();

    @Query("SELECT SUM(amount) FROM (SELECT * FROM EXPENSES_TABLE WHERE type=true)")
    Integer getSumEarned();

    @Query("SELECT SUM(amount) FROM expenses_table WHERE category =\"Money Given\"")
    Integer getSumGiven();

    @Query("SELECT SUM(amount) FROM expenses_table WHERE category =\"Money Received\"")
    Integer getSumReceived();

    @Query("SELECT SUM(amount) FROM (SELECT * FROM EXPENSES_TABLE WHERE SUBSTR(date_created, 6, 2)=:month AND SUBSTR(date_created, 1, 4)=:year AND type=false) ")
    Integer getSumSpendF(String month, String year);

    @Query("SELECT SUM(amount) FROM (SELECT * FROM EXPENSES_TABLE WHERE SUBSTR(date_created, 6, 2)=:month AND SUBSTR(date_created, 1, 4)=:year AND type=true) ")
    Integer getSumEarnedF(String month, String year);

    @Query("SELECT SUM(amount) FROM (SELECT * FROM EXPENSES_TABLE WHERE SUBSTR(date_created,6,2)=:month AND SUBSTR(date_created,1,4)=:year AND category=\"Money Given\")")
    Integer getSumGivenF(String month, String year);

    @Query("SELECT SUM(amount) FROM (SELECT * FROM EXPENSES_TABLE WHERE SUBSTR(date_created,6,2)=:month AND SUBSTR(date_created,1,4)=:year AND category=\"Money Received\")")
    Integer getSumReceivedF(String month, String year);

    // Returns LiveData instead of a sync list (used for export – called via Executor in repo)
    @Query("SELECT * FROM expenses_table ORDER BY id ASC")
    List<Expenses> getExpensesAllList();

    @SuppressWarnings(androidx.room.RoomWarnings.CURSOR_MISMATCH)
    @Query("SELECT id,category,sub_category,SUM(amount) as amount,type from expenses_table where category=:category AND type=:type GROUP BY sub_category")
    List<Expenses> getSubCats(String category, boolean type);

    @SuppressWarnings(androidx.room.RoomWarnings.CURSOR_MISMATCH)
    @Query("SELECT id,category,sub_category,SUM(amount) as amount,type from(SELECT * FROM EXPENSES_TABLE WHERE SUBSTR(date_created, 6, 2)=:month AND SUBSTR(date_created, 1, 4)=:year)where category=:category AND type=:type GROUP BY sub_category")
    List<Expenses> getSubsCatsF(String month, String year, String category, boolean type);

    @Query("SELECT mode as Mode,CAST(COUNT(mode) AS REAL)*100/ (Select count(mode) AS count from expenses_table) as Count from expenses_table GROUP BY mode")
    LiveData<List<ModeWrapper>> getModeDist();

    // --- v2.0 new queries ---

    /** Full-text search across category, sub_category, note, and mode. */
    @Query("SELECT * FROM expenses_table WHERE category LIKE '%' || :query || '%' OR sub_category LIKE '%' || :query || '%' OR note LIKE '%' || :query || '%' OR mode LIKE '%' || :query || '%' ORDER BY id DESC")
    LiveData<List<Expenses>> searchExpenses(String query);

    /** Net balance for a given month (earned - spent). Runs on Executor. */
    @Query("SELECT " +
           "(SELECT COALESCE(SUM(amount),0) FROM expenses_table WHERE SUBSTR(date_created,6,2)=:month AND SUBSTR(date_created,1,4)=:year AND type=1) - " +
           "(SELECT COALESCE(SUM(amount),0) FROM expenses_table WHERE SUBSTR(date_created,6,2)=:month AND SUBSTR(date_created,1,4)=:year AND type=0)")
    LiveData<Double> getNetBalance(String month, String year);

    /** Current month total spend (for budget check). Runs on Executor. */
    @Query("SELECT COALESCE(SUM(amount),0) FROM expenses_table WHERE SUBSTR(date_created,6,2)=:month AND SUBSTR(date_created,1,4)=:year AND type=0")
    double getMonthSpend(String month, String year);

    /** Total Active Balance (Lifetime) */
    @Query("SELECT (SELECT COALESCE(SUM(amount),0) FROM expenses_table WHERE type=1) - (SELECT COALESCE(SUM(amount),0) FROM expenses_table WHERE type=0)")
    LiveData<Double> getTotalNetBalance();

    /** Daily sums for the bar chart. */
    @SuppressWarnings(androidx.room.RoomWarnings.CURSOR_MISMATCH)
    @Query("SELECT date_created as date, " +
           "SUM(CASE WHEN type=0 THEN amount ELSE 0 END) as totalSpent, " +
           "SUM(CASE WHEN type=1 THEN amount ELSE 0 END) as totalEarned " +
           "FROM expenses_table " +
           "WHERE SUBSTR(date_created,6,2)=:month AND SUBSTR(date_created,1,4)=:year " +
           "GROUP BY date_created ORDER BY date_created ASC")
    LiveData<List<DailySum>> getDailySums(String month, String year);

    @Query("SELECT SUBSTR(date_created,1,7) AS month_year, SUM(amount) AS total " +
           "FROM expenses_table " +
           "WHERE category = :category AND type = 0 " +
           "GROUP BY month_year ORDER BY month_year ASC")
    LiveData<List<MonthlySum>> getMonthlySumForCategory(String category);
}
