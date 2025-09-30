package com.expensesplitter.app.presentation.group

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expensesplitter.app.data.local.entity.GroupEntity
import com.expensesplitter.app.data.model.MemberData
import com.expensesplitter.app.data.preferences.UserPreferencesRepository
import com.expensesplitter.app.data.remote.GoogleSheetsService
import com.expensesplitter.app.data.repository.GroupRepository
import com.expensesplitter.app.data.repository.UserRepository
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
    private val preferencesRepository: UserPreferencesRepository,
    private val userRepository: UserRepository
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
    
    fun onMemberFieldChange(index: Int, field: String, value: String) {
        val currentMembers = _uiState.value.members.toMutableList()
        if (index < currentMembers.size) {
            val member = currentMembers[index]
            currentMembers[index] = when (field) {
                "email" -> member.copy(email = value)
                "firstName" -> member.copy(firstName = value)
                "googleId" -> member.copy(googleId = value)
                else -> member
            }
            _uiState.value = _uiState.value.copy(
                members = currentMembers,
                memberErrors = _uiState.value.memberErrors.toMutableMap().apply {
                    remove(index)
                }
            )
        }
    }
    
    fun addMember() {
        val currentMembers = _uiState.value.members
        if (currentMembers.size < 5) {
            _uiState.value = _uiState.value.copy(
                members = currentMembers + MemberData()
            )
        }
    }
    
    fun removeMember(index: Int) {
        val currentMembers = _uiState.value.members.toMutableList()
        if (index < currentMembers.size) {
            currentMembers.removeAt(index)
            _uiState.value = _uiState.value.copy(
                members = currentMembers,
                memberErrors = _uiState.value.memberErrors.toMutableMap().apply {
                    remove(index)
                }
            )
        }
    }
    
    fun createGroup() {
        val state = _uiState.value
        
        // Validate inputs
        if (state.groupName.isBlank()) {
            _uiState.value = state.copy(groupNameError = "Group name is required")
            return
        }
        
        // Validate members
        val memberErrors = mutableMapOf<Int, String>()
        state.members.forEachIndexed { index, member ->
            if (member.email.isNotBlank() || member.firstName.isNotBlank() || member.googleId.isNotBlank()) {
                if (member.email.isBlank()) {
                    memberErrors[index] = "Email is required"
                } else if (!isValidEmail(member.email)) {
                    memberErrors[index] = "Invalid email address"
                } else if (member.firstName.isBlank()) {
                    memberErrors[index] = "First name is required"
                }
            }
        }
        
        if (memberErrors.isNotEmpty()) {
            _uiState.value = state.copy(memberErrors = memberErrors)
            return
        }
        
        _uiState.value = state.copy(isLoading = true, error = null)
        
        viewModelScope.launch {
            try {
                Log.d("CreateGroupViewModel", "Creating group: ${state.groupName}")
                
                // Get current user ID and email
                val userId = preferencesRepository.userId.first() ?: "unknown"
                val currentUser = userRepository.getCurrentUser()
                val currentUserEmail = currentUser?.email ?: ""
                
                // Generate group ID
                val groupId = UUID.randomUUID().toString()
                
                // Create Google Sheet
                Log.d("CreateGroupViewModel", "Creating Google Sheet for group: ${state.groupName}")
                
                // Get or create Expense Splitter folder
                val folderResult = sheetsService.getOrCreateExpenseSplitterFolder()
                if (folderResult.isFailure) {
                    Log.e("CreateGroupViewModel", "Failed to create folder", folderResult.exceptionOrNull())
                    _uiState.value = state.copy(
                        isLoading = false,
                        error = "Failed to create folder: ${folderResult.exceptionOrNull()?.message}"
                    )
                    return@launch
                }
                
                val folderId = folderResult.getOrNull()!!
                
                // Create members list with current user and additional members
                val currentUserMember = MemberData(
                    email = currentUserEmail,
                    firstName = currentUser?.displayName?.split(" ")?.firstOrNull() ?: "Me",
                    googleId = currentUser?.userId ?: ""
                )
                
                val validMembers = state.members.filter { it.isValid() }
                val allMembers = listOf(currentUserMember) + validMembers
                
                // Convert to storage format for Google Sheets
                val memberStrings = allMembers.map { it.toStorageString() }
                
                // Create spreadsheet with members
                val sheetResult = sheetsService.createGroupSpreadsheet(state.groupName, memberStrings)
                
                if (sheetResult.isFailure) {
                    Log.e("CreateGroupViewModel", "Failed to create Google Sheet", sheetResult.exceptionOrNull())
                    _uiState.value = state.copy(
                        isLoading = false,
                        error = "Failed to create Google Sheet: ${sheetResult.exceptionOrNull()?.message}"
                    )
                    return@launch
                }
                
                val spreadsheet = sheetResult.getOrNull()!!
                
                // Move spreadsheet to Expense Splitter folder
                val moveResult = sheetsService.moveSpreadsheetToFolder(spreadsheet.spreadsheetId, folderId)
                if (moveResult.isFailure) {
                    Log.e("CreateGroupViewModel", "Failed to move spreadsheet to folder", moveResult.exceptionOrNull())
                    // Don't fail the whole operation if move fails
                }
                
                val sheetId = spreadsheet.spreadsheetId
                val driveFileId = spreadsheet.spreadsheetId // Same as sheet ID
                
                Log.d("CreateGroupViewModel", "Google Sheet created: $sheetId, Folder: $folderId")
                
                // Create group entity (reuse members from above)
                val group = GroupEntity(
                    groupId = groupId,
                    groupName = state.groupName,
                    description = state.description.takeIf { it.isNotBlank() },
                    sheetId = sheetId,
                    driveFileId = driveFileId,
                    driveFolderId = folderId,
                    createdBy = userId,
                    members = memberStrings,
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
    val members: List<MemberData> = emptyList(),
    val groupNameError: String? = null,
    val memberErrors: Map<Int, String> = emptyMap(),
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)
