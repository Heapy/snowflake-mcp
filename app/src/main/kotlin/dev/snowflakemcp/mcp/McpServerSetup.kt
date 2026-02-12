package dev.snowflakemcp.mcp

import dev.snowflakemcp.repository.ConnectionRepository
import dev.snowflakemcp.repository.SettingsRepository
import dev.snowflakemcp.service.SnowflakeService
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import io.modelcontextprotocol.kotlin.sdk.types.*
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

fun createMcpServer(
    snowflakeService: SnowflakeService,
    connectionRepo: ConnectionRepository,
    settingsRepo: SettingsRepository,
): Server {
    val server = Server(
        serverInfo = Implementation(
            name = "snowflake-mcp",
            version = "1.0.0",
        ),
        options = ServerOptions(
            capabilities = ServerCapabilities(
                tools = ServerCapabilities.Tools(listChanged = true),
            ),
        ),
    )

    server.addTool(
        name = "execute_sql",
        description = "Execute SQL against the active Snowflake connection. Returns results as a markdown table for SELECT queries, or a status message for other statements. Mutation queries are blocked when read-only mode is enabled.",
        inputSchema = ToolSchema(
            properties = buildJsonObject {
                put("sql", buildJsonObject {
                    put("type", "string")
                    put("description", "The SQL query to execute")
                })
            },
            required = listOf("sql"),
        ),
    ) { request ->
        val sql = request.arguments?.get("sql")?.jsonPrimitive?.content
            ?: return@addTool CallToolResult(
                content = listOf(TextContent(text = "Missing required argument: sql")),
                isError = true,
            )
        val result = snowflakeService.executeSql(sql)
        CallToolResult(
            content = listOf(TextContent(text = result.output)),
            isError = result.isError,
        )
    }

    server.addTool(
        name = "list_connections",
        description = "List all configured Snowflake connections. Passwords are omitted for security. Shows which connection is currently active.",
    ) { _ ->
        val connections = connectionRepo.findAll()
        if (connections.isEmpty()) {
            CallToolResult(content = listOf(TextContent(text = "No connections configured. Add one via the web UI at /connections.")))
        } else {
            val text = connections.joinToString("\n\n") { conn ->
                buildString {
                    appendLine("**${conn.name}**${if (conn.isActive) " (ACTIVE)" else ""}")
                    appendLine("- Account: ${conn.account}")
                    appendLine("- User: ${conn.user}")
                    conn.role?.let { appendLine("- Role: $it") }
                    conn.warehouse?.let { appendLine("- Warehouse: $it") }
                    conn.database?.let { appendLine("- Database: $it") }
                    conn.schema?.let { appendLine("- Schema: $it") }
                }
            }
            CallToolResult(content = listOf(TextContent(text = text)))
        }
    }

    server.addTool(
        name = "get_active_connection",
        description = "Get details of the currently active Snowflake connection. Returns an error if no connection is configured or active.",
    ) { _ ->
        val conn = connectionRepo.findActive()
        if (conn == null) {
            CallToolResult(
                content = listOf(TextContent(text = "No active connection. Configure and activate one via the web UI at /connections.")),
                isError = true,
            )
        } else {
            val text = buildString {
                appendLine("Active connection: **${conn.name}**")
                appendLine("- Account: ${conn.account}")
                appendLine("- User: ${conn.user}")
                conn.role?.let { appendLine("- Role: $it") }
                conn.warehouse?.let { appendLine("- Warehouse: $it") }
                conn.database?.let { appendLine("- Database: $it") }
                conn.schema?.let { appendLine("- Schema: $it") }
            }
            CallToolResult(content = listOf(TextContent(text = text)))
        }
    }

    server.addTool(
        name = "set_read_only",
        description = "Toggle read-only mode on or off. When enabled, mutation queries (INSERT, UPDATE, DELETE, DROP, etc.) are blocked.",
        inputSchema = ToolSchema(
            properties = buildJsonObject {
                put("enabled", buildJsonObject {
                    put("type", "boolean")
                    put("description", "true to enable read-only mode, false to disable")
                })
            },
            required = listOf("enabled"),
        ),
    ) { request ->
        val enabled = request.arguments?.get("enabled")?.jsonPrimitive?.booleanOrNull
            ?: return@addTool CallToolResult(
                content = listOf(TextContent(text = "Missing required argument: enabled (boolean)")),
                isError = true,
            )
        settingsRepo.setReadOnly(enabled)
        val status = if (enabled) "enabled" else "disabled"
        CallToolResult(content = listOf(TextContent(text = "Read-only mode $status.")))
    }

    return server
}
