package com.expensesplitter.app.presentation.group

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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
            
            // Member Email
            OutlinedTextField(
                value = uiState.memberEmail,
                onValueChange = { viewModel.onMemberEmailChange(it) },
                label = { Text("Add Member (Optional)") },
                placeholder = { Text("friend@gmail.com") },
                modifier = Modifier.fillMaxWidth(),
                isError = uiState.memberEmailError != null,
                supportingText = {
                    if (uiState.memberEmailError != null) {
                        Text(uiState.memberEmailError!!)
                    } else {
                        Text("You can add more members later")
                    }
                },
                enabled = !uiState.isLoading,
                singleLine = true
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
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
