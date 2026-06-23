package com.tcssol.expensetracker.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.tcssol.expensetracker.Model.ExpenseViewModel
import com.tcssol.expensetracker.Model.Expenses
import com.tcssol.expensetracker.Model.PersonExp
import com.tcssol.expensetracker.Model.PersonExpViewModel
import com.tcssol.expensetracker.Model.SharedExpenseViewModel
import com.tcssol.expensetracker.R
import com.tcssol.expensetracker.Utils.WorkwithJSONStrings
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Currency
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateExpenseScreen(
    navController: NavController,
    expenseViewModel: ExpenseViewModel,
    personExpViewModel: PersonExpViewModel,
    sharedExpenseViewModel: SharedExpenseViewModel,
    initialType: Int = 0,
    editMode: Boolean = false,
    expenseId: Long = -1L,
    initialTypeExpense: Boolean = false,
    initialCategory: String? = null,
    initialSubcategory: String? = null,
    initialMedium: String? = null,
    initialName: String? = null,
    initialNumber: String? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val symbol = remember { Currency.getInstance(Locale.getDefault()).symbol }

    // State Variables
    var type by remember { mutableStateOf(initialType) } // 0: Spend, 1: Earned, 2: Lend/Borrow
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("General") }
    var selectedSubcategory by remember { mutableStateOf("General") }
    var selectedMedium by remember { mutableStateOf("Cash") }

    // Lend/Borrow specific state
    var isReceived by remember { mutableStateOf(initialTypeExpense) } // true: Borrow/Received, false: Lend/Given
    var name by remember { mutableStateOf(initialName ?: "") }
    var contactNumber by remember { mutableStateOf(initialNumber ?: "") }
    var hasDateReminder by remember { mutableStateOf(false) }
    var reminderDate by remember { mutableStateOf<LocalDate?>(null) }
    var prefillSms by remember { mutableStateOf(false) }
    var smsMessage by remember { mutableStateOf("") }

    // Dropdown expanded states
    var categoryExpanded by remember { mutableStateOf(false) }
    var subcategoryExpanded by remember { mutableStateOf(false) }
    var mediumExpanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    // SharedPreferences category parser
    val prefs = remember { context.getSharedPreferences("com.tcs.expensetracker.stored_categories", Context.MODE_PRIVATE) }
    
    val categoriesList = remember {
        val defaultJson = context.resources.getString(R.string.category_subcategory)
        val jsonStr = prefs.getString("STORED_CATEGORIES", defaultJson) ?: defaultJson
        val jsonParser = WorkwithJSONStrings(jsonStr)
        jsonParser.getList("_elementlist")
    }

    val subcategoriesMap = remember {
        val defaultJson = context.resources.getString(R.string.category_subcategory)
        val jsonStr = prefs.getString("STORED_CATEGORIES", defaultJson) ?: defaultJson
        val jsonParser = WorkwithJSONStrings(jsonStr)
        categoriesList.associateWith { jsonParser.getList(it) }
    }

    val mediumsList = remember {
        val defaultJson = context.resources.getString(R.string.transfer_medium)
        val jsonStr = prefs.getString("STORED_MEDIUM", defaultJson) ?: defaultJson
        val jsonParser = WorkwithJSONStrings(jsonStr)
        jsonParser.getList("_list_medium")
    }

    // Set dynamic subcategories when category changes
    val subcategoriesList = remember(selectedCategory) {
        subcategoriesMap[selectedCategory] ?: listOf("General")
    }

    // Initial setups and pre-fills
    LaunchedEffect(Unit) {
        if (initialCategory != null) selectedCategory = initialCategory
        if (initialSubcategory != null) selectedSubcategory = initialSubcategory
        if (initialMedium != null) selectedMedium = initialMedium
        
        // Auto-select first subcategory when category changes (compatibility helper)
        if (initialSubcategory == null && subcategoriesList.isNotEmpty()) {
            selectedSubcategory = subcategoriesList[0]
        }
    }

    // Load entry if editing
    LaunchedEffect(expenseId) {
        if (editMode && expenseId > 0L) {
            coroutineScope.launch {
                val expense = expenseViewModel.get(expenseId).firstOrNull()
                if (expense != null) {
                    amount = expense.amount.toString()
                    note = expense.note ?: ""
                    selectedCategory = expense.category
                    selectedSubcategory = expense.subCategory
                    selectedMedium = expense.mode
                    type = if (expense.type) 1 else 0
                }
            }
        }
    }

    // Automatically compose SMS template when details update
    LaunchedEffect(amount, contactNumber, isReceived, prefillSms) {
        if (prefillSms) {
            smsMessage = "Please Return $symbol$amount"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (editMode) "Edit Transaction" else "Add Transaction") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Transaction Type Radio group
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Spend", "Earned", "Lend/Borrow").forEachIndexed { index, label ->
                    val isSelected = type == index
                    FilterChip(
                        selected = isSelected,
                        onClick = { type = index },
                        label = { Text(label) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 2. Amount Field
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount ($symbol)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // 3. Note Field
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Note / Comment") },
                modifier = Modifier.fillMaxWidth()
            )

            if (type == 0 || type == 1) {
                // --- Spend / Earned Specific Fields ---
                
                // Category Selector
                Box(modifier = Modifier.fillMaxWidth()) {
                    ExposedDropdownMenuBox(
                        expanded = categoryExpanded,
                        onResult = { categoryExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedCategory,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Category") },
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = categoryExpanded,
                            onDismissRequest = { categoryExpanded = false }
                        ) {
                            categoriesList.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat) },
                                    onClick = {
                                        selectedCategory = cat
                                        categoryExpanded = false
                                        if (subcategoriesMap[cat]?.isNotEmpty() == true) {
                                            selectedSubcategory = subcategoriesMap[cat]!![0]
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                // Subcategory Selector
                Box(modifier = Modifier.fillMaxWidth()) {
                    ExposedDropdownMenuBox(
                        expanded = subcategoryExpanded,
                        onResult = { subcategoryExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedSubcategory,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Sub-category") },
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = subcategoryExpanded,
                            onDismissRequest = { subcategoryExpanded = false }
                        ) {
                            subcategoriesList.forEach { sub ->
                                DropdownMenuItem(
                                    text = { Text(sub) },
                                    onClick = {
                                        selectedSubcategory = sub
                                        subcategoryExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

            } else {
                // --- Lend / Borrow Specific Fields ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Transaction Flow:", fontWeight = FontWeight.Bold)
                    FilterChip(
                        selected = isReceived,
                        onClick = { isReceived = true },
                        label = { Text("Borrow / Received") }
                    )
                    FilterChip(
                        selected = !isReceived,
                        onClick = { isReceived = false },
                        label = { Text("Lend / Given") }
                    )
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Contact Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = contactNumber,
                    onValueChange = { contactNumber = it },
                    label = { Text("Mobile Number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )

                // Date Reminder Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Set Date Reminder", fontWeight = FontWeight.Bold)
                    Switch(
                        checked = hasDateReminder,
                        onCheckedChange = {
                            hasDateReminder = it
                            if (!it) {
                                reminderDate = null
                                prefillSms = false
                            }
                        }
                    )
                }

                if (hasDateReminder) {
                    Button(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.DateRange, contentDescription = "Date")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(reminderDate?.toString() ?: "Select Reminder Date")
                    }

                    // SMS Reminder Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Prefill SMS Reminder", fontWeight = FontWeight.Bold)
                        Switch(
                            checked = prefillSms,
                            onCheckedChange = { prefillSms = it }
                        )
                    }

                    if (prefillSms) {
                        OutlinedTextField(
                            value = smsMessage,
                            onValueChange = { smsMessage = it },
                            label = { Text("SMS Message Body") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Payment Mode Selector (Common to all)
            Box(modifier = Modifier.fillMaxWidth()) {
                ExposedDropdownMenuBox(
                    expanded = mediumExpanded,
                    onResult = { mediumExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedMedium,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Payment Mode") },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = mediumExpanded,
                        onDismissRequest = { mediumExpanded = false }
                    ) {
                        mediumsList.forEach { med ->
                            DropdownMenuItem(
                                text = { Text(med) },
                                onClick = {
                                    selectedMedium = med
                                    mediumExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Save Button
            Button(
                onClick = {
                    val amtVal = amount.trim().toDoubleOrNull()
                    if (amtVal == null || amtVal <= 0.0) {
                        // Show invalid error
                        return@Button
                    }

                    val noteText = note.trim().ifEmpty { null }

                    if (type == 0 || type == 1) {
                        // General Expense Insert/Update
                        val expense = Expenses(
                            dateCreated = LocalDate.now(),
                            category = selectedCategory,
                            subCategory = selectedSubcategory,
                            mode = selectedMedium,
                            amount = amtVal,
                            type = type == 1,
                            note = noteText
                        )
                        if (editMode && expenseId > 0L) {
                            expense.id = expenseId
                            expenseViewModel.update(expense)
                        } else {
                            expenseViewModel.insert(expense)
                        }
                        navController.popBackStack()

                    } else {
                        // Lend/Borrow Insert (type == 2)
                        if (name.trim().isEmpty()) return@Button
                        
                        val categoryStr = if (isReceived) "Money Received" else "Money Given"

                        val personExp = PersonExp(
                            dateCreated = LocalDate.now(),
                            type = isReceived,
                            name = name,
                            contactNumber = contactNumber,
                            mode = selectedMedium,
                            hasDate = hasDateReminder,
                            pendingDate = reminderDate,
                            amount = amtVal,
                            note = noteText
                        )

                        val generalExp = Expenses(
                            dateCreated = LocalDate.now(),
                            category = categoryStr,
                            subCategory = name, // Maps contact name to subcategory field
                            mode = selectedMedium,
                            amount = amtVal,
                            type = isReceived,
                            note = noteText
                        )

                        // Save entities
                        personExpViewModel.insert(personExp)
                        expenseViewModel.insert(generalExp)

                        // Launch SMS Share Intent directly if chosen (background SMS avoided)
                        if (hasDateReminder && prefillSms && contactNumber.isNotEmpty()) {
                            val smsUri = Uri.parse("smsto:$contactNumber")
                            val smsIntent = Intent(Intent.ACTION_SENDTO, smsUri).apply {
                                putExtra("sms_body", smsMessage)
                            }
                            context.startActivity(smsIntent)
                        }

                        navController.popBackStack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Save", fontWeight = FontWeight.Bold)
            }
        }
    }

    // Material 3 Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { mills ->
                        reminderDate = Instant.ofEpochMilli(mills)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

// Custom exposed drop down helper for M3
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExposedDropdownMenuBox(
    expanded: Boolean,
    onResult: (Boolean) -> Unit,
    content: @Composable () -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        content()
        Box(
            modifier = Modifier
                .matchParentSize()
                .combinedClickable(
                    onClick = { onResult(!expanded) }
                )
        )
    }
}
