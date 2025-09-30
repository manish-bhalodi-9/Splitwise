# Changelog

All notable changes to the Expense Splitter project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [1.0.0] - 2025-09-30

### 🎉 Initial Release

First public release of Expense Splitter!

### ✨ Added

#### Authentication
- Google Sign-In integration with OAuth 2.0
- Secure token storage using Android Keystore
- Persistent login sessions

#### Group Management
- Create expense groups
- Each group backed by a Google Sheet
- Add up to 2 members per group
- Switch between multiple groups
- Set default active group

#### Expense Management
- Add expenses with description, amount, date, category
- Edit and delete expenses
- Multiple split methods: Equal, Exact Amounts, Percentages, Shares
- Attach receipt images from camera or gallery
- Add notes and custom tags
- Search expenses by description, amount, notes
- Filter by category, date range, status, tags
- Soft delete with 30-day recovery period

#### Categories
- 13 default categories with emojis
- Create custom categories
- Assign colors and icons
- Deactivate instead of delete

#### Settlement
- Track who owes whom
- Mark expenses as settled
- Add settlement notes
- Settlement history
- Calculate net balances automatically

#### Analytics & Reports
- Dashboard summary cards
- Monthly expense breakdown
- Category-wise pie charts
- Spending trend line charts
- 3-month category trend on expense details
- Export to CSV and PDF

#### Google Sheets Integration
- Automatic spreadsheet creation
- Monthly sheets (YYYY-MM format)
- Real-time sync with conflict resolution
- Metadata tracking
- Audit log

#### Google Drive Integration
- Organized folder structure: `/ExpenseSplitter/Groups/`
- Receipt image uploads
- Automatic folder management

#### Data Sync
- Offline-first architecture
- Background sync every 15 minutes
- Pull-to-refresh manual sync
- Sync queue with retry logic
- Conflict resolution (last-write-wins)
- Sync status indicators

#### Database
- Encrypted Room database with SQLCipher
- 8 entity types for comprehensive data model
- Proper relationships and indexes
- Type converters for complex types

#### UI/UX
- Material Design 3 (Material You)
- Dark mode support
- Dynamic colors (Android 12+)
- Responsive layouts
- Intuitive navigation
- Loading and empty states
- Error handling with user-friendly messages

#### Notifications
- Daily expense reminder (optional)
- Monthly settlement reminder
- Large expense alerts
- Sync failure notifications

#### Settings
- Theme selection (Light/Dark/System)
- Currency selection (INR, USD, EUR, GBP, etc.)
- Notification preferences
- Data export options
- About and version info

#### Security
- Local database encryption
- Secure OAuth token storage
- HTTPS for all API calls
- No third-party tracking
- Privacy-first design

#### Documentation
- Comprehensive README
- Google Cloud Console setup guide
- User manual
- Troubleshooting guide
- Development guide
- Privacy policy

### 🏗️ Architecture
- MVVM architecture
- Clean architecture with layers (Presentation, Domain, Data)
- Hilt dependency injection
- Kotlin Coroutines and Flow
- Repository pattern
- Use case pattern

### 📱 Compatibility
- Minimum SDK: Android 15 (API 35)
- Target SDK: Android 15 (API 35)
- Requires Google Account
- APK distribution (sideloading)

### 🛠️ Development
- Kotlin 1.9.22
- Jetpack Compose
- Room Database 2.6.1
- Hilt 2.50
- Google Play Services Auth 21.2.0
- Google API Client 2.6.0
- Google Sheets API v4
- Google Drive API v3

---

## [Unreleased]

### 🚀 Planned Features

#### Version 1.1.0
- [ ] Multi-user groups (3+ members)
- [ ] Recurring expenses
- [ ] Budget management
- [ ] Enhanced analytics with ML insights
- [ ] Home screen widgets
- [ ] App shortcuts

#### Version 1.2.0
- [ ] Receipt OCR (auto-extract amounts)
- [ ] Voice input for expenses
- [ ] Bank statement import
- [ ] Split by percentage adjustments
- [ ] Currency conversion

#### Version 2.0.0
- [ ] Web application
- [ ] iOS app
- [ ] Real-time sync (Firebase integration)
- [ ] Push notifications between users
- [ ] Collaborative group editing

### 🐛 Known Issues
- None reported yet

---

## Version History

### How to Read Version Numbers

Format: `MAJOR.MINOR.PATCH`

- **MAJOR**: Incompatible API changes or major feature overhaul
- **MINOR**: New features, backward-compatible
- **PATCH**: Bug fixes, backward-compatible

---

## Release Notes Format

Future releases will follow this format:

### [Version] - YYYY-MM-DD

#### Added
- New features

#### Changed
- Changes to existing features

#### Deprecated
- Features being phased out

#### Removed
- Removed features

#### Fixed
- Bug fixes

#### Security
- Security improvements

---

## Feedback and Suggestions

We welcome feedback! If you have suggestions for future versions, please:
1. Check if it's already in "Planned Features"
2. Open an issue on GitHub (if applicable)
3. Contact us via email

---

**Thank you for using Expense Splitter!** 💰
