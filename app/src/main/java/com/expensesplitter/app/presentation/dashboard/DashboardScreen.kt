package com.expensesplitter.app.presentation.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.expensesplitter.app.data.local.entity.ExpenseEntity
import com.expensesplitter.app.util.FormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToAddExpense: () -> Unit,
    onNavigateToCreateGroup: () -> Unit,
    onNavigateToGroups: () -> Unit,
    onNavigateToExpenseDetail: (String) -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Refresh data when screen becomes visible
    LaunchedEffect(Unit) {
        viewModel.refreshData()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = uiState.activeGroup?.groupName ?: "Expense Splitter",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToGroups) {
                        Icon(Icons.Default.Group, contentDescription = "Groups")
                    }
                    IconButton(onClick = { viewModel.refreshData() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        },
        floatingActionButton = {
            if (!uiState.noGroupsAvailable) {
                FloatingActionButton(onClick = onNavigateToAddExpense) {
                    Icon(Icons.Default.Add, contentDescription = "Add Expense")
                }
            }
        }
    ) { paddingValues ->
        when {
            uiState.loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            uiState.noGroupsAvailable -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Groups,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "No Groups Yet",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Create your first expense group to start tracking and splitting expenses with friends or family.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(
                            onClick = onNavigateToCreateGroup,
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Create Group")
                        }
                    }
                }
            }
            
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Month Selector
                    if (uiState.availableMonths.isNotEmpty()) {
                        item {
                            MonthSelector(
                                availableMonths = uiState.availableMonths,
                                selectedMonth = uiState.selectedMonth ?: uiState.availableMonths.firstOrNull() ?: "",
                                onMonthSelected = { viewModel.selectMonth(it) }
                            )
                        }
                    }
                    
                    // Summary Cards
                    item {
                        Text(
                            text = uiState.selectedMonth ?: "This Month",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            SummaryCard(
                                title = "Total Expenses",
                                value = FormatUtils.formatCurrency(
                                    uiState.totalExpenses, 
                                    uiState.currencyCode
                                ),
                                icon = Icons.Default.Receipt,
                                modifier = Modifier.weight(1f)
                            )
                            SummaryCard(
                                title = "Your Share",
                                value = FormatUtils.formatCurrency(
                                    uiState.yourShare, 
                                    uiState.currencyCode
                                ),
                                icon = Icons.Default.Person,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    
                    item {
                        BalanceCard(
                            balance = uiState.balance,
                            currencyCode = uiState.currencyCode
                        )
                    }
                    
                    // Recent Expenses
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Recent Expenses",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            if (uiState.recentExpenses.isNotEmpty()) {
                                TextButton(onClick = { /* TODO: Navigate to all expenses */ }) {
                                    Text("See All")
                                }
                            }
                        }
                    }
                    
                    if (uiState.recentExpenses.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Receipt,
                                            contentDescription = null,
                                            modifier = Modifier.size(48.dp),
                                            tint = MaterialTheme.colorScheme.outline
                                        )
                                        Text(
                                            text = "No expenses yet",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "Tap + to add your first expense",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        items(uiState.recentExpenses) { expense ->
                            ExpenseListItem(
                                expense = expense,
                                currencyCode = uiState.currencyCode,
                                currentUserEmail = uiState.currentUser?.email ?: "",
                                onClick = { onNavigateToExpenseDetail(expense.expenseId) }
                            )
                        }
                    }
                }
            }
        }
        
        // Error snackbar
        uiState.error?.let { error ->
            Snackbar(
                modifier = Modifier.padding(16.dp),
                action = {
                    TextButton(onClick = { viewModel.clearError() }) {
                        Text("Dismiss")
                    }
                }
            ) {
                Text(error)
            }
        }
    }
}

@Composable
fun SummaryCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun BalanceCard(
    balance: Double,
    currencyCode: String
) {
    val (text, color, icon) = when {
        balance > 0 -> Triple(
            "You are owed ${FormatUtils.formatCurrency(balance, currencyCode)}",
            MaterialTheme.colorScheme.tertiary,
            Icons.Default.TrendingUp
        )
        balance < 0 -> Triple(
            "You owe ${FormatUtils.formatCurrency(-balance, currencyCode)}",
            MaterialTheme.colorScheme.error,
            Icons.Default.TrendingDown
        )
        else -> Triple(
            "All settled up!",
            MaterialTheme.colorScheme.primary,
            Icons.Default.CheckCircle
        )
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(32.dp)
                )
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseListItem(
    expense: ExpenseEntity,
    currencyCode: String,
    currentUserEmail: String,
    onClick: () -> Unit
) {
    // Determine if user lent or borrowed
    // If paidBy == currentUser email, then "you lent", else "you borrowed"
    val isPaidByCurrentUser = expense.paidBy == currentUserEmail
    val halfAmount = expense.amount / 2.0
    
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Date column (Month and Day)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(50.dp)
            ) {
                Text(
                    text = FormatUtils.formatMonthShort(expense.date),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = FormatUtils.formatDay(expense.date),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // Category icon
            CategoryIcon(categoryId = expense.categoryId)
            
            // Description and paid by
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = expense.description,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (isPaidByCurrentUser) {
                        "You paid ${FormatUtils.formatCurrency(expense.amount, currencyCode)}"
                    } else {
                        "${expense.paidBy.substringBefore("@")} paid ${FormatUtils.formatCurrency(expense.amount, currencyCode)}"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Lent/Borrowed amount
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = if (isPaidByCurrentUser) "you lent" else "you borrowed",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isPaidByCurrentUser) 
                        androidx.compose.ui.graphics.Color(0xFF4CAF50) // Green
                    else 
                        MaterialTheme.colorScheme.error // Red
                )
                Text(
                    text = FormatUtils.formatCurrency(halfAmount, currencyCode),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isPaidByCurrentUser) 
                        androidx.compose.ui.graphics.Color(0xFF4CAF50) // Green
                    else 
                        MaterialTheme.colorScheme.error // Red
                )
            }
        }
    }
}

@Composable
fun CategoryIcon(categoryId: String) {
    // Simple category icon - enhanced with better categorization
    val (icon, backgroundColor) = when {
        categoryId.contains("food", ignoreCase = true) || 
        categoryId.contains("restaurant", ignoreCase = true) ||
        categoryId.contains("snack", ignoreCase = true) ||
        categoryId.contains("groceries", ignoreCase = true) -> 
            Icons.Default.ShoppingCart to androidx.compose.ui.graphics.Color(0xFFE8F5E9)
        
        categoryId.contains("transport", ignoreCase = true) ||
        categoryId.contains("bus", ignoreCase = true) ||
        categoryId.contains("travel", ignoreCase = true) ||
        categoryId.contains("car", ignoreCase = true) ->
            Icons.Default.DirectionsCar to androidx.compose.ui.graphics.Color(0xFFFCE4EC)
        
        categoryId.contains("entertainment", ignoreCase = true) ||
        categoryId.contains("movie", ignoreCase = true) ->
            Icons.Default.Movie to androidx.compose.ui.graphics.Color(0xFFFFF9C4)
        
        categoryId.contains("utilities", ignoreCase = true) ||
        categoryId.contains("toilet", ignoreCase = true) ||
        categoryId.contains("household", ignoreCase = true) ->
            Icons.Default.Home to androidx.compose.ui.graphics.Color(0xFFE1F5FE)
        
        categoryId.contains("shopping", ignoreCase = true) ->
            Icons.Default.ShoppingBag to androidx.compose.ui.graphics.Color(0xFFF3E5F5)
        
        else -> Icons.Default.Receipt to MaterialTheme.colorScheme.surfaceVariant
    }
    
    Card(
        modifier = Modifier.size(48.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthSelector(
    availableMonths: List<String>,
    selectedMonth: String,
    onMonthSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = selectedMonth,
                onValueChange = {},
                readOnly = true,
                label = { Text("Select Month") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                availableMonths.forEach { month ->
                    DropdownMenuItem(
                        text = { Text(month) },
                        onClick = {
                            onMonthSelected(month)
                            expanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }
    }
}
