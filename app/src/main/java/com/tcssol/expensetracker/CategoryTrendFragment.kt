package com.tcssol.expensetracker

import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.tcssol.expensetracker.Model.ExpenseViewModel
import com.tcssol.expensetracker.Model.MonthlySum
import java.util.Currency
import java.util.Locale

class CategoryTrendFragment : Fragment() {

    private val expenseViewModel: ExpenseViewModel by viewModels()
    private var categoryName: String = ""

    private lateinit var tvTitle: TextView
    private lateinit var tvAverage: TextView
    private lateinit var tvDelta: TextView
    private lateinit var barChart: BarChart

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_category_trend, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        categoryName = arguments?.getString("category") ?: ""

        tvTitle = view.findViewById(R.id.tvTrendCategoryName)
        tvAverage = view.findViewById(R.id.tvTrendAverage)
        tvDelta = view.findViewById(R.id.tvTrendDelta)
        barChart = view.findViewById(R.id.barChartTrend)

        tvTitle.text = "$categoryName Trends"

        val btnBack: ImageButton = view.findViewById(R.id.btnTrendBack)
        btnBack.setOnClickListener {
            parentFragmentManager.beginTransaction().remove(this).commit()
            // Keep container visible if we go back to the previous overlay (FixedVariableFragment)
            // wait! We opened CategoryTrendFragment OVER FixedVariableFragment.
            // When we dismiss CategoryTrendFragment, we want to return to FixedVariableFragment.
            // Since showOverlayFragment replaces the container's fragment, removing ourselves will leave the container empty!
            // To prevent this, when opening CategoryTrendFragment we should add it to the transaction or backstack,
            // or simply transaction replace without addToBackStack?
            // Actually, we can use childFragmentManager, or let MainActivity handle backstack properly!
            // Wait, in MainActivity.java:
            // getSupportFragmentManager().beginTransaction().replace(container, fragment).commit()
            // It did not add to backstack!
            // Let's modify MainActivity's showOverlayFragment to support back stack, OR we can just open FixedVariableFragment again!
            // Wait! Re-opening FixedVariableFragment is super simple:
            (activity as? MainActivity)?.showOverlayFragment(FixedVariableFragment())
        }

        setupChart()

        expenseViewModel.getMonthlySumForCategory(categoryName).observe(viewLifecycleOwner) { list ->
            bindData(list ?: emptyList())
        }
    }

    private fun getThemeColor(attrId: Int): Int {
        val typedValue = TypedValue()
        requireContext().theme.resolveAttribute(attrId, typedValue, true)
        return typedValue.data
    }

    private fun setupChart() {
        barChart.description.isEnabled = false
        barChart.setDrawGridBackground(false)
        barChart.setDrawBarShadow(false)
        barChart.setFitBars(true)
        barChart.legend.isEnabled = false

        val primaryTextColor = getThemeColor(android.R.attr.textColorPrimary)

        val xAxis = barChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.textColor = primaryTextColor
        xAxis.granularity = 1f

        val leftAxis = barChart.axisLeft
        leftAxis.setDrawGridLines(true)
        leftAxis.textColor = primaryTextColor
        leftAxis.axisMinimum = 0f

        val rightAxis = barChart.axisRight
        rightAxis.isEnabled = false
    }

    private fun bindData(list: List<MonthlySum>) {
        if (list.isEmpty()) {
            tvAverage.text = "No history available"
            tvDelta.text = "Start adding transactions to see trend"
            barChart.clear()
            return
        }

        val symbol = Currency.getInstance(Locale.getDefault()).symbol

        // 1. Calculate Average
        val totalSum = list.sumOf { it.total }
        val avg = totalSum / list.size
        tvAverage.text = "Average Spend: " + symbol + String.format(Locale.getDefault(), "%,.0f", avg) + "/mo"

        // 2. Calculate MoM Delta
        if (list.size >= 2) {
            val latest = list.last()
            val previous = list[list.size - 2]
            if (previous.total > 0) {
                val changePct = ((latest.total - previous.total) / previous.total) * 100
                val formattedPct = String.format(Locale.getDefault(), "%.1f", Math.abs(changePct))
                if (changePct > 0) {
                    tvDelta.text = "▲ +$formattedPct% vs last month"
                    tvDelta.setTextColor(Color.parseColor("#EF4444")) // Red warning (spending increased)
                } else if (changePct < 0) {
                    tvDelta.text = "▼ -$formattedPct% vs last month"
                    tvDelta.setTextColor(Color.parseColor("#16A34A")) // Green success (spending decreased)
                } else {
                    tvDelta.text = "Flat vs last month"
                    tvDelta.setTextColor(getThemeColor(android.R.attr.textColorSecondary))
                }
            } else {
                tvDelta.text = "No previous spend comparison"
            }
        } else {
            tvDelta.text = "Only 1 month of history available"
        }

        // 3. Populate Bar Chart Entries
        val entries = list.mapIndexed { index, item ->
            BarEntry(index.toFloat(), item.total.toFloat())
        }

        // X-axis label formatter
        val monthsAbbr = arrayOf("", "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
        barChart.xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                val index = value.toInt()
                if (index in list.indices) {
                    val monthYear = list[index].monthYear
                    val parts = monthYear.split("-")
                    if (parts.size == 2) {
                        try {
                            val monthIdx = parts[1].toInt()
                            val monthStr = if (monthIdx in 1..12) monthsAbbr[monthIdx] else parts[1]
                            val yearStr = parts[0].substring(2)
                            return "$monthStr '$yearStr"
                        } catch (e: Exception) {
                            return monthYear
                        }
                    }
                    return monthYear
                }
                return ""
            }
        }

        val primaryColor = getThemeColor(com.google.android.material.R.attr.colorPrimary)
        val dataSet = BarDataSet(entries, "Monthly Spend").apply {
            color = primaryColor
            setDrawValues(true)
            valueTextColor = getThemeColor(android.R.attr.textColorPrimary)
            valueTextSize = 10f
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return symbol + String.format(Locale.getDefault(), "%,.0f", value)
                }
            }
        }

        barChart.data = BarData(dataSet)
        barChart.invalidate()
    }
}
