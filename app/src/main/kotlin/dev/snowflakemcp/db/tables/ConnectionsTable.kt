package dev.snowflakemcp.db.tables

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.datetime

object ConnectionsTable : IntIdTable("connections") {
    val name = varchar("name", 255).uniqueIndex()
    val account = varchar("account", 255)
    val user = varchar("user", 255)
    val password = varchar("password", 512)
    val role = varchar("role", 255).nullable()
    val warehouse = varchar("warehouse", 255).nullable()
    val database = varchar("database", 255).nullable()
    val schema = varchar("schema", 255).nullable()
    val isActive = bool("is_active").default(false)
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}
