package com.tcssol.expensetracker

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tcssol.expensetracker.Adapters.ModeDistributionAdapter
import com.tcssol.expensetracker.Model.ExpenseViewModel
import com.tcssol.expensetracker.Model.PersonExpViewModel
import com.tcssol.expensetracker.Model.SharedExpenseViewModel
import com.tcssol.expensetracker.Utils.ModeWrapper
import com.tcssol.expensetracker.Utils.Wrapped
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.components.Legend
import android.graphics.Color
import android.util.TypedValue
import androidx.core.content.ContextCompat
import com.tcssol.expensetracker.databinding.Fragment3Binding
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
                    binding.frag3SetAmtEarned.text = (if (list[0] == null) 0 else list[0]).toString()
                    binding.frag3SetAmtSpend.text = "-" + (if (list[1] == null) 0 else list[1]).toString()
                    binding.frag3SetAmtReceived.text = (if (list[2] == null) 0 else list[2]).toString()
                    binding.frag3SetAmtGiven.text = "-" + (if (list[3] == null) 0 else list[3]).toString()
                }
            }
        }

        setupPieChart()
        
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
}