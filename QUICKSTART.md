# Quick Start Guide - Expense Splitter

Get up and running with Expense Splitter in 15 minutes!

---

## Prerequisites

- ✅ Android Studio Hedgehog or later
- ✅ Android SDK 35+
- ✅ JDK 17
- ✅ Google Account
- ✅ Android device/emulator with Android 15+

---

## Step 1: Get the Project (2 minutes)

### If you have the source code:
```bash
cd Splitwise
```

### If cloning from Git:
```bash
git clone <repository-url>
cd Splitwise
```

---

## Step 2: Open in Android Studio (2 minutes)

1. Launch Android Studio
2. Click **"Open"**
3. Navigate to the `Splitwise` folder
4. Click **"OK"**
5. Wait for Gradle sync to complete (may take a few minutes)

**If sync fails:** Check your internet connection and Android SDK installation.

---

## Step 3: Configure Google Cloud Console (5 minutes)

### Quick Version:

1. **Go to [Google Cloud Console](https://console.cloud.google.com)**

2. **Create Project**
   - Click "New Project"
   - Name: "ExpenseSplitter"
   - Click "Create"

3. **Enable APIs** (APIs & Services > Library)
   - Enable "Google Sheets API"
   - Enable "Google Drive API"
   - Enable "Google People API" (optional but recommended)
   
   **Note:** Google Sign-In doesn't require a separate API - it's part of Google Identity Services

4. **Get SHA-1 Fingerprint**
   
   **Windows (PowerShell):**
   ```powershell
   & "C:\Program Files\Android\Android Studio\jbr\bin\keytool.exe" -list -v -keystore "$env:USERPROFILE\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android
   ```
   
   **Mac/Linux:**
   ```bash
   keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
   ```
   
   Copy the SHA1 value from the output (looks like `AB:CD:EF:12:34:56...`)
   
   Copy the SHA1 value (looks like: `AB:CD:EF:12:34:...`)

5. **Configure OAuth Consent Screen** (APIs & Services > OAuth consent screen)
   - Select "External"
   - App name: "Expense Splitter"
   - User support email: Your email
   - Add your email to test users
   - Add scopes:
     - `https://www.googleapis.com/auth/spreadsheets`
     - `https://www.googleapis.com/auth/drive.file`
     - `userinfo.profile`
     - `userinfo.email`
   - Save

6. **Create OAuth Client ID** (APIs & Services > Credentials)
   - Click "Create Credentials" > "OAuth 2.0 Client ID"
   - Application type: "Android"
   - Name: "Expense Splitter Android (Debug)"
   - Package name: `com.expensesplitter.app`
   - SHA-1: Paste your SHA-1 from step 4
   - Click "Create"
   - **COPY YOUR CLIENT ID** (looks like: `123456789012-abc...xyz.apps.googleusercontent.com`)

**Need more details?** See `docs/GOOGLE_CLOUD_SETUP.md`

---

## Step 4: Add Your Client ID (1 minute)

1. In Android Studio, open: `app/src/main/res/values/strings.xml`

2. Find this line:
   ```xml
   <string name="google_client_id">YOUR_GOOGLE_CLIENT_ID_HERE</string>
   ```

3. Replace `YOUR_GOOGLE_CLIENT_ID_HERE` with your actual Client ID:
   ```xml
   <string name="google_client_id">123456789012-abcdefghijklmnop.apps.googleusercontent.com</string>
   ```

4. Save the file (Ctrl+S / Cmd+S)

---

## Step 5: Build and Run (3 minutes)

### Option A: Using Android Studio (Easiest)

1. Connect your Android device (USB debugging enabled) or start an emulator
2. In Android Studio toolbar, click the green **"Run"** button (▶️)
3. Select your device
4. Wait for app to build and install
5. App will launch automatically!

### Option B: Using Command Line

```bash
# Build debug APK
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug
```

**Windows users:** Use `gradlew.bat` instead of `./gradlew`

---

## Step 6: Test the App (2 minutes)

1. **App launches** - You should see the splash screen, then the authentication screen

2. **Sign In**
   - Tap "Sign in with Google"
   - Select your Google account
   - Grant permissions when prompted
   - Wait for authentication to complete

3. **Success!** 
   - You should be taken to the dashboard (placeholder screen)
   - This confirms authentication works!

4. **Check Google Drive** (Optional)
   - Open Google Drive on web or mobile
   - You should see a folder: `/ExpenseSplitter/Groups/`
   - This confirms Drive API works!

---

## 🎉 Congratulations!

You now have:
- ✅ Expense Splitter running on your device
- ✅ Google Sign-In working
- ✅ APIs configured correctly
- ✅ Ready to use the app!

---

## What's Next?

### For Users:
- Read the **User Manual** (`docs/USER_MANUAL.md`)
- Create your first group
- Add your first expense
- Explore features

### For Developers:
- Read the **Development Guide** (`docs/DEVELOPMENT_GUIDE.md`)
- Explore the codebase
- Implement remaining screens
- Run tests

---

## Troubleshooting

### "Sign in failed"
- ❌ Wrong: SHA-1 fingerprint doesn't match
- ✅ Solution: Verify SHA-1 in Google Cloud Console

### "App won't build"
- ❌ Wrong: Gradle sync failed
- ✅ Solution: File > Invalidate Caches and Restart

### "Permission denied"
- ❌ Wrong: APIs not enabled or scopes not added
- ✅ Solution: Check Google Cloud Console setup

### "Can't install APK"
- ❌ Wrong: Unknown sources disabled
- ✅ Solution: Enable in Settings > Security

**More issues?** See `docs/TROUBLESHOOTING.md`

---

## Common Issues & Quick Fixes

### ❌ "keytool is not recognized" (Windows)

**Problem:** PowerShell doesn't find keytool command.

**Solution:** Use the full path to keytool:
```powershell
& "C:\Program Files\Android\Android Studio\jbr\bin\keytool.exe" -list -v -keystore "$env:USERPROFILE\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android
```

**Find keytool location:**
```powershell
Get-ChildItem -Path "C:\Program Files" -Recurse -Filter "keytool.exe" -ErrorAction SilentlyContinue | Select-Object FullName
```

### ❌ "debug.keystore not found"

**Problem:** Debug keystore doesn't exist.

**Solution:** Build the app once in Android Studio - it will generate automatically.

### ❌ Can't find Google Sign-In API

**Problem:** Looking for "Google Sign-In API" in API Library.

**Solution:** You don't need to enable it! It's part of Google Identity Services. Just enable:
- Google Sheets API
- Google Drive API
- Google People API (optional)

---

## Quick Reference

### Important Files
- `strings.xml` - Add your Client ID here
- `build.gradle.kts` - Dependencies and configuration
- `AndroidManifest.xml` - Permissions and components

### Important Folders
- `app/src/main/java/...` - Kotlin source code
- `app/src/main/res/` - Resources (strings, colors, etc.)
- `docs/` - All documentation

### Key Commands
```bash
./gradlew assembleDebug    # Build debug APK
./gradlew installDebug     # Install on device
./gradlew test             # Run unit tests
./gradlew clean            # Clean build
```

---

## Need Help?

1. **Troubleshooting Guide**: `docs/TROUBLESHOOTING.md`
2. **User Manual**: `docs/USER_MANUAL.md`
3. **Development Guide**: `docs/DEVELOPMENT_GUIDE.md`
4. **Google Cloud Setup**: `docs/GOOGLE_CLOUD_SETUP.md`

---

## Summary Checklist

- [ ] Android Studio installed
- [ ] Project opened and synced
- [ ] Google Cloud project created
- [ ] APIs enabled
- [ ] OAuth consent screen configured
- [ ] OAuth Client ID created
- [ ] Client ID added to strings.xml
- [ ] App built successfully
- [ ] App installed on device
- [ ] Google Sign-In tested and working

**All checked?** You're ready to use Expense Splitter! 🚀

---

**Time taken:** ~15 minutes  
**Difficulty:** Easy  
**Result:** Fully functional authentication! ✨
