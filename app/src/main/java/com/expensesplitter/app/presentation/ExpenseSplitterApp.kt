package com.expensesplitter.app.presentation

import androidx.compose.runtime.*
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.expensesplitter.app.presentation.auth.AuthScreen
import com.expensesplitter.app.presentation.dashboard.DashboardScreen
import com.expensesplitter.app.presentation.expense.AddExpenseScreen
import com.expensesplitter.app.presentation.group.CreateGroupScreen
import com.expensesplitter.app.presentation.group.GroupListScreen

@Composable
fun ExpenseSplitterApp() {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = "auth"
    ) {
        composable("auth") {
            AuthScreen(
                onAuthSuccess = {
                    navController.navigate("dashboard") {
                        popUpTo("auth") { inclusive = true }
                    }
                }
            )
        }
        
        composable("dashboard") {
            DashboardScreen(
                onNavigateToAddExpense = {
                    navController.navigate("addExpense")
                },
                onNavigateToCreateGroup = {
                    navController.navigate("createGroup")
                },
                onNavigateToGroups = {
                    navController.navigate("groupList")
                },
                onNavigateToExpenseDetail = { expenseId ->
                    navController.navigate("addExpense?expenseId=$expenseId")
                }
            )
        }
        
        composable(
            route = "addExpense?expenseId={expenseId}",
            arguments = listOf(
                navArgument("expenseId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val expenseId = backStackEntry.arguments?.getString("expenseId")
            AddExpenseScreen(
                expenseId = expenseId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onExpenseAdded = {
                    navController.popBackStack()
                }
            )
        }
        
        composable("createGroup") {
            CreateGroupScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onGroupCreated = {
                    navController.popBackStack()
                }
            )
        }
        
        composable("groupList") {
            GroupListScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToCreateGroup = {
                    navController.navigate("createGroup")
                },
                onGroupSelected = {
                    navController.popBackStack()
                }
            )
        }
    }
}
