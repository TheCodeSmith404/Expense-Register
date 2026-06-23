package com.tcssol.expensetracker.Data

import android.app.Application
import com.tcssol.expensetracker.Model.DailySum
import com.tcssol.expensetracker.Model.Expenses
import com.tcssol.expensetracker.Utils.ModeWrapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.LocalDate

class ExpensesRepository(application: Application) {
    private val expenseDao: ExpenseDao = ExpensesDatabase.getDatabase(application).expenseDao()
    val allExpenses: Flow<List<Expenses>> = expenseDao.getExpenses()
    val groupedExpenses: Flow<List<Expenses>> = expenseDao.getGroupedItems()
    val modeDist: Flow<List<ModeWrapper>> = expenseDao.getModeDist()

    suspend fun getAllExpensesList(): List<Expenses> {
        return expenseDao.getExpensesAllList()
    }

    suspend fun insert(expenses: Expenses) {
        expenseDao.insertExpense(expenses)
    }

    fun get(id: Long): Flow<Expenses?> {
        return expenseDao.get(id)
    }

    suspend fun update(expenses: Expenses) {
        expenseDao.update(expenses)
    }

    suspend fun delete(expenses: Expenses) {
        expenseDao.delete(expenses)
    }

    fun getExpenseDao(): ExpenseDao {
        return expenseDao
    }

    fun getMonthlyExpenses(date: LocalDate): Flow<List<Expenses>> {
        val year = date.year.toString()
        var month = date.monthValue.toString()
        if (month.length == 1) month = "0$month"
        return expenseDao.getMonth(month, year)
    }

    fun getGroupedMonthlyExpenses(date: LocalDate): Flow<List<Expenses>> {
        val year = date.year.toString()
        var month = date.monthValue.toString()
        if (month.length == 1) month = "0$month"
        return expenseDao.getGroupedMonth(month, year)
    }

    fun getFrag3DataFiltered(montht: Int, yearl: Int): Flow<List<Int?>> = flow {
        val month = if (montht < 10) "0$montht" else montht.toString()
        val year = yearl.toString()
        val list = listOf(
            expenseDao.getSumEarnedF(month, year),
            expenseDao.getSumSpendF(month, year),
            expenseDao.getSumReceivedF(month, year),
            expenseDao.getSumGivenF(month, year)
        )
        emit(list)
    }

    fun getFrag3Data(): Flow<List<Int?>> = flow {
        val list = listOf(
            expenseDao.getSumEarned(),
            expenseDao.getSumSpend(),
            expenseDao.getSumReceived(),
            expenseDao.getSumGiven()
        )
        emit(list)
    }

    suspend fun getSubCats(category: String, type: Boolean): List<Expenses> {
        return expenseDao.getSubCats(category, type)
    }

    suspend fun getSubCatsF(montht: Int, year: String, category: String, type: Boolean): List<Expenses> {
        val month = if (montht < 10) "0$montht" else montht.toString()
        return expenseDao.getSubsCatsF(month, year, category, type)
    }

    fun searchExpenses(query: String): Flow<List<Expenses>> {
        return expenseDao.searchExpenses(query)
    }

    fun getNetBalance(month: String, year: String): Flow<Double?> {
        return expenseDao.getNetBalance(month, year)
    }

    fun getTotalNetBalance(): Flow<Double?> {
        return expenseDao.getTotalNetBalance()
    }

    fun getDailySums(montht: Int, yearl: Int): Flow<List<DailySum>> {
        val month = if (montht < 10) "0$montht" else montht.toString()
        val year = yearl.toString()
        return expenseDao.getDailySums(month, year)
    }

    suspend fun getMonthSpend(month: String, year: String): Double {
        return expenseDao.getMonthSpend(month, year)
    }
}
