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
import com.tcssol.expensetracker.Model.DailySum
import com.tcssol.expensetracker.Model.ExpenseViewModel
import com.tcssol.expensetracker.Model.Expenses
import com.tcssol.expensetracker.Model.PersonExpViewModel
import com.tcssol.expensetracker.Model.SharedExpenseViewModel
import com.tcssol.expensetracker.Utils.ModeWrapper
import com.tcssol.expensetracker.Utils.Wrapped
import com.tcssol.expensetracker.Views.CategoryBarChartView
import com.tcssol.expensetracker.databinding.Fragment3Binding
import java.time.LocalDate
import java.time.format.DateTimeFormatter
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
    private var dailySource: LiveData<List<DailySum>>? = null
    private var chartEmpty = true

    // Latest rows per dataset; the toggles only choose which one is rendered.
    private var categoryItems: List<CategoryBarChartView.Item> = emptyList()
    private var dailyItems: List<CategoryBarChartView.Item> = emptyList()      // chronological, for bars
    private var dailyTopItems: List<CategoryBarChartView.Item> = emptyList()   // top days + Other, for pie
    private var modeItems: List<CategoryBarChartView.Item> = emptyList()       // percentages
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
        val prefs = requireContext().getSharedPreferences("ui_prefs", 0)
        binding.chartToggleGroup.check(
            if (prefs.getString(PREF_CHART_TYPE, "bars") == "pie") R.id.buttonChartPie
            else R.id.buttonChartBars
        )
        binding.chartToggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                prefs.edit().putString(
                    PREF_CHART_TYPE,
                    if (checkedId == R.id.buttonChartPie) "pie" else "bars"
                ).apply()
                renderChart()
            }
        }

        // Dataset selector: category totals, daily spend, or payment-medium share
        binding.chartDataToggleGroup.check(
            when (prefs.getString(PREF_CHART_DATA, "category")) {
                "daily" -> R.id.buttonDataDaily
                "medium" -> R.id.buttonDataMedium
                else -> R.id.buttonDataCategory
            }
        )
        binding.chartDataToggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                prefs.edit().putString(
                    PREF_CHART_DATA,
                    when (checkedId) {
                        R.id.buttonDataDaily -> "daily"
                        R.id.buttonDataMedium -> "medium"
                        else -> "category"
                    }
                ).apply()
                renderChart()
            }
        }

        setChartSource(expenseViewModel.allExpensesGrouped)
        setDailySource(LocalDate.now().monthValue, LocalDate.now().year)

        sharedExpenseViewModel!!.getObject().observe(
            viewLifecycleOwner
        ) { data: Wrapped ->
            if (data.year > 0 && data.month > 0) {
                setChartSource(
                    expenseViewModel.getAllExpensesGroupedMonthly(
                        LocalDate.of(data.year, data.month, 1)
                    )
                )
                setDailySource(data.month, data.year)
            } else {
                setChartSource(expenseViewModel.allExpensesGrouped)
                setDailySource(LocalDate.now().monthValue, LocalDate.now().year)
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

            // Same data feeds the "Medium" chart dataset (values are 0-100 shares)
            modeItems = (data ?: emptyList())
                .filterNotNull()
                .sortedByDescending { it.perentage ?: 0.0 }
                .map { CategoryBarChartView.Item(it.name, it.perentage ?: 0.0) }
            renderChart()
        }
    }

    private fun setChartSource(source: LiveData<List<Expenses>>) {
        chartSource?.removeObservers(viewLifecycleOwner)
        chartSource = source
        source.observe(viewLifecycleOwner) { list -> bindCategoryChart(list) }
    }

    private fun setDailySource(month: Int, year: Int) {
        dailySource?.removeObservers(viewLifecycleOwner)
        dailySource = expenseViewModel.getDailySums(month, year)
        dailySource!!.observe(viewLifecycleOwner) { list -> bindDailyChart(list) }
    }

    /**
     * Grouped rows -> spend-only category totals, largest first.
     * Categories beyond the top [MAX_CHART_ROWS] are folded into "Other".
     */
    private fun bindCategoryChart(list: List<Expenses>?) {
        val spend = (list ?: emptyList())
            .filter { !it.isType && it.category != "Money Given" && it.category != "Money Received" }
            .sortedByDescending { it.amount }

        val items = mutableListOf<CategoryBarChartView.Item>()
        spend.take(MAX_CHART_ROWS).forEach { items.add(CategoryBarChartView.Item(it.category, it.amount)) }
        if (spend.size > MAX_CHART_ROWS) {
            val other = spend.drop(MAX_CHART_ROWS).sumOf { it.amount }
            items.add(CategoryBarChartView.Item(getString(R.string.chart_other), other))
        }
        categoryItems = items
        renderChart()
    }

    /**
     * Daily sums -> chronological rows for the bars; for the pie the biggest
     * spending days are kept and the rest folded into "Other" so it stays readable.
     */
    private fun bindDailyChart(list: List<DailySum>?) {
        val fmt = DateTimeFormatter.ofPattern("d MMM")
        val days = (list ?: emptyList()).filter { it.totalSpent > 0 }

        dailyItems = days.sortedBy { it.date }
            .map { CategoryBarChartView.Item(it.date.format(fmt), it.totalSpent) }

        val bySpend = days.sortedByDescending { it.totalSpent }
        val top = bySpend.take(MAX_CHART_ROWS)
            .map { CategoryBarChartView.Item(it.date.format(fmt), it.totalSpent) }
            .toMutableList()
        if (bySpend.size > MAX_CHART_ROWS) {
            top.add(CategoryBarChartView.Item(
                getString(R.string.chart_other),
                bySpend.drop(MAX_CHART_ROWS).sumOf { it.totalSpent }))
        }
        dailyTopItems = top
        renderChart()
    }

    /** Pushes the selected dataset into whichever chart type is active. */
    private fun renderChart() {
        if (_binding == null) return
        val symbol = Currency.getInstance(Locale.getDefault()).symbol
        val pieSelected = binding.chartToggleGroup.checkedButtonId == R.id.buttonChartPie

        val items: List<CategoryBarChartView.Item>
        val prefix: String
        val suffix: String
        val label: String
        when (binding.chartDataToggleGroup.checkedButtonId) {
            R.id.buttonDataDaily -> {
                items = if (pieSelected) dailyTopItems else dailyItems
                prefix = symbol; suffix = ""
                label = getString(R.string.chart_daily_spending)
            }
            R.id.buttonDataMedium -> {
                items = modeItems
                prefix = ""; suffix = "%"
                label = getString(R.string.chart_medium_share)
            }
            else -> {
                items = categoryItems
                prefix = symbol; suffix = ""
                label = getString(R.string.spending_by_category)
            }
        }

        binding.labelCategoryChart.text = label
        binding.categoryChart.setData(items, prefix, suffix)
        binding.categoryPieChart.setData(items, prefix, suffix)
        chartEmpty = items.isEmpty()

        binding.chartEmptyText.visibility = if (chartEmpty) View.VISIBLE else View.GONE
        binding.categoryChart.visibility =
            if (!chartEmpty && !pieSelected) View.VISIBLE else View.GONE
        binding.categoryPieChart.visibility =
            if (!chartEmpty && pieSelected) View.VISIBLE else View.GONE
    }

    companion object {
        // 5 real rows + "Other" keeps the pie readable (≤6 slices)
        private const val MAX_CHART_ROWS = 5
        private const val PREF_CHART_TYPE = "dashboard_chart_type"
        private const val PREF_CHART_DATA = "dashboard_chart_data"
    }
}