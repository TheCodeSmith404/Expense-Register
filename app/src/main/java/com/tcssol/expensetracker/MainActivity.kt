package com.tcssol.expensetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tcssol.expensetracker.Model.ExpenseViewModel
import com.tcssol.expensetracker.Model.PersonExpViewModel
import com.tcssol.expensetracker.Model.SharedExpenseViewModel
import com.tcssol.expensetracker.ui.AboutScreen
import com.tcssol.expensetracker.ui.CreateExpenseScreen
import com.tcssol.expensetracker.ui.DashboardScreen
import com.tcssol.expensetracker.ui.EditCategoriesScreen
import com.tcssol.expensetracker.ui.SettingsScreen
import com.tcssol.expensetracker.ui.theme.ExpenseTrackerTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize ViewModels
        val expenseViewModel = ViewModelProvider(this)[ExpenseViewModel::class.java]
        val personExpViewModel = ViewModelProvider(this)[PersonExpViewModel::class.java]
        val sharedExpenseViewModel = ViewModelProvider(this)[SharedExpenseViewModel::class.java]

        setContent {
            ExpenseTrackerTheme {
                val navController = rememberNavController()
                
                NavHost(navController = navController, startDestination = "dashboard") {
                    composable("dashboard") {
                        DashboardScreen(
                            navController = navController,
                            expenseViewModel = expenseViewModel,
                            personExpViewModel = personExpViewModel,
                            sharedExpenseViewModel = sharedExpenseViewModel
                        )
                    }
                    
                    composable(
                        route = "create_expense?type={type}&edit_mode={edit_mode}&expense_id={expense_id}&type_expense={type_expense}&category={category}&subcategory={subcategory}&medium={medium}&name={name}&number={number}",
                        arguments = listOf(
                            navArgument("type") { type = NavType.IntType; defaultValue = 0 },
                            navArgument("edit_mode") { type = NavType.BoolType; defaultValue = false },
                            navArgument("expense_id") { type = NavType.LongType; defaultValue = -1L },
                            navArgument("type_expense") { type = NavType.BoolType; defaultValue = false },
                            navArgument("category") { type = NavType.StringType; nullable = true; defaultValue = null },
                            navArgument("subcategory") { type = NavType.StringType; nullable = true; defaultValue = null },
                            navArgument("medium") { type = NavType.StringType; nullable = true; defaultValue = null },
                            navArgument("name") { type = NavType.StringType; nullable = true; defaultValue = null },
                            navArgument("number") { type = NavType.StringType; nullable = true; defaultValue = null }
                        )
                    ) { backStackEntry ->
                        val type = backStackEntry.arguments?.getInt("type") ?: 0
                        val editMode = backStackEntry.arguments?.getBoolean("edit_mode") ?: false
                        val expenseId = backStackEntry.arguments?.getLong("expense_id") ?: -1L
                        val typeExpense = backStackEntry.arguments?.getBoolean("type_expense") ?: false
                        val category = backStackEntry.arguments?.getString("category")
                        val subcategory = backStackEntry.arguments?.getString("subcategory")
                        val medium = backStackEntry.arguments?.getString("medium")
                        val name = backStackEntry.arguments?.getString("name")
                        val number = backStackEntry.arguments?.getString("number")

                        CreateExpenseScreen(
                            navController = navController,
                            expenseViewModel = expenseViewModel,
                            personExpViewModel = personExpViewModel,
                            sharedExpenseViewModel = sharedExpenseViewModel,
                            initialType = type,
                            editMode = editMode,
                            expenseId = expenseId,
                            initialTypeExpense = typeExpense,
                            initialCategory = category,
                            initialSubcategory = subcategory,
                            initialMedium = medium,
                            initialName = name,
                            initialNumber = number
                        )
                    }

                    composable("edit_categories") {
                        EditCategoriesScreen(navController = navController)
                    }

                    composable("settings") {
                        SettingsScreen(navController = navController)
                    }

                    composable("about") {
                        AboutScreen(navController = navController)
                    }
                }
            }
        }
    }
}
