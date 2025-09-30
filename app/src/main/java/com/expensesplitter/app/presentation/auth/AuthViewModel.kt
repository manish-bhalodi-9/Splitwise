package com.expensesplitter.app.presentation.auth

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expensesplitter.app.R
import com.expensesplitter.app.data.remote.GoogleApiClient
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
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
        // Get both client IDs from resources (generated from local.properties at build time)
        val androidClientId = context.getString(R.string.google_client_id)
        val webClientId = context.getString(R.string.google_web_client_id)
        android.util.Log.d("AuthViewModel", "Android Client ID: $androidClientId")
        android.util.Log.d("AuthViewModel", "Web Client ID: $webClientId")
        return googleApiClient.getGoogleSignInClient(androidClientId, webClientId).signInIntent
    }
    
    fun handleSignInResult(data: Intent?) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                val account = task.getResult(ApiException::class.java)
                
                android.util.Log.d("AuthViewModel", "Sign-in account: ${account?.email}")
                android.util.Log.d("AuthViewModel", "Has IdToken: ${account?.idToken != null}")
                
                if (account != null) {
                    val result = googleApiClient.initializeServices(account)
                    _authState.value = if (result.isSuccess) {
                        android.util.Log.d("AuthViewModel", "Services initialized successfully")
                        AuthState.Authenticated(account.email ?: "")
                    } else {
                        android.util.Log.e("AuthViewModel", "Failed to initialize services: ${result.exceptionOrNull()?.message}")
                        AuthState.Error("Failed to initialize services: ${result.exceptionOrNull()?.message}")
                    }
                } else {
                    android.util.Log.e("AuthViewModel", "Account is null")
                    _authState.value = AuthState.Error("Sign in failed: account is null")
                }
            } catch (e: ApiException) {
                android.util.Log.e("AuthViewModel", "ApiException: statusCode=${e.statusCode}, message=${e.message}", e)
                _authState.value = AuthState.Error("Sign in failed (${e.statusCode}): ${e.message}")
            } catch (e: Exception) {
                android.util.Log.e("AuthViewModel", "Exception during sign-in", e)
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
