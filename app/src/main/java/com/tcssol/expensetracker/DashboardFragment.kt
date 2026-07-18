package com.tcssol.expensetracker

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.tcssol.expensetracker.Model.DailySum
import com.tcssol.expensetracker.Model.ExpenseViewModel
import com.tcssol.expensetracker.Model.Expenses
import com.tcssol.expensetracker.Model.SharedExpenseViewModel
import com.tcssol.expensetracker.Utils.Wrapped
import com.tcssol.expensetracker.databinding.FragmentDashboardBinding
import java.time.LocalDate
import java.time.YearMonth
import java.util.*

class DashboardFragment : Fragment() {
    private val expenseViewModel: ExpenseViewModel by viewModels()
    private val sharedExpenseViewModel: SharedExpenseViewModel by activityViewModels()

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private var chartSource: LiveData<List<Expenses>>? = null
    private var monthlyExpensesSource: LiveData<List<Expenses>>? = null
    private var currentMonthDailySumsSource: LiveData<List<DailySum>>? = null
    private var prevMonthDailySumsSource: LiveData<List<DailySum>>? = null

    private val categoryConfigMap = mutableMapOf<String, Boolean>()
    private var currentMonthGroupedList: List<Expenses> = emptyList()

    // Line Chart Period Toggle Properties
    private var isDailyTrendMode = true
    private var currentDailySums: List<DailySum> = emptyList()
    private var previousDailySums: List<DailySum> = emptyList()
    private var allExpensesList: List<Expenses> = emptyList()
    private var selectedMonth: Int = Calendar.getInstance().get(Calendar.MONTH) + 1
    private var selectedYear: Int = Calendar.getInstance().get(Calendar.YEAR)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val symbol = Currency.getInstance(Locale.getDefault()).symbol

        // Active Lifetime Balance
        expenseViewModel.totalNetBalance.observe(viewLifecycleOwner) { balance ->
            val balVal = balance ?: 0.0
            if (com.tcssol.expensetracker.Utils.TimeViewManager.isTimeViewMode()) {
                binding.tvTotalBalance.text = com.tcssol.expensetracker.Utils.TimeViewManager.formatAmount(balVal, false)
            } else {
                binding.tvTotalBalance.text = symbol + String.format(Locale.getDefault(), "%,.2f", balVal)
            }
            val balanceColor = if (balVal < 0) R.color.expense else R.color.income
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
                val earned = (list?.getOrNull(0) ?: 0).toDouble()
                val spend = (list?.getOrNull(1) ?: 0).toDouble()
                val received = (list?.getOrNull(2) ?: 0).toDouble()
                val given = (list?.getOrNull(3) ?: 0).toDouble()

                if (com.tcssol.expensetracker.Utils.TimeViewManager.isTimeViewMode()) {
                    binding.frag3SetAmtEarned.text = com.tcssol.expensetracker.Utils.TimeViewManager.formatAmount(earned, false)
                    binding.frag3SetAmtSpend.text = com.tcssol.expensetracker.Utils.TimeViewManager.formatAmount(-spend, false)
                    binding.frag3SetAmtReceived.text = com.tcssol.expensetracker.Utils.TimeViewManager.formatAmount(received, false)
                    binding.frag3SetAmtGiven.text = com.tcssol.expensetracker.Utils.TimeViewManager.formatAmount(-given, false)
                } else {
                    binding.frag3SetAmtEarned.text = symbol + String.format(Locale.getDefault(), "%,.0f", earned)
                    binding.frag3SetAmtSpend.text = "-" + symbol + String.format(Locale.getDefault(), "%,.0f", spend)
                    binding.frag3SetAmtReceived.text = symbol + String.format(Locale.getDefault(), "%,.0f", received)
                    binding.frag3SetAmtGiven.text = "-" + symbol + String.format(Locale.getDefault(), "%,.0f", given)
                }
            }

            // Bind Category Custom Chart Data Source
            if (data.year > 0 && data.month > 0) {
                setChartSource(expenseViewModel.getAllExpensesGroupedMonthly(LocalDate.of(data.year, data.month, 1)))
            } else {
                setChartSource(expenseViewModel.allExpensesGrouped)
            }

            // Setup Fixed vs Variable tracking
            val selectedDate = if (data.year > 0 && data.month > 0) {
                LocalDate.of(data.year, data.month, 1)
            } else {
                LocalDate.now()
            }
            selectedMonth = selectedDate.monthValue
            selectedYear = selectedDate.year
            observeMonthlyExpensesForBreakdown(selectedDate)
        }

        setupCategoryPieChart()
        setupLineChart()

        // Observe CategoryConfig to identify fixed categories
        expenseViewModel.allCategoryConfigs.observe(viewLifecycleOwner) { configs ->
            categoryConfigMap.clear()
            configs?.forEach { config ->
                categoryConfigMap[config.categoryName] = config.isFixed
            }
            calculateFixedVariableBreakdown()
        }

        // Tap on breakdown card opens category-wise distribution dialog
        binding.breakdownCard.isClickable = true
        binding.breakdownCard.isFocusable = true
        binding.breakdownCard.setOnClickListener {
            (activity as? MainActivity)?.showOverlayFragment(FixedVariableFragment())
        }

        // Setup switch listener for segmented daily/monthly toggle
        binding.toggleChartPeriod.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                isDailyTrendMode = (checkedId == R.id.btnDaily)
                updateTrendsChart()
            }
        }

        // Keep allExpensesList updated dynamically for monthly comparison
        expenseViewModel.allExpenses.observe(viewLifecycleOwner) { list ->
            allExpensesList = list ?: emptyList()
            updateTrendsChart()
        }

        // Handle Line Chart Data (Daily Sums with Month Comparison)
        sharedExpenseViewModel.getObject().observe(viewLifecycleOwner) { data ->
            selectedMonth = if (data.month > 0) data.month else Calendar.getInstance().get(Calendar.MONTH) + 1
            selectedYear = if (data.year > 0) data.year else Calendar.getInstance().get(Calendar.YEAR)

            val currentSumsLive = expenseViewModel.getDailySums(selectedMonth, selectedYear)
            
            // Previous month calculation
            val prevLocalDate = LocalDate.of(selectedYear, selectedMonth, 1).minusMonths(1)
            val prevSumsLive = expenseViewModel.getDailySums(prevLocalDate.monthValue, prevLocalDate.year)

            // Remove existing observers
            currentMonthDailySumsSource?.removeObservers(viewLifecycleOwner)
            prevMonthDailySumsSource?.removeObservers(viewLifecycleOwner)

            currentMonthDailySumsSource = currentSumsLive
            prevMonthDailySumsSource = prevSumsLive

            currentSumsLive.observe(viewLifecycleOwner) { currentSums ->
                currentDailySums = currentSums ?: emptyList()
                prevSumsLive.observe(viewLifecycleOwner) { prevSums ->
                    previousDailySums = prevSums ?: emptyList()
                    updateTrendsChart()
                }
            }
        }
    }

    private fun observeMonthlyExpensesForBreakdown(date: LocalDate) {
        monthlyExpensesSource?.removeObservers(viewLifecycleOwner)
        val source = expenseViewModel.getAllExpensesMonthly(date)
        monthlyExpensesSource = source
        source.observe(viewLifecycleOwner) { list ->
            currentMonthGroupedList = list ?: emptyList()
            calculateFixedVariableBreakdown()
        }
    }

    private fun isExpenseFixed(category: String, subCategory: String): Boolean {
        val subKey = "$category::$subCategory"
        if (categoryConfigMap.containsKey(subKey)) {
            return categoryConfigMap[subKey] == true
        }
        if (categoryConfigMap.containsKey(category)) {
            return categoryConfigMap[category] == true
        }
        return false
    }

    private fun calculateFixedVariableBreakdown() {
        var fixedTotal = 0.0
        var variableTotal = 0.0

        currentMonthGroupedList.forEach { expense ->
            if (!expense.isType && expense.category != "Money Given" && expense.category != "Money Received") {
                if (isExpenseFixed(expense.category, expense.subCategory)) {
                    fixedTotal += expense.amount
                } else {
                    variableTotal += expense.amount
                }
            }
        }

        val symbol = Currency.getInstance(Locale.getDefault()).symbol
        if (com.tcssol.expensetracker.Utils.TimeViewManager.isTimeViewMode()) {
            binding.tvFixedSpend.text = com.tcssol.expensetracker.Utils.TimeViewManager.formatAmount(fixedTotal, false)
            binding.tvVariableSpend.text = com.tcssol.expensetracker.Utils.TimeViewManager.formatAmount(variableTotal, false)
        } else {
            binding.tvFixedSpend.text = symbol + String.format(Locale.getDefault(), "%,.2f", fixedTotal)
            binding.tvVariableSpend.text = symbol + String.format(Locale.getDefault(), "%,.2f", variableTotal)
        }

        val totalSpend = fixedTotal + variableTotal
        val daysInMonth = if (selectedYear == LocalDate.now().year && selectedMonth == LocalDate.now().monthValue) {
            LocalDate.now().dayOfMonth
        } else {
            java.time.YearMonth.of(selectedYear, selectedMonth).lengthOfMonth()
        }

        val avgDaily = if (daysInMonth > 0) totalSpend / daysInMonth else 0.0
        if (totalSpend > 0) {
            if (com.tcssol.expensetracker.Utils.TimeViewManager.isTimeViewMode()) {
                binding.tvAvgDailySpend.text = "Avg. daily spend this month: " + com.tcssol.expensetracker.Utils.TimeViewManager.formatAmount(avgDaily, false)
            } else {
                binding.tvAvgDailySpend.text = "Avg. daily spend this month: " + symbol + String.format(Locale.getDefault(), "%,.0f", avgDaily)
            }
            binding.tvAvgDailySpend.visibility = View.VISIBLE
        } else {
            binding.tvAvgDailySpend.visibility = View.GONE
        }
    }


    private fun setChartSource(source: LiveData<List<Expenses>>) {
        chartSource?.removeObservers(viewLifecycleOwner)
        chartSource = source
        source.observe(viewLifecycleOwner) { list -> bindCategoryPieChart(list) }
    }

    private fun setupCategoryPieChart() {
        val pieChart = binding.categoryPieChart
        pieChart.setUsePercentValues(false)
        pieChart.description.isEnabled = false
        pieChart.setExtraOffsets(5f, 5f, 5f, 5f)
        pieChart.isDrawHoleEnabled = true
        
        val typedValue = TypedValue()
        requireContext().theme.resolveAttribute(com.google.android.material.R.attr.colorSurface, typedValue, true)
        pieChart.setHoleColor(typedValue.data)

        pieChart.holeRadius = 72f
        pieChart.transparentCircleRadius = 75f
        
        val textColorTypedValue = TypedValue()
        requireContext().theme.resolveAttribute(android.R.attr.textColorPrimary, textColorTypedValue, true)
        pieChart.setCenterTextColor(textColorTypedValue.data)
        pieChart.setDrawEntryLabels(false)
        
        pieChart.legend.isEnabled = false
    }

    private fun bindCategoryPieChart(list: List<Expenses>?) {
        val spend = (list ?: emptyList())
            .filter { !it.isType && it.category != "Money Given" && it.category != "Money Received" }
            .groupBy { it.category }
            .map { (cat, items) -> cat to items.sumOf { it.amount } }
            .sortedByDescending { it.second }

        val totalSpent = spend.sumOf { it.second }

        if (spend.isEmpty()) {
            binding.categoryPieChart.visibility = View.GONE
            binding.llCategoryLegend.visibility = View.GONE
            binding.chartEmptyText.visibility = View.VISIBLE
            return
        }

        binding.categoryPieChart.visibility = View.VISIBLE
        binding.llCategoryLegend.visibility = View.VISIBLE
        binding.chartEmptyText.visibility = View.GONE

        // Prepare chart entries
        val limit = MAX_CHART_ROWS
        val chartItems = mutableListOf<Pair<String, Double>>()
        spend.take(limit).forEach { chartItems.add(it) }
        if (spend.size > limit) {
            val otherSum = spend.drop(limit).sumOf { it.second }
            chartItems.add("Other" to otherSum)
        }

        val entries = chartItems.map { PieEntry(it.second.toFloat(), it.first) }
        
        val colorsList = arrayListOf(
            Color.parseColor("#16A34A"), // Green (primary brand)
            Color.parseColor("#F59E0B"), // Amber (secondary brand)
            Color.parseColor("#8B5CF6"), // Purple
            Color.parseColor("#0F766E"), // Teal
            Color.parseColor("#EC4899"), // Pink
            Color.parseColor("#F97316")  // Orange
        )

        val dataSet = PieDataSet(entries, "").apply {
            colors = colorsList
            setDrawValues(false)
            sliceSpace = 2.5f
        }

        binding.categoryPieChart.data = PieData(dataSet)
        
        // Spannable Center Text: "Total Spending" on top, amount in bold below
        val symbol = Currency.getInstance(Locale.getDefault()).symbol
        val centerTextString = if (com.tcssol.expensetracker.Utils.TimeViewManager.isTimeViewMode()) {
            val totalHoursStr = com.tcssol.expensetracker.Utils.TimeViewManager.formatAmount(totalSpent, false)
            "Total Spending\n$totalHoursStr"
        } else {
            val totalFormatted = String.format(Locale.getDefault(), "%,d", totalSpent.toInt())
            "Total Spending\n$symbol$totalFormatted"
        }
        val spannable = SpannableString(centerTextString).apply {
            setSpan(RelativeSizeSpan(0.80f), 0, 14, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            setSpan(ForegroundColorSpan(getSecondaryTextColor()), 0, 14, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            
            setSpan(RelativeSizeSpan(1.8f), 15, centerTextString.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            setSpan(StyleSpan(android.graphics.Typeface.BOLD), 15, centerTextString.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            setSpan(ForegroundColorSpan(getThemeColor(com.google.android.material.R.attr.colorOnSurface)), 15, centerTextString.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        binding.categoryPieChart.centerText = spannable
        binding.categoryPieChart.invalidate()

        // Populate Legend
        binding.llCategoryLegend.removeAllViews()
        val inflater = LayoutInflater.from(requireContext())
        chartItems.forEachIndexed { index, pair ->
            val percentage = if (totalSpent > 0) (pair.second / totalSpent * 100).toInt() else 0
            val rowView = inflater.inflate(R.layout.dashboard_category_legend_item, binding.llCategoryLegend, false)
            
            val viewColorIndicator = rowView.findViewById<View>(R.id.viewColorIndicator)
            val tvCategoryName = rowView.findViewById<android.widget.TextView>(R.id.tvCategoryName)
            val tvCategoryValue = rowView.findViewById<android.widget.TextView>(R.id.tvCategoryValue)

            val color = colorsList[index % colorsList.size]
            val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.circle)?.mutate() as? GradientDrawable
            drawable?.setColor(color)
            viewColorIndicator.background = drawable

            tvCategoryName.text = pair.first
            if (com.tcssol.expensetracker.Utils.TimeViewManager.isTimeViewMode()) {
                tvCategoryValue.text = "${com.tcssol.expensetracker.Utils.TimeViewManager.formatAmount(pair.second, false)} ($percentage%)"
            } else {
                tvCategoryValue.text = "$symbol${String.format(Locale.getDefault(), "%,d", pair.second.toInt())} ($percentage%)"
            }
            
            binding.llCategoryLegend.addView(rowView)
        }
    }

    private fun setupLineChart() {
        val lineChart = binding.lineChart
        lineChart.description.isEnabled = false
        lineChart.setPinchZoom(false)
        lineChart.setTouchEnabled(true)
        lineChart.isDragEnabled = true
        lineChart.isScaleXEnabled = true
        lineChart.isScaleYEnabled = false
        
        // Add margins to prevent axes numbers cropping on screen boundary
        lineChart.setExtraOffsets(12f, 16f, 12f, 16f)
        
        val markerView = LineChartMarkerView(requireContext(), R.layout.chart_marker_view) { isDailyTrendMode }
        markerView.chartView = lineChart
        lineChart.marker = markerView

        // X Axis setup
        lineChart.xAxis.apply {
            isEnabled = true
            setDrawAxisLine(true)
            setDrawLabels(true)
            position = XAxis.XAxisPosition.BOTTOM
            setDrawGridLines(false)
            granularity = 1f
            textColor = getThemeColor(com.google.android.material.R.attr.colorOnSurface)
            axisLineColor = getThemeColor(com.google.android.material.R.attr.colorOnSurface) and 0x55FFFFFF
        }
        
        // Left Axis setup
        lineChart.axisLeft.apply {
            isEnabled = true
            setDrawAxisLine(true)
            setDrawLabels(true)
            textColor = getThemeColor(com.google.android.material.R.attr.colorOnSurface)
            axisLineColor = getThemeColor(com.google.android.material.R.attr.colorOnSurface) and 0x55FFFFFF
            setDrawGridLines(true)
            gridColor = getThemeColor(com.google.android.material.R.attr.colorOnSurface) and 0x22FFFFFF
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    if (com.tcssol.expensetracker.Utils.TimeViewManager.isTimeViewMode()) {
                        return String.format(Locale.getDefault(), "%.0fh", value)
                    } else {
                        val symbol = Currency.getInstance(Locale.getDefault()).symbol
                        return symbol + String.format(Locale.getDefault(), "%,.0f", value)
                    }
                }
            }
        }
        
        lineChart.axisRight.isEnabled = false
        lineChart.legend.apply {
            textColor = getThemeColor(com.google.android.material.R.attr.colorOnSurface)
            verticalAlignment = Legend.LegendVerticalAlignment.TOP
            horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
            orientation = Legend.LegendOrientation.HORIZONTAL
            setDrawInside(false)
        }
    }

    private fun updateTrendsChart() {
        if (isDailyTrendMode) {
            val prevLocalDate = LocalDate.of(selectedYear, selectedMonth, 1).minusMonths(1)
            updateDailyLineChart(
                selectedYear, selectedMonth, currentDailySums,
                prevLocalDate.year, prevLocalDate.monthValue, previousDailySums
            )
        } else {
            updateMonthlyLineChart(selectedYear, allExpensesList)
        }
    }

    private fun updateDailyLineChart(
        currYear: Int, currMonth: Int, currSums: List<DailySum>,
        prevYear: Int, prevMonth: Int, prevSums: List<DailySum>
    ) {
        val currEntries = getFilledLineEntries(currYear, currMonth, currSums, limitToToday = true)
        val prevEntries = getFilledLineEntries(prevYear, prevMonth, prevSums, limitToToday = false)

        val currDataSet = LineDataSet(currEntries, "This Month").apply {
            color = ContextCompat.getColor(requireContext(), R.color.colorPrimary)
            setCircleColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
            lineWidth = 3f
            circleRadius = 4f
            setDrawCircleHole(false)
            valueTextSize = 0f
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawFilled(true)
            
            val fillDrawable = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(
                    ContextCompat.getColor(requireContext(), R.color.colorPrimary) and 0x55FFFFFF,
                    Color.TRANSPARENT
                )
            )
            this.fillDrawable = fillDrawable
        }

        val prevDataSet = LineDataSet(prevEntries, "Previous Month").apply {
            color = Color.parseColor("#80B45309") // muted amber dashed line
            setDrawCircles(false)
            lineWidth = 2f
            enableDashedLine(10f, 10f, 0f)
            valueTextSize = 0f
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawFilled(false)
        }

        binding.lineChart.data = LineData(prevDataSet, currDataSet)
        
        // Days labels
        binding.lineChart.xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return value.toInt().toString()
            }
        }
        binding.lineChart.invalidate()
    }

    private fun updateMonthlyLineChart(year: Int, allExpenses: List<Expenses>) {
        val currEntries = getMonthlyFilledLineEntries(year, allExpenses, limitToMonth = true)
        val prevEntries = getMonthlyFilledLineEntries(year - 1, allExpenses, limitToMonth = false)

        val currDataSet = LineDataSet(currEntries, "This Year").apply {
            color = ContextCompat.getColor(requireContext(), R.color.colorPrimary)
            setCircleColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
            lineWidth = 3f
            circleRadius = 4f
            setDrawCircleHole(false)
            valueTextSize = 0f
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawFilled(true)
            
            val fillDrawable = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(
                    ContextCompat.getColor(requireContext(), R.color.colorPrimary) and 0x55FFFFFF,
                    Color.TRANSPARENT
                )
            )
            this.fillDrawable = fillDrawable
        }

        val prevDataSet = LineDataSet(prevEntries, "Previous Year").apply {
            color = Color.parseColor("#80B45309") // muted amber dashed line
            setDrawCircles(false)
            lineWidth = 2f
            enableDashedLine(10f, 10f, 0f)
            valueTextSize = 0f
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawFilled(false)
        }

        binding.lineChart.data = LineData(prevDataSet, currDataSet)
        
        // Month names labels
        val monthsAbbr = arrayOf("", "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
        binding.lineChart.xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                val idx = value.toInt()
                return if (idx in 1..12) monthsAbbr[idx] else ""
            }
        }
        binding.lineChart.invalidate()
    }

    private fun getFilledLineEntries(year: Int, month: Int, dailySums: List<DailySum>, limitToToday: Boolean): List<Entry> {
        val daysInMonth = YearMonth.of(year, month).lengthOfMonth()
        val limitDay = if (limitToToday) {
            val today = LocalDate.now()
            if (year == today.year && month == today.monthValue) {
                today.dayOfMonth
            } else {
                daysInMonth
            }
        } else {
            daysInMonth
        }
        val sumMap = dailySums.associate { it.date.dayOfMonth to it.totalSpent }
        
        val rate = com.tcssol.expensetracker.Utils.TimeViewManager.getHourlyRate()
        val isTime = com.tcssol.expensetracker.Utils.TimeViewManager.isTimeViewMode() && rate > 0

        return (1..limitDay).map { day ->
            val total = sumMap[day] ?: 0.0
            val graphVal = if (isTime) total / rate else total
            Entry(day.toFloat(), graphVal.toFloat())
        }
    }

    private fun getMonthlyFilledLineEntries(year: Int, allExpenses: List<Expenses>, limitToMonth: Boolean): List<Entry> {
        val limitMonth = if (limitToMonth) {
            val today = LocalDate.now()
            if (year == today.year) {
                today.monthValue
            } else {
                12
            }
        } else {
            12
        }
        val filtered = allExpenses.filter {
            it.dateCreated.year == year &&
            !it.isType &&
            it.category != "Money Given" &&
            it.category != "Money Received"
        }
        val sumMap = filtered.groupBy { it.dateCreated.monthValue }
            .mapValues { (_, items) -> items.sumOf { it.amount } }

        val rate = com.tcssol.expensetracker.Utils.TimeViewManager.getHourlyRate()
        val isTime = com.tcssol.expensetracker.Utils.TimeViewManager.isTimeViewMode() && rate > 0

        return (1..limitMonth).map { month ->
            val total = sumMap[month] ?: 0.0
            val graphVal = if (isTime) total / rate else total
            Entry(month.toFloat(), graphVal.toFloat())
        }
      }

    private fun getPrimaryTextColor(): Int {
        val typedValue = TypedValue()
        requireContext().theme.resolveAttribute(android.R.attr.textColorPrimary, typedValue, true)
        return typedValue.data
    }

    private fun getSecondaryTextColor(): Int {
        val typedValue = TypedValue()
        requireContext().theme.resolveAttribute(android.R.attr.textColorSecondary, typedValue, true)
        return typedValue.data
    }

    private fun getThemeColor(attrId: Int): Int {
        val typedValue = TypedValue()
        requireContext().theme.resolveAttribute(attrId, typedValue, true)
        return typedValue.data
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    class LineChartMarkerView(
        context: android.content.Context, 
        layoutResource: Int,
        private val isDailyMode: () -> Boolean
    ) : com.github.mikephil.charting.components.MarkerView(context, layoutResource) {
        
        private val tvDate: android.widget.TextView = findViewById(R.id.tvMarkerDate)
        private val tvAmount: android.widget.TextView = findViewById(R.id.tvMarkerAmount)
        private val monthsAbbr = arrayOf("", "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

        override fun refreshContent(e: Entry?, highlight: com.github.mikephil.charting.highlight.Highlight?) {
            if (e != null) {
                val xVal = e.x.toInt()
                if (isDailyMode()) {
                    tvDate.text = "Day $xVal"
                } else {
                    tvDate.text = if (xVal in 1..12) monthsAbbr[xVal] else "Month $xVal"
                }
                val isTime = com.tcssol.expensetracker.Utils.TimeViewManager.isTimeViewMode()
                if (isTime) {
                    val rate = com.tcssol.expensetracker.Utils.TimeViewManager.getHourlyRate()
                    val originalCurrency = e.y.toDouble() * rate
                    tvAmount.text = com.tcssol.expensetracker.Utils.TimeViewManager.formatAmount(originalCurrency, false)
                } else {
                    tvAmount.text = "₹${String.format(java.util.Locale.getDefault(), "%,d", e.y.toInt())}"
                }
            }
            super.refreshContent(e, highlight)
        }

        override fun getOffset(): com.github.mikephil.charting.utils.MPPointF {
            return com.github.mikephil.charting.utils.MPPointF((-(width / 2)).toFloat(), (-height).toFloat())
        }
    }

    companion object {
        private const val MAX_CHART_ROWS = 6
    }
}
