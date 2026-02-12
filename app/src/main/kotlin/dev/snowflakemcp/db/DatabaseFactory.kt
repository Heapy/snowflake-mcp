package dev.snowflakemcp.db

import dev.snowflakemcp.db.tables.ConnectionsTable
import dev.snowflakemcp.db.tables.QueryHistoryTable
import dev.snowflakemcp.db.tables.SettingsTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.DriverManager

object DatabaseFactory {
    fun init(dbPath: String = "snowflake-mcp.db") {
        val url = "jdbc:sqlite:$dbPath"

        // WAL mode must be set outside a transaction
        DriverManager.getConnection(url).use { conn ->
            conn.createStatement().execute("PRAGMA journal_mode=WAL")
        }

        Database.connect(url, driver = "org.sqlite.JDBC")

        transaction {
            SchemaUtils.create(ConnectionsTable, QueryHistoryTable, SettingsTable)

            // Seed default settings
            if (SettingsTable.selectAll().where { SettingsTable.key eq "read_only_mode" }.count() == 0L) {
                SettingsTable.insert {
                    it[key] = "read_only_mode"
                    it[value] = "true"
                }
            }
        }
    }
}
