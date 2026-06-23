package com.tcssol.expensetracker.Data

import android.app.Application
import com.tcssol.expensetracker.Model.PersonExp
import kotlinx.coroutines.flow.Flow

class PersonExpRepository(application: Application) {
    private val personExpDao: PersonExpDao = PersonExpDatabase.getDatabase(application).personExpDao()
    val allExpenses: Flow<List<PersonExp>> = personExpDao.getExpenses()
    val allExpensesGrouped: Flow<List<PersonExp>> = personExpDao.getExpensesGrouped()

    suspend fun getAllExpensesList(): List<PersonExp> {
        return personExpDao.getExpensesList()
    }

    suspend fun getAllExpensesListSync(): List<PersonExp> {
        return personExpDao.getExpensesList()
    }

    fun getAllExpensesGroupedFiltered(montht: String, yeart: String): Flow<List<PersonExp>> {
        var month = montht
        if (month.length == 1) month = "0$month"
        return personExpDao.getExpensesFilteredGrouped(month, yeart)
    }

    fun getAllExpensesFiltered(montht: String, yeart: String): Flow<List<PersonExp>> {
        var month = montht
        if (month.length == 1) month = "0$month"
        return personExpDao.getExpensesFiltered(month, yeart)
    }

    suspend fun insert(personExp: PersonExp) {
        personExpDao.insertPersonExpense(personExp)
    }

    fun get(id: Long): Flow<PersonExp?> {
        return personExpDao.get(id)
    }

    suspend fun update(exp: PersonExp) {
        personExpDao.update(exp)
    }

    suspend fun delete(exp: PersonExp) {
        personExpDao.delete(exp)
    }

    fun getPersonExpDao(): PersonExpDao {
        return personExpDao
    }
}
