package com.expensesplitter.app.data.remote

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleApiClient @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val scopes = listOf(
        SheetsScopes.SPREADSHEETS,
        DriveScopes.DRIVE_FILE,
        "https://www.googleapis.com/auth/userinfo.profile",
        "https://www.googleapis.com/auth/userinfo.email"
    )
    
    private var _credential: GoogleAccountCredential? = null
    private var _sheetsService: Sheets? = null
    private var _driveService: Drive? = null
    
    val credential: GoogleAccountCredential?
        get() = _credential
    
    val sheetsService: Sheets?
        get() = _sheetsService
    
    val driveService: Drive?
        get() = _driveService
    
    fun getGoogleSignInClient(androidClientId: String, webClientId: String): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)  // Use Web Client ID for ID token
            .requestServerAuthCode(webClientId)  // Use Web Client ID for server auth
            .requestEmail()
            .requestProfile()
            .requestScopes(
                com.google.android.gms.common.api.Scope(SheetsScopes.SPREADSHEETS),
                com.google.android.gms.common.api.Scope(DriveScopes.DRIVE_FILE)
            )
            .build()
        
        return GoogleSignIn.getClient(context, gso)
    }
    
    suspend fun initializeServices(account: GoogleSignInAccount) = withContext(Dispatchers.IO) {
        try {
            _credential = GoogleAccountCredential.usingOAuth2(
                context,
                scopes
            ).apply {
                selectedAccount = account.account
            }
            
            val transport = NetHttpTransport()
            val jsonFactory = GsonFactory.getDefaultInstance()
            
            _sheetsService = Sheets.Builder(transport, jsonFactory, _credential)
                .setApplicationName("Expense Splitter")
                .build()
            
            _driveService = Drive.Builder(transport, jsonFactory, _credential)
                .setApplicationName("Expense Splitter")
                .build()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun getLastSignedInAccount(): GoogleSignInAccount? {
        return GoogleSignIn.getLastSignedInAccount(context)
    }
    
    suspend fun signOut() = withContext(Dispatchers.IO) {
        try {
            _credential = null
            _sheetsService = null
            _driveService = null
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun isSignedIn(): Boolean {
        return getLastSignedInAccount() != null && _credential != null
    }
}
