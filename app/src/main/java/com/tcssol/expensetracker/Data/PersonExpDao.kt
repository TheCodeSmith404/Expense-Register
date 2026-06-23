package com.tcssol.expensetracker.Data

import androidx.room.*
import com.tcssol.expensetracker.Model.PersonExp
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonExpDao {

    @Insert
    suspend fun insertPersonExpense(expenseEntry: PersonExp)

    @Query("DELETE FROM person_expenses")
    suspend fun deleteAll()

    @Query("SELECT * FROM person_expenses ORDER BY id DESC")
    fun getExpenses(): Flow<List<PersonExp>>

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("SELECT id, name, SUM(amount) AS amount, type, contact_number, mode, has_date, pending_date, note FROM person_expenses GROUP BY name, type ORDER BY name")
    fun getExpensesGrouped(): Flow<List<PersonExp>>

    @Query("SELECT * FROM person_expenses WHERE SUBSTR(date_created, 6, 2)=:month AND SUBSTR(date_created, 1, 4)=:year ORDER BY id DESC")
    fun getExpensesFiltered(month: String, year: String): Flow<List<PersonExp>>

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("SELECT id, name, SUM(amount) AS amount, type, contact_number, mode, has_date, pending_date, note FROM person_expenses WHERE SUBSTR(date_created, 6, 2)=:month AND SUBSTR(date_created, 1, 4)=:year GROUP BY name, type ORDER BY name")
    fun getExpensesFilteredGrouped(month: String, year: String): Flow<List<PersonExp>>

    @Query("SELECT * FROM person_expenses WHERE person_expenses.id=:id")
    fun get(id: Long): Flow<PersonExp?>

    @Update
    suspend fun update(expense: PersonExp)

    @Delete
    suspend fun delete(expense: PersonExp)

    @Query("SELECT * FROM person_expenses ORDER BY id ASC")
    suspend fun getExpensesList(): List<PersonExp>
}
