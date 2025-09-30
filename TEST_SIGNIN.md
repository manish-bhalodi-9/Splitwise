# Testing Google Sign-In - Step by Step

## ✅ Good News!

The OAuth configuration is now **CORRECT**! The sign-in popup appeared and you were able to:
1. ✅ See the account picker
2. ✅ Select your account  
3. ✅ Grant permissions

## ⚠️ What Happened

The app process was **killed before it could receive the sign-in result**. This happened because:
- You granted permissions ✅
- Then immediately **swiped away the app** or **pressed back** ❌
- Android killed the process before the callback executed

**From logs:**
```
Killing 14107:com.expensesplitter.app.debug: remove task
Process 14107 exited due to signal 9 (Killed)
```

## 🧪 How to Test Properly

### Step 1: Start Fresh
```powershell
# Clear logs
adb logcat -c

# Start monitoring  
adb logcat | Select-String -Pattern "AuthViewModel|expensesplitter"
```

### Step 2: Open App
1. **Launch "Expense Splitter"** on your phone
2. You should see the sign-in screen
3. **DO NOT close the app or swipe it away**

### Step 3: Sign In
1. Tap **"Sign in with Google"**
2. Account picker appears
3. **Select your account**
4. **Grant all permissions**
5. **Wait on this screen - don't touch anything!**
6. **Keep the app in foreground!**

### Step 4: Watch What Happens

**✅ If Successful:**
- You'll see "Authenticating..." briefly
- App navigates to dashboard/home screen
- You're signed in!

**❌ If Failed:**
- Error message appears
- App stays on sign-in screen
- Check logs for error code

---

## 📊 Expected Logs (Success)

When sign-in works, you should see these logs:

```
AuthViewModel: Client ID: 375538907803-...
AuthViewModel: Sign-in account: your.email@gmail.com
AuthViewModel: Has IdToken: true
AuthViewModel: Services initialized successfully
```

## 📊 Expected Logs (Failure)

If sign-in fails, you'll see:

```
AuthViewModel: Client ID: 375538907803-...
AuthViewModel: ApiException: statusCode=XX, message=...
AuthViewModel: Sign in failed (XX): ...
```

---

## 🎯 Critical: Don't Kill The App!

**Common Mistakes:**
- ❌ Pressing back button after granting permissions
- ❌ Swiping app away from recent apps
- ❌ Switching to another app immediately
- ❌ Locking the phone

**Correct Behavior:**
- ✅ Grant permissions
- ✅ Wait for callback
- ✅ Keep app in foreground
- ✅ Let the navigation happen automatically

---

## 🔍 What We're Looking For

After you grant permissions and **wait**, one of these will happen:

### Scenario A: Success (Navigate to Dashboard)
```
SignInHubActivity closes
→ AuthViewModel receives callback
→ "Sign-in account: your.email@gmail.com"
→ "Services initialized successfully"
→ Navigate to authenticated screen
```

### Scenario B: Failure (Show Error)
```
SignInHubActivity closes
→ AuthViewModel receives callback with error
→ "ApiException: statusCode=XX"
→ Error message displayed
→ Stay on sign-in screen
```

### Scenario C: Killed (What happened before)
```
SignInHubActivity closes
→ User swipes app away
→ Process killed
→ NO callback received
→ App restarts fresh
```

---

## 🚀 Ready to Test?

1. **Open a PowerShell terminal**
2. **Run:** `adb logcat -c`
3. **Run:** `adb logcat | Select-String -Pattern "AuthViewModel"`
4. **Open the app on your phone**
5. **Tap "Sign in with Google"**
6. **Grant permissions**
7. **WAIT - DON'T TOUCH ANYTHING!**
8. **Watch the logs in PowerShell**

---

## 💡 What Success Looks Like

**On Phone:**
1. Sign-in screen appears
2. Tap "Sign in with Google"
3. Account picker appears
4. Select account
5. Grant permissions screen
6. Tap "Allow"
7. **Brief loading state**
8. **Automatically navigate to dashboard**
9. **You're signed in!**

**In Logs:**
```
AuthViewModel: Client ID: 375538907803-sqeij0p5612eas37867dhhsol3oi0rtv.apps.googleusercontent.com
AuthViewModel: Sign-in account: your.email@gmail.com
AuthViewModel: Has IdToken: true
AuthViewModel: Services initialized successfully
```

---

## 📱 Ready? Let's test it properly this time!

**Remember:** After granting permissions, **DON'T touch the phone** - let the app complete the sign-in process!
