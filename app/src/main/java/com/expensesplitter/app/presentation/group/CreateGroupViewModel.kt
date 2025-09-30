package com.expensesplitter.app.presentation.group

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expensesplitter.app.data.local.entity.GroupEntity
import com.expensesplitter.app.data.preferences.UserPreferencesRepository
import com.expensesplitter.app.data.remote.GoogleSheetsService
import com.expensesplitter.app.data.repository.GroupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CreateGroupViewModel @Inject constructor(
    private val groupRepository: GroupRepository,
    private val sheetsService: GoogleSheetsService,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(CreateGroupUiState())
    val uiState: StateFlow<CreateGroupUiState> = _uiState.asStateFlow()
    
    fun onGroupNameChange(name: String) {
        _uiState.value = _uiState.value.copy(
            groupName = name,
            groupNameError = null
        )
    }
    
    fun onDescriptionChange(description: String) {
        _uiState.value = _uiState.value.copy(description = description)
    }
    
    fun onMemberEmailChange(email: String) {
        _uiState.value = _uiState.value.copy(
            memberEmail = email,
            memberEmailError = null
        )
    }
    
    fun createGroup() {
        val state = _uiState.value
        
        // Validate inputs
        if (state.groupName.isBlank()) {
            _uiState.value = state.copy(groupNameError = "Group name is required")
            return
        }
        
        if (state.memberEmail.isNotBlank() && !isValidEmail(state.memberEmail)) {
            _uiState.value = state.copy(memberEmailError = "Invalid email address")
            return
        }
        
        _uiState.value = state.copy(isLoading = true, error = null)
        
        viewModelScope.launch {
            try {
                Log.d("CreateGroupViewModel", "Creating group: ${state.groupName}")
                
                // Get current user ID
                val userId = preferencesRepository.userId.first() ?: "unknown"
                
                // Generate group ID
                val groupId = UUID.randomUUID().toString()
                
                // Create Google Sheet
                Log.d("CreateGroupViewModel", "Creating Google Sheet for group: ${state.groupName}")
                val sheetResult = sheetsService.createGroupSpreadsheet(state.groupName)
                
                if (sheetResult.isFailure) {
                    Log.e("CreateGroupViewModel", "Failed to create Google Sheet", sheetResult.exceptionOrNull())
                    _uiState.value = state.copy(
                        isLoading = false,
                        error = "Failed to create Google Sheet: ${sheetResult.exceptionOrNull()?.message}"
                    )
                    return@launch
                }
                
                val spreadsheet = sheetResult.getOrNull()!!
                val sheetId = spreadsheet.spreadsheetId
                val driveFileId = spreadsheet.spreadsheetId // Same as sheet ID
                val driveFolderId = "" // Can be set later if needed
                
                Log.d("CreateGroupViewModel", "Google Sheet created: $sheetId")
                
                // Create group entity
                val members = if (state.memberEmail.isNotBlank()) {
                    listOf(userId, state.memberEmail)
                } else {
                    listOf(userId)
                }
                
                val group = GroupEntity(
                    groupId = groupId,
                    groupName = state.groupName,
                    description = state.description.takeIf { it.isNotBlank() },
                    sheetId = sheetId,
                    driveFileId = driveFileId,
                    driveFolderId = driveFolderId,
                    createdBy = userId,
                    members = members,
                    isDefault = true // First group is default
                )
                
                // Save to database
                groupRepository.insertGroup(group)
                
                // Set as active group
                groupRepository.setActiveGroup(groupId)
                preferencesRepository.setActiveGroupId(groupId)
                
                Log.d("CreateGroupViewModel", "Group created successfully: $groupId")
                
                // TODO: Create Google Sheet in background
                // For now, just mark as success
                
                _uiState.value = state.copy(
                    isLoading = false,
                    isSuccess = true
                )
            } catch (e: Exception) {
                Log.e("CreateGroupViewModel", "Error creating group", e)
                _uiState.value = state.copy(
                    isLoading = false,
                    error = "Failed to create group: ${e.message}"
                )
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}

data class CreateGroupUiState(
    val groupName: String = "",
    val description: String = "",
    val memberEmail: String = "",
    val groupNameError: String? = null,
    val memberEmailError: String? = null,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)
