package com.tcssol.expensetracker.Model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tcssol.expensetracker.Data.ExpenseDao
import com.tcssol.expensetracker.Data.ExpensesRepository
import com.tcssol.expensetracker.Utils.ModeWrapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.time.LocalDate

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {
    val repository: ExpensesRepository = ExpensesRepository(application)
    val allExpenses: Flow<List<Expenses>> = repository.allExpenses
    val allExpensesGrouped: Flow<List<Expenses>> = repository.groupedExpenses
    val modeDist: Flow<List<ModeWrapper>> = repository.modeDist
    val frag3Data: Flow<List<Int?>> = repository.getFrag3Data()
    val totalNetBalance: Flow<Double?> = repository.getTotalNetBalance()

    suspend fun getSubCatsF(month: Int, year: Int, category: String, type: Boolean): List<Expenses> {
        return if (month > 0 && year > 0) {
            repository.getSubCatsF(month, year.toString(), category, type)
        } else {
            repository.getSubCats(category, type)
        }
    }

    suspend fun getAllExpensesList(): List<Expenses> {
        return repository.getAllExpensesList()
    }

    fun getAllExpensesGroupedMonthly(date: LocalDate): Flow<List<Expenses>> {
        return repository.getGroupedMonthlyExpenses(date)
    }

    fun getAllExpensesMonthly(date: LocalDate): Flow<List<Expenses>> {
        return repository.getMonthlyExpenses(date)
    }

    fun getFrag3DataFiltered(month: Int, year: Int): Flow<List<Int?>> {
        return repository.getFrag3DataFiltered(month, year)
    }

    fun insert(task: Expenses) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insert(task)
        }
    }

    fun get(id: Long): Flow<Expenses?> {
        return repository.get(id)
    }

    fun update(expenses: Expenses) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.update(expenses)
        }
    }

    fun delete(expenses: Expenses) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.delete(expenses)
        }
    }

    fun getExpenseDao(): ExpenseDao {
        return repository.getExpenseDao()
    }

    fun search(query: String): Flow<List<Expenses>> {
        return repository.searchExpenses(query)
    }

    fun getNetBalance(month: String, year: String): Flow<Double?> {
        return repository.getNetBalance(month, year)
    }

    fun getDailySums(month: Int, year: Int): Flow<List<DailySum>> {
        return repository.getDailySums(month, year)
    }

    fun getMonthSpendAsync(month: String, year: String, callback: MonthSpendCallback) {
        viewModelScope.launch(Dispatchers.IO) {
            val spend = repository.getMonthSpend(month, year)
            callback.onResult(spend)
        }
    }

    interface MonthSpendCallback {
        fun onResult(totalSpend: Double)
    }
}
