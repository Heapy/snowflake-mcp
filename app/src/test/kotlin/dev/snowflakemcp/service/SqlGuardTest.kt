package dev.snowflakemcp.service

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class SqlGuardTest {
    @Test
    fun `SELECT queries are allowed`() {
        assertFalse(SqlGuard.isMutation("SELECT * FROM users"))
        assertFalse(SqlGuard.isMutation("select count(*) from orders"))
        assertFalse(SqlGuard.isMutation("  SELECT 1"))
    }

    @Test
    fun `SHOW and DESCRIBE are allowed`() {
        assertFalse(SqlGuard.isMutation("SHOW TABLES"))
        assertFalse(SqlGuard.isMutation("DESCRIBE TABLE users"))
        assertFalse(SqlGuard.isMutation("SHOW DATABASES"))
    }

    @Test
    fun `INSERT is blocked`() {
        assertTrue(SqlGuard.isMutation("INSERT INTO users VALUES (1, 'test')"))
        assertTrue(SqlGuard.isMutation("insert into users values (1)"))
    }

    @Test
    fun `UPDATE is blocked`() {
        assertTrue(SqlGuard.isMutation("UPDATE users SET name = 'test'"))
    }

    @Test
    fun `DELETE is blocked`() {
        assertTrue(SqlGuard.isMutation("DELETE FROM users"))
    }

    @Test
    fun `DDL statements are blocked`() {
        assertTrue(SqlGuard.isMutation("CREATE TABLE test (id INT)"))
        assertTrue(SqlGuard.isMutation("DROP TABLE users"))
        assertTrue(SqlGuard.isMutation("ALTER TABLE users ADD COLUMN age INT"))
        assertTrue(SqlGuard.isMutation("TRUNCATE TABLE users"))
    }

    @Test
    fun `other mutation statements are blocked`() {
        assertTrue(SqlGuard.isMutation("MERGE INTO target USING source ON condition"))
        assertTrue(SqlGuard.isMutation("REPLACE INTO users VALUES (1)"))
        assertTrue(SqlGuard.isMutation("GRANT SELECT ON db TO role"))
        assertTrue(SqlGuard.isMutation("REVOKE SELECT ON db FROM role"))
        assertTrue(SqlGuard.isMutation("RENAME TABLE old TO new"))
        assertTrue(SqlGuard.isMutation("COPY INTO table FROM @stage"))
        assertTrue(SqlGuard.isMutation("PUT file:///tmp/file @stage"))
        assertTrue(SqlGuard.isMutation("REMOVE @stage/file"))
    }

    @Test
    fun `line comments are stripped before checking`() {
        assertFalse(SqlGuard.isMutation("-- this is a comment\nSELECT 1"))
        assertTrue(SqlGuard.isMutation("-- comment\nDROP TABLE users"))
    }

    @Test
    fun `block comments are stripped before checking`() {
        assertFalse(SqlGuard.isMutation("/* block comment */ SELECT 1"))
        assertTrue(SqlGuard.isMutation("/* block comment */ DELETE FROM users"))
    }

    @Test
    fun `case insensitive detection`() {
        assertTrue(SqlGuard.isMutation("drop TABLE users"))
        assertTrue(SqlGuard.isMutation("Drop Table users"))
        assertTrue(SqlGuard.isMutation("INSERT into users values (1)"))
    }

    @Test
    fun `whitespace is handled`() {
        assertTrue(SqlGuard.isMutation("   INSERT INTO users VALUES (1)"))
        assertFalse(SqlGuard.isMutation("   SELECT 1"))
    }
}
