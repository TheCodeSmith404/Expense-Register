package com.tcssol.expensetracker.Data

import androidx.room.*
import com.tcssol.expensetracker.Model.DailySum
import com.tcssol.expensetracker.Model.Expenses
import com.tcssol.expensetracker.Utils.ModeWrapper
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {

    @Insert
    suspend fun insertExpense(expenseEntry: Expenses)

    @Query("DELETE FROM expenses_table")
    suspend fun deleteAll()

    @Query("SELECT * FROM expenses_table ORDER BY id DESC")
    fun getExpenses(): Flow<List<Expenses>>

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("SELECT id, category, SUM(amount) AS amount, type, GROUP_CONCAT(DISTINCT sub_category) AS sub_category FROM EXPENSES_TABLE GROUP BY category, type")
    fun getGroupedItems(): Flow<List<Expenses>>

    @Query("SELECT * FROM EXPENSES_TABLE WHERE expenses_table.id=:id")
    fun get(id: Long): Flow<Expenses?>

    @Update
    suspend fun update(expense: Expenses)

    @Delete
    suspend fun delete(expense: Expenses)

    @Query("SELECT * FROM EXPENSES_TABLE WHERE SUBSTR(date_created, 6, 2)=:month AND SUBSTR(date_created, 1, 4)=:year ORDER BY id DESC")
    fun getMonth(month: String, year: String): Flow<List<Expenses>>

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("SELECT id, category, SUM(amount) AS amount, type, GROUP_CONCAT(DISTINCT sub_category) AS sub_category, date_created FROM (SELECT * FROM EXPENSES_TABLE WHERE SUBSTR(date_created, 6, 2)=:month AND SUBSTR(date_created, 1, 4)=:year) GROUP BY category, type")
    fun getGroupedMonth(month: String, year: String): Flow<List<Expenses>>

    @Query("SELECT SUM(amount) FROM (SELECT * FROM EXPENSES_TABLE WHERE type=false)")
    suspend fun getSumSpend(): Int?

    @Query("SELECT SUM(amount) FROM (SELECT * FROM EXPENSES_TABLE WHERE type=true)")
    suspend fun getSumEarned(): Int?

    @Query("SELECT SUM(amount) FROM expenses_table WHERE category =\"Money Given\"")
    suspend fun getSumGiven(): Int?

    @Query("SELECT SUM(amount) FROM expenses_table WHERE category =\"Money Received\"")
    suspend fun getSumReceived(): Int?

    @Query("SELECT SUM(amount) FROM (SELECT * FROM EXPENSES_TABLE WHERE SUBSTR(date_created, 6, 2)=:month AND SUBSTR(date_created, 1, 4)=:year AND type=false) ")
    suspend fun getSumSpendF(month: String, year: String): Int?

    @Query("SELECT SUM(amount) FROM (SELECT * FROM EXPENSES_TABLE WHERE SUBSTR(date_created, 6, 2)=:month AND SUBSTR(date_created, 1, 4)=:year AND type=true) ")
    suspend fun getSumEarnedF(month: String, year: String): Int?

    @Query("SELECT SUM(amount) FROM (SELECT * FROM EXPENSES_TABLE WHERE SUBSTR(date_created,6,2)=:month AND SUBSTR(date_created,1,4)=:year AND category=\"Money Given\")")
    suspend fun getSumGivenF(month: String, year: String): Int?

    @Query("SELECT SUM(amount) FROM (SELECT * FROM EXPENSES_TABLE WHERE SUBSTR(date_created,6,2)=:month AND SUBSTR(date_created,1,4)=:year AND category=\"Money Received\")")
    suspend fun getSumReceivedF(month: String, year: String): Int?

    @Query("SELECT * FROM expenses_table ORDER BY id ASC")
    suspend fun getExpensesAllList(): List<Expenses>

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("SELECT id,category,sub_category,SUM(amount) as amount,type from expenses_table where category=:category AND type=:type GROUP BY sub_category")
    suspend fun getSubCats(category: String, type: Boolean): List<Expenses>

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("SELECT id,category,sub_category,SUM(amount) as amount,type from(SELECT * FROM EXPENSES_TABLE WHERE SUBSTR(date_created, 6, 2)=:month AND SUBSTR(date_created, 1, 4)=:year)where category=:category AND type=:type GROUP BY sub_category")
    suspend fun getSubsCatsF(month: String, year: String, category: String, type: Boolean): List<Expenses>

    @Query("SELECT mode as Mode, CAST(COUNT(mode) AS REAL)*100/ (Select count(mode) AS count from expenses_table) as Count from expenses_table GROUP BY mode")
    fun getModeDist(): Flow<List<ModeWrapper>>

    @Query("SELECT * FROM expenses_table WHERE category LIKE '%' || :query || '%' OR sub_category LIKE '%' || :query || '%' OR note LIKE '%' || :query || '%' OR mode LIKE '%' || :query || '%' ORDER BY id DESC")
    fun searchExpenses(query: String): Flow<List<Expenses>>

    @Query("SELECT (SELECT COALESCE(SUM(amount),0) FROM expenses_table WHERE SUBSTR(date_created,6,2)=:month AND SUBSTR(date_created,1,4)=:year AND type=1) - (SELECT COALESCE(SUM(amount),0) FROM expenses_table WHERE SUBSTR(date_created,6,2)=:month AND SUBSTR(date_created,1,4)=:year AND type=0)")
    fun getNetBalance(month: String, year: String): Flow<Double?>

    @Query("SELECT COALESCE(SUM(amount),0) FROM expenses_table WHERE SUBSTR(date_created,6,2)=:month AND SUBSTR(date_created,1,4)=:year AND type=0")
    suspend fun getMonthSpend(month: String, year: String): Double

    @Query("SELECT (SELECT COALESCE(SUM(amount),0) FROM expenses_table WHERE type=1) - (SELECT COALESCE(SUM(amount),0) FROM expenses_table WHERE type=0)")
    fun getTotalNetBalance(): Flow<Double?>

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("SELECT date_created as date, SUM(CASE WHEN type=0 THEN amount ELSE 0 END) as totalSpent, SUM(CASE WHEN type=1 THEN amount ELSE 0 END) as totalEarned FROM expenses_table WHERE SUBSTR(date_created,6,2)=:month AND SUBSTR(date_created,1,4)=:year GROUP BY date_created ORDER BY date_created ASC")
    fun getDailySums(month: String, year: String): Flow<List<DailySum>>
}
