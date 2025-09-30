# Troubleshooting Guide - Expense Splitter

Common issues and their solutions.

---

## Authentication Issues

### Problem: "Sign in failed" error

**Possible Causes:**
- Incorrect SHA-1 fingerprint in Google Cloud Console
- Package name mismatch
- OAuth consent screen not configured properly

**Solutions:**
1. Verify SHA-1 fingerprint:
   ```bash
   keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
   ```
2. Check that package name in Google Cloud Console matches `com.expensesplitter.app`
3. Ensure OAuth consent screen is published (or user is added to test users)
4. Clear app data and try again

### Problem: "Permission denied" when accessing Google Sheets

**Solutions:**
1. Check that Google Sheets API is enabled in Google Cloud Console
2. Verify scopes are added to OAuth consent screen
3. Revoke access and re-authenticate:
   - Go to https://myaccount.google.com/permissions
   - Remove Expense Splitter
   - Sign in again in the app

### Problem: "API not enabled" error

**Solutions:**
1. Go to Google Cloud Console
2. Navigate to "APIs & Services" > "Library"
3. Search for and enable:
   - Google Sheets API
   - Google Drive API
4. Wait a few minutes for changes to propagate
5. Restart the app

---

## Sync Issues

### Problem: Data not syncing to Google Sheets

**Solutions:**
1. Check internet connection
2. Pull to refresh to force sync
3. Check sync status in dashboard (should show "Synced" or "Syncing")
4. If stuck, go to Settings > Data & Sync > Force Sync
5. Check Google Drive to see if folder exists: `/ExpenseSplitter/Groups/`

### Problem: "Sync failed" error

**Solutions:**
1. Check internet connection
2. Verify Google Sheets API quota hasn't been exceeded (unlikely for personal use)
3. Check that the Google Sheet still exists in Drive
4. Try creating a new group (creates new sheet)
5. Check if you have storage space in Google Drive

### Problem: Duplicate expenses appearing

**Possible Cause:** Conflict during sync

**Solutions:**
1. This shouldn't happen, but if it does:
   - Note which expenses are duplicates
   - Delete the duplicates manually
   - Report the issue (helps us fix the bug)

---

## Database Issues

### Problem: App crashes on launch

**Solutions:**
1. Clear app cache: Settings > Apps > Expense Splitter > Clear Cache
2. If that doesn't work, clear app data (WARNING: deletes local data):
   - Settings > Apps > Expense Splitter > Clear Data
   - Data in Google Sheets is safe and will re-sync
3. Reinstall the app if issue persists

### Problem: "Database error" message

**Solutions:**
1. Restart the app
2. If issue persists, clear app data and re-sync
3. Check available storage space on device

---

## Group Issues

### Problem: Can't create new group

**Solutions:**
1. Check internet connection (requires online for first creation)
2. Verify Google Drive API is enabled
3. Check that you have storage space in Google Drive
4. Try signing out and signing in again

### Problem: Group not showing in list

**Solutions:**
1. Pull to refresh
2. Check if group was archived (go to Settings > Groups > Show Archived)
3. Verify the Google Sheet exists in Drive folder

### Problem: Can't add member to group

**Solutions:**
1. Ensure email is a valid Gmail address
2. Check that you typed the email correctly
3. Member limit is 2 (current version limitation)

---

## Expense Issues

### Problem: Can't add expense

**Solutions:**
1. Check that all required fields are filled:
   - Description
   - Amount (must be greater than 0)
   - Date
   - Category
2. Verify amount is in valid format (no special characters except decimal point)
3. Try again with simpler values to isolate the issue

### Problem: Receipt upload fails

**Solutions:**
1. Check internet connection
2. Verify image size isn't too large (should be < 2MB)
3. Try compressing the image
4. Check camera/storage permissions:
   - Settings > Apps > Expense Splitter > Permissions
5. Check available Google Drive storage

### Problem: Can't edit expense

**Solution:**
- If expense is settled, unsettle it first
- Only the creator can edit (future versions may allow shared editing)

---

## UI/Display Issues

### Problem: App displays incorrectly/UI elements overlapping

**Solutions:**
1. Restart the app
2. Check if your device has sufficient memory
3. Update Android System WebView if applicable
4. Try changing app theme in Settings

### Problem: Charts not displaying

**Solutions:**
1. Ensure there's data for the selected time period
2. Try a different date range
3. Pull to refresh
4. Restart the app

### Problem: Dark mode not working

**Solutions:**
1. Go to Settings > Theme
2. Try selecting "Dark" explicitly (not "System")
3. Restart the app

---

## Performance Issues

### Problem: App is slow/laggy

**Solutions:**
1. Close other apps to free up memory
2. Clear app cache
3. Check device storage (keep at least 500MB free)
4. Restart device
5. Consider limiting sync frequency in Settings

### Problem: High battery drain

**Solutions:**
1. Reduce sync frequency:
   - Settings > Data & Sync > Sync Frequency
2. Disable unused notifications
3. Close app when not in use (don't leave running in background)

---

## Installation Issues

### Problem: "App not installed" error

**Solutions:**
1. Enable "Install from Unknown Sources":
   - Settings > Security > Unknown Sources
   - Or Settings > Apps > Special Access > Install Unknown Apps
2. Check available storage space
3. Ensure Android version is 15 or higher
4. Try downloading APK again (might be corrupted)

### Problem: "Parse error" when installing

**Solutions:**
1. APK file might be corrupted - re-download
2. Ensure device meets minimum requirements (Android 15+)
3. Try installing on a different device to verify APK

---

## Data Issues

### Problem: Lost expenses after app update

**Solution:**
- Data is stored in Google Sheets and should sync automatically
- Pull to refresh to force sync
- Check Google Sheets directly in Drive
- If data is in sheets but not in app, try clearing app cache and re-syncing

### Problem: Want to export data

**Solutions:**
1. In-app export:
   - Settings > Data Export > Export to CSV/PDF
2. Direct access:
   - Open Google Drive
   - Navigate to `/ExpenseSplitter/Groups/`
   - Download the sheet manually

### Problem: Need to recover deleted expense

**Solutions:**
- Deleted expenses are soft-deleted for 30 days
- Currently, recovery requires contacting support
- Future versions will have in-app recovery

---

## Network Issues

### Problem: "No internet connection" even when connected

**Solutions:**
1. Check if other apps can access internet
2. Try switching between WiFi and mobile data
3. Restart device
4. Check if firewall/VPN is blocking the app

### Problem: Sync very slow

**Solutions:**
1. Check internet speed
2. Try on different network (WiFi vs mobile data)
3. Reduce number of receipts (large images slow down sync)
4. Sync during off-peak hours if using mobile data with limits

---

## Account Issues

### Problem: Want to switch Google accounts

**Solutions:**
1. Sign out from current account
2. Sign in with different account
3. Note: Each account has its own separate data

### Problem: Can't sign out

**Solutions:**
1. Go to Settings > Profile > Sign Out
2. If that doesn't work, clear app data (WARNING: deletes local data)
3. Data in Google Sheets remains safe

---

## Still Having Issues?

If your issue isn't listed here:

1. Check the FAQ in the User Manual
2. Review the Google Cloud Setup guide
3. Verify all setup steps were completed correctly
4. Try the app on a different device to isolate the issue
5. Check Android version (must be 15+)

### Reporting Bugs

When reporting issues, please include:
- Device model and Android version
- App version
- Steps to reproduce the issue
- Screenshots if applicable
- Error messages if any

---

## Emergency Data Recovery

If you need to access your data urgently:

1. Open Google Drive on web or mobile
2. Navigate to `/ExpenseSplitter/Groups/`
3. Open your group's spreadsheet
4. All expense data is there in readable format
5. Download as Excel/CSV if needed

Your data is always safe in Google Sheets, even if the app has issues!

---

**Last Updated:** September 30, 2025
