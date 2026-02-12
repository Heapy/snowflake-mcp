package dev.snowflakemcp.repository

import dev.snowflakemcp.db.tables.SettingsTable
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

class SettingsRepository {
    fun get(key: String): String? = transaction {
        SettingsTable.selectAll().where { SettingsTable.key eq key }
            .singleOrNull()?.get(SettingsTable.value)
    }

    fun set(key: String, value: String): Unit = transaction {
        val exists = SettingsTable.selectAll().where { SettingsTable.key eq key }.count() > 0
        if (exists) {
            SettingsTable.update({ SettingsTable.key eq key }) {
                it[SettingsTable.value] = value
            }
        } else {
            SettingsTable.insert {
                it[SettingsTable.key] = key
                it[SettingsTable.value] = value
            }
        }
    }

    fun isReadOnly(): Boolean = get("read_only_mode")?.toBoolean() ?: true

    fun setReadOnly(enabled: Boolean) = set("read_only_mode", enabled.toString())
}
