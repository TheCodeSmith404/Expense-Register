package com.tcssol.expensetracker.ui.tabs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tcssol.expensetracker.Model.ExpenseViewModel
import com.tcssol.expensetracker.Model.SharedExpenseViewModel
import com.tcssol.expensetracker.Utils.Wrapped
import com.tcssol.expensetracker.ui.components.DonutPieChart
import com.tcssol.expensetracker.ui.components.SimpleBarChart
import com.tcssol.expensetracker.ui.observeAsState
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Currency
import java.util.Locale

@Composable
fun SummaryTab(
    expenseViewModel: ExpenseViewModel,
    sharedExpenseViewModel: SharedExpenseViewModel
) {
    val symbol = remember { Currency.getInstance(Locale.getDefault()).symbol }
    val wrappedState by sharedExpenseViewModel.getObject().observeAsState(Wrapped(-12, -2024))
    
    // Resolve date filters
    val month = remember(wrappedState) {
        if (wrappedState.month > 0) wrappedState.month else Calendar.getInstance().get(Calendar.MONTH) + 1
    }
    val year = remember(wrappedState) {
        if (wrappedState.year > 0) wrappedState.year else Calendar.getInstance().get(Calendar.YEAR)
    }

    // 1. Lifetime Balance State
    val totalBalance by expenseViewModel.totalNetBalance.collectAsState(initial = 0.0)

    // 2. Summary stats flow
    val statsFlow = remember(wrappedState) {
        if (wrappedState.month > 0 && wrappedState.year > 0) {
            expenseViewModel.getFrag3DataFiltered(wrappedState.month, wrappedState.year)
        } else {
            expenseViewModel.frag3Data
        }
    }
    val stats by statsFlow.collectAsState(initial = listOf(0, 0, 0, 0))

    // 3. Payment Mode distribution
    val modeDist by expenseViewModel.modeDist.collectAsState(initial = emptyList())
    val paymentModeSlices = remember(modeDist) {
        modeDist.map { Pair(it.name, it.perentage) }
    }

    // 4. Category Spend distribution
    val categoryListFlow = remember(wrappedState) {
        if (wrappedState.month > 0 && wrappedState.year > 0) {
            val date = LocalDate.of(wrappedState.year, wrappedState.month, 1)
            expenseViewModel.getAllExpensesGroupedMonthly(date)
        } else {
            expenseViewModel.allExpensesGrouped
        }
    }
    val categoryList by categoryListFlow.collectAsState(initial = emptyList())
    val categorySlices = remember(categoryList) {
        categoryList.filter { !it.type && it.amount > 0 && it.category.isNotEmpty() }
            .map { Pair(it.category, it.amount) }
    }

    // 5. Daily sum bar chart data
    val dailySums by expenseViewModel.getDailySums(month, year).collectAsState(initial = emptyList())
    val barChartData = remember(dailySums) {
        val formatter = DateTimeFormatter.ofPattern("dd")
        dailySums.map { Pair(it.date.format(formatter), it.totalSpent) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Lifetime Net Balance Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Lifetime Active Balance",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                val balanceVal = totalBalance ?: 0.0
                val formattedBalance = "$symbol${String.format("%.2f", Math.abs(balanceVal))}"
                Text(
                    text = if (balanceVal < 0) "−$formattedBalance" else formattedBalance,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (balanceVal < 0) Color(0xFFE94560) else Color(0xFF4CAF50)
                )
            }
        }

        // Summary Stats Grid
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Transaction Summary",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Divider()

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Total Earned", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$symbol${stats.getOrNull(0) ?: 0}", fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Total Spent", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("-$symbol${stats.getOrNull(1) ?: 0}", fontWeight = FontWeight.Bold, color = Color(0xFFE94560))
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("P2P Received", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$symbol${stats.getOrNull(2) ?: 0}", fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("P2P Given", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("-$symbol${stats.getOrNull(3) ?: 0}", fontWeight = FontWeight.Bold, color = Color(0xFFE94560))
                    }
                }
            }
        }

        // Mode distribution pie chart
        Card(modifier = Modifier.fillMaxWidth()) {
            DonutPieChart(
                title = "Payment Medium Distribution",
                slices = paymentModeSlices
            )
        }

        // Category spend pie chart
        Card(modifier = Modifier.fillMaxWidth()) {
            DonutPieChart(
                title = "Category Spend Distribution",
                slices = categorySlices
            )
        }

        // Daily sums bar chart
        Card(modifier = Modifier.fillMaxWidth()) {
            SimpleBarChart(
                title = "Daily Spend Trend",
                bars = barChartData
            )
        }
    }
}
