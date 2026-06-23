package com.tcssol.expensetracker.ui

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.tcssol.expensetracker.Model.ExpenseViewModel
import com.tcssol.expensetracker.Model.PersonExpViewModel
import com.tcssol.expensetracker.Model.SharedExpenseViewModel
import com.tcssol.expensetracker.R
import com.tcssol.expensetracker.Utils.DataBaseExporter
import com.tcssol.expensetracker.Utils.Wrapped
import com.tcssol.expensetracker.ui.tabs.AllEntriesTab
import com.tcssol.expensetracker.ui.tabs.CategoriesTab
import com.tcssol.expensetracker.ui.tabs.P2pTab
import com.tcssol.expensetracker.ui.tabs.SummaryTab
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.time.LocalDate

private val MonthList = arrayOf(
    "January", "February", "March", "April", "May", "June",
    "July", "August", "September", "October", "November", "December"
)

private val YearsList = (2015..2035).map { it.toString() }.toTypedArray()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    expenseViewModel: ExpenseViewModel,
    personExpViewModel: PersonExpViewModel,
    sharedExpenseViewModel: SharedExpenseViewModel
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedTab by remember { mutableStateOf(0) }
    var currentMonth by remember { mutableStateOf(LocalDate.now().monthValue) }
    var currentYear by remember { mutableStateOf(LocalDate.now().year) }
    
    val wrappedState by sharedExpenseViewModel.getObject().observeAsState(Wrapped(-12, -2024))

    var monthDropdownExpanded by remember { mutableStateOf(false) }
    var yearDropdownExpanded by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }

    // Sync views when filter wrapped state changes
    LaunchedEffect(wrappedState) {
        if (wrappedState.month > 0 && wrappedState.year > 0) {
            currentMonth = wrappedState.month
            currentYear = wrappedState.year
        }
    }

    // Budget Limit Check
    LaunchedEffect(wrappedState, currentMonth, currentYear) {
        val prefs = context.getSharedPreferences("com.tcs.expensetracker.stored_categories", Context.MODE_PRIVATE)
        val budget = prefs.getFloat("MONTHLY_BUDGET", 0f)
        if (budget > 0f) {
            val monStr = if (currentMonth < 10) "0$currentMonth" else currentMonth.toString()
            val yrStr = currentYear.toString()
            expenseViewModel.getMonthSpendAsync(monStr, yrStr) { totalSpend ->
                if (totalSpend >= budget) {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            message = "⚠️ Budget exceeded! Spent ₹${totalSpend.toInt()} of ₹${budget.toInt()}",
                            duration = SnackbarDuration.Long
                        )
                    }
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text("Register", style = MaterialTheme.typography.titleLarge)
                },
                actions = {
                    // Month Selector
                    Box {
                        TextButton(onClick = { monthDropdownExpanded = true }) {
                            Text(if (wrappedState.month > 0) MonthList[currentMonth - 1].take(3) else "All")
                        }
                        DropdownMenu(
                            expanded = monthDropdownExpanded,
                            onDismissRequest = { monthDropdownExpanded = false }
                        ) {
                            MonthList.forEachIndexed { idx, name ->
                                DropdownMenuItem(
                                    text = { Text(name) },
                                    onClick = {
                                        monthDropdownExpanded = false
                                        currentMonth = idx + 1
                                        sharedExpenseViewModel.setObject(Wrapped(currentMonth, currentYear))
                                    }
                                )
                            }
                        }
                    }

                    // Year Selector
                    Box {
                        TextButton(onClick = { yearDropdownExpanded = true }) {
                            Text(if (wrappedState.year > 0) currentYear.toString() else "All")
                        }
                        DropdownMenu(
                            expanded = yearDropdownExpanded,
                            onDismissRequest = { yearDropdownExpanded = false }
                        ) {
                            YearsList.forEach { name ->
                                DropdownMenuItem(
                                    text = { Text(name) },
                                    onClick = {
                                        yearDropdownExpanded = false
                                        currentYear = name.toInt()
                                        sharedExpenseViewModel.setObject(Wrapped(currentMonth, currentYear))
                                    }
                                )
                            }
                        }
                    }

                    // Overflow Menu
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Manage Categories") },
                                onClick = {
                                    menuExpanded = false
                                    navController.navigate("edit_categories")
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Settings") },
                                onClick = {
                                    menuExpanded = false
                                    navController.navigate("settings")
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Export Data") },
                                onClick = {
                                    menuExpanded = false
                                    showExportDialog = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("About") },
                                onClick = {
                                    menuExpanded = false
                                    navController.navigate("about")
                                }
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(painterResource(id = R.drawable.baseline_category_24), contentDescription = "Categories") },
                    label = { Text("Categories") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(painterResource(id = R.drawable.baseline_people_24), contentDescription = "P2P") },
                    label = { Text("P2P") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(painterResource(id = R.drawable.baseline_pie_chart_24), contentDescription = "Summary") },
                    label = { Text("Summary") }
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(painterResource(id = R.drawable.baseline_view_list_24), contentDescription = "All") },
                    label = { Text("All") }
                )
            }
        },
        floatingActionButton = {
            if (selectedTab != 2) { // Summary screen has no FAB
                FloatingActionButton(onClick = {
                    val defaultType = when (selectedTab) {
                        0 -> 0 // Categories Spend
                        1 -> 2 // P2P Lend/borrow
                        else -> 0
                    }
                    navController.navigate("create_expense?type=$defaultType")
                }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Expense")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = wrappedState.month == -12 && wrappedState.year == -2024,
                    onClick = {
                        sharedExpenseViewModel.setObject(Wrapped(-12, -2024))
                    },
                    label = { Text("All Time") }
                )
                FilterChip(
                    selected = wrappedState.month == LocalDate.now().monthValue && wrappedState.year == LocalDate.now().year,
                    onClick = {
                        currentMonth = LocalDate.now().monthValue
                        currentYear = LocalDate.now().year
                        sharedExpenseViewModel.setObject(Wrapped(currentMonth, currentYear))
                    },
                    label = { Text("This Month") }
                )
                FilterChip(
                    selected = wrappedState.month == LocalDate.now().minusMonths(1).monthValue && wrappedState.year == LocalDate.now().minusMonths(1).year,
                    onClick = {
                        val prev = LocalDate.now().minusMonths(1)
                        currentMonth = prev.monthValue
                        currentYear = prev.year
                        sharedExpenseViewModel.setObject(Wrapped(currentMonth, currentYear))
                    },
                    label = { Text("Previous Month") }
                )
            }

            // Tab Content
            Box(modifier = Modifier.weight(1f)) {
                when (selectedTab) {
                    0 -> CategoriesTab(navController, expenseViewModel, sharedExpenseViewModel)
                    1 -> P2pTab(navController, personExpViewModel, sharedExpenseViewModel)
                    2 -> SummaryTab(expenseViewModel, sharedExpenseViewModel)
                    3 -> AllEntriesTab(navController, expenseViewModel, sharedExpenseViewModel)
                }
            }
        }
    }

    // Export Dialog Composable
    if (showExportDialog) {
        var selectedTable by remember { mutableStateOf(1) } // 1: Expenses, 2: PersonExp
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Export Database") },
            text = {
                Column {
                    Text("Select a table to export:")
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = selectedTable == 1,
                            onClick = { selectedTable = 1 }
                        )
                        Text("General Expenses Table", modifier = Modifier.padding(start = 8.dp))
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = selectedTable == 2,
                            onClick = { selectedTable = 2 }
                        )
                        Text("P2P Transactions Table", modifier = Modifier.padding(start = 8.dp))
                    }
                }
            },
            confirmButton = {
                Row {
                    TextButton(onClick = {
                        showExportDialog = false
                        coroutineScope.launch(Dispatchers.IO) {
                            if (selectedTable == 1) {
                                val list = expenseViewModel.repository.getAllExpensesList()
                                DataBaseExporter.exportCSVExpenses(context, null, list)
                            } else {
                                val list = personExpViewModel.repository.getAllExpensesListSync()
                                DataBaseExporter.exportCsvPersonExpenses(context, null, list)
                            }
                        }
                    }) {
                        Text("CSV")
                    }
                    TextButton(onClick = {
                        showExportDialog = false
                        coroutineScope.launch(Dispatchers.IO) {
                            if (selectedTable == 1) {
                                val list = expenseViewModel.repository.getAllExpensesList()
                                DataBaseExporter.exportTxtExpenses(context, null, list)
                            } else {
                                val list = personExpViewModel.repository.getAllExpensesListSync()
                                DataBaseExporter.exportTxtPersonExpenses(context, null, list)
                            }
                        }
                    }) {
                        Text("TXT")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// Extension to bridge LiveData to Compose State
@Composable
fun <T> LiveData<T>.observeAsState(initial: T): State<T> {
    val state = remember { mutableStateOf(initial) }
    DisposableEffect(this) {
        val observer = androidx.lifecycle.Observer<T> { value ->
            if (value != null) {
                state.value = value
            }
        }
        observeForever(observer)
        onDispose {
            removeObserver(observer)
        }
    }
    return state
}
