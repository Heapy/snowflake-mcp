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
import kotlinx.coroutines.CompletableDeferred

suspend fun main() {
    System.setProperty("net.snowflake.jdbc.loggerImpl", "net.snowflake.client.log.SLF4JLogger")

    val dbPath = System.getenv("SNOWFLAKE_MCP_DB_PATH") ?: "snowflake-mcp.db"
    DatabaseFactory.init(dbPath)

    val connectionRepo = ConnectionRepository()
    val queryHistoryRepo = QueryHistoryRepository()
    val settingsRepo = SettingsRepository()
    val snowflakeService = SnowflakeService(connectionRepo, queryHistoryRepo, settingsRepo)
    val mcpServer = createMcpServer(snowflakeService, connectionRepo, settingsRepo)

    val server = embeddedServer(CIO, host = "127.0.0.1", port = 33300) {
        mcp { mcpServer }
        routing {
            uiRoutes(queryHistoryRepo, connectionRepo, settingsRepo)
            apiRoutes(queryHistoryRepo, connectionRepo, settingsRepo, snowflakeService)
        }
    }
    val deferred = CompletableDeferred<Unit>()

    Runtime.getRuntime().addShutdownHook(Thread {
        server.stop(1000, 5000)
        deferred.complete(Unit)
    })

    server.start(wait = false)

    if (System.getProperty("os.name").lowercase().contains("mac")) {
        ProcessBuilder("open", "http://127.0.0.1:33300/queries").start()
    }

    deferred.await()
}
