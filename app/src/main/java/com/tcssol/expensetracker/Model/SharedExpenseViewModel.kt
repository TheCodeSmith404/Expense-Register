package com.tcssol.expensetracker.Model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.tcssol.expensetracker.Utils.Wrapped

class SharedExpenseViewModel : ViewModel() {

    companion object {
        private val _object = MutableLiveData<Wrapped>(Wrapped(-12, -2024))
        @JvmStatic
        val Object: LiveData<Wrapped> = _object

        private val _category = MutableLiveData<String>()
        @JvmStatic
        val category: LiveData<String> = _category

        private val _subCategory = MutableLiveData<String>()
        @JvmStatic
        val subCategory: LiveData<String> = _subCategory

        private val _medium = MutableLiveData<String>()
        @JvmStatic
        val medium: LiveData<String> = _medium

        @JvmStatic
        var change: Boolean = false
    }

    fun getCategory(): LiveData<String> = category

    fun setCategory(text: String) {
        _category.value = text
    }

    fun getSubCategory(): LiveData<String> = subCategory

    fun setSubCategory(text: String) {
        _subCategory.value = text
    }

    fun getMedium(): LiveData<String> = medium

    fun setMedium(text: String) {
        _medium.value = text
    }

    fun getObject(): LiveData<Wrapped> = Object

    fun setObject(wrap: Wrapped) {
        _object.value = wrap
    }
}
