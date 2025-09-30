package com.expensesplitter.app.presentation.group

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expensesplitter.app.data.local.entity.GroupEntity
import com.expensesplitter.app.data.preferences.UserPreferencesRepository
import com.expensesplitter.app.data.repository.GroupRepository
import com.expensesplitter.app.data.repository.ExpenseRepository
import com.expensesplitter.app.data.repository.UserRepository
import kotlinx.coroutines.flow.first
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GroupListViewModel @Inject constructor(
    private val groupRepository: GroupRepository,
    private val preferencesRepository: UserPreferencesRepository,
    private val expenseRepository: com.expensesplitter.app.data.repository.ExpenseRepository,
    private val userRepository: com.expensesplitter.app.data.repository.UserRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(GroupListUiState())
    val uiState: StateFlow<GroupListUiState> = _uiState.asStateFlow()
    
    init {
        loadGroups()
    }
    
    private fun loadGroups() {
        viewModelScope.launch {
            try {
                val currentUser = userRepository.getCurrentUser()
                val currentUserEmail = currentUser?.email ?: ""
                
                groupRepository.getAllGroups().collect { groups ->
                    val activeGroup = groupRepository.getActiveGroup()
                    
                    // Calculate balance for each group
                    val groupsWithBalance = groups.map { group ->
                        calculateGroupBalance(group, currentUserEmail)
                    }
                    
                    _uiState.value = _uiState.value.copy(
                        groups = groupsWithBalance,
                        activeGroupId = activeGroup?.groupId,
                        loading = false
                    )
                }
            } catch (e: Exception) {
                Log.e("GroupListViewModel", "Error loading groups", e)
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    error = "Failed to load groups: ${e.message}"
                )
            }
        }
    }
    
    private suspend fun calculateGroupBalance(group: GroupEntity, currentUserEmail: String): GroupWithBalance {
        return try {
            val expenses = expenseRepository.getExpensesByGroup(group.groupId).first()
            
            val totalExpenses = expenses.sumOf { it.amount }
            val yourShare = expenses.sumOf { it.amount / group.members.size.coerceAtLeast(2).toDouble() }
            val yourExpenses = expenses.filter { it.paidBy.contains(currentUserEmail) }.sumOf { it.amount }
            val balance = yourExpenses - yourShare
            
            GroupWithBalance(
                group = group,
                balance = balance,
                memberCount = group.members.size
            )
        } catch (e: Exception) {
            Log.e("GroupListViewModel", "Error calculating balance for group ${group.groupId}", e)
            GroupWithBalance(
                group = group,
                balance = 0.0,
                memberCount = group.members.size
            )
        }
    }
    
    fun setActiveGroup(groupId: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(switchingGroup = true)
                groupRepository.setActiveGroup(groupId)
                preferencesRepository.setActiveGroupId(groupId)
                Log.d("GroupListViewModel", "Set active group: $groupId")
                kotlinx.coroutines.delay(500)
                _uiState.value = _uiState.value.copy(switchingGroup = false)
            } catch (e: Exception) {
                Log.e("GroupListViewModel", "Error setting active group", e)
                _uiState.value = _uiState.value.copy(
                    switchingGroup = false,
                    error = "Failed to set active group: ${e.message}"
                )
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class GroupListUiState(
    val groups: List<GroupWithBalance> = emptyList(),
    val activeGroupId: String? = null,
    val loading: Boolean = true,
    val switchingGroup: Boolean = false,
    val error: String? = null
)

data class GroupWithBalance(
    val group: GroupEntity,
    val balance: Double,
    val memberCount: Int
)
