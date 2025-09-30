# Critical: Google Sign-In Not Working - Root Cause Analysis

## 🔴 Problem Summary

**Symptom:** When you select your Google account, the SignInHubActivity closes immediately and returns to your app WITHOUT completing sign-in.

**From Logs:**
```
SignInHubActivity appears → closes immediately
MainActivity becomes active again (no authentication)
NO AuthViewModel logs = callback never reached
```

## 🔍 Root Cause

The sign-in is failing **before it reaches your app code**. This happens at the Google Play Services level, which means:

1. **Google Cloud OAuth Client ID configuration issue**
2. **Package name or SHA-1 fingerprint mismatch**
3. **Missing required OAuth scopes or incorrect consent screen setup**

---

## ✅ Solution: Fix Google Cloud Console Configuration

### Step 1: Verify OAuth Client ID

**Go to:** [Google Cloud Console - Credentials](https://console.cloud.google.com/apis/credentials)

1. Find your Android Client ID
2. Click "Edit" (pencil icon)
3. **Verify these EXACT values:**

```
Name: Android client (auto created by Google Service)
Application type: Android
Package name: com.expensesplitter.app.debug  ← MUST match exactly
SHA-1 certificate fingerprint: D0:91:1E:0E:9D:0F:18:CB:CD:EB:EE:1E:16:18:8C:52:D1:A8:47:C9
```

### ⚠️ Common Mistakes:
- ❌ Package name is `com.expensesplitter.app` (missing `.debug`)
- ❌ SHA-1 is from release keystore instead of debug
- ❌ Extra spaces in SHA-1 fingerprint
- ❌ Using uppercase letters in package name

---

### Step 2: Check OAuth Consent Screen

**Go to:** [OAuth Consent Screen](https://console.cloud.google.com/apis/credentials/consent)

1. **User Type:** Must be "External" (or "Internal" if using Google Workspace)
2. **Test Users:** Add your email address that you're trying to sign in with
3. **Scopes:** Must include:
   - `.../auth/userinfo.email`
   - `.../auth/userinfo.profile`
   - `.../auth/spreadsheets`
   - `.../auth/drive.file`

### ⚠️ Critical:
- If your email is NOT in "Test users" list, sign-in will fail silently
- If app is in "Testing" mode, only test users can sign in
- If app is "Published", anyone can sign in

---

### Step 3: Verify APIs are Enabled

**Go to:** [APIs & Services - Library](https://console.cloud.google.com/apis/library)

**Must be ENABLED:**
- ✅ Google Sheets API
- ✅ Google Drive API  
- ✅ Google People API (optional but recommended)

**Click on each:**
1. If it says "MANAGE" → it's enabled ✅
2. If it says "ENABLE" → click it to enable

---

## 🔧 Quick Fix Checklist

Run through these in order:

### 1. Get Your Current Debug Keystore SHA-1
```powershell
& "C:\Program Files\Java\jdk-21\bin\keytool.exe" -list -v -keystore "$env:USERPROFILE\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android
```

**Expected Output:**
```
SHA1: D0:91:1E:0E:9D:0F:18:CB:CD:EB:EE:1E:16:18:8C:52:D1:A8:47:C9
```

### 2. Get Your App's Package Name
```powershell
& "C:\Users\manis\AppData\Local\Android\Sdk\platform-tools\adb.exe" shell "dumpsys package com.expensesplitter.app.debug | grep -A 1 'Package'"
```

**Expected:** `com.expensesplitter.app.debug`

### 3. Verify Google Cloud Console
- Package name: `com.expensesplitter.app.debug`
- SHA-1: Matches the one from Step 1
- Your email in Test Users list

---

## 🎯 Most Likely Issue: Wrong Client ID Type

### Problem:
You might be using the **wrong type** of Client ID.

### Check This:

**Go to Credentials page** and look at your Client IDs:

You should see **TWO** different Client IDs:

1. **Web application client** (for backend/web)
   - Format: `XXXXX.apps.googleusercontent.com`
   - Used with `.requestServerAuthCode()`

2. **Android client** (for your app)  
   - Format: `XXXXX.apps.googleusercontent.com`
   - Used with `.requestIdToken()`
   - Has package name and SHA-1

### ✅ Make Sure:
- You're using the **Android client ID** (the one with package name + SHA-1)
- NOT the Web client ID
- NOT the auto-created Google Services JSON client ID

---

## 📝 Correct Configuration

### In Google Cloud Console:

**Android OAuth Client:**
```
Name: Android client 1 (or similar)
Client ID: 375538907803-sqeij0p5612eas37867dhhsol3oi0rtv.apps.googleusercontent.com
Type: Android
Package name: com.expensesplitter.app.debug
SHA-1: D0:91:1E:0E:9D:0F:18:CB:CD:EB:EE:1E:16:18:8C:52:D1:A8:47:C9
```

### In local.properties:
```properties
google.client.id=375538907803-sqeij0p5612eas37867dhhsol3oi0rtv.apps.googleusercontent.com
```

---

## 🧪 Testing Steps

### After Fixing Configuration:

1. **Wait 5-10 minutes** for Google's servers to propagate changes
2. **Clear Google Play Services cache:**
   ```
   Settings → Apps → Google Play Services → Storage → Clear Cache
   ```
3. **Uninstall your app completely**
4. **Rebuild and reinstall:**
   ```powershell
   .\gradlew.bat clean assembleDebug
   adb install -r app\build\outputs\apk\debug\app-debug.apk
   ```
5. **Try signing in again**

---

## 🚨 Alternative: Create New OAuth Client

If nothing works, create a fresh OAuth client:

1. **Go to Credentials**
2. **Click "Create Credentials" → "OAuth Client ID"**
3. **Select "Android"**
4. **Fill in:**
   - Name: `Expense Splitter Debug`
   - Package name: `com.expensesplitter.app.debug`
   - SHA-1: `D0:91:1E:0E:9D:0F:18:CB:CD:EB:EE:1E:16:18:8C:52:D1:A8:47:C9`
5. **Click "Create"**
6. **Copy the new Client ID**
7. **Update `local.properties`** with the new Client ID
8. **Rebuild and test**

---

## 📊 Expected vs. Current Behavior

### ❌ Current (Failing):
```
1. Tap "Sign in with Google"
2. Account picker appears
3. Select account
4. SignInHubActivity closes immediately
5. Back to MainActivity
6. NO logs from AuthViewModel
```

### ✅ Expected (Working):
```
1. Tap "Sign in with Google"
2. Account picker appears
3. Select account
4. Brief "Signing in..." screen
5. AuthViewModel logs: "Sign-in account: email@gmail.com"
6. AuthViewModel logs: "Has IdToken: true"
7. Navigate to authenticated screen
```

---

## 🔑 Key Points

1. **The callback never fires** = Problem is in Google Play Services, not your code
2. **SignInHubActivity closes instantly** = OAuth configuration rejected by Google
3. **Most common cause** = Package name or SHA-1 mismatch
4. **Second most common** = Email not in Test Users list
5. **Third most common** = Using Web Client ID instead of Android Client ID

---

## ✉️ What to Check Next

**Tell me:**
1. Is your email address added to the "Test users" list in OAuth consent screen?
2. When you edit the Android OAuth client in Google Cloud Console, does the package name show `com.expensesplitter.app.debug` or `com.expensesplitter.app`?
3. Does the SHA-1 in Google Cloud Console match: `D0:91:1E:0E:9D:0F:18:CB:CD:EB:EE:1E:16:18:8C:52:D1:A8:47:C9`?

These three things are the most likely causes of silent sign-in failure.

---

**Next steps:** Check these three items in Google Cloud Console and let me know what you find!
