package dev.snowflakemcp.model

import java.time.LocalDateTime

data class SnowflakeConnection(
    val id: Int = 0,
    val name: String,
    val account: String,
    val user: String,
    val password: String,
    val role: String? = null,
    val warehouse: String? = null,
    val database: String? = null,
    val schema: String? = null,
    val isActive: Boolean = false,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
)
