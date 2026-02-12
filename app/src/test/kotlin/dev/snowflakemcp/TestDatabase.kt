package dev.snowflakemcp

import dev.snowflakemcp.db.DatabaseFactory
import java.io.File

object TestDatabase {
    fun init() {
        val tempFile = File.createTempFile("snowflake-mcp-test-", ".db")
        tempFile.deleteOnExit()
        DatabaseFactory.init(tempFile.absolutePath)
    }
}
