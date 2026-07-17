package com.tcssol.expensetracker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.tcssol.expensetracker.Adapters.ModeDistributionAdapter
import com.tcssol.expensetracker.Model.DailySum
import com.tcssol.expensetracker.Model.ExpenseViewModel
import com.tcssol.expensetracker.Model.PersonExpViewModel
import com.tcssol.expensetracker.Model.SharedExpenseViewModel
import com.tcssol.expensetracker.Utils.ModeWrapper
import com.tcssol.expensetracker.Utils.Wrapped
import com.tcssol.expensetracker.databinding.Fragment3Binding
import java.time.format.DateTimeFormatter
import java.util.*
import android.graphics.Color
import android.util.Log
import android.util.TypedValue
import androidx.core.content.ContextCompat
import com.tcssol.expensetracker.Model.Expenses
import java.time.LocalDate

/**
 * TODO Add charts and other views to show trends and options to set budgets
 */
class Fragment3 : Fragment() {
    private val expenseViewModel: ExpenseViewModel by viewModels()
    private val sharedExpenseViewModel: SharedExpenseViewModel by activityViewModels()
    private val personExpViewModel: PersonExpViewModel by  viewModels()

    private var view: View? = null
    private var _binding:Fragment3Binding?=null
    private val binding get()= _binding!!
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
        
        val symbol = Currency.getInstance(Locale.getDefault()).symbol

        // Observe Total Active Balance (Lifetime)
        expenseViewModel.totalNetBalance.observe(viewLifecycleOwner) { balance ->
            binding.tvTotalBalance.text = symbol + String.format("%.2f", balance ?: 0.0)
            if ((balance ?: 0.0) < 0) {
                binding.tvTotalBalance.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
            } else {
                binding.tvTotalBalance.setTextColor(ContextCompat.getColor(requireContext(), R.color.green))
            }
        }
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
        sharedExpenseViewModel!!.getObject().observe(
            viewLifecycleOwner
        ) { data: Wrapped ->
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
                    binding.frag3SetAmtEarned.text = symbol + (if (list[0] == null) 0 else list[0]).toString()
                    binding.frag3SetAmtSpend.text = "-" + symbol + (if (list[1] == null) 0 else list[1]).toString()
                    binding.frag3SetAmtReceived.text = symbol + (if (list[2] == null) 0 else list[2]).toString()
                    binding.frag3SetAmtGiven.text = "-" + symbol + (if (list[3] == null) 0 else list[3]).toString()
                }
            }
        }

        setupPieChart()
        setupCategoryPieChart()
        setupBarChart()

        // Handle Bar Chart Data
        sharedExpenseViewModel.getObject().observe(viewLifecycleOwner) { data ->
            val month = if (data.month > 0) data.month else Calendar.getInstance().get(Calendar.MONTH) + 1
            val year = if (data.year > 0) data.year else Calendar.getInstance().get(Calendar.YEAR)
            
            expenseViewModel.getDailySums(month, year).observe(viewLifecycleOwner) { dailySums ->
                updateBarChart(dailySums)
            }
        }

        // Handle Category Pie Chart Data
        sharedExpenseViewModel.getObject().observe(viewLifecycleOwner) { data ->
            if (data.year > 0 || data.month > 0) {
                val date = LocalDate.of(data.year, data.month, 1)
                expenseViewModel.getAllExpensesGroupedMonthly(date).observe(viewLifecycleOwner) { list ->
                    updateCategoryPieChart(list)
                }
            } else {
                expenseViewModel.allExpensesGrouped.observe(viewLifecycleOwner) { list ->
                    updateCategoryPieChart(list)
                }
            }
        }
        
        expenseViewModel!!.getModeDist().observe(viewLifecycleOwner) { data: List<ModeWrapper?>? ->
            if (data != null && data.isNotEmpty()) {
                val entries = ArrayList<PieEntry>()
                for (wrapper in data) {
                    if (wrapper != null) {
                        entries.add(PieEntry(wrapper.perentage.toFloat(), wrapper.name))
                    }
                }
                
                val dataSet = PieDataSet(entries, "")
                
                // Use material theme colors for the pie slices
                val colors = ArrayList<Int>()
                colors.add(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
                colors.add(ContextCompat.getColor(requireContext(), R.color.colorSecondary))
                colors.add(ContextCompat.getColor(requireContext(), R.color.green))
                colors.add(Color.parseColor("#FFA726")) // Orange
                colors.add(Color.parseColor("#29B6F6")) // Light blue
                dataSet.colors = colors
                
                dataSet.valueTextColor = ContextCompat.getColor(requireContext(), R.color.colorOnPrimary)
                dataSet.valueTextSize = 12f
                dataSet.valueFormatter = PercentFormatter(binding.pieChart)
                
                val pieData = PieData(dataSet)
                binding.pieChart.data = pieData
                binding.pieChart.invalidate() // refresh
            }
        }
    }

    private fun setupPieChart() {
        val pieChart = binding.pieChart
        pieChart.setUsePercentValues(true)
        pieChart.description.isEnabled = false
        pieChart.setExtraOffsets(5f, 10f, 5f, 5f)
        
        pieChart.dragDecelerationFrictionCoef = 0.95f
        
        pieChart.isDrawHoleEnabled = true
        // Get surface color for the center hole
        val typedValue = TypedValue()
        requireContext().theme.resolveAttribute(com.google.android.material.R.attr.colorSurface, typedValue, true)
        pieChart.setHoleColor(typedValue.data)
        pieChart.setTransparentCircleColor(Color.WHITE)
        pieChart.setTransparentCircleAlpha(110)
        
        pieChart.holeRadius = 58f
        pieChart.transparentCircleRadius = 61f
        
        pieChart.setDrawCenterText(true)
        pieChart.centerText = "Payment\nMedium"
        // Get text color for center
        val textColorTypedValue = TypedValue()
        requireContext().theme.resolveAttribute(android.R.attr.textColorPrimary, textColorTypedValue, true)
        pieChart.setCenterTextColor(textColorTypedValue.data)
        
        pieChart.rotationAngle = 0f
        pieChart.isRotationEnabled = true
        pieChart.isHighlightPerTapEnabled = true
        
        pieChart.animateY(1400, com.github.mikephil.charting.animation.Easing.EaseInOutQuad)
        
        val l = pieChart.legend
        l.verticalAlignment = Legend.LegendVerticalAlignment.TOP
        l.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
        l.orientation = Legend.LegendOrientation.VERTICAL
        l.setDrawInside(false)
        l.xEntrySpace = 7f
        l.yEntrySpace = 0f
        l.yOffset = 0f
        l.textColor = textColorTypedValue.data
    }

    private fun setupBarChart() {
        val barChart = binding.barChart
        barChart.description.isEnabled = false
        barChart.setPinchZoom(false)
        barChart.setDrawBarShadow(false)
        barChart.setDrawGridBackground(false)

        val xAxis = barChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.granularity = 1f
        xAxis.textColor = getPrimaryTextColor()

        barChart.axisLeft.textColor = getPrimaryTextColor()
        barChart.axisRight.isEnabled = false
        
        barChart.legend.textColor = getPrimaryTextColor()
        barChart.animateY(1000)
    }

    private fun updateBarChart(dailySums: List<DailySum>) {
        if (dailySums.isEmpty()) {
            binding.barChart.clear()
            return
        }

        val spentEntries = ArrayList<BarEntry>()
        val dateLabels = ArrayList<String>()

        val formatter = DateTimeFormatter.ofPattern("dd")
        
        dailySums.forEachIndexed { index, dailySum ->
            spentEntries.add(BarEntry(index.toFloat(), dailySum.totalSpent.toFloat()))
            dateLabels.add(dailySum.date.format(formatter))
        }

        val spentSet = BarDataSet(spentEntries, "Spent")
        spentSet.color = ContextCompat.getColor(requireContext(), R.color.red)
        spentSet.valueTextColor = getPrimaryTextColor()
        spentSet.valueTextSize = 10f

        val data = BarData(spentSet)
        data.barWidth = 0.6f
        
        binding.barChart.data = data
        
        binding.barChart.xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                val idx = value.toInt()
                return if (idx >= 0 && idx < dateLabels.size) dateLabels[idx] else ""
            }
        }
        
        binding.barChart.invalidate()
    }

    private fun getPrimaryTextColor(): Int {
        val typedValue = TypedValue()
        requireContext().theme.resolveAttribute(android.R.attr.textColorPrimary, typedValue, true)
        return typedValue.data
    }

    private fun setupCategoryPieChart() {
        val pieChart = binding.pieChartCategory
        pieChart.setUsePercentValues(true)
        pieChart.description.isEnabled = false
        pieChart.setExtraOffsets(5f, 10f, 5f, 5f)
        
        pieChart.dragDecelerationFrictionCoef = 0.95f
        
        pieChart.isDrawHoleEnabled = true
        val typedValue = TypedValue()
        requireContext().theme.resolveAttribute(com.google.android.material.R.attr.colorSurface, typedValue, true)
        pieChart.setHoleColor(typedValue.data)
        pieChart.setTransparentCircleColor(Color.WHITE)
        pieChart.setTransparentCircleAlpha(110)
        
        pieChart.holeRadius = 58f
        pieChart.transparentCircleRadius = 61f
        
        pieChart.setDrawCenterText(true)
        pieChart.centerText = "Category\nSpend"
        val textColorTypedValue = TypedValue()
        requireContext().theme.resolveAttribute(android.R.attr.textColorPrimary, textColorTypedValue, true)
        pieChart.setCenterTextColor(textColorTypedValue.data)
        
        pieChart.rotationAngle = 0f
        pieChart.isRotationEnabled = true
        pieChart.isHighlightPerTapEnabled = true
        
        pieChart.animateY(1400, com.github.mikephil.charting.animation.Easing.EaseInOutQuad)
        
        val l = pieChart.legend
        l.verticalAlignment = Legend.LegendVerticalAlignment.TOP
        l.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
        l.orientation = Legend.LegendOrientation.VERTICAL
        l.setDrawInside(false)
        l.xEntrySpace = 7f
        l.yEntrySpace = 0f
        l.yOffset = 0f
        l.textColor = textColorTypedValue.data
    }

    private fun updateCategoryPieChart(list: List<Expenses>?) {
        if (list == null || list.isEmpty()) {
            binding.pieChartCategory.clear()
            return
        }

        val entries = ArrayList<PieEntry>()
        for (expense in list) {
            if (!expense.isType) {
                if (expense.amount > 0 && !expense.category.isNullOrEmpty()) {
                    entries.add(PieEntry(expense.amount.toFloat(), expense.category))
                }
            }
        }

        if (entries.isEmpty()) {
            binding.pieChartCategory.clear()
            return
        }

        val dataSet = PieDataSet(entries, "")
        
        val colors = ArrayList<Int>()
        colors.add(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
        colors.add(ContextCompat.getColor(requireContext(), R.color.colorSecondary))
        colors.add(ContextCompat.getColor(requireContext(), R.color.red))
        colors.add(Color.parseColor("#9C27B0")) // Purple
        colors.add(Color.parseColor("#FF9800")) // Orange
        colors.add(Color.parseColor("#00BCD4")) // Cyan
        colors.add(Color.parseColor("#E91E63")) // Pink
        dataSet.colors = colors

        dataSet.valueTextColor = ContextCompat.getColor(requireContext(), R.color.colorOnPrimary)
        dataSet.valueTextSize = 12f
        dataSet.valueFormatter = PercentFormatter(binding.pieChartCategory)

        val pieData = PieData(dataSet)
        binding.pieChartCategory.data = pieData
        binding.pieChartCategory.invalidate()
    }
}