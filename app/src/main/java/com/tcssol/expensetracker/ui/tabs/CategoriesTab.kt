package com.tcssol.expensetracker.ui.tabs

import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.tcssol.expensetracker.Model.ExpenseViewModel
import com.tcssol.expensetracker.Model.Expenses
import com.tcssol.expensetracker.Model.SharedExpenseViewModel
import com.tcssol.expensetracker.Utils.Wrapped
import com.tcssol.expensetracker.ui.observeAsState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.util.Currency
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CategoriesTab(
    navController: NavController,
    expenseViewModel: ExpenseViewModel,
    sharedExpenseViewModel: SharedExpenseViewModel
) {
    val context = LocalContext.current
    val symbol = remember { Currency.getInstance(Locale.getDefault()).symbol }
    val wrappedState by sharedExpenseViewModel.getObject().observeAsState(Wrapped(-12, -2024))

    // Grouped items state
    val groupedItems by remember(wrappedState) {
        if (wrappedState.month > 0 && wrappedState.year > 0) {
            val date = LocalDate.of(wrappedState.year, wrappedState.month, 1)
            expenseViewModel.getAllExpensesGroupedMonthly(date)
        } else {
            expenseViewModel.allExpensesGrouped
        }
    }.collectAsState(initial = emptyList())

    // Month strings for API calls
    val monthStr = remember(wrappedState) {
        if (wrappedState.month > 0) {
            if (wrappedState.month < 10) "0${wrappedState.month}" else wrappedState.month.toString()
        } else {
            val cur = LocalDate.now().monthValue
            if (cur < 10) "0$cur" else cur.toString()
        }
    }
    val yearStr = remember(wrappedState) {
        if (wrappedState.year > 0) wrappedState.year.toString() else LocalDate.now().year.toString()
    }

    // Net balance details
    val netBalance by expenseViewModel.getNetBalance(monthStr, yearStr).collectAsState(initial = 0.0)
    
    val frag3DataFlow = remember(wrappedState) {
        if (wrappedState.month > 0 && wrappedState.year > 0) {
            expenseViewModel.getFrag3DataFiltered(wrappedState.month, wrappedState.year)
        } else {
            expenseViewModel.frag3Data
        }
    }
    val frag3Data by frag3DataFlow.collectAsState(initial = listOf(0, 0, 0, 0))

    // Budget Limit State
    val prefs = remember { context.getSharedPreferences("com.tcs.expensetracker.stored_categories", Context.MODE_PRIVATE) }
    val budget = remember(wrappedState) { prefs.getFloat("MONTHLY_BUDGET", 0f) }

    // Popup subcategories state
    var activeCategoryForPopup by remember { mutableStateOf<Expenses?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            // Net Balance Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Net Balance",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    val balanceVal = netBalance ?: 0.0
                    val formattedBalance = "$symbol${String.format(Locale.getDefault(), "%.0f", Math.abs(balanceVal))}"
                    Text(
                        text = if (balanceVal < 0) "−$formattedBalance" else formattedBalance,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (balanceVal < 0) Color(0xFFE94560) else Color(0xFF4CAF50),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Earned", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = "$symbol${frag3Data.getOrNull(0) ?: 0}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4CAF50)
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Spent", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = "$symbol${frag3Data.getOrNull(1) ?: 0}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE94560)
                            )
                        }
                    }

                    // Budget Progress Bar
                    if (budget > 0) {
                        Spacer(modifier = Modifier.height(12.dp))
                        val spent = frag3Data.getOrNull(1) ?: 0
                        val progress = (spent / budget).coerceIn(0f, 1f)
                        
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth().height(6.dp),
                            color = if (progress >= 1f) Color(0xFFE94560) else MaterialTheme.colorScheme.secondary,
                            trackColor = MaterialTheme.colorScheme.surface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Budget: $symbol$spent of $symbol${budget.toInt()} used",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Section header
        item {
            Text(
                text = "Expenditure by Category",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Category list
        items(groupedItems) { item ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        onClick = { activeCategoryForPopup = item },
                        onLongClick = {
                            // Navigation Pre-fill
                            val typeParam = 1
                            val changeViewParam = if (item.category == "Money Received" || item.category == "Money Given") 1 else 0
                            val typeExpParam = item.type
                            val catParam = if (changeViewParam == 0) item.category else ""
                            navController.navigate("create_expense?type=$typeParam&type_expense=$typeExpParam&category=$catParam&name=${if (changeViewParam == 1) "Money" else ""}")
                        }
                    ),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = item.category,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (!item.subCategory.isNullOrEmpty()) {
                            Text(
                                text = item.subCategory,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Text(
                        text = "${if (item.type) "+" else "-"}$symbol${item.amount.toInt()}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (item.type) Color(0xFF4CAF50) else Color(0xFFE94560)
                    )
                }
            }
        }
    }

    // Subcategory details dialog popup
    activeCategoryForPopup?.let { categoryItem ->
        var subcategoriesList by remember { mutableStateOf<List<Expenses>>(emptyList()) }
        LaunchedEffect(categoryItem) {
            withContext(Dispatchers.IO) {
                subcategoriesList = expenseViewModel.getSubCatsF(
                    wrappedState.month,
                    wrappedState.year,
                    categoryItem.category,
                    categoryItem.type
                )
            }
        }

        Dialog(onDismissRequest = { activeCategoryForPopup = null }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = categoryItem.category,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (categoryItem.type) Color(0xFF4CAF50) else Color(0xFFE94560),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    if (subcategoriesList.isEmpty()) {
                        Text("Loading subcategories...", style = MaterialTheme.typography.bodyMedium)
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(subcategoriesList) { sub ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = sub.subCategory,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "$symbol${sub.amount.toInt()}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { activeCategoryForPopup = null },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Close")
                    }
                }
            }
        }
    }
}
