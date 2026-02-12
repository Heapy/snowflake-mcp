package dev.snowflakemcp.db.tables

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.datetime

object QueryHistoryTable : IntIdTable("query_history") {
    val connectionName = varchar("connection_name", 255)
    val sql = text("sql")
    val snowflakeQueryId = varchar("snowflake_query_id", 255).nullable()
    val status = varchar("status", 50) // SUCCESS, ERROR, BLOCKED
    val errorMessage = text("error_message").nullable()
    val rowCount = integer("row_count").nullable()
    val durationMs = long("duration_ms").nullable()
    val executedAt = datetime("executed_at")
}
