package com.expensesplitter.app.presentation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.expensesplitter.app.presentation.auth.AuthScreen

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
            // Dashboard will be implemented
            DashboardPlaceholder()
        }
    }
}

@Composable
fun DashboardPlaceholder() {
    Scaffold { paddingValues ->
        Surface(
            modifier = Modifier.padding(paddingValues)
        ) {
            Text(
                text = "Dashboard Coming Soon",
                style = MaterialTheme.typography.headlineMedium
            )
        }
    }
}
