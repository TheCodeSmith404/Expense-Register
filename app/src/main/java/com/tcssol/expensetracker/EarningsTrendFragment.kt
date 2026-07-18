package com.tcssol.expensetracker

import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.tcssol.expensetracker.Model.ExpenseViewModel
import com.tcssol.expensetracker.Model.EarningsHistory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EarningsTrendFragment : Fragment() {

    private val expenseViewModel: ExpenseViewModel by viewModels()
    private lateinit var lineChart: LineChart
    private var historyList: List<EarningsHistory> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_earnings_trend, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lineChart = view.findViewById(R.id.lineChartTrend)

        val btnBack: ImageButton = view.findViewById(R.id.btnTrendBack)
        btnBack.setOnClickListener {
            parentFragmentManager.beginTransaction().remove(this).commit()
            (activity as? MainActivity)?.hideOverlayContainer()
        }

        setupChart()

        expenseViewModel.allEarningsHistory.observe(viewLifecycleOwner) { list ->
            historyList = list ?: emptyList()
            updateChart()
        }
    }

    private fun getThemeColor(attrId: Int): Int {
        val typedValue = TypedValue()
        requireContext().theme.resolveAttribute(attrId, typedValue, true)
        return typedValue.data
    }

    private fun setupChart() {
        lineChart.description.isEnabled = false
        lineChart.setDrawGridBackground(false)
        lineChart.setExtraOffsets(8f, 20f, 16f, 8f)
        lineChart.setDragEnabled(true)
        lineChart.setScaleEnabled(false)
        lineChart.legend.isEnabled = false

        // colorOnSurface always resolves to a direct color – never a state-list reference
        val onSurface = getThemeColor(com.google.android.material.R.attr.colorOnSurface)
        val gridAlpha = Color.argb(0x22, Color.red(onSurface), Color.green(onSurface), Color.blue(onSurface))
        val axisAlpha = Color.argb(0x55, Color.red(onSurface), Color.green(onSurface), Color.blue(onSurface))

        lineChart.xAxis.apply {
            isEnabled = true
            setDrawLabels(true)
            setDrawAxisLine(true)
            position = XAxis.XAxisPosition.BOTTOM
            setDrawGridLines(false)
            textColor = onSurface
            textSize = 10f
            granularity = 1f
            setAvoidFirstLastClipping(true)
            axisLineColor = axisAlpha
        }

        lineChart.axisLeft.apply {
            isEnabled = true
            setDrawLabels(true)
            setDrawAxisLine(true)
            setDrawGridLines(true)
            textColor = onSurface
            textSize = 10f
            axisMinimum = 0f
            axisLineColor = axisAlpha
            gridColor = gridAlpha
            setLabelCount(5, true)
        }

        lineChart.axisRight.isEnabled = false
    }

    private fun updateChart() {
        if (historyList.isEmpty()) {
            lineChart.clear()
            val container = view?.findViewById<android.widget.LinearLayout>(R.id.llEarningsHistoryCards)
            container?.removeAllViews()
            return
        }

        // Sort just in case it is not ordered by timestamp
        val sortedList = historyList.sortedBy { it.timestamp }

        val entries = sortedList.mapIndexed { index, history ->
            Entry(index.toFloat(), history.hourlyRate.toFloat())
        }

        val primaryColor = getThemeColor(com.google.android.material.R.attr.colorPrimary)
        val onSurface   = getThemeColor(com.google.android.material.R.attr.colorOnSurface)

        val dataSet = LineDataSet(entries, "Hourly Rate").apply {
            color = primaryColor
            setCircleColor(primaryColor)
            circleHoleColor = android.graphics.Color.TRANSPARENT
            lineWidth = 2.5f
            circleRadius = 5f
            circleHoleRadius = 2.5f
            // Draw the value above each data point
            setDrawValues(true)
            valueTextColor = onSurface
            valueTextSize = 10f
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    val symbol = java.util.Currency.getInstance(Locale.getDefault()).symbol
                    return "$symbol${String.format(Locale.getDefault(), "%,.0f", value)}/hr"
                }
            }
            // ── Line chart, no fill ──
            setDrawFilled(false)
            mode = LineDataSet.Mode.CUBIC_BEZIER
        }

        lineChart.data = LineData(dataSet)

        // X axis: date of each snapshot
        val dateFormatAxis = SimpleDateFormat("d MMM", Locale.getDefault())
        lineChart.xAxis.apply {
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    val idx = value.toInt()
                    return if (idx in sortedList.indices) dateFormatAxis.format(Date(sortedList[idx].timestamp)) else ""
                }
            }
            axisMinimum = -0.5f
            axisMaximum = (sortedList.size - 1) + 0.5f
            setLabelCount(minOf(sortedList.size, 6), false)
        }

        // Y axis: currency formatter
        lineChart.axisLeft.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                val symbol = java.util.Currency.getInstance(Locale.getDefault()).symbol
                return when {
                    value >= 1_000f -> "$symbol${String.format(Locale.getDefault(), "%.0fk", value / 1_000f)}"
                    else            -> "$symbol${String.format(Locale.getDefault(), "%.0f", value)}"
                }
            }
        }

        // Scroll to most recent if many snapshots
        lineChart.setVisibleXRangeMaximum(minOf(sortedList.size, 6).toFloat())
        lineChart.animateX(400)
        lineChart.post { lineChart.moveViewToX((sortedList.size - 1).toFloat()) }
        lineChart.invalidate()

        // Populate cards list below the chart
        val container = view?.findViewById<android.widget.LinearLayout>(R.id.llEarningsHistoryCards)
        container?.removeAllViews()

        val listSortedDesc = historyList.sortedByDescending { it.timestamp }
        val inflater = LayoutInflater.from(requireContext())
        val symbol = java.util.Currency.getInstance(java.util.Locale.getDefault()).symbol
        val dateFormatCard = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

        listSortedDesc.forEach { history ->
            val row = inflater.inflate(R.layout.item_earnings_snapshot, container, false)
            row.findViewById<android.widget.TextView>(R.id.tvSnapshotRate).text = 
                String.format(Locale.getDefault(), "%s%,.2f / hr", symbol, history.hourlyRate)
            row.findViewById<android.widget.TextView>(R.id.tvSnapshotDate).text = 
                dateFormatCard.format(Date(history.timestamp))
            row.findViewById<android.widget.TextView>(R.id.tvSnapshotMonthly).text = 
                String.format(Locale.getDefault(), "%s%,.0f", symbol, history.monthlyEarnings)
            row.findViewById<android.widget.TextView>(R.id.tvSnapshotDays).text = 
                "${history.daysWorked} days"
            row.findViewById<android.widget.TextView>(R.id.tvSnapshotHours).text = 
                String.format(Locale.getDefault(), "%.1f hrs/day", history.hoursWorked)
            container?.addView(row)
        }
    }
}
