package com.expensesplitter.app.data.model

/**
 * Data class to hold member information
 */
data class MemberData(
    val email: String = "",
    val firstName: String = "",
    val googleId: String = "" // Numeric Google ID
) {
    fun isValid(): Boolean {
        return email.isNotBlank() && firstName.isNotBlank()
    }
    
    fun toDisplayString(): String {
        return "$firstName <$email>"
    }
    
    fun toStorageString(): String {
        // Format: email|firstName|googleId
        return "$email|$firstName|$googleId"
    }
    
    companion object {
        fun fromStorageString(value: String): MemberData {
            val parts = value.split("|")
            return MemberData(
                email = parts.getOrNull(0) ?: "",
                firstName = parts.getOrNull(1) ?: "",
                googleId = parts.getOrNull(2) ?: ""
            )
        }
    }
}
