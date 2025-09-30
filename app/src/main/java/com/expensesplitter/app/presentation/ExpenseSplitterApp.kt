package com.expensesplitter.app.presentation

import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.expensesplitter.app.presentation.auth.AuthScreen
import com.expensesplitter.app.presentation.dashboard.DashboardScreen
import com.expensesplitter.app.presentation.expense.AddExpenseScreen
import com.expensesplitter.app.presentation.group.CreateGroupScreen

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
                onNavigateToExpenseDetail = { _ ->
                    // TODO: Navigate to expense detail
                }
            )
        }
        
        composable("addExpense") {
            AddExpenseScreen(
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
    }
}
