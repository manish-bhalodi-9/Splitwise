# Google Sign-In Fix - Detailed Changes

## Issues Fixed in This Update

### 1. ❌ **Root Cause: Wrong OAuth Configuration**

**Problem:**
```kotlin
// OLD - WRONG: Used requestServerAuthCode with Android Client ID
.requestServerAuthCode(clientId)
```

**Why it failed:**
- `.requestServerAuthCode()` is for **server-side authentication** and requires a **Web Client ID**
- Your Client ID is an **Android Client ID** (ends with `.apps.googleusercontent.com`)
- Android Client IDs work with `.requestIdToken()`, not `.requestServerAuthCode()`

**Fix:**
```kotlin
// NEW - CORRECT: Use requestIdToken with Android Client ID
.requestIdToken(clientId)
```

---

### 2. ✅ **Added Comprehensive Error Logging**

**What was added:**
```kotlin
android.util.Log.d("AuthViewModel", "Client ID: $clientId")
android.util.Log.d("AuthViewModel", "Sign-in account: ${account?.email}")
android.util.Log.d("AuthViewModel", "Has IdToken: ${account?.idToken != null}")
android.util.Log.e("AuthViewModel", "ApiException: statusCode=${e.statusCode}")
```

**Why it helps:**
- Shows the actual Client ID being used
- Reveals the error status code (e.g., `12501` = cancelled, `10` = developer error)
- Tracks the sign-in flow step-by-step

---

### 3. 📦 **Fixed 16 KB Page Size Warning**

**Problem:**
```
APK is not compatible with 16 KB devices
lib/arm64-v8a/libsqlcipher.so not aligned at 16 KB boundaries
```

**Fix:**
```kotlin
packaging {
    jniLibs {
        useLegacyPackaging = false  // Enables proper alignment
    }
}
```

**Why it matters:**
- Starting November 1st, 2025, Google Play requires 16 KB page size support for Android 15+
- This ensures native libraries are properly aligned

---

## Files Modified

### 1. `GoogleApiClient.kt`
**Changed:**
```diff
- .requestServerAuthCode(clientId)
+ .requestIdToken(clientId)
```

**Impact:** Now uses the correct OAuth flow for Android Client ID

---

### 2. `AuthViewModel.kt`
**Changed:**
- Added `@ApplicationContext` context injection
- Added logging for Client ID
- Added detailed exception logging with status codes
- Added service initialization error logging

**Impact:** Better visibility into what's happening during sign-in

---

### 3. `app/build.gradle.kts`
**Changed:**
```kotlin
packaging {
    jniLibs {
        useLegacyPackaging = false
    }
}
```

**Impact:** APK is now compatible with 16 KB page size devices

---

## Common Google Sign-In Error Codes

When you test, the logs will show error codes if sign-in fails:

| Status Code | Meaning | Solution |
|------------|---------|----------|
| `7` | Network error | Check internet connection |
| `10` | Developer error | Client ID mismatch or API not enabled |
| `12500` | Sign-in failed | Generic error, check APIs enabled |
| `12501` | User cancelled | User pressed back/cancelled |
| `12502` | Sign-in in progress | Wait for current sign-in to complete |

---

## Testing Instructions

### Step 1: Open the App
1. Launch "Expense Splitter" on your phone
2. You should see the sign-in screen

### Step 2: Monitor Logs (In PowerShell)
The logs are already being monitored. They will show:
```
AuthViewModel: Client ID: 375538907803-sqeij0p5612eas37867dhhsol3oi0rtv.apps.googleusercontent.com
```

### Step 3: Sign In
1. Tap "Sign in with Google"
2. Select your email address
3. Watch what happens

### Step 4: Check Results

**✅ If Successful:**
- You'll see: `AuthViewModel: Sign-in account: your.email@gmail.com`
- You'll see: `AuthViewModel: Has IdToken: true`
- You'll see: `AuthViewModel: Services initialized successfully`
- App navigates to dashboard

**❌ If Failed:**
- You'll see: `ApiException: statusCode=XX`
- Error message appears on screen
- SignInHubActivity closes immediately

---

## What Changed vs. Before

### Before (Not Working):
```kotlin
// Used server auth code (wrong for Android Client ID)
GoogleSignInOptions.Builder()
    .requestServerAuthCode(clientId)  // ❌ Wrong
```

**Result:** Sign-in popup appeared, then closed immediately with no error message

### After (Should Work):
```kotlin
// Uses ID token (correct for Android Client ID)
GoogleSignInOptions.Builder()
    .requestIdToken(clientId)  // ✅ Correct
```

**Expected Result:** Sign-in completes successfully, returns ID token, services initialize

---

## Verification Checklist

After testing, verify:

- [ ] Client ID in logs matches: `375538907803-sqeij0p5612eas37867dhhsol3oi0rtv.apps.googleusercontent.com`
- [ ] No `statusCode=10` error (developer error)
- [ ] No `statusCode=12501` (user cancelled)
- [ ] ID token is present in logs: `Has IdToken: true`
- [ ] Services initialized: `Services initialized successfully`
- [ ] No 16 KB page size warning when building

---

## If It Still Doesn't Work

### Check 1: Client ID Configuration
**In Google Cloud Console:**
1. Go to APIs & Services > Credentials
2. Find your Android Client ID
3. Verify package name: `com.expensesplitter.app.debug`
4. Verify SHA-1: `D0:91:1E:0E:9D:0F:18:CB:CD:EB:EE:1E:16:18:8C:52:D1:A8:47:C9`

### Check 2: APIs Enabled
**In Google Cloud Console:**
1. Go to APIs & Services > Enabled APIs
2. Verify these are enabled:
   - Google Sheets API ✓
   - Google Drive API ✓
   - Google People API ✓

### Check 3: OAuth Consent Screen
**In Google Cloud Console:**
1. Go to APIs & Services > OAuth consent screen
2. Verify your email is added as a test user
3. Verify scopes include:
   - `.../auth/spreadsheets`
   - `.../auth/drive.file`
   - `.../auth/userinfo.email`
   - `.../auth/userinfo.profile`

### Check 4: App Configuration
**In local.properties:**
```properties
google.client.id=375538907803-sqeij0p5612eas37867dhhsol3oi0rtv.apps.googleusercontent.com
```

---

## Expected Log Output (Success)

```
AuthViewModel: Client ID: 375538907803-sqeij0p5612eas37867dhhsol3oi0rtv.apps.googleusercontent.com
AuthViewModel: Sign-in account: your.email@gmail.com
AuthViewModel: Has IdToken: true
AuthViewModel: Services initialized successfully
```

---

## Expected Log Output (Failure)

```
AuthViewModel: Client ID: 375538907803-sqeij0p5612eas37867dhhsol3oi0rtv.apps.googleusercontent.com
AuthViewModel: ApiException: statusCode=10, message=10: 
AuthViewModel: Sign in failed (10): 10: 
```

**StatusCode 10 = Developer Error:** Usually means Client ID doesn't match or SHA-1 is wrong

---

## Summary

### What We Fixed:
1. ✅ Changed from `requestServerAuthCode` to `requestIdToken`
2. ✅ Added comprehensive logging to debug issues
3. ✅ Fixed 16 KB page size compatibility warning

### What You Should Do:
1. 🧪 **Test sign-in** - Tap the button and select your email
2. 👀 **Watch the logs** - Check for error codes
3. 📱 **Report results** - Share what you see (success or error code)

**The main fix is in place - test it now!** 🚀
