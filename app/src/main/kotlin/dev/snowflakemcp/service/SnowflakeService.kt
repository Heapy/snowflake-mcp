package dev.snowflakemcp.service

import dev.snowflakemcp.model.QueryRecord
import dev.snowflakemcp.model.SnowflakeConnection
import dev.snowflakemcp.repository.ConnectionRepository
import dev.snowflakemcp.repository.QueryHistoryRepository
import dev.snowflakemcp.repository.SettingsRepository
import net.snowflake.client.api.statement.SnowflakeStatement
import java.sql.Connection
import java.sql.DriverManager
import java.util.Properties

class SnowflakeService(
    private val connectionRepo: ConnectionRepository,
    private val queryHistoryRepo: QueryHistoryRepository,
    private val settingsRepo: SettingsRepository,
) {
    private var cachedConnection: Connection? = null
    private var cachedConnectionId: Int? = null

    fun executeSql(sql: String): QueryResult {
        val activeConn = connectionRepo.findActive()
            ?: return QueryResult.error("No active connection configured")

        if (settingsRepo.isReadOnly() && SqlGuard.isMutation(sql)) {
            queryHistoryRepo.add(
                QueryRecord(
                    connectionName = activeConn.name,
                    sql = sql,
                    status = "BLOCKED",
                    errorMessage = "Query blocked by read-only mode",
                )
            )
            return QueryResult.error("Query blocked: read-only mode is enabled. Mutation queries are not allowed.")
        }

        val startTime = System.currentTimeMillis()
        return try {
            val jdbc = getOrCreateConnection(activeConn)
            val stmt = jdbc.createStatement()
            val hasResultSet = stmt.execute(sql)
            val durationMs = System.currentTimeMillis() - startTime

            val snowflakeQueryId = try {
                stmt.unwrap(SnowflakeStatement::class.java).getQueryID()
            } catch (_: Exception) {
                null
            }

            if (hasResultSet) {
                val rs = stmt.resultSet
                val meta = rs.metaData
                val colCount = meta.columnCount
                val columns = (1..colCount).map { meta.getColumnLabel(it) }
                val rows = mutableListOf<List<String>>()
                while (rs.next()) {
                    rows.add((1..colCount).map { rs.getString(it) ?: "NULL" })
                }
                rs.close()
                stmt.close()

                queryHistoryRepo.add(
                    QueryRecord(
                        connectionName = activeConn.name,
                        sql = sql,
                        snowflakeQueryId = snowflakeQueryId,
                        status = "SUCCESS",
                        rowCount = rows.size,
                        durationMs = durationMs,
                    )
                )

                QueryResult.success(formatMarkdownTable(columns, rows), rows.size, snowflakeQueryId)
            } else {
                val updateCount = stmt.updateCount
                stmt.close()

                queryHistoryRepo.add(
                    QueryRecord(
                        connectionName = activeConn.name,
                        sql = sql,
                        snowflakeQueryId = snowflakeQueryId,
                        status = "SUCCESS",
                        rowCount = updateCount,
                        durationMs = durationMs,
                    )
                )

                QueryResult.success(
                    "Statement executed successfully. Rows affected: $updateCount",
                    updateCount,
                    snowflakeQueryId,
                )
            }
        } catch (e: Exception) {
            val durationMs = System.currentTimeMillis() - startTime
            queryHistoryRepo.add(
                QueryRecord(
                    connectionName = activeConn.name,
                    sql = sql,
                    status = "ERROR",
                    errorMessage = e.message,
                    durationMs = durationMs,
                )
            )
            QueryResult.error("Query failed: ${e.message}")
        }
    }

    fun testConnection(conn: SnowflakeConnection): String {
        return try {
            val jdbc = createJdbcConnection(conn)
            val stmt = jdbc.createStatement()
            val rs = stmt.executeQuery("SELECT CURRENT_VERSION()")
            val version = if (rs.next()) rs.getString(1) else "unknown"
            rs.close()
            stmt.close()
            jdbc.close()
            "Connected successfully. Snowflake version: $version"
        } catch (e: Exception) {
            "Connection failed: ${e.message}"
        }
    }

    @Synchronized
    private fun getOrCreateConnection(conn: SnowflakeConnection): Connection {
        val existing = cachedConnection
        if (existing != null && cachedConnectionId == conn.id && existing.isValid(5)) {
            return existing
        }
        existing?.close()
        val jdbc = createJdbcConnection(conn)
        cachedConnection = jdbc
        cachedConnectionId = conn.id
        return jdbc
    }

    private fun createJdbcConnection(conn: SnowflakeConnection): Connection {
        val props = Properties().apply {
            setProperty("user", conn.user)
            setProperty("password", conn.password)
            conn.role?.let { setProperty("role", it) }
            conn.warehouse?.let { setProperty("warehouse", it) }
            conn.database?.let { setProperty("db", it) }
            conn.schema?.let { setProperty("schema", it) }
        }
        val url = "jdbc:snowflake://${conn.account}.snowflakecomputing.com"
        return DriverManager.getConnection(url, props)
    }

    private fun formatMarkdownTable(columns: List<String>, rows: List<List<String>>): String {
        if (columns.isEmpty()) return "No columns returned."
        if (rows.isEmpty()) {
            return "Query returned 0 rows.\n\n| ${columns.joinToString(" | ")} |\n| ${columns.joinToString(" | ") { "---" }} |"
        }

        val sb = StringBuilder()
        sb.appendLine("| ${columns.joinToString(" | ")} |")
        sb.appendLine("| ${columns.joinToString(" | ") { "---" }} |")
        rows.forEach { row ->
            sb.appendLine("| ${row.joinToString(" | ")} |")
        }
        sb.appendLine("\n${rows.size} row(s) returned.")
        return sb.toString()
    }

    data class QueryResult(
        val output: String,
        val isError: Boolean,
        val rowCount: Int? = null,
        val queryId: String? = null,
    ) {
        companion object {
            fun success(output: String, rowCount: Int? = null, queryId: String? = null) =
                QueryResult(output, isError = false, rowCount = rowCount, queryId = queryId)

            fun error(message: String) = QueryResult(message, isError = true)
        }
    }
}
