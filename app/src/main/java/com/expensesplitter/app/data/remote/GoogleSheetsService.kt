package com.expensesplitter.app.data.remote

import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleSheetsService @Inject constructor(
    private val googleApiClient: GoogleApiClient
) {
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    
    /**
     * Create a new spreadsheet for a group
     */
    suspend fun createGroupSpreadsheet(groupName: String): Result<Spreadsheet> = withContext(Dispatchers.IO) {
        try {
            val service = googleApiClient.sheetsService
                ?: return@withContext Result.failure(Exception("Sheets service not initialized"))
            
            val spreadsheet = Spreadsheet().apply {
                properties = SpreadsheetProperties().apply {
                    title = "Group - $groupName - ${SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())}"
                }
                sheets = listOf(
                    Sheet().apply {
                        properties = SheetProperties().apply {
                            title = "Metadata"
                            sheetId = 0
                        }
                    }
                )
            }
            
            val result = service.spreadsheets().create(spreadsheet)
                .setFields("spreadsheetId,spreadsheetUrl,sheets.properties")
                .execute()
            
            // Initialize metadata sheet
            initializeMetadataSheet(result.spreadsheetId, groupName)
            
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Initialize metadata sheet with group information
     */
    private suspend fun initializeMetadataSheet(spreadsheetId: String, groupName: String) = withContext(Dispatchers.IO) {
        try {
            val service = googleApiClient.sheetsService ?: return@withContext
            
            val values = listOf(
                listOf("Group Name", groupName),
                listOf("Created Date", dateFormat.format(Date())),
                listOf("Created By", googleApiClient.getLastSignedInAccount()?.email ?: ""),
                listOf("Members", ""),
                listOf("Last Updated", dateFormat.format(Date()))
            )
            
            val body = ValueRange().setValues(values)
            
            service.spreadsheets().values()
                .update(spreadsheetId, "Metadata!A1:B5", body)
                .setValueInputOption("RAW")
                .execute()
        } catch (e: Exception) {
            // Log error
        }
    }
    
    /**
     * Create or get monthly expense sheet
     */
    suspend fun getOrCreateMonthlySheet(spreadsheetId: String, yearMonth: String): Result<Sheet> = withContext(Dispatchers.IO) {
        try {
            val service = googleApiClient.sheetsService
                ?: return@withContext Result.failure(Exception("Sheets service not initialized"))
            
            // Check if sheet exists
            val spreadsheet = service.spreadsheets().get(spreadsheetId).execute()
            val existingSheet = spreadsheet.sheets.find { it.properties.title == yearMonth }
            
            if (existingSheet != null) {
                return@withContext Result.success(existingSheet)
            }
            
            // Create new sheet
            val request = Request().apply {
                addSheet = AddSheetRequest().apply {
                    properties = SheetProperties().apply {
                        title = yearMonth
                    }
                }
            }
            
            val batchRequest = BatchUpdateSpreadsheetRequest().apply {
                requests = listOf(request)
            }
            
            val response = service.spreadsheets().batchUpdate(spreadsheetId, batchRequest).execute()
            val newSheet = response.replies[0].addSheet
            
            // Initialize headers
            initializeExpenseSheetHeaders(spreadsheetId, yearMonth)
            
            Result.success(Sheet().apply {
                properties = newSheet.properties
            })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Initialize expense sheet headers
     */
    private suspend fun initializeExpenseSheetHeaders(spreadsheetId: String, sheetName: String) = withContext(Dispatchers.IO) {
        try {
            val service = googleApiClient.sheetsService ?: return@withContext
            
            val headers = listOf(
                listOf(
                    "Expense ID", "Date", "Description", "Amount", "Currency", "Category",
                    "Paid By", "Split With", "Split Type", "Split Details", "Created By",
                    "Created At", "Last Edited By", "Last Edited At", "Notes", "Receipt URL",
                    "Tags", "Status", "Settled Date", "Settlement Notes"
                )
            )
            
            val body = ValueRange().setValues(headers)
            
            service.spreadsheets().values()
                .update(spreadsheetId, "$sheetName!A1:T1", body)
                .setValueInputOption("RAW")
                .execute()
            
            // Format header row (bold)
            val requests = listOf(
                Request().apply {
                    repeatCell = RepeatCellRequest().apply {
                        range = GridRange().apply {
                            sheetId = getSheetId(spreadsheetId, sheetName)
                            startRowIndex = 0
                            endRowIndex = 1
                        }
                        cell = CellData().apply {
                            userEnteredFormat = CellFormat().apply {
                                textFormat = TextFormat().apply {
                                    bold = true
                                }
                            }
                        }
                        fields = "userEnteredFormat.textFormat.bold"
                    }
                }
            )
            
            val batchRequest = BatchUpdateSpreadsheetRequest().apply {
                this.requests = requests
            }
            
            service.spreadsheets().batchUpdate(spreadsheetId, batchRequest).execute()
        } catch (e: Exception) {
            // Log error
        }
    }
    
    /**
     * Get sheet ID by name
     */
    private suspend fun getSheetId(spreadsheetId: String, sheetName: String): Int {
        val service = googleApiClient.sheetsService ?: return 0
        val spreadsheet = service.spreadsheets().get(spreadsheetId).execute()
        return spreadsheet.sheets.find { it.properties.title == sheetName }?.properties?.sheetId ?: 0
    }
    
    /**
     * Append expense row to sheet
     */
    suspend fun appendExpenseRow(
        spreadsheetId: String,
        sheetName: String,
        expenseData: List<Any>
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val service = googleApiClient.sheetsService
                ?: return@withContext Result.failure(Exception("Sheets service not initialized"))
            
            val body = ValueRange().setValues(listOf(expenseData))
            
            service.spreadsheets().values()
                .append(spreadsheetId, "$sheetName!A:T", body)
                .setValueInputOption("RAW")
                .setInsertDataOption("INSERT_ROWS")
                .execute()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Update expense row
     */
    suspend fun updateExpenseRow(
        spreadsheetId: String,
        sheetName: String,
        row: Int,
        expenseData: List<Any>
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val service = googleApiClient.sheetsService
                ?: return@withContext Result.failure(Exception("Sheets service not initialized"))
            
            val body = ValueRange().setValues(listOf(expenseData))
            
            service.spreadsheets().values()
                .update(spreadsheetId, "$sheetName!A$row:T$row", body)
                .setValueInputOption("RAW")
                .execute()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Find expense row by ID
     */
    suspend fun findExpenseRow(spreadsheetId: String, sheetName: String, expenseId: String): Int? = withContext(Dispatchers.IO) {
        try {
            val service = googleApiClient.sheetsService ?: return@withContext null
            
            val response = service.spreadsheets().values()
                .get(spreadsheetId, "$sheetName!A:A")
                .execute()
            
            val values = response.getValues() ?: return@withContext null
            
            values.forEachIndexed { index, row ->
                if (row.isNotEmpty() && row[0].toString() == expenseId) {
                    return@withContext index + 1 // 1-based index
                }
            }
            
            null
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Read all expenses from a sheet
     */
    suspend fun readExpensesFromSheet(
        spreadsheetId: String,
        sheetName: String
    ): Result<List<List<Any>>> = withContext(Dispatchers.IO) {
        try {
            val service = googleApiClient.sheetsService
                ?: return@withContext Result.failure(Exception("Sheets service not initialized"))
            
            val response = service.spreadsheets().values()
                .get(spreadsheetId, "$sheetName!A2:T") // Skip header row
                .execute()
            
            val values = response.getValues() ?: emptyList()
            Result.success(values)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
