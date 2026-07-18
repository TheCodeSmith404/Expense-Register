package com.tcssol.expensetracker

import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.tabs.TabLayout
import com.tcssol.expensetracker.Model.ExpenseViewModel
import com.tcssol.expensetracker.Model.Expenses
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Currency
import java.util.Locale

class CategoryCompareFragment : Fragment() {

    private val expenseViewModel: ExpenseViewModel by viewModels()

    private lateinit var lineChart: LineChart
    private lateinit var tabLayout: TabLayout
    private lateinit var tvSubtitle: TextView

    private var allExpensesList: List<Expenses> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_category_compare, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lineChart = view.findViewById(R.id.lineChartCompare)
        tabLayout = view.findViewById(R.id.tabLayoutCompare)
        tvSubtitle = view.findViewById(R.id.tvCompareChartSubtitle)

        val btnBack: ImageButton = view.findViewById(R.id.btnCompareBack)
        btnBack.setOnClickListener {
            parentFragmentManager.beginTransaction().remove(this).commit()
            (activity as? MainActivity)?.showOverlayFragment(FixedVariableFragment())
        }

        setupChart()

        expenseViewModel.allExpenses.observe(viewLifecycleOwner) { list ->
            allExpensesList = list ?: emptyList()
            updateChart()
        }

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                updateChart()
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun getThemeColor(attrId: Int): Int {
        val typedValue = TypedValue()
        requireContext().theme.resolveAttribute(attrId, typedValue, true)
        return typedValue.data
    }

    private fun setupChart() {
        lineChart.description.isEnabled = false
        lineChart.setDrawGridBackground(false)
        lineChart.setExtraOffsets(8f, 16f, 16f, 8f)
        lineChart.setDragEnabled(true)
        lineChart.setScaleEnabled(false)
        lineChart.setPinchZoom(false)

        // Use colorOnSurface – always a direct resolved color, same pattern as DashboardFragment
        val onSurface = getThemeColor(com.google.android.material.R.attr.colorOnSurface)
        val gridAlpha  = Color.argb(0x22, Color.red(onSurface), Color.green(onSurface), Color.blue(onSurface))
        val axisAlpha  = Color.argb(0x55, Color.red(onSurface), Color.green(onSurface), Color.blue(onSurface))

        // Legend
        lineChart.legend.apply {
            isEnabled = true
            textColor = onSurface
            textSize = 11f
            isWordWrapEnabled = true
            xEntrySpace = 12f
        }

        // X Axis
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

        // Left Axis
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
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    if (com.tcssol.expensetracker.Utils.TimeViewManager.isTimeViewMode()) {
                        return String.format(Locale.getDefault(), "%.0fh", value)
                    }
                    val symbol = Currency.getInstance(Locale.getDefault()).symbol
                    return when {
                        value >= 100_000f -> "$symbol${String.format(Locale.getDefault(), "%.1fL", value / 100_000f)}"
                        value >= 1_000f   -> "$symbol${String.format(Locale.getDefault(), "%.0fk", value / 1_000f)}"
                        else              -> "$symbol${String.format(Locale.getDefault(), "%.0f", value)}"
                    }
                }
            }
        }

        lineChart.axisRight.isEnabled = false

        // Custom tooltip MarkerView
        val marker = CompareMarkerView(requireContext(), R.layout.chart_marker_view) { tabLayout.selectedTabPosition == 0 }
        marker.chartView = lineChart
        lineChart.marker = marker
    }

    private fun updateChart() {
        if (allExpensesList.isEmpty()) {
            lineChart.clear()
            return
        }

        val symbol = Currency.getInstance(Locale.getDefault()).symbol
        val colorsList = listOf(
            Color.parseColor("#16A34A"), // Green
            Color.parseColor("#F59E0B"), // Amber
            Color.parseColor("#8B5CF6"), // Purple
            Color.parseColor("#0F766E"), // Teal
            Color.parseColor("#EC4899"), // Pink
            Color.parseColor("#F97316"), // Orange
            Color.parseColor("#3B82F6")  // Blue
        )

        val isMonthlyMode = tabLayout.selectedTabPosition == 0

        // Only expenses, exclude P2P pass-through categories
        val spendings = allExpensesList.filter {
            !it.isType && it.category != "Money Given" && it.category != "Money Received"
        }

        val categories = spendings.map { it.category }.distinct()
        val dataSets = mutableListOf<LineDataSet>()

        val rate = com.tcssol.expensetracker.Utils.TimeViewManager.getHourlyRate()
        val isTime = com.tcssol.expensetracker.Utils.TimeViewManager.isTimeViewMode() && rate > 0

        if (isMonthlyMode) {
            // Collect every distinct "yyyy-MM" month that actually has spending data
            val monthFmt = DateTimeFormatter.ofPattern("yyyy-MM")
            val allMonths = spendings
                .map { it.dateCreated.format(monthFmt) }
                .distinct()
                .sorted()   // chronological order, oldest → newest

            val monthCount = allMonths.size
            tvSubtitle.text = "Monthly Spending Comparison ($monthCount months)"

            val grouped = spendings.groupBy { it.category }

            var catColorIdx = 0
            categories.forEach { category ->
                val catExpenses = grouped[category] ?: emptyList()
                val monthSums = catExpenses.groupBy {
                    it.dateCreated.format(monthFmt)
                }.mapValues { e -> e.value.sumOf { it.amount } }

                val entries = allMonths.mapIndexed { idx, monthKey ->
                    val sum = monthSums[monthKey] ?: 0.0
                    Entry(idx.toFloat(), (if (isTime) sum / rate else sum).toFloat())
                }

                if (entries.any { it.y > 0 }) {
                    val col = colorsList[catColorIdx % colorsList.size]
                    catColorIdx++
                    dataSets.add(LineDataSet(entries, category).apply {
                        color = col
                        setCircleColor(col)
                        circleHoleColor = Color.TRANSPARENT
                        lineWidth = 2f
                        circleRadius = 4f
                        circleHoleRadius = 2f
                        setDrawValues(false)
                        setDrawCircles(true)
                        setDrawFilled(false)
                        mode = LineDataSet.Mode.CUBIC_BEZIER
                    })
                }
            }

            // X-axis: abbreviated month labels; "Jan '24" style when year changes
            val monthsAbbr = arrayOf("", "Jan", "Feb", "Mar", "Apr", "May", "Jun",
                "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
            lineChart.xAxis.apply {
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        val idx = value.toInt()
                        if (idx !in allMonths.indices) return ""
                        val parts = allMonths[idx].split("-")
                        return try {
                            val abbr = monthsAbbr[parts[1].toInt()]
                            // Show year suffix on Jan or first month in list
                            if (idx == 0 || parts[1] == "01") "$abbr '${parts[0].takeLast(2)}"
                            else abbr
                        } catch (e: Exception) { "" }
                    }
                }
                axisMinimum = -0.5f
                axisMaximum = (monthCount - 1) + 0.5f
                // Show at most 6 labels at once; MPChart picks the right ones when scrolling
                setLabelCount(minOf(monthCount, 6), false)
                setLabelRotationAngle(0f)
            }

            // Viewport: always show the 6 most recent months; user can scroll left to see history
            val visibleWindow = minOf(monthCount, 6).toFloat()
            lineChart.setVisibleXRangeMaximum(visibleWindow)
            // After data is set we'll scroll to the rightmost (latest) month

        } else {
            tvSubtitle.text = "Daily Spending Comparison (This Month)"

            val currentMonth = LocalDate.now().monthValue
            val currentYear = LocalDate.now().year
            val daysInMonth = LocalDate.now().lengthOfMonth()

            val thisMonthSpendings = spendings.filter {
                it.dateCreated.monthValue == currentMonth && it.dateCreated.year == currentYear
            }

            val grouped = thisMonthSpendings.groupBy { it.category }

            var catColorIdx = 0
            categories.forEach { category ->
                val catExpenses = grouped[category] ?: emptyList()
                val dailySums = catExpenses.groupBy { it.dateCreated.dayOfMonth }
                    .mapValues { e -> e.value.sumOf { it.amount } }

                val entries = (1..daysInMonth).map { day ->
                    val sum = dailySums[day] ?: 0.0
                    Entry(day.toFloat(), (if (isTime) sum / rate else sum).toFloat())
                }

                if (entries.any { it.y > 0 }) {
                    val col = colorsList[catColorIdx % colorsList.size]
                    catColorIdx++
                    dataSets.add(LineDataSet(entries, category).apply {
                        color = col
                        setCircleColor(col)
                        lineWidth = 1.8f
                        circleRadius = 0f   // no circles for 31 days – too cluttered
                        setDrawValues(false)
                        setDrawCircles(false)
                        setDrawFilled(false)
                        mode = LineDataSet.Mode.LINEAR
                    })
                }
            }

            // X-axis: only show every 5th day label to avoid clutter
            lineChart.xAxis.apply {
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        val d = value.toInt()
                        return if (d % 5 == 0 || d == 1 || d == daysInMonth) "$d" else ""
                    }
                }
                axisMinimum = 0.5f
                axisMaximum = daysInMonth + 0.5f
                setLabelCount(7, false)
                setLabelRotationAngle(0f)
            }
        }

        if (dataSets.isNotEmpty()) {
            lineChart.data = LineData(dataSets as List<LineDataSet>)
            lineChart.animateX(400)
            // In monthly mode: scroll to the rightmost (latest) data point after animation
            if (isMonthlyMode) {
                lineChart.post {
                    val maxX = lineChart.data?.xMax ?: 0f
                    lineChart.moveViewToX(maxX)
                }
            }
        } else {
            lineChart.clear()
        }
        lineChart.invalidate()
    }

    class CompareMarkerView(
        context: android.content.Context,
        layoutResource: Int,
        private val isMonthly: () -> Boolean
    ) : com.github.mikephil.charting.components.MarkerView(context, layoutResource) {
        private val tvDate: TextView = findViewById(R.id.tvMarkerDate)
        private val tvAmount: TextView = findViewById(R.id.tvMarkerAmount)
        private val monthsAbbr = arrayOf("", "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

        override fun refreshContent(e: Entry?, highlight: com.github.mikephil.charting.highlight.Highlight?) {
            if (e != null) {
                val xVal = e.x.toInt()
                if (isMonthly()) {
                    tvDate.text = "Month $xVal"
                } else {
                    tvDate.text = "Day $xVal"
                }
                val isTime = com.tcssol.expensetracker.Utils.TimeViewManager.isTimeViewMode()
                if (isTime) {
                    val rate = com.tcssol.expensetracker.Utils.TimeViewManager.getHourlyRate()
                    val originalCurrency = e.y.toDouble() * rate
                    tvAmount.text = com.tcssol.expensetracker.Utils.TimeViewManager.formatAmount(originalCurrency, false)
                } else {
                    val symbol = Currency.getInstance(Locale.getDefault()).symbol
                    tvAmount.text = "$symbol${String.format(Locale.getDefault(), "%,.0f", e.y)}"
                }
            }
            super.refreshContent(e, highlight)
        }

        override fun getOffset(): com.github.mikephil.charting.utils.MPPointF {
            return com.github.mikephil.charting.utils.MPPointF((-(width / 2)).toFloat(), (-height).toFloat())
        }
    }
}

