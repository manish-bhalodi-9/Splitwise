package com.expensesplitter.app.presentation.dashboard

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expensesplitter.app.data.local.entity.CategoryEntity
import com.expensesplitter.app.data.local.entity.ExpenseEntity
import com.expensesplitter.app.data.local.entity.GroupEntity
import com.expensesplitter.app.data.local.entity.UserEntity
import com.expensesplitter.app.data.preferences.UserPreferencesRepository
import com.expensesplitter.app.data.repository.CategoryRepository
import com.expensesplitter.app.data.repository.ExpenseRepository
import com.expensesplitter.app.data.repository.GroupRepository
import com.expensesplitter.app.data.repository.UserRepository
import com.expensesplitter.app.util.CategoryDefaults
import com.expensesplitter.app.util.FormatUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val groupRepository: GroupRepository,
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()
    
    init {
        Log.d("DashboardViewModel", "Initializing DashboardViewModel")
        initializeApp()
        loadDashboardData()
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
                        val yourExpenses = expenses.filter { it.paidBy == currentUser?.userId }.sumOf { it.amount }
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
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
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
    val noGroupsAvailable: Boolean = false
)
