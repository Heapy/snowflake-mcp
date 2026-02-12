package dev.snowflakemcp.repository

import dev.snowflakemcp.TestDatabase
import dev.snowflakemcp.model.QueryRecord
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class QueryHistoryRepositoryTest {
    private val repo = QueryHistoryRepository()

    @BeforeEach
    fun setUp() {
        TestDatabase.init()
    }

    @Test
    fun `add and retrieve query record`() {
        val record = repo.add(
            QueryRecord(
                connectionName = "test-conn",
                sql = "SELECT 1",
                status = "SUCCESS",
                rowCount = 1,
                durationMs = 42,
            )
        )
        assertTrue(record.id > 0)

        val all = repo.findAll()
        assertEquals(1, all.size)
        assertEquals("SELECT 1", all[0].sql)
        assertEquals("SUCCESS", all[0].status)
        assertEquals(1, all[0].rowCount)
        assertEquals(42, all[0].durationMs)
    }

    @Test
    fun `records are ordered by executed_at descending`() {
        repo.add(QueryRecord(connectionName = "c", sql = "SELECT 1", status = "SUCCESS"))
        Thread.sleep(10)
        repo.add(QueryRecord(connectionName = "c", sql = "SELECT 2", status = "SUCCESS"))

        val all = repo.findAll()
        assertEquals("SELECT 2", all[0].sql)
        assertEquals("SELECT 1", all[1].sql)
    }

    @Test
    fun `count returns total records`() {
        assertEquals(0, repo.count())
        repo.add(QueryRecord(connectionName = "c", sql = "SELECT 1", status = "SUCCESS"))
        repo.add(QueryRecord(connectionName = "c", sql = "SELECT 2", status = "ERROR"))
        assertEquals(2, repo.count())
    }

    @Test
    fun `pagination works`() {
        repeat(25) { i ->
            repo.add(QueryRecord(connectionName = "c", sql = "SELECT $i", status = "SUCCESS"))
        }

        val page1 = repo.findAll(page = 1)
        assertEquals(20, page1.size)

        val page2 = repo.findAll(page = 2)
        assertEquals(5, page2.size)
    }

    @Test
    fun `blocked queries are recorded`() {
        repo.add(
            QueryRecord(
                connectionName = "c",
                sql = "DROP TABLE users",
                status = "BLOCKED",
                errorMessage = "Query blocked by read-only mode",
            )
        )

        val all = repo.findAll()
        assertEquals(1, all.size)
        assertEquals("BLOCKED", all[0].status)
        assertEquals("Query blocked by read-only mode", all[0].errorMessage)
    }
}
