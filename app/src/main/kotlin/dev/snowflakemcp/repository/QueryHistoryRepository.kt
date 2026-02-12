package dev.snowflakemcp.repository

import dev.snowflakemcp.db.tables.QueryHistoryTable
import dev.snowflakemcp.model.QueryRecord
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

class QueryHistoryRepository {
    companion object {
        const val PAGE_SIZE = 20
    }

    fun add(record: QueryRecord): QueryRecord = transaction {
        val id = QueryHistoryTable.insertAndGetId {
            it[connectionName] = record.connectionName
            it[sql] = record.sql
            it[snowflakeQueryId] = record.snowflakeQueryId
            it[status] = record.status
            it[errorMessage] = record.errorMessage
            it[rowCount] = record.rowCount
            it[durationMs] = record.durationMs
            it[executedAt] = LocalDateTime.now()
        }
        record.copy(id = id.value)
    }

    fun findAll(page: Int = 1): List<QueryRecord> = transaction {
        QueryHistoryTable.selectAll()
            .orderBy(QueryHistoryTable.executedAt, SortOrder.DESC)
            .limit(PAGE_SIZE)
            .offset(((page - 1) * PAGE_SIZE).toLong())
            .map { it.toQueryRecord() }
    }

    fun count(): Long = transaction {
        QueryHistoryTable.selectAll().count()
    }

    private fun ResultRow.toQueryRecord() = QueryRecord(
        id = this[QueryHistoryTable.id].value,
        connectionName = this[QueryHistoryTable.connectionName],
        sql = this[QueryHistoryTable.sql],
        snowflakeQueryId = this[QueryHistoryTable.snowflakeQueryId],
        status = this[QueryHistoryTable.status],
        errorMessage = this[QueryHistoryTable.errorMessage],
        rowCount = this[QueryHistoryTable.rowCount],
        durationMs = this[QueryHistoryTable.durationMs],
        executedAt = this[QueryHistoryTable.executedAt],
    )
}
