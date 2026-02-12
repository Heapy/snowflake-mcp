package dev.snowflakemcp.model

import java.time.LocalDateTime

data class QueryRecord(
    val id: Int = 0,
    val connectionName: String,
    val sql: String,
    val snowflakeQueryId: String? = null,
    val status: String, // SUCCESS, ERROR, BLOCKED
    val errorMessage: String? = null,
    val rowCount: Int? = null,
    val durationMs: Long? = null,
    val executedAt: LocalDateTime = LocalDateTime.now(),
)
