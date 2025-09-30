package com.expensesplitter.app.presentation.dashboard

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expensesplitter.app.data.local.entity.CategoryEntity
import com.expensesplitter.app.data.local.entity.ExpenseEntity
import com.expensesplitter.app.data.local.entity.GroupEntity
import com.expensesplitter.app.data.local.entity.UserEntity
import com.expensesplitter.app.data.preferences.UserPreferencesRepository
import com.expensesplitter.app.data.remote.GoogleSheetsService
import com.expensesplitter.app.data.repository.CategoryRepository
import com.expensesplitter.app.data.repository.ExpenseRepository
import com.expensesplitter.app.data.repository.GroupRepository
import com.expensesplitter.app.data.repository.UserRepository
import com.expensesplitter.app.util.CategoryDefaults
import com.expensesplitter.app.util.FormatUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val groupRepository: GroupRepository,
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository,
    private val preferencesRepository: UserPreferencesRepository,
    private val googleSheetsService: GoogleSheetsService
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()
    
    init {
        Log.d("DashboardViewModel", "Initializing DashboardViewModel")
        initializeApp()
        observeActiveGroup()
    }
    
    private fun observeActiveGroup() {
        viewModelScope.launch {
            preferencesRepository.activeGroupId.collect { groupId ->
                Log.d("DashboardViewModel", "Active group changed: $groupId")
                loadDashboardData()
                loadAvailableMonths()
            }
        }
    }
    
    private fun initializeApp() {
        viewModelScope.launch {
            try {
                Log.d("DashboardViewModel", "Checking first launch")
                preferencesRepository.isFirstLaunch.first().let { isFirstLaunch ->
                    if (isFirstLaunch) {
                        Log.d("DashboardViewModel", "First launch detected, initializing categories")
                        initializeCategories()
                        preferencesRepository.setFirstLaunchComplete()
                    }
                }
            } catch (e: Exception) {
                Log.e("DashboardViewModel", "Error initializing app", e)
                _uiState.update { it.copy(error = "Failed to initialize: ${e.message}") }
            }
        }
    }
    
    private suspend fun initializeCategories() {
        try {
            val defaultCategories = CategoryDefaults.getDefaultCategories()
            categoryRepository.insertCategories(defaultCategories)
            Log.d("DashboardViewModel", "Inserted ${defaultCategories.size} default categories")
        } catch (e: Exception) {
            Log.e("DashboardViewModel", "Error inserting categories", e)
        }
    }
    
    private fun loadDashboardData() {
        viewModelScope.launch {
            try {
                Log.d("DashboardViewModel", "Loading dashboard data")
                
                // Get current user
                val currentUser = userRepository.getCurrentUser()
                Log.d("DashboardViewModel", "Current user: ${currentUser?.email ?: "None"}")
                
                // Get active group
                val activeGroup = groupRepository.getActiveGroup()
                Log.d("DashboardViewModel", "Active group: ${activeGroup?.groupName ?: "None"}")
                
                if (activeGroup == null) {
                    _uiState.update { it.copy(
                        loading = false,
                        noGroupsAvailable = true,
                        currentUser = currentUser
                    )}
                    return@launch
                }
                
                // Get expenses for current month
                val now = System.currentTimeMillis()
                val monthStart = FormatUtils.getMonthStartTimestamp(now)
                val monthEnd = FormatUtils.getMonthEndTimestamp(now)
                
                // Collect expenses
                expenseRepository.getExpensesByDateRange(activeGroup.groupId, monthStart, monthEnd)
                    .combine(preferencesRepository.currencyCode) { expenses, currency ->
                        Pair(expenses, currency)
                    }
                    .collect { (expenses, currency) ->
                        Log.d("DashboardViewModel", "Loaded ${expenses.size} expenses for current month")
                        
                        val totalExpenses = expenses.sumOf { it.amount }
                        
                        // Calculate current user's share (simplified - assumes equal split)
                        val yourShare = expenses.sumOf { it.amount / 2.0 }
                        
                        // Calculate balance (amount you owe or are owed)
                        // Compare paidBy (email) with current user email
                        val yourExpenses = expenses.filter { it.paidBy == currentUser?.email }.sumOf { it.amount }
                        val balance = yourExpenses - yourShare
                        
                        _uiState.update { 
                            it.copy(
                                loading = false,
                                currentUser = currentUser,
                                activeGroup = activeGroup,
                                recentExpenses = expenses.take(10),
                                totalExpenses = totalExpenses,
                                yourShare = yourShare,
                                balance = balance,
                                currencyCode = currency,
                                noGroupsAvailable = false
                            )
                        }
                    }
            } catch (e: Exception) {
                Log.e("DashboardViewModel", "Error loading dashboard data", e)
                _uiState.update { 
                    it.copy(
                        loading = false, 
                        error = "Failed to load data: ${e.message}"
                    )
                }
            }
        }
    }
    
    fun refreshData() {
        Log.d("DashboardViewModel", "Refreshing dashboard data")
        _uiState.update { it.copy(loading = true, error = null) }
        loadDashboardData()
        loadAvailableMonths()
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    private fun loadAvailableMonths() {
        viewModelScope.launch {
            try {
                val activeGroup = groupRepository.getActiveGroup()
                if (activeGroup?.sheetId == null) {
                    Log.d("DashboardViewModel", "No active group or sheets file ID")
                    return@launch
                }
                
                // Check if spreadsheet exists
                val exists = googleSheetsService.spreadsheetExists(activeGroup.sheetId)
                if (!exists) {
                    Log.e("DashboardViewModel", "Spreadsheet not found: ${activeGroup.sheetId}")
                    _uiState.update { it.copy(
                        error = "Group file not found. Please create the group again."
                    )}
                    return@launch
                }
                
                val result = googleSheetsService.listSheetNames(activeGroup.sheetId)
                if (result.isSuccess) {
                    val sheetNames = result.getOrNull() ?: emptyList()
                    Log.d("DashboardViewModel", "Available months: $sheetNames")
                    
                    // Get current month in format "MMMM-yyyy"
                    val currentMonth = SimpleDateFormat("MMMM-yyyy", Locale.getDefault()).format(Date())
                    
                    val selectedMonth = if (sheetNames.contains(currentMonth)) {
                        currentMonth
                    } else {
                        sheetNames.firstOrNull()
                    }
                    
                    _uiState.update { it.copy(
                        availableMonths = sheetNames,
                        selectedMonth = selectedMonth
                    )}
                    
                    // Load expenses for selected month if available
                    if (selectedMonth != null) {
                        loadExpensesForSelectedMonth()
                    }
                } else {
                    Log.e("DashboardViewModel", "Failed to list sheets", result.exceptionOrNull())
                    _uiState.update { it.copy(
                        availableMonths = emptyList(),
                        selectedMonth = null
                    )}
                }
            } catch (e: Exception) {
                Log.e("DashboardViewModel", "Error loading available months", e)
            }
        }
    }
    
    fun selectMonth(month: String) {
        Log.d("DashboardViewModel", "Selecting month: $month")
        _uiState.update { it.copy(selectedMonth = month, loading = true) }
        loadExpensesForSelectedMonth()
    }
    
    private fun loadExpensesForSelectedMonth() {
        viewModelScope.launch {
            try {
                val state = _uiState.value
                val activeGroup = state.activeGroup
                val selectedMonth = state.selectedMonth
                
                if (activeGroup == null || selectedMonth == null) {
                    Log.d("DashboardViewModel", "No active group or selected month")
                    _uiState.update { it.copy(loading = false) }
                    return@launch
                }
                
                Log.d("DashboardViewModel", "Loading expenses for month: $selectedMonth")
                
                // Read expenses from Google Sheets
                val result = googleSheetsService.readExpensesFromSheet(activeGroup.sheetId, selectedMonth)
                if (result.isSuccess) {
                    val rows = result.getOrNull() ?: emptyList()
                    Log.d("DashboardViewModel", "Loaded ${rows.size} expense rows from sheet")
                    
                    // Convert rows to expense entities (simplified - you may want to enhance this)
                    val expenses = rows.mapNotNull { row ->
                        try {
                            if (row.size >= 4) {
                                // Parse basic expense data from sheet
                                // Format: ExpenseID, Date, Description, Amount, ...
                                val expenseId = row.getOrNull(0)?.toString() ?: return@mapNotNull null
                                val description = row.getOrNull(2)?.toString() ?: ""
                                val amount = row.getOrNull(3)?.toString()?.toDoubleOrNull() ?: 0.0
                                
                                // Create a simplified expense entity for display
                                // Note: This is a simplified version - you may want to enhance this
                                ExpenseEntity(
                                    expenseId = expenseId,
                                    groupId = activeGroup.groupId,
                                    description = description,
                                    amount = amount,
                                    currency = state.currencyCode,
                                    categoryId = "",
                                    date = System.currentTimeMillis(),
                                    paidBy = "",
                                    splitType = com.expensesplitter.app.data.local.entity.SplitType.EQUAL,
                                    status = com.expensesplitter.app.data.local.entity.ExpenseStatus.ACTIVE,
                                    createdBy = "",
                                    createdAt = System.currentTimeMillis()
                                )
                            } else null
                        } catch (e: Exception) {
                            Log.e("DashboardViewModel", "Error parsing expense row", e)
                            null
                        }
                    }
                    
                    val totalExpenses = expenses.sumOf { it.amount }
                    val yourShare = expenses.sumOf { it.amount / 2.0 }
                    val currentUser = state.currentUser
                    // Compare paidBy (email) with current user email
                    val yourExpenses = expenses.filter { it.paidBy == currentUser?.email }.sumOf { it.amount }
                    val balance = yourExpenses - yourShare
                    
                    _uiState.update { it.copy(
                        loading = false,
                        recentExpenses = expenses.take(10),
                        totalExpenses = totalExpenses,
                        yourShare = yourShare,
                        balance = balance
                    )}
                } else {
                    Log.e("DashboardViewModel", "Failed to read expenses", result.exceptionOrNull())
                    _uiState.update { it.copy(
                        loading = false,
                        error = "Failed to load expenses from sheet: ${result.exceptionOrNull()?.message}"
                    )}
                }
            } catch (e: Exception) {
                Log.e("DashboardViewModel", "Error loading expenses for month", e)
                _uiState.update { it.copy(
                    loading = false,
                    error = "Failed to load expenses: ${e.message}"
                )}
            }
        }
    }
}

data class DashboardUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val currentUser: UserEntity? = null,
    val activeGroup: GroupEntity? = null,
    val recentExpenses: List<ExpenseEntity> = emptyList(),
    val totalExpenses: Double = 0.0,
    val yourShare: Double = 0.0,
    val balance: Double = 0.0, // Positive = you are owed, Negative = you owe
    val currencyCode: String = "INR",
    val noGroupsAvailable: Boolean = false,
    val availableMonths: List<String> = emptyList(),
    val selectedMonth: String? = null
)
