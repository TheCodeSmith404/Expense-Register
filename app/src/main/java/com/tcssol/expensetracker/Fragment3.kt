package com.tcssol.expensetracker

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.tcssol.expensetracker.Model.DailySum
import com.tcssol.expensetracker.Model.ExpenseViewModel
import com.tcssol.expensetracker.Model.Expenses
import com.tcssol.expensetracker.Model.SharedExpenseViewModel
import com.tcssol.expensetracker.Utils.ModeWrapper
import com.tcssol.expensetracker.Utils.Wrapped
import com.tcssol.expensetracker.Views.CategoryBarChartView
import com.tcssol.expensetracker.databinding.Fragment3Binding
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class Fragment3 : Fragment() {
    private val expenseViewModel: ExpenseViewModel by viewModels()
    private val sharedExpenseViewModel: SharedExpenseViewModel by activityViewModels()

    private var _binding: Fragment3Binding? = null
    private val binding get() = _binding!!
    private var chartSource: LiveData<List<Expenses>>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = Fragment3Binding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val symbol = Currency.getInstance(Locale.getDefault()).symbol

        // Active Lifetime Balance
        expenseViewModel.totalNetBalance.observe(viewLifecycleOwner) { balance ->
            binding.tvTotalBalance.text = symbol + String.format(Locale.getDefault(), "%,.2f", balance ?: 0.0)
            val balanceColor = if ((balance ?: 0.0) < 0) R.color.expense else R.color.income
            binding.tvTotalBalance.setTextColor(ContextCompat.getColor(requireContext(), balanceColor))
        }

        // Standard Summary Stats binding (Filtered / Lifetime)
        sharedExpenseViewModel.getObject().observe(viewLifecycleOwner) { data: Wrapped ->
            val dataLive = if (data.year > 0 || data.month > 0) {
                expenseViewModel.getFrag3DataFiltered(data.month, data.year)
            } else {
                expenseViewModel.frag3Data
            }
            dataLive.observe(viewLifecycleOwner) { list ->
                binding.frag3SetAmtEarned.text = symbol + (list?.getOrNull(0) ?: 0)
                binding.frag3SetAmtSpend.text = "-" + symbol + (list?.getOrNull(1) ?: 0)
                binding.frag3SetAmtReceived.text = symbol + (list?.getOrNull(2) ?: 0)
                binding.frag3SetAmtGiven.text = "-" + symbol + (list?.getOrNull(3) ?: 0)
            }

            // Bind Category Custom Chart Data Source
            if (data.year > 0 && data.month > 0) {
                setChartSource(expenseViewModel.getAllExpensesGroupedMonthly(LocalDate.of(data.year, data.month, 1)))
            } else {
                setChartSource(expenseViewModel.allExpensesGrouped)
            }
        }

        setupPieChart()
        setupBarChart()

        // Handle Bar Chart Data (Daily Sums)
        sharedExpenseViewModel.getObject().observe(viewLifecycleOwner) { data ->
            val month = if (data.month > 0) data.month else Calendar.getInstance().get(Calendar.MONTH) + 1
            val year = if (data.year > 0) data.year else Calendar.getInstance().get(Calendar.YEAR)
            expenseViewModel.getDailySums(month, year).observe(viewLifecycleOwner) { dailySums ->
                updateBarChart(dailySums)
            }
        }

        // Handle Payment Medium Distribution
        expenseViewModel.getModeDist().observe(viewLifecycleOwner) { data ->
            updatePaymentMediumChart(data)
        }
    }

    private fun setChartSource(source: LiveData<List<Expenses>>) {
        chartSource?.removeObservers(viewLifecycleOwner)
        chartSource = source
        source.observe(viewLifecycleOwner) { list -> bindCategoryChart(list) }
    }

    private fun bindCategoryChart(list: List<Expenses>?) {
        val symbol = Currency.getInstance(Locale.getDefault()).symbol
        val spend = (list ?: emptyList())
            .filter { !it.isType && it.category != "Money Given" && it.category != "Money Received" }
            .sortedByDescending { it.amount }

        val items = mutableListOf<CategoryBarChartView.Item>()
        spend.take(MAX_CHART_ROWS).forEach { items.add(CategoryBarChartView.Item(it.category, it.amount)) }
        if (spend.size > MAX_CHART_ROWS) {
            val other = spend.drop(MAX_CHART_ROWS).sumOf { it.amount }
            items.add(CategoryBarChartView.Item("Other", other))
        }

        binding.categoryChart.setData(items, symbol)
        binding.categoryChart.visibility = if (items.isEmpty()) View.GONE else View.VISIBLE
        binding.chartEmptyText.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun setupPieChart() {
        val pieChart = binding.pieChart
        pieChart.setUsePercentValues(true)
        pieChart.description.isEnabled = false
        pieChart.setExtraOffsets(5f, 10f, 5f, 5f)
        pieChart.isDrawHoleEnabled = true
        
        val typedValue = TypedValue()
        requireContext().theme.resolveAttribute(com.google.android.material.R.attr.colorSurface, typedValue, true)
        pieChart.setHoleColor(typedValue.data)

        pieChart.holeRadius = 58f
        pieChart.transparentCircleRadius = 61f
        pieChart.centerText = "Payment\nMedium"
        
        val textColorTypedValue = TypedValue()
        requireContext().theme.resolveAttribute(android.R.attr.textColorPrimary, textColorTypedValue, true)
        pieChart.setCenterTextColor(textColorTypedValue.data)
        
        val l = pieChart.legend
        l.verticalAlignment = Legend.LegendVerticalAlignment.TOP
        l.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
        l.orientation = Legend.LegendOrientation.VERTICAL
        l.textColor = textColorTypedValue.data
    }

    private fun updatePaymentMediumChart(data: List<ModeWrapper?>?) {
        if (data.isNullOrEmpty()) {
            binding.pieChart.clear()
            return
        }
        val entries = data.filterNotNull().map { PieEntry(it.perentage.toFloat(), it.name) }
        val dataSet = PieDataSet(entries, "").apply {
            colors = arrayListOf(
                ContextCompat.getColor(requireContext(), R.color.colorPrimary),
                ContextCompat.getColor(requireContext(), R.color.colorSecondary),
                ContextCompat.getColor(requireContext(), R.color.green),
                Color.parseColor("#FFA726"),
                Color.parseColor("#29B6F6")
            )
            valueTextColor = ContextCompat.getColor(requireContext(), R.color.colorOnPrimary)
            valueTextSize = 12f
            valueFormatter = PercentFormatter(binding.pieChart)
        }
        binding.pieChart.data = PieData(dataSet)
        binding.pieChart.invalidate()
    }

    private fun setupBarChart() {
        val barChart = binding.barChart
        barChart.description.isEnabled = false
        barChart.setPinchZoom(false)
        barChart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            setDrawGridLines(false)
            granularity = 1f
            textColor = getPrimaryTextColor()
        }
        barChart.axisLeft.textColor = getPrimaryTextColor()
        barChart.axisRight.isEnabled = false
        barChart.legend.textColor = getPrimaryTextColor()
    }

    private fun updateBarChart(dailySums: List<DailySum>) {
        if (dailySums.isEmpty()) {
            binding.barChart.clear()
            return
        }
        val spentEntries = dailySums.mapIndexed { index, dailySum -> BarEntry(index.toFloat(), dailySum.totalSpent.toFloat()) }
        val dateLabels = dailySums.map { it.date.format(DateTimeFormatter.ofPattern("dd")) }

        val spentSet = BarDataSet(spentEntries, "Spent").apply {
            color = ContextCompat.getColor(requireContext(), R.color.expense)
            valueTextColor = getPrimaryTextColor()
            valueTextSize = 10f
        }
        binding.barChart.data = BarData(spentSet).apply { barWidth = 0.6f }
        binding.barChart.xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                val idx = value.toInt()
                return dateLabels.getOrNull(idx) ?: ""
            }
        }
        binding.barChart.invalidate()
    }

    private fun getPrimaryTextColor(): Int {
        val typedValue = TypedValue()
        requireContext().theme.resolveAttribute(android.R.attr.textColorPrimary, typedValue, true)
        return typedValue.data
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val MAX_CHART_ROWS = 6
    }
}