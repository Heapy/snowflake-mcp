package dev.snowflakemcp.repository

import dev.snowflakemcp.TestDatabase
import dev.snowflakemcp.model.SnowflakeConnection
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ConnectionRepositoryTest {
    private val repo = ConnectionRepository()

    @BeforeEach
    fun setUp() {
        TestDatabase.init()
    }

    private fun testConnection(name: String = "test") = SnowflakeConnection(
        name = name,
        account = "xy12345.us-east-1",
        user = "testuser",
        password = "testpass",
        role = "SYSADMIN",
        warehouse = "COMPUTE_WH",
    )

    @Test
    fun `create and find connection`() {
        val created = repo.create(testConnection())
        assertTrue(created.id > 0)

        val found = repo.findById(created.id)
        assertNotNull(found)
        assertEquals("test", found!!.name)
        assertEquals("xy12345.us-east-1", found.account)
        assertEquals("testuser", found.user)
    }

    @Test
    fun `find all connections`() {
        repo.create(testConnection("alpha"))
        repo.create(testConnection("beta"))

        val all = repo.findAll()
        assertEquals(2, all.size)
        assertEquals("alpha", all[0].name) // ordered by name
        assertEquals("beta", all[1].name)
    }

    @Test
    fun `update connection`() {
        val created = repo.create(testConnection())
        val updated = created.copy(account = "new-account.us-west-2")
        assertTrue(repo.update(created.id, updated))

        val found = repo.findById(created.id)
        assertEquals("new-account.us-west-2", found!!.account)
    }

    @Test
    fun `delete connection`() {
        val created = repo.create(testConnection())
        assertTrue(repo.delete(created.id))
        assertNull(repo.findById(created.id))
    }

    @Test
    fun `activate connection deactivates others`() {
        val c1 = repo.create(testConnection("conn1"))
        val c2 = repo.create(testConnection("conn2"))

        repo.activate(c1.id)
        assertTrue(repo.findById(c1.id)!!.isActive)
        assertFalse(repo.findById(c2.id)!!.isActive)

        repo.activate(c2.id)
        assertFalse(repo.findById(c1.id)!!.isActive)
        assertTrue(repo.findById(c2.id)!!.isActive)
    }

    @Test
    fun `find active connection`() {
        val c1 = repo.create(testConnection("conn1"))
        assertNull(repo.findActive())

        repo.activate(c1.id)
        val active = repo.findActive()
        assertNotNull(active)
        assertEquals("conn1", active!!.name)
    }
}
