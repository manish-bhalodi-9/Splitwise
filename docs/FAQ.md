# Frequently Asked Questions (FAQ)

Common questions and answers about Expense Splitter.

---

## General Questions

### What is Expense Splitter?
Expense Splitter is a personal expense management and splitting application for Android. It helps you track expenses, split them with a partner, and analyze your spending patterns. All data is stored securely in your personal Google Drive.

### Is it free to use?
Yes, completely free! There are no in-app purchases, subscriptions, or ads.

### Which platforms are supported?
Currently, Android 15+ (API 35 and higher). iOS and web versions are planned for the future.

### How do I install it?
Download the APK file and install it on your Android device. You'll need to enable "Install from Unknown Sources" in your device settings.

---

## Account & Authentication

### Do I need a Google account?
Yes, a Google account is required for authentication and to access Google Sheets and Google Drive for data storage.

### Can I use multiple Google accounts?
You can sign out and sign in with a different account, but each account has its own separate data.

### What if I lose access to my Google account?
Your expense data is stored in your Google Drive. If you lose access to your Google account, you won't be able to access your data. Always keep your Google account credentials secure.

### Can I use this without Google Sign-In?
No, Google Sign-In is required as it's integral to the app's functionality (data storage, authentication, API access).

---

## Data & Privacy

### Where is my data stored?
- **Local**: Encrypted on your Android device
- **Cloud**: Your personal Google Drive under `/ExpenseSplitter/Groups/`

### Is my data secure?
Yes! Multiple security layers:
- Local database encrypted with SQLCipher
- OAuth tokens stored in Android Keystore
- HTTPS for all communications
- No third-party access to your data

### Do you collect my data?
No! We don't collect, store, or access your data. Everything stays in your Google Drive. No analytics, no tracking, no third-party services.

### Can I delete my data?
Yes, anytime! Delete expenses in the app, delete groups, or manually delete files from your Google Drive.

### Can I export my data?
Yes! Export options:
- In-app: Settings > Data Export (CSV/PDF)
- Manual: Download Google Sheets from Drive

### Who can see my expenses?
Only you and the group members you explicitly add. No one else has access.

---

## Groups & Sharing

### How many people can be in a group?
Currently, 2 people per group (you + one other person). Multi-user groups (3+) are planned for future versions.

### Can I have multiple groups?
Yes! Create as many groups as you need. Each group has its own Google Sheet.

### How do I invite someone to a group?
Add their Gmail address when creating or editing a group. They'll need their own installation of the app to view and add expenses.

### Can I remove someone from a group?
Yes, but settle all shared expenses first to avoid confusion.

### What happens if I delete a group?
All expenses in that group are deleted from the app. The Google Sheet remains in your Drive (you can delete it manually).

---

## Expenses

### What can I track?
- Description
- Amount (any currency)
- Date
- Category
- Who paid
- Split details
- Notes
- Receipt images
- Tags

### Can I add expenses in different currencies?
Yes, but currency conversion is manual. Each expense has a currency field.

### What are the split methods?
1. **Equal**: Divide equally among all
2. **Exact Amounts**: Specify exact amounts per person
3. **Percentages**: Define percentage per person
4. **Shares**: Divide by shares (e.g., 2:1 ratio)

### Can I edit settled expenses?
You need to unsettle them first, then edit.

### How do I delete an expense?
Swipe left on the expense or tap Delete in expense details. It's soft-deleted (recoverable for 30 days).

### Can I recover deleted expenses?
Currently, recovery requires contacting support. In-app recovery is planned for future versions.

### Can I add recurring expenses?
Not yet, but it's planned for a future version!

---

## Categories

### What categories are available?
13 default categories:
- Food & Dining
- Groceries
- Transportation
- Housing & Utilities
- Entertainment
- Healthcare
- Shopping
- Bills & EMIs
- Travel
- Education
- Gifts
- Maintenance
- Others

### Can I create custom categories?
Yes! Go to Settings > Categories > Add Category

### Can I delete a category?
If expenses exist in that category, you can only deactivate it (not delete). Empty categories can be deleted.

---

## Sync & Offline

### Does it work offline?
Yes! Fully functional offline. Changes sync automatically when you're back online.

### How often does it sync?
- Automatic: Every 15 minutes (when app is open and online)
- Manual: Pull to refresh
- On change: Immediately after adding/editing expenses

### What if sync fails?
The app will retry automatically. Check your internet connection and ensure Google APIs are enabled.

### Can I force a sync?
Yes, pull down to refresh on any screen, or go to Settings > Data & Sync > Force Sync

### What happens during a conflict?
The app uses "last-write-wins" strategy. The most recent change is kept. You'll be notified if a conflict occurs.

---

## Settlement

### How do I settle up?
1. Open expense details
2. Tap "Settle Up"
3. Add notes (optional)
4. Confirm

Or settle all pending expenses at once from the Settlement tab.

### What does "You owe" mean?
It's the net amount you need to pay to your partner.

### What does "You are owed" mean?
It's the net amount your partner needs to pay you.

### How is the balance calculated?
The app calculates the net balance by:
1. Summing all amounts you paid
2. Subtracting your share of total expenses
3. Result = Balance (positive = you're owed, negative = you owe)

### Can I see settlement history?
Yes! Go to Settlement > History to view all past settlements.

---

## Analytics & Reports

### What analytics are available?
- Total expenses (monthly, yearly)
- Category breakdown (pie charts)
- Spending trends (line charts)
- Category trends (3-month history)
- Month-over-month comparison

### Can I export reports?
Yes! Export to CSV or PDF from the Analytics screen.

### How far back can I see data?
All data since you started using the app. No time limit!

### Can I filter analytics by date?
Yes, use the date range selector to filter by:
- Last 7 days
- Last 30 days
- Last 3/6/12 months
- Custom range

---

## Technical Questions

### What Android version is required?
Android 15 (API 35) or higher.

### Why such a high Android version?
The app uses latest Android features and security APIs. Lower version support may be added in the future.

### How much storage does it use?
- App size: ~20-30 MB
- Database: Varies (typically < 10 MB for hundreds of expenses)
- Receipts: Stored in Google Drive, not on device

### Does it drain battery?
No, the app is optimized for battery efficiency. Background sync is minimal.

### Can I use it on a tablet?
Yes, if it runs Android 15+. The UI adapts to larger screens.

### Is there a wear OS app?
Not yet, but it's on the roadmap!

---

## Google Sheets & Drive

### Where exactly is my data in Google Drive?
`/ExpenseSplitter/Groups/[Your Group Name].xlsx`

### Can I edit the Google Sheet directly?
Yes, but it's not recommended. The app may overwrite your changes. Use the app for all edits.

### Can I share the Google Sheet with others?
Yes, but they should use the app to view/edit, not the sheet directly.

### What if I accidentally delete the Google Sheet?
Your local data is still safe. You can force a sync to recreate the sheet, but cloud data will be lost.

### Can I move the Google Sheet?
No, keep it in the `/ExpenseSplitter/Groups/` folder. Moving it will break sync.

---

## Troubleshooting

### App won't sign in
- Check internet connection
- Verify SHA-1 fingerprint in Google Cloud Console
- Ensure OAuth consent screen is configured
- Try signing out and signing in again

### Sync not working
- Check internet connection
- Verify Google Sheets and Drive APIs are enabled
- Force sync from Settings
- Check if you have Google Drive storage space

### App crashes on launch
- Clear app cache: Settings > Apps > Expense Splitter > Clear Cache
- If that doesn't work, clear app data (WARNING: deletes local data, but Google Sheets data is safe)
- Reinstall the app

### Receipt upload fails
- Check internet connection
- Ensure image size < 2MB
- Verify camera/storage permissions
- Check Google Drive storage space

**More troubleshooting:** See `docs/TROUBLESHOOTING.md`

---

## Feature Requests

### Can you add [feature]?
We welcome feature requests! Check the roadmap in `CHANGELOG.md` to see if it's planned.

### Why isn't [feature] included?
This is version 1.0 with core functionality. Many features are planned for future versions.

### When will [feature] be added?
We don't have specific timelines, but check the changelog for updates.

---

## Support

### How do I report a bug?
[Add your bug reporting method - email, GitHub issues, etc.]

### How do I get help?
1. Check this FAQ
2. Read the User Manual (`docs/USER_MANUAL.md`)
3. Check Troubleshooting Guide (`docs/TROUBLESHOOTING.md`)
4. Contact support

### Is there a user community?
Not yet, but it's something we're considering!

---

## About the App

### Who developed this?
[Add your information or keep anonymous]

### Is it open source?
[Specify if open source or closed]

### Can I contribute?
[Specify contribution policy]

### What's the roadmap?
Check `CHANGELOG.md` for planned features.

### When was it released?
Version 1.0 released on September 30, 2025.

---

## Comparison Questions

### How is this different from Splitwise?
- **Splitwise**: Multi-user, social features, cloud server
- **Expense Splitter**: Personal (2 users), privacy-focused, your own Google Drive

### Why not just use Splitwise?
- You want complete data ownership
- You prefer privacy (no third-party servers)
- You want offline-first functionality
- You like the simplicity of 2-person splitting

### Is this better than spreadsheets?
Yes! Features spreadsheets don't have:
- Mobile-first design
- Offline support
- Automatic calculations
- Receipt attachments
- Visual analytics
- Category tracking

---

## Legal & Compliance

### Is this GDPR compliant?
Yes, we follow GDPR principles. You own and control all your data.

### What about CCPA?
Yes, CCPA principles are followed. No data selling, full transparency.

### What's your privacy policy?
See `docs/PRIVACY_POLICY.md` for complete details.

### Are there terms of service?
This is a personal-use app. Use at your own risk. No warranties provided.

---

## Miscellaneous

### Can I use this for business expenses?
It's designed for personal use, but you can use it for small business expenses if you wish.

### Can I track investments or loans?
Not directly, but you can use custom categories and notes to track these.

### Is there a desktop app?
Not yet. Web app is planned for the future.

### Can I print expenses?
Yes, export to PDF and print from there.

### Does it support biometric authentication?
Not in version 1.0, but it's planned!

### Can I backup to other cloud services?
Currently only Google Drive. Other cloud options may be added later.

---

## Still Have Questions?

If your question isn't answered here:
1. Check the **User Manual** (`docs/USER_MANUAL.md`)
2. Review the **Troubleshooting Guide** (`docs/TROUBLESHOOTING.md`)
3. Contact support: [Your contact method]

---

**Last Updated:** September 30, 2025  
**Version:** 1.0.0
