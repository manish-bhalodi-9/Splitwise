package com.expensesplitter.app.presentation.auth

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expensesplitter.app.data.remote.GoogleApiClient
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val googleApiClient: GoogleApiClient
) : ViewModel() {
    
    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    init {
        checkExistingAuth()
    }
    
    private fun checkExistingAuth() {
        viewModelScope.launch {
            val account = googleApiClient.getLastSignedInAccount()
            if (account != null) {
                _authState.value = AuthState.Loading
                val result = googleApiClient.initializeServices(account)
                _authState.value = if (result.isSuccess) {
                    AuthState.Authenticated(account.email ?: "")
                } else {
                    AuthState.Unauthenticated
                }
            } else {
                _authState.value = AuthState.Unauthenticated
            }
        }
    }
    
    fun getSignInIntent(): Intent {
        // Get client ID from resources
        val clientId = "YOUR_GOOGLE_CLIENT_ID_HERE" // This should come from strings.xml
        return googleApiClient.getGoogleSignInClient(clientId).signInIntent
    }
    
    fun handleSignInResult(data: Intent?) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                val account = task.getResult(ApiException::class.java)
                
                if (account != null) {
                    val result = googleApiClient.initializeServices(account)
                    _authState.value = if (result.isSuccess) {
                        AuthState.Authenticated(account.email ?: "")
                    } else {
                        AuthState.Error("Failed to initialize services")
                    }
                } else {
                    _authState.value = AuthState.Error("Sign in failed")
                }
            } catch (e: ApiException) {
                _authState.value = AuthState.Error("Sign in failed: ${e.message}")
            } catch (e: Exception) {
                _authState.value = AuthState.Error("An error occurred: ${e.message}")
            }
        }
    }
    
    fun signOut() {
        viewModelScope.launch {
            googleApiClient.signOut()
            _authState.value = AuthState.Unauthenticated
        }
    }
}

sealed class AuthState {
    object Initial : AuthState()
    object Loading : AuthState()
    object Unauthenticated : AuthState()
    data class Authenticated(val email: String) : AuthState()
    data class Error(val message: String) : AuthState()
}
