package com.expensesplitter.app.presentation.group

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGroupScreen(
    onNavigateBack: () -> Unit,
    onGroupCreated: () -> Unit,
    viewModel: CreateGroupViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Navigate back on success
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onGroupCreated()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Group") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Create a new expense group to start tracking and splitting expenses with friends or family.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // Group Name
            OutlinedTextField(
                value = uiState.groupName,
                onValueChange = { viewModel.onGroupNameChange(it) },
                label = { Text("Group Name *") },
                placeholder = { Text("e.g., Home Expenses, Trip to Bali") },
                modifier = Modifier.fillMaxWidth(),
                isError = uiState.groupNameError != null,
                supportingText = uiState.groupNameError?.let { { Text(it) } },
                enabled = !uiState.isLoading,
                singleLine = true
            )
            
            // Description
            OutlinedTextField(
                value = uiState.description,
                onValueChange = { viewModel.onDescriptionChange(it) },
                label = { Text("Description (Optional)") },
                placeholder = { Text("What is this group for?") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading,
                minLines = 2,
                maxLines = 4
            )
            
            // Members Section
            Text(
                text = "Members (Max 5 additional members)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
            
            Text(
                text = "You will be added as the group creator automatically",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // Member input fields
            uiState.members.forEachIndexed { index, member ->
                MemberInputCard(
                    index = index,
                    member = member,
                    error = uiState.memberErrors[index],
                    onEmailChange = { viewModel.onMemberFieldChange(index, "email", it) },
                    onFirstNameChange = { viewModel.onMemberFieldChange(index, "firstName", it) },
                    onGoogleIdChange = { viewModel.onMemberFieldChange(index, "googleId", it) },
                    onRemove = { viewModel.removeMember(index) },
                    enabled = !uiState.isLoading
                )
            }
            
            // Add Member Button
            if (uiState.members.size < 5) {
                OutlinedButton(
                    onClick = { viewModel.addMember() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Member (${uiState.members.size}/5)")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Create Button
            Button(
                onClick = { viewModel.createGroup() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Creating...")
                } else {
                    Text("Create Group")
                }
            }
            
            // Error message
            uiState.error?.let { error ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { viewModel.clearError() }) {
                            Text("Dismiss")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MemberInputCard(
    index: Int,
    member: com.expensesplitter.app.data.model.MemberData,
    error: String?,
    onEmailChange: (String) -> Unit,
    onFirstNameChange: (String) -> Unit,
    onGoogleIdChange: (String) -> Unit,
    onRemove: () -> Unit,
    enabled: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Member ${index + 1}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                IconButton(
                    onClick = onRemove,
                    enabled = enabled
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Remove member",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            
            OutlinedTextField(
                value = member.email,
                onValueChange = onEmailChange,
                label = { Text("Email *") },
                placeholder = { Text("friend@gmail.com") },
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled,
                singleLine = true,
                isError = error != null
            )
            
            OutlinedTextField(
                value = member.firstName,
                onValueChange = onFirstNameChange,
                label = { Text("First Name *") },
                placeholder = { Text("John") },
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled,
                singleLine = true
            )
            
            OutlinedTextField(
                value = member.googleId,
                onValueChange = onGoogleIdChange,
                label = { Text("Google ID (Optional)") },
                placeholder = { Text("114775293737264093813") },
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled,
                singleLine = true,
                supportingText = { Text("Numeric Google user ID", style = MaterialTheme.typography.bodySmall) }
            )
            
            if (error != null) {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
