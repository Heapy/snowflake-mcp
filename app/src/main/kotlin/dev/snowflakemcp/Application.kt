package dev.snowflakemcp

import dev.snowflakemcp.db.DatabaseFactory
import dev.snowflakemcp.mcp.createMcpServer
import dev.snowflakemcp.repository.ConnectionRepository
import dev.snowflakemcp.repository.QueryHistoryRepository
import dev.snowflakemcp.repository.SettingsRepository
import dev.snowflakemcp.service.SnowflakeService
import dev.snowflakemcp.web.routes.apiRoutes
import dev.snowflakemcp.web.routes.uiRoutes
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.routing.routing
import io.modelcontextprotocol.kotlin.sdk.server.mcp

fun main() {
    System.setProperty("net.snowflake.jdbc.loggerImpl", "net.snowflake.client.log.SLF4JLogger")

    val dbPath = System.getenv("SNOWFLAKE_MCP_DB_PATH") ?: "snowflake-mcp.db"
    DatabaseFactory.init(dbPath)

    val connectionRepo = ConnectionRepository()
    val queryHistoryRepo = QueryHistoryRepository()
    val settingsRepo = SettingsRepository()
    val snowflakeService = SnowflakeService(connectionRepo, queryHistoryRepo, settingsRepo)
    val mcpServer = createMcpServer(snowflakeService, connectionRepo, settingsRepo)

    val host = System.getenv("SNOWFLAKE_MCP_HOST") ?: "127.0.0.1"
    val port = System.getenv("SNOWFLAKE_MCP_PORT")?.toInt() ?: 8080
    embeddedServer(CIO, host = host, port = port) {
        mcp { mcpServer }
        routing {
            uiRoutes(queryHistoryRepo, connectionRepo, settingsRepo)
            apiRoutes(queryHistoryRepo, connectionRepo, settingsRepo, snowflakeService)
        }
    }.start(wait = true)
}
