package com.tcssol.expensetracker.ui

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.tcssol.expensetracker.R
import com.tcssol.expensetracker.Utils.WorkwithJSONStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCategoriesScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("com.tcs.expensetracker.stored_categories", Context.MODE_PRIVATE) }
    
    var selectedTab by remember { mutableStateOf(0) } // 0: Categories, 1: Payment Modes

    // Load category JSON
    val defaultCatJson = context.resources.getString(R.string.category_subcategory)
    val catJsonStr = remember { mutableStateOf(prefs.getString("STORED_CATEGORIES", defaultCatJson) ?: defaultCatJson) }
    val categoriesParser = remember(catJsonStr.value) { WorkwithJSONStrings(catJsonStr.value) }
    
    val categoriesList = remember { mutableStateListOf<String>().apply { addAll(categoriesParser.getList("_elementlist")) } }
    var activeCategory by remember { mutableStateOf(categoriesList.firstOrNull() ?: "") }
    
    val subcategoriesList = remember(activeCategory, catJsonStr.value) {
        mutableStateListOf<String>().apply { addAll(categoriesParser.getList(activeCategory)) }
    }

    // Load medium JSON
    val defaultMedJson = context.resources.getString(R.string.transfer_medium)
    val medJsonStr = remember { mutableStateOf(prefs.getString("STORED_MEDIUM", defaultMedJson) ?: defaultMedJson) }
    val mediumsParser = remember(medJsonStr.value) { WorkwithJSONStrings(medJsonStr.value) }
    val mediumsList = remember { mutableStateListOf<String>().apply { addAll(mediumsParser.getList("_list_medium")) } }

    // Dialog state
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }
    
    var showAddSubcategoryDialog by remember { mutableStateOf(false) }
    var newSubcategoryName by remember { mutableStateOf("") }

    var showAddMediumDialog by remember { mutableStateOf(false) }
    var newMediumName by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Edit Categories") },
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
        ) {
            // Tab Row
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Categories") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Payment Modes") }
                )
            }

            Box(modifier = Modifier.weight(1f)) {
                if (selectedTab == 0) {
                    // --- Categories and Subcategories Tab ---
                    Row(modifier = Modifier.fillMaxSize()) {
                        // Left Pane: Categories List
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(8.dp)
                        ) {
                            Text(
                                "Categories",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                items(categoriesList) { cat ->
                                    val isSelected = cat == activeCategory
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { activeCategory = cat },
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                cat,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                            )
                                            IconButton(
                                                onClick = {
                                                    categoriesList.remove(cat)
                                                    categoriesParser.removeKey(cat)
                                                    categoriesParser.updateElementList("_elementlist", categoriesList.toList())
                                                    catJsonStr.value = categoriesParser.getJSONString()
                                                    if (activeCategory == cat) {
                                                        activeCategory = categoriesList.firstOrNull() ?: ""
                                                    }
                                                },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    newCategoryName = ""
                                    showAddCategoryDialog = true
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add")
                                Text("Category")
                            }
                        }

                        Divider(modifier = Modifier.fillMaxHeight().width(1.dp))

                        // Right Pane: Subcategories List for Selected Category
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(8.dp)
                        ) {
                            Text(
                                text = "Subcategories",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                items(subcategoriesList) { sub ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(sub, style = MaterialTheme.typography.bodyMedium)
                                            IconButton(
                                                onClick = {
                                                    subcategoriesList.remove(sub)
                                                    categoriesParser.updateElementList(activeCategory, subcategoriesList.toList())
                                                    catJsonStr.value = categoriesParser.getJSONString()
                                                },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    if (activeCategory.isEmpty()) return@Button
                                    newSubcategoryName = ""
                                    showAddSubcategoryDialog = true
                                },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = activeCategory.isNotEmpty(),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add")
                                Text("Subcategory")
                            }
                        }
                    }

                } else {
                    // --- Payment Modes Tab ---
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Text(
                            "Payment Modes",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(mediumsList) { med ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(med, style = MaterialTheme.typography.titleMedium)
                                        IconButton(onClick = {
                                            mediumsList.remove(med)
                                            mediumsParser.updateElementList("_list_medium", mediumsList.toList())
                                            medJsonStr.value = mediumsParser.getJSONString()
                                        }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                newMediumName = ""
                                showAddMediumDialog = true
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add")
                            Text("Add Payment Mode")
                        }
                    }
                }
            }

            // Fixed Save Button at the Bottom
            Button(
                onClick = {
                    prefs.edit().apply {
                        putString("STORED_CATEGORIES", catJsonStr.value)
                        putString("STORED_MEDIUM", medJsonStr.value)
                        apply()
                    }
                    navController.popBackStack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(50.dp)
            ) {
                Icon(Icons.Default.Done, contentDescription = "Save")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Changes", fontWeight = FontWeight.Bold)
            }
        }
    }

    // Dialogs
    if (showAddCategoryDialog) {
        AlertDialog(
            onDismissRequest = { showAddCategoryDialog = false },
            title = { Text("Add Category") },
            text = {
                OutlinedTextField(
                    value = newCategoryName,
                    onValueChange = { newCategoryName = it },
                    label = { Text("Category Name") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val name = newCategoryName.trim()
                    if (name.isNotEmpty() && !categoriesList.contains(name)) {
                        categoriesList.add(name)
                        categoriesParser.updateElementList("_elementlist", categoriesList.toList())
                        categoriesParser.updateElementList(name, listOf("General"))
                        catJsonStr.value = categoriesParser.getJSONString()
                        activeCategory = name
                    }
                    showAddCategoryDialog = false
                }) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCategoryDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showAddSubcategoryDialog) {
        AlertDialog(
            onDismissRequest = { showAddSubcategoryDialog = false },
            title = { Text("Add Subcategory") },
            text = {
                OutlinedTextField(
                    value = newSubcategoryName,
                    onValueChange = { newSubcategoryName = it },
                    label = { Text("Subcategory Name") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val name = newSubcategoryName.trim()
                    if (name.isNotEmpty() && !subcategoriesList.contains(name)) {
                        subcategoriesList.add(name)
                        categoriesParser.updateElementList(activeCategory, subcategoriesList.toList())
                        catJsonStr.value = categoriesParser.getJSONString()
                    }
                    showAddSubcategoryDialog = false
                }) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddSubcategoryDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showAddMediumDialog) {
        AlertDialog(
            onDismissRequest = { showAddMediumDialog = false },
            title = { Text("Add Payment Mode") },
            text = {
                OutlinedTextField(
                    value = newMediumName,
                    onValueChange = { newMediumName = it },
                    label = { Text("Mode Name") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val name = newMediumName.trim()
                    if (name.isNotEmpty() && !mediumsList.contains(name)) {
                        mediumsList.add(name)
                        mediumsParser.updateElementList("_list_medium", mediumsList.toList())
                        medJsonStr.value = mediumsParser.getJSONString()
                    }
                    showAddMediumDialog = false
                }) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddMediumDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
