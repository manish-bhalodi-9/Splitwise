package com.expensesplitter.app.presentation.expense

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expensesplitter.app.data.local.entity.CategoryEntity
import com.expensesplitter.app.data.local.entity.ExpenseEntity
import com.expensesplitter.app.data.local.entity.ExpenseSplitEntity
import com.expensesplitter.app.data.preferences.UserPreferencesRepository
import com.expensesplitter.app.data.remote.GoogleSheetsService
import com.expensesplitter.app.data.repository.CategoryRepository
import com.expensesplitter.app.data.repository.ExpenseRepository
import com.expensesplitter.app.data.repository.GroupRepository
import com.expensesplitter.app.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AddExpenseViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository,
    private val groupRepository: GroupRepository,
    private val userRepository: UserRepository,
    private val preferencesRepository: UserPreferencesRepository,
    private val sheetsService: GoogleSheetsService
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AddExpenseUiState())
    val uiState: StateFlow<AddExpenseUiState> = _uiState.asStateFlow()
    
    private var editingExpenseId: String? = null
    
    init {
        loadCategories()
        loadActiveGroup()
    }
    
    fun loadExpenseForEdit(expenseId: String) {
        viewModelScope.launch {
            try {
                editingExpenseId = expenseId
                val expense = expenseRepository.getExpenseById(expenseId).first()
                if (expense != null) {
                    val category = categoryRepository.getCategoryById(expense.categoryId)
                    _uiState.value = _uiState.value.copy(
                        description = expense.description,
                        amount = expense.amount.toString(),
                        selectedCategory = category,
                        date = expense.date,
                        paidByUser = expense.paidBy,
                        splitMethod = when (expense.splitType) {
                            com.expensesplitter.app.data.local.entity.SplitType.EQUAL -> SplitMethod.EQUAL
                            com.expensesplitter.app.data.local.entity.SplitType.EXACT_AMOUNTS -> SplitMethod.EXACT
                            com.expensesplitter.app.data.local.entity.SplitType.PERCENTAGES -> SplitMethod.PERCENTAGE
                            com.expensesplitter.app.data.local.entity.SplitType.SHARES -> SplitMethod.SHARES
                        },
                        isEditMode = true
                    )
                    Log.d("AddExpenseViewModel", "Loaded expense for editing: $expenseId")
                }
            } catch (e: Exception) {
                Log.e("AddExpenseViewModel", "Error loading expense", e)
                _uiState.value = _uiState.value.copy(
                    error = "Failed to load expense: ${e.message}"
                )
            }
        }
    }
    
    private fun loadCategories() {
        viewModelScope.launch {
            try {
                categoryRepository.getActiveCategories().collect { categories ->
                    _uiState.value = _uiState.value.copy(
                        categories = categories,
                        selectedCategory = categories.firstOrNull()
                    )
                }
            } catch (e: Exception) {
                Log.e("AddExpenseViewModel", "Error loading categories", e)
                _uiState.value = _uiState.value.copy(
                    error = "Failed to load categories: ${e.message}"
                )
            }
        }
    }
    
    private fun loadActiveGroup() {
        viewModelScope.launch {
            try {
                val group = groupRepository.getActiveGroup()
                val userId = preferencesRepository.userId.first()
                val currentUser = userRepository.getCurrentUser()
                val currentUserEmail = currentUser?.email ?: ""
                
                _uiState.value = _uiState.value.copy(
                    activeGroupId = group?.groupId,
                    currentUserId = userId,
                    currentUserEmail = currentUserEmail,
                    groupMembers = group?.members ?: emptyList(),
                    paidByUser = currentUserEmail // Set current user email as default
                )
                
                Log.d("AddExpenseViewModel", "Loaded group members: ${group?.members}")
                Log.d("AddExpenseViewModel", "Current user email: $currentUserEmail")
            } catch (e: Exception) {
                Log.e("AddExpenseViewModel", "Error loading active group", e)
            }
        }
    }
    
    fun onDescriptionChange(description: String) {
        _uiState.value = _uiState.value.copy(
            description = description,
            descriptionError = null
        )
    }
    
    fun onPaidByUserChange(user: String) {
        _uiState.value = _uiState.value.copy(paidByUser = user)
    }
    
    fun onAmountChange(amount: String) {
        // Only allow valid decimal numbers
        if (amount.isEmpty() || amount.matches(Regex("^\\d*\\.?\\d*$"))) {
            _uiState.value = _uiState.value.copy(
                amount = amount,
                amountError = null
            )
        }
    }
    
    fun onCategorySelect(category: CategoryEntity) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
    }
    
    fun onDateChange(timestamp: Long) {
        _uiState.value = _uiState.value.copy(date = timestamp)
    }
    
    fun onSplitMethodChange(method: SplitMethod) {
        _uiState.value = _uiState.value.copy(splitMethod = method)
    }
    
    fun addExpense() {
        val state = _uiState.value
        
        // Validate inputs
        if (state.description.isBlank()) {
            _uiState.value = state.copy(descriptionError = "Description is required")
            return
        }
        
        val amountValue = state.amount.toDoubleOrNull()
        if (amountValue == null || amountValue <= 0) {
            _uiState.value = state.copy(amountError = "Enter a valid amount")
            return
        }
        
        if (state.selectedCategory == null) {
            _uiState.value = state.copy(error = "Please select a category")
            return
        }
        
        if (state.activeGroupId == null) {
            _uiState.value = state.copy(error = "No active group selected")
            return
        }
        
        if (state.currentUserId == null) {
            _uiState.value = state.copy(error = "User not logged in")
            return
        }
        
        _uiState.value = state.copy(isLoading = true, error = null)
        
        viewModelScope.launch {
            try {
                if (state.isEditMode && editingExpenseId != null) {
                    // Update existing expense
                    updateExpense(editingExpenseId!!, state, amountValue)
                } else {
                    // Create new expense
                    createNewExpense(state, amountValue)
                }
            } catch (e: Exception) {
                Log.e("AddExpenseViewModel", "Error saving expense", e)
                _uiState.value = state.copy(
                    isLoading = false,
                    error = "Failed to save expense: ${e.message}"
                )
            }
        }
    }
    
    private suspend fun createNewExpense(state: AddExpenseUiState, amountValue: Double) {
        Log.d("AddExpenseViewModel", "Creating expense: ${state.description}, amount: $amountValue")
        
        val expenseId = UUID.randomUUID().toString()
        
        // Create expense entity
        val expense = ExpenseEntity(
            expenseId = expenseId,
            groupId = state.activeGroupId!!,
            description = state.description,
            amount = amountValue,
            categoryId = state.selectedCategory!!.categoryId,
            paidBy = state.paidByUser.ifBlank { state.currentUserEmail },
            date = state.date,
            splitType = when(state.splitMethod) {
                SplitMethod.EQUAL -> com.expensesplitter.app.data.local.entity.SplitType.EQUAL
                SplitMethod.EXACT -> com.expensesplitter.app.data.local.entity.SplitType.EXACT_AMOUNTS
                SplitMethod.PERCENTAGE -> com.expensesplitter.app.data.local.entity.SplitType.PERCENTAGES
                SplitMethod.SHARES -> com.expensesplitter.app.data.local.entity.SplitType.SHARES
            },
            createdBy = state.currentUserId!!,
            notes = null
        )
        
        // Create split entities (for now, simple equal split with 2 people)
        // TODO: Implement proper member selection and custom splits
        val splits = when (state.splitMethod) {
            SplitMethod.EQUAL -> {
                // Equal split between 2 members (simplified)
                listOf(
                    ExpenseSplitEntity(
                        splitId = 0L, // Auto-generated
                        expenseId = expenseId,
                        userId = state.currentUserId!!,
                        amount = amountValue / 2.0,
                        isPaid = true // The payer has paid their share
                    ),
                    ExpenseSplitEntity(
                        splitId = 0L, // Auto-generated
                        expenseId = expenseId,
                        userId = "other_user", // Placeholder for other member
                        amount = amountValue / 2.0,
                        isPaid = false
                    )
                )
            }
            SplitMethod.EXACT -> {
                // TODO: Implement exact amounts split
                listOf()
            }
            SplitMethod.PERCENTAGE -> {
                // TODO: Implement percentage split
                listOf()
            }
            SplitMethod.SHARES -> {
                // TODO: Implement shares split
                listOf()
            }
        }
        
        // Insert expense with splits
        expenseRepository.insertExpenseWithSplits(expense, splits)
        
        Log.d("AddExpenseViewModel", "Expense created successfully: $expenseId")
        
        // Sync to Google Sheets
        syncExpenseToSheets(expense, state)
        
        _uiState.value = state.copy(
            isLoading = false,
            isSuccess = true
        )
    }
    
    private suspend fun updateExpense(expenseId: String, state: AddExpenseUiState, amountValue: Double) {
        Log.d("AddExpenseViewModel", "Updating expense: $expenseId")
        
        // Get existing expense
        val existingExpense = expenseRepository.getExpenseById(expenseId).first()
            ?: throw Exception("Expense not found")
        
        // Create updated expense entity
        val updatedExpense = existingExpense.copy(
            description = state.description,
            amount = amountValue,
            categoryId = state.selectedCategory!!.categoryId,
            date = state.date,
            paidBy = state.paidByUser.ifBlank { state.currentUserEmail },
            splitType = when(state.splitMethod) {
                SplitMethod.EQUAL -> com.expensesplitter.app.data.local.entity.SplitType.EQUAL
                SplitMethod.EXACT -> com.expensesplitter.app.data.local.entity.SplitType.EXACT_AMOUNTS
                SplitMethod.PERCENTAGE -> com.expensesplitter.app.data.local.entity.SplitType.PERCENTAGES
                SplitMethod.SHARES -> com.expensesplitter.app.data.local.entity.SplitType.SHARES
            },
            lastEditedBy = state.currentUserId,
            lastEditedAt = System.currentTimeMillis()
        )
        
        // Update splits (simplified - recreate splits)
        val splits = when (state.splitMethod) {
            SplitMethod.EQUAL -> {
                listOf(
                    ExpenseSplitEntity(
                        splitId = 0L,
                        expenseId = expenseId,
                        userId = state.currentUserId!!,
                        amount = amountValue / 2.0,
                        isPaid = true
                    ),
                    ExpenseSplitEntity(
                        splitId = 0L,
                        expenseId = expenseId,
                        userId = "other_user",
                        amount = amountValue / 2.0,
                        isPaid = false
                    )
                )
            }
            else -> listOf()
        }
        
        // Update in database
        expenseRepository.updateExpense(updatedExpense)
        
        Log.d("AddExpenseViewModel", "Expense updated successfully")
        
        // Update in Google Sheets (not append)
        updateExpenseInSheets(updatedExpense, state)
        
        _uiState.value = state.copy(
            isLoading = false,
            isSuccess = true
        )
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    private suspend fun syncExpenseToSheets(expense: ExpenseEntity, state: AddExpenseUiState) {
        try {
            // Get group to get sheet ID
            val group = groupRepository.getGroupByIdSuspend(expense.groupId) ?: return
            
            // Format date for sheet name (e.g., "September-2025")
            val dateFormat = SimpleDateFormat("MMMM-yyyy", Locale.getDefault())
            val sheetName = dateFormat.format(Date(expense.date))
            
            Log.d("AddExpenseViewModel", "Syncing expense to sheet: $sheetName")
            
            // Get or create monthly sheet
            val sheetResult = sheetsService.getOrCreateMonthlySheet(group.sheetId, sheetName)
            if (sheetResult.isFailure) {
                Log.e("AddExpenseViewModel", "Failed to get/create sheet", sheetResult.exceptionOrNull())
                return
            }
            
            // Prepare expense data row
            val expenseData = prepareExpenseData(expense, state)
            
            // Append to sheet
            val appendResult = sheetsService.appendExpenseRow(group.sheetId, sheetName, expenseData)
            if (appendResult.isSuccess) {
                Log.d("AddExpenseViewModel", "Expense synced to Google Sheets successfully")
            } else {
                Log.e("AddExpenseViewModel", "Failed to sync expense", appendResult.exceptionOrNull())
            }
        } catch (e: Exception) {
            Log.e("AddExpenseViewModel", "Error syncing to sheets", e)
        }
    }
    
    private suspend fun updateExpenseInSheets(expense: ExpenseEntity, state: AddExpenseUiState) {
        try {
            // Get group to get sheet ID
            val group = groupRepository.getGroupByIdSuspend(expense.groupId) ?: return
            
            // Format date for sheet name (e.g., "September-2025")
            val dateFormat = SimpleDateFormat("MMMM-yyyy", Locale.getDefault())
            val sheetName = dateFormat.format(Date(expense.date))
            
            Log.d("AddExpenseViewModel", "Updating expense in sheet: $sheetName")
            
            // Find the row with this expense ID
            val row = sheetsService.findExpenseRow(group.sheetId, sheetName, expense.expenseId)
            if (row == null) {
                Log.e("AddExpenseViewModel", "Expense row not found in sheet, appending instead")
                // If not found, append as new row
                syncExpenseToSheets(expense, state)
                return
            }
            
            Log.d("AddExpenseViewModel", "Found expense at row $row, updating...")
            
            // Prepare expense data row
            val expenseData = prepareExpenseData(expense, state)
            
            // Update the row
            val updateResult = sheetsService.updateExpenseRow(group.sheetId, sheetName, row, expenseData)
            if (updateResult.isSuccess) {
                Log.d("AddExpenseViewModel", "Expense updated in Google Sheets successfully")
            } else {
                Log.e("AddExpenseViewModel", "Failed to update expense", updateResult.exceptionOrNull())
            }
        } catch (e: Exception) {
            Log.e("AddExpenseViewModel", "Error updating expense in sheets", e)
        }
    }
    
    private fun prepareExpenseData(expense: ExpenseEntity, state: AddExpenseUiState): List<Any> {
        return listOf(
            expense.expenseId,
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(expense.date)),
            expense.description,
            expense.amount,
            state.selectedCategory?.name ?: "",
            expense.paidBy,
            expense.splitType.name,
            expense.splitDetails ?: "",
            expense.createdBy,
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(expense.createdAt)),
            expense.lastEditedBy ?: "",
            expense.lastEditedAt?.let { SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(it)) } ?: "",
            expense.notes ?: "",
            expense.receiptUrls.joinToString(", "),
            expense.status.name,
            expense.settledDate?.let { SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(it)) } ?: "",
            expense.settlementNotes ?: ""
        )
    }
}

data class AddExpenseUiState(
    val description: String = "",
    val amount: String = "",
    val selectedCategory: CategoryEntity? = null,
    val categories: List<CategoryEntity> = emptyList(),
    val date: Long = System.currentTimeMillis(),
    val splitMethod: SplitMethod = SplitMethod.EQUAL,
    val paidByUser: String = "", // User email who paid
    val groupMembers: List<String> = emptyList(), // List of member emails
    val currentUserEmail: String = "", // Current user's email
    val descriptionError: String? = null,
    val amountError: String? = null,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val activeGroupId: String? = null,
    val currentUserId: String? = null,
    val isEditMode: Boolean = false
)

enum class SplitMethod {
    EQUAL,
    EXACT,
    PERCENTAGE,
    SHARES
}
