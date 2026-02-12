package dev.snowflakemcp.repository

import dev.snowflakemcp.TestDatabase
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SettingsRepositoryTest {
    private val repo = SettingsRepository()

    @BeforeEach
    fun setUp() {
        TestDatabase.init()
    }

    @Test
    fun `default read-only mode is true`() {
        assertTrue(repo.isReadOnly())
    }

    @Test
    fun `set and get value`() {
        repo.set("test_key", "test_value")
        assertEquals("test_value", repo.get("test_key"))
    }

    @Test
    fun `update existing value`() {
        repo.set("test_key", "value1")
        repo.set("test_key", "value2")
        assertEquals("value2", repo.get("test_key"))
    }

    @Test
    fun `toggle read-only mode`() {
        assertTrue(repo.isReadOnly())

        repo.setReadOnly(false)
        assertFalse(repo.isReadOnly())

        repo.setReadOnly(true)
        assertTrue(repo.isReadOnly())
    }

    @Test
    fun `get returns null for missing key`() {
        assertNull(repo.get("nonexistent"))
    }
}
