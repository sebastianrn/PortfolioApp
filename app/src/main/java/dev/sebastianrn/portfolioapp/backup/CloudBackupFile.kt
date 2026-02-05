package dev.sebastianrn.portfolioapp.backup

data class CloudBackupFile(
    val id: String,
    val name: String,
    val size: Long,
    val createdTime: Long,
    val modifiedTime: Long
)
