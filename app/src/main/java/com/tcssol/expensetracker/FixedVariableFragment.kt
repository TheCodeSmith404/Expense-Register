package com.tcssol.expensetracker

import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.tcssol.expensetracker.Model.ExpenseViewModel
import com.tcssol.expensetracker.Model.Expenses
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Currency
import java.util.Locale

class FixedVariableFragment : Fragment() {

    private val expenseViewModel: ExpenseViewModel by viewModels()
    private val categoryConfigMap = mutableMapOf<String, Boolean>()
    private val categoryBudgetMap = mutableMapOf<String, Double>()
    
    private var selectedDate = LocalDate.now().withDayOfMonth(1)
    
    private lateinit var tvCurrentMonth: TextView
    private lateinit var tvVariableTotal: TextView
    private lateinit var tvFixedTotal: TextView
    private lateinit var tvBreakdownEmpty: TextView
    private lateinit var llVariableCategories: LinearLayout
    private lateinit var llFixedCategories: LinearLayout
    private lateinit var sectionVariable: View
    private lateinit var sectionFixed: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_fixed_variable, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Bind views
        tvCurrentMonth = view.findViewById(R.id.tvCurrentMonth)
        tvVariableTotal = view.findViewById(R.id.tvVariableTotal)
        tvFixedTotal = view.findViewById(R.id.tvFixedTotal)
        tvBreakdownEmpty = view.findViewById(R.id.tvBreakdownEmpty)
        llVariableCategories = view.findViewById(R.id.llVariableCategories)
        llFixedCategories = view.findViewById(R.id.llFixedCategories)
        sectionVariable = view.findViewById(R.id.sectionVariable)
        sectionFixed = view.findViewById(R.id.sectionFixed)

        val btnBack: ImageButton = view.findViewById(R.id.btnBack)
        btnBack.setOnClickListener {
            parentFragmentManager.beginTransaction().remove(this).commit()
            (activity as? MainActivity)?.hideOverlayContainer()
        }

        val btnPrevMonth: ImageButton = view.findViewById(R.id.btnPrevMonth)
        btnPrevMonth.setOnClickListener {
            selectedDate = selectedDate.minusMonths(1)
            updateMonthView()
        }

        val btnNextMonth: ImageButton = view.findViewById(R.id.btnNextMonth)
        btnNextMonth.setOnClickListener {
            selectedDate = selectedDate.plusMonths(1)
            updateMonthView()
        }

        val btnCategoryCompareCharts: ImageButton = view.findViewById(R.id.btnCategoryCompareCharts)
        btnCategoryCompareCharts.setOnClickListener {
            val fragment = CategoryCompareFragment()
            (activity as? MainActivity)?.showOverlayFragment(fragment)
        }

        // Observe CategoryConfig to identify fixed categories & budgets
        expenseViewModel.allCategoryConfigs.observe(viewLifecycleOwner) { configs ->
            categoryConfigMap.clear()
            categoryBudgetMap.clear()
            configs?.forEach { config ->
                categoryConfigMap[config.categoryName] = config.isFixed
                categoryBudgetMap[config.categoryName] = config.budget
            }
            loadMonthlyData()
        }

        updateMonthView()
    }

    private fun updateMonthView() {
        val monthName = selectedDate.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
        val year = selectedDate.year
        tvCurrentMonth.text = "$monthName $year"
        loadMonthlyData()
    }

    private fun loadMonthlyData() {
        expenseViewModel.getAllExpensesMonthly(selectedDate).observe(viewLifecycleOwner) { list ->
            calculateAndPopulate(list ?: emptyList())
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

    private fun getThemeColor(attrId: Int): Int {
        val typedValue = TypedValue()
        requireContext().theme.resolveAttribute(attrId, typedValue, true)
        return typedValue.data
    }

    private fun calculateAndPopulate(expensesList: List<Expenses>) {
        val symbol = Currency.getInstance(Locale.getDefault()).symbol

        data class CategoryEntry(val name: String, val amount: Double)

        val fixedMap = mutableMapOf<String, Double>()
        val variableMap = mutableMapOf<String, Double>()

        expensesList.forEach { expense ->
            if (!expense.isType && expense.category != "Money Given" && expense.category != "Money Received") {
                val amount = expense.amount
                if (isExpenseFixed(expense.category, expense.subCategory)) {
                    fixedMap[expense.category] = (fixedMap[expense.category] ?: 0.0) + amount
                } else {
                    variableMap[expense.category] = (variableMap[expense.category] ?: 0.0) + amount
                }
            }
        }

        val fixedList = fixedMap.entries.sortedByDescending { it.value }.map { CategoryEntry(it.key, it.value) }
        val variableList = variableMap.entries.sortedByDescending { it.value }.map { CategoryEntry(it.key, it.value) }

        val fixedTotal = fixedList.sumOf { it.amount }
        val variableTotal = variableList.sumOf { it.amount }
        val grandTotal = fixedTotal + variableTotal

        if (com.tcssol.expensetracker.Utils.TimeViewManager.isTimeViewMode()) {
            tvFixedTotal.text = com.tcssol.expensetracker.Utils.TimeViewManager.formatAmount(fixedTotal, false)
            tvVariableTotal.text = com.tcssol.expensetracker.Utils.TimeViewManager.formatAmount(variableTotal, false)
        } else {
            tvFixedTotal.text = symbol + String.format(Locale.getDefault(), "%,.0f", fixedTotal)
            tvVariableTotal.text = symbol + String.format(Locale.getDefault(), "%,.0f", variableTotal)
        }

        // Color list for categories (same as pie chart)
        val colorsList = listOf(
            Color.parseColor("#16A34A"), // Green
            Color.parseColor("#F59E0B"), // Amber
            Color.parseColor("#8B5CF6"), // Purple
            Color.parseColor("#0F766E"), // Teal
            Color.parseColor("#EC4899"), // Pink
            Color.parseColor("#F97316")  // Orange
        )

        fun populateSection(container: LinearLayout, items: List<CategoryEntry>, sectionTotal: Double, isFixed: Boolean) {
            container.removeAllViews()
            val inflater = LayoutInflater.from(requireContext())
            val defaultColor = if (isFixed) getThemeColor(com.google.android.material.R.attr.colorSecondary) else getThemeColor(com.google.android.material.R.attr.colorPrimary)
            
            items.forEachIndexed { index, entry ->
                val row = inflater.inflate(R.layout.item_fv_category, container, false)
                
                val viewDot = row.findViewById<View>(R.id.viewCategoryDot)
                val dotColor = colorsList[index % colorsList.size]
                val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.circle)?.mutate() as? android.graphics.drawable.GradientDrawable
                drawable?.setColor(dotColor)
                viewDot.background = drawable

                row.findViewById<TextView>(R.id.tvFVCategoryName).text = entry.name
                
                if (com.tcssol.expensetracker.Utils.TimeViewManager.isTimeViewMode()) {
                    row.findViewById<TextView>(R.id.tvFVCategoryAmount).text =
                        com.tcssol.expensetracker.Utils.TimeViewManager.formatAmount(entry.amount, false)
                } else {
                    row.findViewById<TextView>(R.id.tvFVCategoryAmount).text =
                        symbol + String.format(Locale.getDefault(), "%,.0f", entry.amount)
                }

                val pct = if (grandTotal > 0) (entry.amount / grandTotal * 100).toInt() else 0
                row.findViewById<TextView>(R.id.tvFVCategoryPercent).text = "$pct%"
                
                // Budget logic
                val budgetLimit = categoryBudgetMap[entry.name] ?: 0.0
                val tvBudget = row.findViewById<TextView>(R.id.tvFVCategoryBudget)
                val pb = row.findViewById<ProgressBar>(R.id.pbFVCategory)
                
                var accentColor = defaultColor
                
                if (budgetLimit > 0) {
                    val remaining = budgetLimit - entry.amount
                    val ratio = entry.amount / budgetLimit
                    
                    if (com.tcssol.expensetracker.Utils.TimeViewManager.isTimeViewMode()) {
                        val limitStr = com.tcssol.expensetracker.Utils.TimeViewManager.formatAmount(budgetLimit, false)
                        if (remaining >= 0) {
                            val remStr = com.tcssol.expensetracker.Utils.TimeViewManager.formatAmount(remaining, false)
                            tvBudget.text = "Budget: $limitStr ($remStr left)"
                            tvBudget.setTextColor(getThemeColor(android.R.attr.textColorSecondary))
                        } else {
                            val overStr = com.tcssol.expensetracker.Utils.TimeViewManager.formatAmount(-remaining, false)
                            tvBudget.text = "Budget: $limitStr ($overStr over)"
                            tvBudget.setTextColor(Color.parseColor("#EF4444")) // Warning Red
                        }
                    } else {
                        if (remaining >= 0) {
                            tvBudget.text = "Budget: " + symbol + String.format(Locale.getDefault(), "%,.0f", budgetLimit) + " (" + symbol + String.format(Locale.getDefault(), "%,.0f", remaining) + " left)"
                            tvBudget.setTextColor(getThemeColor(android.R.attr.textColorSecondary))
                        } else {
                            tvBudget.text = "Budget: " + symbol + String.format(Locale.getDefault(), "%,.0f", budgetLimit) + " (" + symbol + String.format(Locale.getDefault(), "%,.0f", -remaining) + " over)"
                            tvBudget.setTextColor(Color.parseColor("#EF4444")) // Warning Red
                        }
                    }
                    tvBudget.visibility = View.VISIBLE
                    
                    accentColor = when {
                        ratio < 0.6 -> defaultColor
                        ratio < 0.9 -> getThemeColor(com.google.android.material.R.attr.colorSecondary) // Amber
                        else -> Color.parseColor("#EF4444") // Warning Red
                    }
                    
                    val progressPct = (ratio * 100).toInt()
                    pb.progress = progressPct.coerceAtMost(100)
                } else {
                    tvBudget.visibility = View.GONE
                    val progressPct = if (sectionTotal > 0) (entry.amount / sectionTotal * 100).toInt() else 0
                    pb.progress = progressPct
                }
                
                row.setOnClickListener {
                    val fragment = CategoryTrendFragment().apply {
                        arguments = Bundle().apply {
                            putString("category", entry.name)
                        }
                    }
                    (activity as? MainActivity)?.showOverlayFragment(fragment)
                }

                pb.progressDrawable?.setColorFilter(accentColor, PorterDuff.Mode.SRC_IN)
                container.addView(row)
            }
        }

        if (variableList.isEmpty() && fixedList.isEmpty()) {
            tvBreakdownEmpty.visibility = View.VISIBLE
            sectionVariable.visibility = View.GONE
            sectionFixed.visibility = View.GONE
        } else {
            tvBreakdownEmpty.visibility = View.GONE
            sectionVariable.visibility = if (variableList.isNotEmpty()) View.VISIBLE else View.GONE
            sectionFixed.visibility = if (fixedList.isNotEmpty()) View.VISIBLE else View.GONE

            populateSection(llVariableCategories, variableList, variableTotal, false)
            populateSection(llFixedCategories, fixedList, fixedTotal, true)
        }
    }
}
