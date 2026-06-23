package com.tcssol.expensetracker.ui

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import java.util.Currency
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val symbol = remember { Currency.getInstance(Locale.getDefault()).symbol }
    val prefs = remember { context.getSharedPreferences("com.tcs.expensetracker.stored_categories", Context.MODE_PRIVATE) }
    
    var budgetInput by remember {
        val savedBudget = prefs.getFloat("MONTHLY_BUDGET", 0f)
        mutableStateOf(if (savedBudget > 0f) savedBudget.toInt().toString() else "")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Budgeting",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            OutlinedTextField(
                value = budgetInput,
                onValueChange = { budgetInput = it },
                label = { Text("Monthly Budget Limit ($symbol)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Text(
                text = "Set to 0 or leave empty to clear the monthly budget warning notifications.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    val trimmed = budgetInput.trim()
                    var budget = 0f
                    if (trimmed.isNotEmpty()) {
                        try {
                            budget = trimmed.toFloat()
                        } catch (e: NumberFormatException) {
                            Toast.makeText(context, "Invalid amount", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                    }
                    
                    prefs.edit().putFloat("MONTHLY_BUDGET", budget).apply()
                    Toast.makeText(
                        context,
                        if (budget > 0f) "Budget set to $symbol${budget.toInt()}" else "Budget cleared",
                        Toast.LENGTH_SHORT
                    ).show()
                    navController.popBackStack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Save Settings", fontWeight = FontWeight.Bold)
            }
        }
    }
}
