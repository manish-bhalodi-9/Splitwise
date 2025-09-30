# Development Guide - Expense Splitter

Guide for developers who want to understand, modify, or contribute to the Expense Splitter project.

---

## Table of Contents

1. [Architecture Overview](#architecture-overview)
2. [Project Structure](#project-structure)
3. [Key Components](#key-components)
4. [Database Schema](#database-schema)
5. [API Integration](#api-integration)
6. [Sync Mechanism](#sync-mechanism)
7. [Building the Project](#building-the-project)
8. [Testing](#testing)
9. [Release Process](#release-process)
10. [Contributing](#contributing)

---

## Architecture Overview

The app follows **MVVM (Model-View-ViewModel)** architecture with the following layers:

```
┌─────────────────────────────────────┐
│         Presentation Layer          │
│  (Compose UI, ViewModels, Screens)  │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│          Domain Layer               │
│   (Use Cases, Business Logic)       │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│           Data Layer                │
│  (Repositories, Room DB, API)       │
└─────────────────────────────────────┘
```

### Key Principles:
- **Single Source of Truth**: Room Database
- **Unidirectional Data Flow**: UI → ViewModel → UseCase → Repository
- **Offline-First**: All operations work offline, sync when online
- **Reactive**: Kotlin Flow for reactive data streams
- **Dependency Injection**: Hilt for clean dependencies

---

## Project Structure

```
com.expensesplitter.app/
├── data/
│   ├── local/
│   │   ├── dao/              # Room DAOs
│   │   ├── entity/           # Room Entities
│   │   ├── converter/        # Type Converters
│   │   └── ExpenseSplitterDatabase.kt
│   ├── remote/
│   │   ├── GoogleApiClient.kt
│   │   ├── GoogleSheetsService.kt
│   │   └── GoogleDriveService.kt
│   ├── repository/           # Repository implementations
│   ├── model/                # Data models (DTOs)
│   └── sync/                 # Sync logic
│
├── domain/
│   ├── model/                # Domain models
│   ├── usecase/              # Business logic use cases
│   └── repository/           # Repository interfaces
│
├── presentation/
│   ├── auth/                 # Authentication screen
│   ├── dashboard/            # Dashboard screen
│   ├── expense/              # Expense management screens
│   ├── analytics/            # Analytics screens
│   ├── group/                # Group management screens
│   ├── settings/             # Settings screens
│   ├── common/               # Shared UI components
│   └── theme/                # Material Theme
│
├── di/                       # Hilt modules
│   ├── DatabaseModule.kt
│   ├── NetworkModule.kt
│   └── DataStoreModule.kt
│
└── util/                     # Utility classes
    ├── DateUtils.kt
    ├── CurrencyUtils.kt
    └── Extensions.kt
```

---

## Key Components

### 1. Database Layer (Room)

**ExpenseSplitterDatabase**: Main database class with encrypted storage using SQLCipher.

**Key Entities:**
- `UserEntity`: User information
- `GroupEntity`: Expense groups
- `ExpenseEntity`: Individual expenses
- `ExpenseSplitEntity`: Split details per user
- `CategoryEntity`: Expense categories
- `SettlementEntity`: Settlement records
- `SyncQueueEntity`: Offline sync queue
- `AuditLogEntity`: Audit trail

### 2. Google API Integration

**GoogleApiClient**: Manages Google Sign-In and API credentials.

**GoogleSheetsService**: 
- Create spreadsheets
- Read/write expense data
- Manage monthly sheets

**GoogleDriveService**:
- Folder management
- Receipt uploads
- File operations

### 3. Sync Mechanism

The app uses an **offline-first sync queue**:

1. User performs action (add/edit/delete expense)
2. Action saved to local database immediately
3. Action queued in `SyncQueueEntity`
4. Background worker syncs queue with Google Sheets
5. Conflicts resolved using last-write-wins strategy

### 4. Navigation

Using Jetpack Compose Navigation:
- `NavHost` in `ExpenseSplitterApp.kt`
- Screens: auth, dashboard, expense, analytics, groups, settings

---

## Database Schema

### Relationships

```
GroupEntity 1──── * ExpenseEntity
ExpenseEntity 1──── * ExpenseSplitEntity
ExpenseEntity *──── 1 CategoryEntity
GroupEntity 1──── * SettlementEntity
```

### Indexes

Critical indexes for performance:
- `expenses.groupId`
- `expenses.date`
- `expenses.categoryId`
- `expense_splits.expenseId`
- `settlements.groupId`

---

## API Integration

### Google Sheets Structure

Each group creates a Google Spreadsheet with:

**Sheet: Metadata**
- Group information
- Created date, members, etc.

**Monthly Sheets: YYYY-MM**
- One sheet per month (e.g., "2025-09")
- Columns: Expense ID, Date, Description, Amount, etc.

**Sheet: Categories**
- Custom categories for the group

**Sheet: Settlements**
- Settlement records

**Sheet: Audit Log**
- All changes tracked

### Sync Flow

```
1. Local DB Change
   ↓
2. Add to SyncQueue
   ↓
3. WorkManager schedules sync
   ↓
4. SyncWorker processes queue
   ↓
5. Call GoogleSheetsService
   ↓
6. Update Google Sheet
   ↓
7. Mark as synced in local DB
```

---

## Building the Project

### Prerequisites

- Android Studio Hedgehog or later
- JDK 17
- Android SDK 35+
- Gradle 8.3+

### Setup Steps

1. **Clone Repository**
   ```bash
   git clone <repo-url>
   cd Splitwise
   ```

2. **Configure Google Cloud**
   - Follow `docs/GOOGLE_CLOUD_SETUP.md`
   - Add your Client ID to `strings.xml`

3. **Sync Gradle**
   ```bash
   ./gradlew build
   ```

4. **Run on Device/Emulator**
   ```bash
   ./gradlew installDebug
   ```

### Build Variants

- **Debug**: Development build with logging
- **Release**: Production build with ProGuard/R8

---

## Testing

### Unit Tests

Located in `app/src/test/`:
- ViewModel tests
- UseCase tests
- Repository tests
- Utility tests

Run:
```bash
./gradlew test
```

### Instrumented Tests

Located in `app/src/androidTest/`:
- Database tests
- UI tests
- Integration tests

Run:
```bash
./gradlew connectedAndroidTest
```

### Test Coverage

Target: 70%+

Generate report:
```bash
./gradlew jacocoTestReport
```

---

## Release Process

### 1. Prepare Release

1. Update version in `build.gradle.kts`:
   ```kotlin
   versionCode = 2
   versionName = "1.1.0"
   ```

2. Update `CHANGELOG.md`

3. Run all tests:
   ```bash
   ./gradlew test connectedAndroidTest
   ```

### 2. Generate Signed APK

#### Create Keystore (first time only)

```bash
keytool -genkey -v -keystore release-keystore.jks -keyalg RSA -keysize 2048 -validity 10000 -alias expense-splitter
```

Store keystore securely! Do NOT commit to version control.

#### Sign APK

1. Update `build.gradle.kts` with signing config
2. Build release:
   ```bash
   ./gradlew assembleRelease
   ```

3. APK location: `app/build/outputs/apk/release/app-release.apk`

### 3. Test Release APK

1. Install on clean device
2. Test all critical flows
3. Verify Google Sign-In works
4. Test sync functionality

### 4. Distribute

1. Upload to GitHub Releases
2. Update README with download link
3. Notify users

---

## Contributing

### Code Style

- Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use meaningful variable names
- Add KDoc comments for public APIs
- Keep functions small and focused

### Git Workflow

1. Create feature branch: `git checkout -b feature/new-feature`
2. Make changes and commit: `git commit -m "Add new feature"`
3. Push branch: `git push origin feature/new-feature`
4. Create pull request

### Commit Messages

Format:
```
<type>: <subject>

<body>
```

Types: `feat`, `fix`, `docs`, `style`, `refactor`, `test`, `chore`

Example:
```
feat: Add receipt image compression

- Compress images before upload to reduce size
- Max size set to 2MB
- Improves sync performance
```

---

## Useful Commands

### Gradle Tasks

```bash
# Build project
./gradlew build

# Run tests
./gradlew test

# Install debug APK
./gradlew installDebug

# Generate release APK
./gradlew assembleRelease

# Clean build
./gradlew clean

# Check dependencies
./gradlew dependencies
```

### ADB Commands

```bash
# Install APK
adb install app-debug.apk

# Uninstall app
adb uninstall com.expensesplitter.app

# Clear app data
adb shell pm clear com.expensesplitter.app

# View logs
adb logcat | grep ExpenseSplitter

# Get SHA-1 fingerprint
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
```

---

## Debugging Tips

### Database Inspection

Use Android Studio Database Inspector:
1. Run app on device/emulator
2. View > Tool Windows > App Inspection
3. Select Database Inspector tab

### Network Debugging

Enable logging in `GoogleSheetsService` and `GoogleDriveService`.

### ProGuard Issues

If release build crashes, check ProGuard rules in `proguard-rules.pro`.

---

## Performance Optimization

### Database
- Use proper indexes
- Batch operations where possible
- Limit query results

### UI
- Use `LazyColumn` for lists
- Avoid unnecessary recomposition
- Use `remember` and `derivedStateOf`

### Sync
- Batch Google Sheets API calls
- Compress receipts before upload
- Use background workers efficiently

---

## Security Considerations

1. **Never commit sensitive data**:
   - Keystores
   - OAuth credentials
   - API keys

2. **Use encryption**:
   - SQLCipher for database
   - Android Keystore for tokens

3. **Validate input**:
   - Sanitize user input
   - Validate amounts and dates

4. **Secure communication**:
   - HTTPS only
   - Certificate pinning (optional)

---

## Resources

- [Android Developers](https://developer.android.com/)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)
- [Room Database](https://developer.android.com/training/data-storage/room)
- [Google Sheets API](https://developers.google.com/sheets/api)
- [Hilt Dependency Injection](https://developer.android.com/training/dependency-injection/hilt-android)

---

**Happy Coding! 🚀**
