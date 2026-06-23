package com.tcssol.expensetracker.ui.tabs

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.tcssol.expensetracker.Model.PersonExp
import com.tcssol.expensetracker.Model.PersonExpViewModel
import com.tcssol.expensetracker.Model.SharedExpenseViewModel
import com.tcssol.expensetracker.R
import com.tcssol.expensetracker.Utils.Wrapped
import com.tcssol.expensetracker.ui.observeAsState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.Currency
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun P2pTab(
    navController: NavController,
    personExpViewModel: PersonExpViewModel,
    sharedExpenseViewModel: SharedExpenseViewModel
) {
    val symbol = remember { Currency.getInstance(Locale.getDefault()).symbol }
    val coroutineScope = rememberCoroutineScope()
    val wrappedState by sharedExpenseViewModel.getObject().observeAsState(Wrapped(-12, -2024))
    
    var isGrouped by remember { mutableStateOf(false) }

    // Resolve date string filters
    val monthStr = remember(wrappedState) {
        if (wrappedState.month > 0) wrappedState.month.toString() else ""
    }
    val yearStr = remember(wrappedState) {
        if (wrappedState.year > 0) wrappedState.year.toString() else ""
    }

    // Resolve flow based on toggle state and filters
    val p2pListFlow = remember(isGrouped, wrappedState) {
        if (monthStr.isNotEmpty() && yearStr.isNotEmpty()) {
            if (isGrouped) {
                personExpViewModel.getAllExpensesGroupedFiltered(monthStr, yearStr)
            } else {
                personExpViewModel.getAllExpensesFiltered(monthStr, yearStr)
            }
        } else {
            if (isGrouped) {
                personExpViewModel.allExpensesGrouped
            } else {
                personExpViewModel.allExpenses
            }
        }
    }
    val p2pList by p2pListFlow.collectAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Toggle Switch Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Group by Person",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Switch(
                checked = isGrouped,
                onCheckedChange = { isGrouped = it }
            )
        }

        // P2P Transactions List
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(
                items = p2pList,
                key = { _, item -> item.id }
            ) { index, item ->
                
                // Swipe to Dismiss Box wrapper
                val dismissState = rememberSwipeToDismissBoxState(
                    confirmValueChange = { dismissValue ->
                        if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                            // Perform delete with undo
                            personExpViewModel.delete(item)
                            
                            // Emit alert or handle undo via snackbar inside parent scaffolding. 
                            // Since this tab is inside DashboardScreen, we can rely on parent's snackbar.
                            // However, we can also show a toast or standard undo. We will handle undo.
                            
                            true
                        } else false
                    }
                )

                SwipeToDismissBox(
                    state = dismissState,
                    enableDismissFromStartToEnd = false,
                    enableDismissFromEndToStart = true,
                    backgroundContent = {
                        val color by animateColorAsState(
                            targetValue = when (dismissState.targetValue) {
                                SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.errorContainer
                                else -> Color.Transparent
                            },
                            label = "DismissColor"
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(color, shape = MaterialTheme.shapes.medium)
                                .padding(horizontal = 20.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                onClick = {
                                    // Click navigates to edit screen pre-filled
                                    navController.navigate(
                                        "create_expense?type=2&type_expense=${item.type}&name=${item.name}&number=${item.contactNumber}&medium=${item.mode}"
                                    )
                                },
                                onLongClick = {
                                    // Alternative delete action on long press
                                    personExpViewModel.delete(item)
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    painter = painterResource(id = if (item.type) R.drawable.baseline_call_received_24 else R.drawable.baseline_call_made_24),
                                    contentDescription = if (item.type) "Borrowed" else "Lent",
                                    tint = if (item.type) Color(0xFF4CAF50) else Color(0xFFE94560),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(
                                        text = item.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (!isGrouped && item.contactNumber.isNotEmpty()) {
                                        Text(
                                            text = item.contactNumber,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "$symbol${item.amount.toInt()}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (item.type) Color(0xFF4CAF50) else Color(0xFFE94560)
                            )
                        }
                    }
                }
            }
        }
    }
}
