package dev.snowflakemcp.repository

import dev.snowflakemcp.db.tables.ConnectionsTable
import dev.snowflakemcp.model.SnowflakeConnection
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

class ConnectionRepository {
    fun findAll(): List<SnowflakeConnection> = transaction {
        ConnectionsTable.selectAll().orderBy(ConnectionsTable.name).map { it.toConnection() }
    }

    fun findById(id: Int): SnowflakeConnection? = transaction {
        ConnectionsTable.selectAll().where { ConnectionsTable.id eq id }.singleOrNull()?.toConnection()
    }

    fun findActive(): SnowflakeConnection? = transaction {
        ConnectionsTable.selectAll().where { ConnectionsTable.isActive eq true }.singleOrNull()?.toConnection()
    }

    fun create(conn: SnowflakeConnection): SnowflakeConnection = transaction {
        val now = LocalDateTime.now()
        val id = ConnectionsTable.insertAndGetId {
            it[name] = conn.name
            it[account] = conn.account
            it[user] = conn.user
            it[password] = conn.password
            it[role] = conn.role
            it[warehouse] = conn.warehouse
            it[database] = conn.database
            it[schema] = conn.schema
            it[isActive] = conn.isActive
            it[createdAt] = now
            it[updatedAt] = now
        }
        conn.copy(id = id.value, createdAt = now, updatedAt = now)
    }

    fun update(id: Int, conn: SnowflakeConnection): Boolean = transaction {
        ConnectionsTable.update({ ConnectionsTable.id eq id }) {
            it[name] = conn.name
            it[account] = conn.account
            it[user] = conn.user
            it[password] = conn.password
            it[role] = conn.role
            it[warehouse] = conn.warehouse
            it[database] = conn.database
            it[schema] = conn.schema
            it[updatedAt] = LocalDateTime.now()
        } > 0
    }

    fun delete(id: Int): Boolean = transaction {
        ConnectionsTable.deleteWhere { ConnectionsTable.id eq id } > 0
    }

    fun activate(id: Int): Boolean = transaction {
        ConnectionsTable.update { it[isActive] = false }
        ConnectionsTable.update({ ConnectionsTable.id eq id }) {
            it[isActive] = true
            it[updatedAt] = LocalDateTime.now()
        } > 0
    }

    private fun ResultRow.toConnection() = SnowflakeConnection(
        id = this[ConnectionsTable.id].value,
        name = this[ConnectionsTable.name],
        account = this[ConnectionsTable.account],
        user = this[ConnectionsTable.user],
        password = this[ConnectionsTable.password],
        role = this[ConnectionsTable.role],
        warehouse = this[ConnectionsTable.warehouse],
        database = this[ConnectionsTable.database],
        schema = this[ConnectionsTable.schema],
        isActive = this[ConnectionsTable.isActive],
        createdAt = this[ConnectionsTable.createdAt],
        updatedAt = this[ConnectionsTable.updatedAt],
    )
}
