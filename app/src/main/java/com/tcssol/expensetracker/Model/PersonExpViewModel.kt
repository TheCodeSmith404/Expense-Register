package com.tcssol.expensetracker.Model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tcssol.expensetracker.Data.PersonExpDao
import com.tcssol.expensetracker.Data.PersonExpRepository
import com.tcssol.expensetracker.Model.PersonExp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class PersonExpViewModel(application: Application) : AndroidViewModel(application) {
    val repository: PersonExpRepository = PersonExpRepository(application)
    val allExpenses: Flow<List<PersonExp>> = repository.allExpenses
    val allExpensesGrouped: Flow<List<PersonExp>> = repository.allExpensesGrouped

    fun getAllExpensesGroupedFiltered(month: String, year: String): Flow<List<PersonExp>> {
        return repository.getAllExpensesGroupedFiltered(month, year)
    }

    fun getAllExpensesGrouped(): Flow<List<PersonExp>> {
        return allExpensesGrouped
    }

    fun getAllExpensesFiltered(month: String, year: String): Flow<List<PersonExp>> {
        return repository.getAllExpensesFiltered(month, year)
    }

    fun getAllExpensesList(callback: (List<PersonExp>) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val list = repository.getAllExpensesListSync()
            callback(list)
        }
    }

    suspend fun getAllExpensesListSync(): List<PersonExp> {
        return repository.getAllExpensesListSync()
    }

    fun getAllExpenses(): Flow<List<PersonExp>> {
        return allExpenses
    }

    fun insert(personExp: PersonExp) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insert(personExp)
        }
    }

    fun delete(personExp: PersonExp) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.delete(personExp)
        }
    }

    fun getPersonExpDao(): PersonExpDao {
        return repository.getPersonExpDao()
    }
}
