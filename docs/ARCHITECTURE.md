# Architecture Overview - Expense Splitter

This document provides a comprehensive overview of the application's architecture, design patterns, and component interactions.

---

## Table of Contents

1. [High-Level Architecture](#high-level-architecture)
2. [Layered Architecture](#layered-architecture)
3. [Component Diagram](#component-diagram)
4. [Data Flow](#data-flow)
5. [Sync Architecture](#sync-architecture)
6. [Database Schema](#database-schema)
7. [Dependency Graph](#dependency-graph)

---

## High-Level Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                      User Interface                          │
│                   (Jetpack Compose)                          │
└───────────────────────┬─────────────────────────────────────┘
                        │
┌───────────────────────▼─────────────────────────────────────┐
│                   Presentation Layer                         │
│              (ViewModels, UI State)                          │
└───────────────────────┬─────────────────────────────────────┘
                        │
┌───────────────────────▼─────────────────────────────────────┐
│                    Domain Layer                              │
│             (Use Cases, Business Logic)                      │
└───────────────────────┬─────────────────────────────────────┘
                        │
┌───────────────────────▼─────────────────────────────────────┐
│                     Data Layer                               │
│  ┌─────────────────┐     ┌────────────────────────────┐    │
│  │  Local Storage  │     │    Remote Data Sources     │    │
│  │  (Room + Crypto)│     │  (Google Sheets & Drive)   │    │
│  └─────────────────┘     └────────────────────────────┘    │
└─────────────────────────────────────────────────────────────┘
```

---

## Layered Architecture

### 1. Presentation Layer

**Responsibility:** UI rendering and user interaction

**Components:**
```
presentation/
├── auth/
│   ├── AuthScreen.kt        (Compose UI)
│   └── AuthViewModel.kt     (State management)
├── dashboard/
│   ├── DashboardScreen.kt
│   └── DashboardViewModel.kt
├── expense/
│   ├── ExpenseListScreen.kt
│   ├── ExpenseDetailScreen.kt
│   ├── AddEditExpenseScreen.kt
│   └── ExpenseViewModel.kt
├── analytics/
│   ├── AnalyticsScreen.kt
│   └── AnalyticsViewModel.kt
├── group/
│   ├── GroupListScreen.kt
│   └── GroupViewModel.kt
├── settings/
│   ├── SettingsScreen.kt
│   └── SettingsViewModel.kt
└── common/
    ├── LoadingIndicator.kt
    ├── ErrorMessage.kt
    └── EmptyState.kt
```

**Key Characteristics:**
- ✅ Pure UI logic only
- ✅ No business logic
- ✅ Observes StateFlow from ViewModels
- ✅ Emits user events to ViewModels

### 2. Domain Layer

**Responsibility:** Business logic and rules

**Components:**
```
domain/
├── model/
│   ├── Expense.kt          (Domain models)
│   ├── Group.kt
│   ├── User.kt
│   └── ...
├── usecase/
│   ├── expense/
│   │   ├── AddExpenseUseCase.kt
│   │   ├── EditExpenseUseCase.kt
│   │   ├── DeleteExpenseUseCase.kt
│   │   └── GetExpensesUseCase.kt
│   ├── settlement/
│   │   ├── SettleExpenseUseCase.kt
│   │   └── CalculateBalanceUseCase.kt
│   ├── sync/
│   │   ├── SyncExpensesUseCase.kt
│   │   └── HandleConflictUseCase.kt
│   └── analytics/
│       ├── GetMonthlyStatsUseCase.kt
│       └── GetCategoryBreakdownUseCase.kt
└── repository/
    ├── ExpenseRepository.kt    (Interface)
    ├── GroupRepository.kt
    ├── UserRepository.kt
    └── SyncRepository.kt
```

**Key Characteristics:**
- ✅ Framework-agnostic
- ✅ Testable business logic
- ✅ Single Responsibility Principle
- ✅ Dependency Inversion (interfaces)

### 3. Data Layer

**Responsibility:** Data operations and persistence

**Components:**
```
data/
├── local/
│   ├── dao/
│   │   ├── ExpenseDao.kt
│   │   ├── GroupDao.kt
│   │   └── ...
│   ├── entity/
│   │   ├── ExpenseEntity.kt
│   │   ├── GroupEntity.kt
│   │   └── ...
│   └── ExpenseSplitterDatabase.kt
├── remote/
│   ├── GoogleApiClient.kt
│   ├── GoogleSheetsService.kt
│   └── GoogleDriveService.kt
├── repository/
│   ├── ExpenseRepositoryImpl.kt
│   ├── GroupRepositoryImpl.kt
│   └── SyncRepositoryImpl.kt
└── sync/
    ├── SyncWorker.kt
    └── ConflictResolver.kt
```

**Key Characteristics:**
- ✅ Single source of truth (Room DB)
- ✅ Offline-first approach
- ✅ Repository pattern
- ✅ Data mapping (Entity ↔ Domain)

---

## Component Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                    Compose UI (Views)                        │
└──────────┬──────────────────────────────────────────────────┘
           │ observes StateFlow
           │ emits events
┌──────────▼──────────────────────────────────────────────────┐
│                     ViewModels                               │
│  - AuthViewModel                                             │
│  - DashboardViewModel                                        │
│  - ExpenseViewModel                                          │
│  - etc.                                                      │
└──────────┬──────────────────────────────────────────────────┘
           │ calls use cases
┌──────────▼──────────────────────────────────────────────────┐
│                      Use Cases                               │
│  - AddExpenseUseCase                                         │
│  - GetExpensesUseCase                                        │
│  - SyncExpensesUseCase                                       │
│  - etc.                                                      │
└──────────┬──────────────────────────────────────────────────┘
           │ depends on repositories (interface)
┌──────────▼──────────────────────────────────────────────────┐
│                    Repositories                              │
│  - ExpenseRepository (impl)                                  │
│  - GroupRepository (impl)                                    │
│  - SyncRepository (impl)                                     │
└──────┬────────────────────────────────┬─────────────────────┘
       │                                │
┌──────▼──────────┐            ┌────────▼────────────────────┐
│   Room Database │            │   Google APIs               │
│  (SQLCipher)    │            │  - Sheets Service           │
│  - DAOs         │            │  - Drive Service            │
│  - Entities     │            │  - API Client               │
└─────────────────┘            └─────────────────────────────┘
```

---

## Data Flow

### 1. User Adds Expense

```
1. User fills expense form
   ↓
2. AddExpenseScreen emits event → ExpenseViewModel
   ↓
3. ExpenseViewModel calls → AddExpenseUseCase
   ↓
4. AddExpenseUseCase validates → ExpenseRepository.addExpense()
   ↓
5. ExpenseRepository:
   - Saves to Room DB (instant)
   - Adds to SyncQueue
   ↓
6. Background SyncWorker:
   - Picks up from SyncQueue
   - Syncs to Google Sheets
   - Updates sync status
   ↓
7. UI observes updated StateFlow → Shows "Synced"
```

### 2. User Views Expenses

```
1. ExpenseListScreen launched
   ↓
2. ExpenseViewModel.init() → GetExpensesUseCase
   ↓
3. GetExpensesUseCase → ExpenseRepository.getExpenses()
   ↓
4. ExpenseRepository queries Room DB (Flow)
   ↓
5. Room DB emits updates automatically
   ↓
6. Data flows through:
   Entity → Domain Model → UI State
   ↓
7. UI observes StateFlow → Renders list
```

### 3. Background Sync

```
1. WorkManager schedules periodic sync (every 15 min)
   ↓
2. SyncWorker wakes up
   ↓
3. Queries SyncQueue for pending items
   ↓
4. For each pending item:
   - Calls GoogleSheetsService
   - Updates/Creates rows in Google Sheets
   - Handles errors with retry logic
   - Marks as synced or failed
   ↓
5. Updates sync status in Room DB
   ↓
6. UI automatically reflects sync status
```

---

## Sync Architecture

### Offline-First Approach

```
┌──────────────────────────────────────────────────────────────┐
│                    User Action (e.g., Add Expense)           │
└────────────────────────┬─────────────────────────────────────┘
                         │
                   ┌─────▼─────┐
                   │  Room DB  │ ◄── Single Source of Truth
                   │  (Instant)│
                   └─────┬─────┘
                         │
                   ┌─────▼─────────┐
                   │  Sync Queue   │ ◄── Queue for background sync
                   └─────┬─────────┘
                         │
                  ┌──────▼──────┐
                  │ Is Online?  │
                  └──────┬──────┘
                    Yes  │  No
              ┌──────────┴──────────┐
              │                     │
      ┌───────▼──────┐      ┌──────▼──────┐
      │ Sync Worker  │      │ Wait for    │
      │ (Background) │      │ Connection  │
      └───────┬──────┘      └─────────────┘
              │
      ┌───────▼──────────┐
      │ Google Sheets    │
      │ Update           │
      └───────┬──────────┘
              │
      ┌───────▼──────────┐
      │ Update Sync      │
      │ Status in DB     │
      └──────────────────┘
```

### Conflict Resolution Strategy

**Last-Write-Wins (LWW):**

```
Device A                    Google Sheets           Device B
   │                              │                     │
   ├─Edit Expense @ 10:00───────►│                     │
   │                              │◄────Edit Expense────┤
   │                              │     @ 10:05         │
   │                              │                     │
   │                       Compare Timestamps           │
   │                              │                     │
   │                       10:05 > 10:00                │
   │                       Keep Device B's version      │
   │                              │                     │
   │◄─────Sync Device B's────────┤                     │
   │      version                 │                     │
```

**Conflict Notification:**
- User notified of conflict
- Can view both versions (future enhancement)
- Manual merge option (future enhancement)

---

## Database Schema

### Entity Relationships

```
┌─────────────┐
│ UserEntity  │
└──────┬──────┘
       │
       │ created_by
       ▼
┌─────────────┐      1:N      ┌──────────────┐
│ GroupEntity ├───────────────►│ExpenseEntity │
└──────┬──────┘                └──────┬───────┘
       │                              │
       │ 1:N                          │ 1:N
       │                              │
       ▼                              ▼
┌──────────────┐              ┌──────────────────┐
│CategoryEntity│              │ExpenseSplitEntity│
└──────────────┘              └──────────────────┘
       ▲
       │ N:1
       │
┌──────┴───────┐
│ExpenseEntity │
└──────────────┘

┌─────────────┐      1:N      ┌─────────────────┐
│ GroupEntity ├───────────────►│SettlementEntity │
└─────────────┘                └─────────────────┘

┌─────────────┐
│SyncQueueEntity│ ◄── Independent
└─────────────┘

┌─────────────┐
│AuditLogEntity│ ◄── Independent
└─────────────┘
```

### Key Indexes

```sql
-- Expenses
INDEX idx_expense_group ON expenses(groupId)
INDEX idx_expense_date ON expenses(date)
INDEX idx_expense_category ON expenses(categoryId)
INDEX idx_expense_status ON expenses(status)

-- Expense Splits
INDEX idx_split_expense ON expense_splits(expenseId)
INDEX idx_split_user ON expense_splits(userId)

-- Settlements
INDEX idx_settlement_group ON settlements(groupId)
INDEX idx_settlement_from ON settlements(fromUser)
INDEX idx_settlement_to ON settlements(toUser)
INDEX idx_settlement_date ON settlements(date)

-- Audit Log
INDEX idx_audit_user ON audit_log(userId)
INDEX idx_audit_timestamp ON audit_log(timestamp)
INDEX idx_audit_entity ON audit_log(entityType)
```

---

## Dependency Graph

### Hilt Dependency Injection

```
┌──────────────────────────────────────────────┐
│      SingletonComponent (Application)        │
└────────────────┬─────────────────────────────┘
                 │
        ┌────────┴─────────┐
        │                  │
┌───────▼───────┐   ┌──────▼──────────┐
│DatabaseModule │   │ NetworkModule   │
└───────┬───────┘   └──────┬──────────┘
        │                  │
   Provides:          Provides:
   - Database         - GoogleApiClient
   - All DAOs         - SheetsService
                      - DriveService
        │                  │
        └─────────┬────────┘
                  │
         ┌────────▼─────────┐
         │RepositoryModule  │
         └────────┬─────────┘
                  │
            Provides:
            - Repositories
                  │
         ┌────────▼──────────┐
         │  UseCaseModule    │
         └────────┬──────────┘
                  │
            Provides:
            - Use Cases
                  │
         ┌────────▼──────────┐
         │   ViewModels      │
         │  (Hilt injected)  │
         └───────────────────┘
```

### Module Dependencies

```
App Module
  ├─► DatabaseModule
  │     ├─► Context
  │     └─► Provides: Database, DAOs
  │
  ├─► NetworkModule
  │     ├─► Context
  │     └─► Provides: API Clients
  │
  ├─► DataStoreModule
  │     ├─► Context
  │     └─► Provides: DataStore
  │
  ├─► RepositoryModule
  │     ├─► DAOs
  │     ├─► API Clients
  │     └─► Provides: Repositories
  │
  └─► ViewModelModule
        ├─► Repositories
        ├─► Use Cases
        └─► Provides: ViewModels
```

---

## Design Patterns Used

### 1. Repository Pattern
**Purpose:** Abstract data sources  
**Implementation:** Repository interfaces in domain, implementations in data

### 2. Use Case Pattern
**Purpose:** Encapsulate business logic  
**Implementation:** One use case per business operation

### 3. Observer Pattern
**Purpose:** Reactive UI updates  
**Implementation:** Kotlin Flow / StateFlow

### 4. Factory Pattern
**Purpose:** Object creation  
**Implementation:** Hilt modules, ViewModelFactory

### 5. Strategy Pattern
**Purpose:** Split calculation algorithms  
**Implementation:** Different split type strategies

### 6. Singleton Pattern
**Purpose:** Single instance for services  
**Implementation:** Hilt @Singleton scope

### 7. Adapter Pattern
**Purpose:** Data mapping  
**Implementation:** Entity ↔ Domain model mappers

---

## State Management

### ViewModel State Pattern

```kotlin
data class ExpenseUiState(
    val expenses: List<Expense> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val syncStatus: SyncStatus = SyncStatus.SYNCED
)

class ExpenseViewModel @Inject constructor(
    private val getExpensesUseCase: GetExpensesUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ExpenseUiState())
    val uiState: StateFlow<ExpenseUiState> = _uiState.asStateFlow()
    
    // UI updates automatically when state changes
}
```

---

## Navigation Architecture

```
NavHost
  ├─► auth (Start destination)
  │     └─► On success → dashboard
  │
  ├─► dashboard
  │     ├─► FAB → addExpense
  │     ├─► Item click → expenseDetail/{id}
  │     └─► Bottom Nav:
  │           ├─► expenses
  │           ├─► analytics
  │           ├─► groups
  │           └─► settings
  │
  ├─► expenses
  │     ├─► Item click → expenseDetail/{id}
  │     └─► FAB → addExpense
  │
  ├─► expenseDetail/{id}
  │     ├─► Edit → editExpense/{id}
  │     └─► Delete → back to list
  │
  ├─► addExpense
  │     └─► Save → back with result
  │
  ├─► editExpense/{id}
  │     └─► Save → back with result
  │
  ├─► analytics
  │     └─► Tab navigation
  │
  ├─► groups
  │     ├─► Item click → switch group
  │     └─► FAB → createGroup
  │
  └─► settings
        ├─► Profile
        ├─► Preferences
        ├─► Categories
        └─► Data Export
```

---

## Performance Considerations

### Database Optimization
- ✅ Proper indexing on frequently queried columns
- ✅ Lazy loading with pagination
- ✅ Efficient queries (avoid N+1 problems)
- ✅ Batch operations for multiple inserts

### Network Optimization
- ✅ Batch API calls to Google Sheets
- ✅ Compress images before upload
- ✅ Cache responses when appropriate
- ✅ Handle rate limiting

### UI Optimization
- ✅ LazyColumn for lists
- ✅ Remember and derivedStateOf for computed values
- ✅ Avoid unnecessary recomposition
- ✅ Image loading with Coil

---

## Security Architecture

```
┌──────────────────────────────────────────────┐
│          User Authentication                  │
│  (Google OAuth 2.0)                          │
└────────────────┬─────────────────────────────┘
                 │
        ┌────────▼─────────┐
        │ Android Keystore │ ◄── Secure token storage
        └────────┬─────────┘
                 │
        ┌────────▼─────────┐
        │  Encrypted DB    │ ◄── SQLCipher
        │  (Local Data)    │
        └────────┬─────────┘
                 │
        ┌────────▼─────────┐
        │   HTTPS Only     │ ◄── Secure communication
        │  (API Calls)     │
        └────────┬─────────┘
                 │
        ┌────────▼─────────┐
        │ Google Drive     │ ◄── User's private storage
        │ (Cloud Backup)   │
        └──────────────────┘
```

---

## Scalability Considerations

### Current Limitations
- 2 users per group
- Single device sync
- Manual conflict resolution

### Future Scalability
- **Multi-user groups**: Extend split calculations
- **Real-time sync**: Add Firebase for instant updates
- **Web app**: Share backend logic
- **Multiple devices**: Improve conflict resolution

---

## Testing Strategy

```
┌──────────────────────────────────────────────┐
│            Unit Tests (70% coverage)         │
│  - ViewModels                                │
│  - Use Cases                                 │
│  - Repositories                              │
│  - Utilities                                 │
└────────────────┬─────────────────────────────┘
                 │
┌────────────────▼─────────────────────────────┐
│         Integration Tests                    │
│  - Database operations                       │
│  - API integration                           │
│  - Sync logic                                │
└────────────────┬─────────────────────────────┘
                 │
┌────────────────▼─────────────────────────────┐
│              UI Tests                        │
│  - Critical user flows                       │
│  - Compose UI testing                        │
│  - Navigation testing                        │
└──────────────────────────────────────────────┘
```

---

## Summary

This architecture provides:

✅ **Separation of Concerns**: Clear layer boundaries  
✅ **Testability**: Each component testable in isolation  
✅ **Scalability**: Easy to extend and maintain  
✅ **Offline-First**: Works without internet  
✅ **Type Safety**: Compile-time guarantees  
✅ **Security**: Multiple layers of protection  
✅ **Performance**: Optimized at every layer  

---

**This architecture follows Android best practices and modern development patterns.**
