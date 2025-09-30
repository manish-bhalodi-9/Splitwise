# Expense Splitter - Personal Expense Management App

A comprehensive expense management and splitting Android application inspired by Splitwise, with Google Sheets integration for data persistence.

## Overview

This application helps track, split, and analyze expenses between two users with seamless Google Sheets synchronization. Perfect for couples, roommates, or friends sharing expenses.

## Features

- **Google Sign-In Authentication**: Secure OAuth 2.0 authentication
- **Expense Management**: Add, edit, delete, and categorize expenses
- **Smart Splitting**: Equal, custom amounts, percentages, and shares
- **Group Management**: Multiple expense groups with dedicated Google Sheets
- **Analytics & Reports**: Comprehensive charts and spending insights
- **Offline Support**: Full offline functionality with automatic sync
- **Receipt Management**: Capture and store receipt images
- **Settlement Tracking**: Track payments and balance between users
- **Category Insights**: 3-month trend analysis per category
- **Material Design 3**: Modern, beautiful UI with dark mode support

## Technical Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM (Model-View-ViewModel)
- **Database**: Room + SQLCipher
- **Dependency Injection**: Hilt
- **Async Operations**: Kotlin Coroutines + Flow
- **Cloud Integration**: Google Sheets API v4, Google Drive API v3
- **Charts**: Vico (Compose Charts)
- **Image Loading**: Coil

## Minimum Requirements

- Android 15+ (API Level 35+)
- Internet connection for sync
- Google Account
- 50 MB storage space

## Project Structure

```
com.expensesplitter.app
├── data/
│   ├── local/          # Room database, DAOs, entities
│   ├── remote/         # Google API clients
│   ├── repository/     # Repository implementations
│   └── model/          # Data models
├── domain/
│   ├── usecase/        # Business logic use cases
│   └── model/          # Domain models
├── presentation/
│   ├── auth/           # Authentication screens
│   ├── dashboard/      # Dashboard screen
│   ├── expense/        # Expense management screens
│   ├── analytics/      # Analytics and reports
│   ├── group/          # Group management
│   ├── settings/       # Settings and profile
│   └── common/         # Shared UI components
├── di/                 # Hilt dependency injection modules
└── util/               # Utility classes and extensions
```

## Setup Instructions

### 1. Clone the Repository

```bash
git clone <repository-url>
cd Splitwise
```

### 2. Google Cloud Console Setup

Follow these steps to configure Google Cloud services:

1. **Create GCP Project**
   - Go to [Google Cloud Console](https://console.cloud.google.com)
   - Create a new project or select existing
   - Note your Project ID

2. **Enable Required APIs**
   - Navigate to "APIs & Services" > "Library"
   - Enable: Google Sheets API v4, Google Drive API v3, Google People API
   - Note: Google Sign-In is part of Google Identity Services (no separate API to enable)

3. **Create OAuth 2.0 Credentials**
   - Go to "APIs & Services" > "Credentials"
   - Configure OAuth consent screen
   - Create OAuth 2.0 Client ID for Android
   - Add package name: `com.expensesplitter.app`
   - Get SHA-1 fingerprint: `keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android`
   - Download credentials JSON

4. **Configure OAuth Scopes**
   - `https://www.googleapis.com/auth/spreadsheets`
   - `https://www.googleapis.com/auth/drive.file`
   - `https://www.googleapis.com/auth/userinfo.profile`
   - `https://www.googleapis.com/auth/userinfo.email`

### 3. Configure the App

1. Open `app/src/main/res/values/strings.xml`
2. Add your Google OAuth Client ID:
   ```xml
   <string name="google_client_id">YOUR_CLIENT_ID_HERE</string>
   ```

3. Update `local.properties` (create if doesn't exist):
   ```properties
   sdk.dir=YOUR_ANDROID_SDK_PATH
   ```

### 4. Build and Run

#### Using Android Studio:
1. Open the project in Android Studio
2. Sync Gradle files
3. Run on device/emulator (Android 15+)

#### Using Command Line:
```bash
./gradlew assembleDebug
./gradlew installDebug
```

### 5. Generate Release APK

```bash
./gradlew assembleRelease
```

APK will be in `app/build/outputs/apk/release/`

## Installation on Device

1. Enable "Install from Unknown Sources" on your Android device
2. Transfer the APK file to your device
3. Open and install the APK
4. Launch the app and sign in with Google

## Usage Guide

### First Time Setup
1. Launch app and sign in with Google
2. Create your first group
3. Add your expense partner

### Adding Expenses
1. Tap the FAB (+) button on dashboard
2. Enter expense details
3. Choose split method
4. Optionally add receipt
5. Save expense

### Viewing Analytics
1. Navigate to Analytics tab
2. Select date range
3. View category breakdown
4. Explore trends and insights

### Settling Up
1. Go to expense detail
2. Tap "Settle Up" button
3. Add settlement notes
4. Confirm settlement

## Data Storage

- **Local Database**: Room SQLite database (encrypted with SQLCipher)
- **Cloud Backup**: Google Sheets (one sheet per group)
- **Receipts**: Google Drive in `/ExpenseSplitter/Groups/` folder

## Security

- OAuth 2.0 tokens stored in Android Keystore
- Local database encrypted with SQLCipher
- HTTPS for all API communications
- No third-party analytics or tracking

## Troubleshooting

### App won't sign in
- Check internet connection
- Verify Google OAuth Client ID is correct
- Ensure SHA-1 fingerprint matches in GCP Console

### Sync not working
- Check internet connection
- Verify Google Sheets and Drive APIs are enabled
- Check app permissions in Google account settings

### Database errors
- Clear app data and re-sync
- Check available storage space

## Contributing

This is a personal project, but suggestions and improvements are welcome!

## License

Personal use only. Not for commercial distribution.

## Version

**Current Version**: 1.0.0  
**Last Updated**: September 30, 2025  
**Minimum Android Version**: Android 15 (API 35)

## Support

For issues or questions, refer to the troubleshooting guide or check the FAQ document.

---

Built with ❤️ for personal expense management
