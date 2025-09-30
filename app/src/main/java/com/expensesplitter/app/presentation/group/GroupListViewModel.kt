package com.expensesplitter.app.presentation.group

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expensesplitter.app.data.local.entity.GroupEntity
import com.expensesplitter.app.data.preferences.UserPreferencesRepository
import com.expensesplitter.app.data.repository.GroupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GroupListViewModel @Inject constructor(
    private val groupRepository: GroupRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(GroupListUiState())
    val uiState: StateFlow<GroupListUiState> = _uiState.asStateFlow()
    
    init {
        loadGroups()
    }
    
    private fun loadGroups() {
        viewModelScope.launch {
            try {
                groupRepository.getAllGroups().collect { groups ->
                    val activeGroup = groupRepository.getActiveGroup()
                    _uiState.value = _uiState.value.copy(
                        groups = groups,
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
    
    fun setActiveGroup(groupId: String) {
        viewModelScope.launch {
            try {
                groupRepository.setActiveGroup(groupId)
                preferencesRepository.setActiveGroupId(groupId)
                Log.d("GroupListViewModel", "Set active group: $groupId")
            } catch (e: Exception) {
                Log.e("GroupListViewModel", "Error setting active group", e)
                _uiState.value = _uiState.value.copy(
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
    val groups: List<GroupEntity> = emptyList(),
    val activeGroupId: String? = null,
    val loading: Boolean = true,
    val error: String? = null
)
