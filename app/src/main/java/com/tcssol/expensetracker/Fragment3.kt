package com.tcssol.expensetracker

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tcssol.expensetracker.Adapters.ModeDistributionAdapter
import com.tcssol.expensetracker.Model.ExpenseViewModel
import com.tcssol.expensetracker.Model.Expenses
import com.tcssol.expensetracker.Model.PersonExpViewModel
import com.tcssol.expensetracker.Model.SharedExpenseViewModel
import com.tcssol.expensetracker.Utils.ModeWrapper
import com.tcssol.expensetracker.Utils.Wrapped
import com.tcssol.expensetracker.Views.CategoryBarChartView
import com.tcssol.expensetracker.databinding.Fragment3Binding
import java.time.LocalDate
import java.util.Currency
import java.util.Locale

/**
 * TODO Add charts and other views to show trends and options to set budgets
 */
class Fragment3 : Fragment() {
    private val expenseViewModel: ExpenseViewModel by viewModels()
    private val sharedExpenseViewModel: SharedExpenseViewModel by viewModels()
    private val personExpViewModel: PersonExpViewModel by  viewModels()

    private var view: View? = null
    private var _binding:Fragment3Binding?=null
    private val binding get()= _binding!!
    private var chartSource: LiveData<List<Expenses>>? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding=Fragment3Binding.inflate(layoutInflater,container, false)
        return binding.root
    }

    /*
    TODO Kotlin coroutines for background updates??
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        personExpViewModel = ViewModelProvider(this).get(
//            PersonExpViewModel::class.java
//        )
//        expenseViewModel = ViewModelProvider(this).get(
//            ExpenseViewModel::class.java
//        )
        val symbol = Currency.getInstance(Locale.getDefault()).symbol
        expenseViewModel!!.frag3Data.observe(
            viewLifecycleOwner
        ) { list: List<Int?> ->
            binding.frag3SetAmtEarned.text = symbol + (if (list[0] == null) 0 else list[0]).toString()
            binding.frag3SetAmtSpend.text =
                "-" + symbol + (if (list[1] == null) 0 else list[1]).toString()
            binding.frag3SetAmtReceived.text = symbol + (if (list[2] == null) 0 else list[2]).toString()
            binding.frag3SetAmtGiven.text =
                "-" + symbol + (if (list[3] == null) 0 else list[3]).toString()
        }

//        sharedExpenseViewModel =
//            ViewModelProvider.AndroidViewModelFactory(requireActivity().application).create<SharedExpenseViewModel>(
//                SharedExpenseViewModel::class.java
//            )
        setChartSource(expenseViewModel.allExpensesGrouped)

        sharedExpenseViewModel!!.getObject().observe(
            viewLifecycleOwner
        ) { data: Wrapped ->
            if (data.year > 0 && data.month > 0) {
                setChartSource(
                    expenseViewModel.getAllExpensesGroupedMonthly(
                        LocalDate.of(data.year, data.month, 1)
                    )
                )
            } else {
                setChartSource(expenseViewModel.allExpensesGrouped)
            }
            if (data.year > 0 || data.month > 0) {
                Log.d("expdao", data.month.toString() + " Data in frag3 " + data.year)
                expenseViewModel!!.getFrag3DataFiltered(data.month, data.year).observe(
                    viewLifecycleOwner
                ) { list: List<Int?> ->
                    binding.frag3SetAmtEarned.text =
                        symbol + (if (list[0] == null) 0 else list[0]).toString()
                    binding.frag3SetAmtSpend.text =
                        "-" + symbol + (if (list[1] == null) 0 else list[1]).toString()
                    binding.frag3SetAmtReceived.text =
                        symbol + (if (list[2] == null) 0 else list[2]).toString()
                    binding.frag3SetAmtGiven.text =
                        "-" + symbol + (if (list[3] == null) 0 else list[3]).toString()
                }
            } else {
                expenseViewModel!!.frag3Data.observe(
                    viewLifecycleOwner
                ) { list: List<Int?> ->
                    binding.frag3SetAmtEarned.text = (if (list[0] == null) 0 else list[0]).toString()
                    binding.frag3SetAmtSpend.text = "-" + (if (list[1] == null) 0 else list[1]).toString()
                    binding.frag3SetAmtReceived.text = (if (list[2] == null) 0 else list[2]).toString()
                    binding.frag3SetAmtGiven.text = "-" + (if (list[3] == null) 0 else list[3]).toString()
                }
            }
        }

        binding.modeRecycleViewfFrag3.setHasFixedSize(true)
        binding.modeRecycleViewfFrag3.layoutManager = LinearLayoutManager(context)
        expenseViewModel!!.getModeDist().observe(
            viewLifecycleOwner
        ) { data: List<ModeWrapper?>? ->
            val adapter = ModeDistributionAdapter(
                context, data
            )
            binding.modeRecycleViewfFrag3.adapter = adapter
        }
    }

    private fun setChartSource(source: LiveData<List<Expenses>>) {
        chartSource?.removeObservers(viewLifecycleOwner)
        chartSource = source
        source.observe(viewLifecycleOwner) { list -> bindCategoryChart(list) }
    }

    /**
     * Grouped rows -> spend-only category totals, largest first.
     * Categories beyond the top 6 are folded into "Other".
     */
    private fun bindCategoryChart(list: List<Expenses>?) {
        val symbol = Currency.getInstance(Locale.getDefault()).symbol
        val spend = (list ?: emptyList())
            .filter { !it.isType && it.category != "Money Given" && it.category != "Money Received" }
            .sortedByDescending { it.amount }

        val items = mutableListOf<CategoryBarChartView.Item>()
        spend.take(MAX_CHART_ROWS).forEach { items.add(CategoryBarChartView.Item(it.category, it.amount)) }
        if (spend.size > MAX_CHART_ROWS) {
            val other = spend.drop(MAX_CHART_ROWS).sumOf { it.amount }
            items.add(CategoryBarChartView.Item(getString(R.string.chart_other), other))
        }

        binding.categoryChart.setData(items, symbol)
        binding.categoryChart.visibility = if (items.isEmpty()) View.GONE else View.VISIBLE
        binding.chartEmptyText.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
    }

    companion object {
        private const val MAX_CHART_ROWS = 6
    }
}