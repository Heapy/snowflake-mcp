package dev.snowflakemcp.service

object SqlGuard {
    private val BLOCKED_PREFIXES = listOf(
        "INSERT", "UPDATE", "DELETE", "CREATE", "DROP", "ALTER",
        "TRUNCATE", "MERGE", "REPLACE", "GRANT", "REVOKE",
        "RENAME", "COPY", "PUT", "REMOVE",
    )

    fun isMutation(sql: String): Boolean {
        val cleaned = stripComments(sql).trim()
        val upper = cleaned.uppercase()
        return BLOCKED_PREFIXES.any { upper.startsWith(it) }
    }

    private fun stripComments(sql: String): String {
        var result = sql.trim()
        // Remove block comments
        while (result.contains("/*")) {
            val start = result.indexOf("/*")
            val end = result.indexOf("*/", start)
            if (end == -1) break
            result = result.substring(0, start) + result.substring(end + 2)
        }
        // Remove leading line comments
        result = result.lines()
            .dropWhile { it.trim().startsWith("--") }
            .joinToString("\n")
        return result.trim()
    }
}
