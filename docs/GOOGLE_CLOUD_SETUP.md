# Google Cloud Console Setup Guide

This guide will walk you through setting up Google Cloud Console for the Expense Splitter application.

## Prerequisites

- A Google Account
- Access to Google Cloud Console
- Android device or emulator for testing

---

## Step 1: Create a Google Cloud Project

1. Go to [Google Cloud Console](https://console.cloud.google.com)
2. Click on the project dropdown at the top
3. Click **"New Project"**
4. Enter project details:
   - **Project Name**: ExpenseSplitter (or your preferred name)
   - **Organization**: Leave as default or select your organization
   - **Location**: Leave as default
5. Click **"Create"**
6. Wait for the project to be created
7. Note down your **Project ID** (you'll need this later)

---

## Step 2: Enable Required APIs

1. In the Google Cloud Console, ensure your project is selected
2. Navigate to **"APIs & Services"** > **"Library"**
3. Search for and enable the following APIs:

### Google Sheets API
- Search for "Google Sheets API"
- Click on it
- Click **"Enable"**

### Google Drive API
- Search for "Google Drive API"
- Click on it
- Click **"Enable"**

### Google Sign-In API (Google Identity Services)
- Search for "Google+ API" (legacy) or use Google Identity Services
- Click **"Enable"**

### Google People API (Optional but recommended)
- Search for "Google People API"
- Click on it
- Click **"Enable"**

---

## Step 3: Configure OAuth Consent Screen

1. Navigate to **"APIs & Services"** > **"OAuth consent screen"**
2. Select user type:
   - **Internal**: If you have a Google Workspace (only users in your organization)
   - **External**: For personal Gmail accounts (recommended for personal use)
3. Click **"Create"**

### App Information
- **App name**: Expense Splitter
- **User support email**: Your email address
- **App logo**: Upload an app logo (optional)
- **Application home page**: Leave empty or add your website
- **Application privacy policy link**: Create a simple privacy policy (required for external)
- **Application terms of service link**: Optional
- **Authorized domains**: Leave empty for now
- **Developer contact information**: Your email address

4. Click **"Save and Continue"**

### Scopes
1. Click **"Add or Remove Scopes"**
2. Add the following scopes:
   - `https://www.googleapis.com/auth/spreadsheets` - Read and write Google Sheets
   - `https://www.googleapis.com/auth/drive.file` - Create and access app-specific files
   - `https://www.googleapis.com/auth/userinfo.profile` - User profile information
   - `https://www.googleapis.com/auth/userinfo.email` - User email address
3. Click **"Update"**
4. Click **"Save and Continue"**

### Test Users (for External apps)
1. Click **"Add Users"**
2. Add your email and your partner's email
3. Click **"Add"**
4. Click **"Save and Continue"**

### Summary
- Review your settings
- Click **"Back to Dashboard"**

---

## Step 4: Create OAuth 2.0 Credentials

### Get Your Debug SHA-1 Certificate Fingerprint

1. Open a terminal/command prompt
2. Run the following command:

**Windows (PowerShell):**
```powershell
keytool -list -v -keystore "$env:USERPROFILE\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android
```

**Mac/Linux:**
```bash
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
```

3. Look for **SHA1** fingerprint in the output
4. Copy the SHA-1 value (looks like: `AB:CD:EF:12:34:56...`)

### Create OAuth Client ID

1. Navigate to **"APIs & Services"** > **"Credentials"**
2. Click **"Create Credentials"** > **"OAuth 2.0 Client ID"**
3. Select **Application type**: **Android**
4. Enter details:
   - **Name**: Expense Splitter Android (Debug)
   - **Package name**: `com.expensesplitter.app`
   - **SHA-1 certificate fingerprint**: Paste the SHA-1 you copied
5. Click **"Create"**
6. A dialog will show your **Client ID** - copy this!
7. Click **"OK"**

### Create Release Credentials (For Production APK)

When you're ready to create a release build:
1. Create a release keystore (if you haven't already)
2. Get the SHA-1 from your release keystore
3. Create another OAuth Client ID for release
4. Use the same package name but different SHA-1

---

## Step 5: Configure the App

1. Open `app/src/main/res/values/strings.xml`
2. Replace `YOUR_GOOGLE_CLIENT_ID_HERE` with your actual Client ID:
   ```xml
   <string name="google_client_id">123456789012-abcdefghijklmnopqrstuvwxyz.apps.googleusercontent.com</string>
   ```

3. Also update in `AuthViewModel.kt` if needed (it should read from resources)

---

## Step 6: Set Up API Quotas (Optional)

1. Navigate to **"APIs & Services"** > **"Quotas"**
2. Review default quotas:
   - **Sheets API**: 100 requests per 100 seconds (default)
   - **Drive API**: 1000 requests per 100 seconds (default)
3. For personal use, default quotas should be sufficient
4. If you need more, request a quota increase

---

## Step 7: Test the Integration

1. Build and run the app on your device
2. Sign in with your Google account
3. Grant the requested permissions
4. Verify that you can access Google Sheets and Drive

### Troubleshooting

**"Sign in failed" error:**
- Verify SHA-1 fingerprint matches
- Check that package name matches exactly
- Ensure OAuth consent screen is configured
- Make sure APIs are enabled

**"Permission denied" error:**
- Check that scopes are added to OAuth consent screen
- Verify user is added to test users list (for external apps)
- Re-authenticate by signing out and signing in again

**"API not enabled" error:**
- Go back to "APIs & Services" > "Library"
- Enable the required API
- Wait a few minutes for changes to propagate

---

## Step 8: Monitor API Usage (Optional)

1. Navigate to **"APIs & Services"** > **"Dashboard"**
2. View usage statistics for your APIs
3. Set up alerts for quota limits if needed

---

## Step 9: Secure Your Credentials

### Important Security Notes:

1. **Never commit credentials to version control**
   - Add `google-services.json` to `.gitignore`
   - Keep Client IDs in `local.properties` (not committed)

2. **Use different credentials for debug and release**
   - Debug credentials for development
   - Release credentials for production APK

3. **Restrict API keys** (if you create any API keys later)
   - Navigate to **"Credentials"**
   - Edit your API key
   - Add restrictions (Android apps, IP addresses, etc.)

4. **Review OAuth consent screen**
   - Only request necessary scopes
   - Keep privacy policy updated
   - Respond to user inquiries

---

## Additional Resources

- [Google Cloud Console](https://console.cloud.google.com)
- [Google Sheets API Documentation](https://developers.google.com/sheets/api)
- [Google Drive API Documentation](https://developers.google.com/drive/api)
- [Google Sign-In for Android](https://developers.google.com/identity/sign-in/android)

---

## Summary Checklist

- [ ] Google Cloud project created
- [ ] Google Sheets API enabled
- [ ] Google Drive API enabled
- [ ] Google Sign-In API enabled
- [ ] OAuth consent screen configured
- [ ] OAuth 2.0 credentials created (Android)
- [ ] SHA-1 fingerprint added
- [ ] Client ID copied to app
- [ ] Test users added
- [ ] App tested with Google Sign-In

---

**Congratulations!** Your Google Cloud Console is now set up for the Expense Splitter app.
