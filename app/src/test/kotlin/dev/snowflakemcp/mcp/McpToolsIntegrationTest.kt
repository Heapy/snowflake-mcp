package dev.snowflakemcp.mcp

import dev.snowflakemcp.InMemoryTransport
import dev.snowflakemcp.TestDatabase
import dev.snowflakemcp.model.SnowflakeConnection
import dev.snowflakemcp.repository.ConnectionRepository
import dev.snowflakemcp.repository.QueryHistoryRepository
import dev.snowflakemcp.repository.SettingsRepository
import dev.snowflakemcp.service.SnowflakeService
import io.modelcontextprotocol.kotlin.sdk.client.Client
import io.modelcontextprotocol.kotlin.sdk.types.Implementation
import io.modelcontextprotocol.kotlin.sdk.types.TextContent
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class McpToolsIntegrationTest {
    private lateinit var connectionRepo: ConnectionRepository
    private lateinit var queryHistoryRepo: QueryHistoryRepository
    private lateinit var settingsRepo: SettingsRepository
    private lateinit var snowflakeService: SnowflakeService

    @BeforeEach
    fun setUp() {
        TestDatabase.init()
        connectionRepo = ConnectionRepository()
        queryHistoryRepo = QueryHistoryRepository()
        settingsRepo = SettingsRepository()
        snowflakeService = SnowflakeService(connectionRepo, queryHistoryRepo, settingsRepo)
    }

    private fun withMcpClient(block: suspend (Client) -> Unit) = runBlocking {
        val server = createMcpServer(snowflakeService, connectionRepo, settingsRepo)
        val (clientTransport, serverTransport) = InMemoryTransport.createLinkedPair()

        server.createSession(serverTransport)
        val client = Client(Implementation(name = "test-client", version = "1.0.0"))
        client.connect(clientTransport)

        try {
            block(client)
        } finally {
            client.close()
        }
    }

    @Test
    fun `list_connections returns empty message when no connections`() = withMcpClient { client ->
        val result = client.callTool("list_connections", emptyMap())
        val text = (result.content.first() as TextContent).text
        assertTrue(text.contains("No connections configured"))
    }

    @Test
    fun `list_connections shows configured connections`() = withMcpClient { client ->
        connectionRepo.create(
            SnowflakeConnection(
                name = "prod",
                account = "xy12345.us-east-1",
                user = "admin",
                password = "secret",
                role = "SYSADMIN",
            )
        )

        val result = client.callTool("list_connections", emptyMap())
        val text = (result.content.first() as TextContent).text
        assertTrue(text.contains("prod"))
        assertTrue(text.contains("xy12345.us-east-1"))
        assertFalse(text.contains("secret"))
    }

    @Test
    fun `get_active_connection returns error when none active`() = withMcpClient { client ->
        val result = client.callTool("get_active_connection", emptyMap())
        assertTrue(result.isError == true)
    }

    @Test
    fun `set_read_only toggles mode`() = withMcpClient { client ->
        assertTrue(settingsRepo.isReadOnly())

        client.callTool("set_read_only", mapOf("enabled" to false))
        assertFalse(settingsRepo.isReadOnly())

        client.callTool("set_read_only", mapOf("enabled" to true))
        assertTrue(settingsRepo.isReadOnly())
    }

    @Test
    fun `execute_sql without active connection returns error`() = withMcpClient { client ->
        val result = client.callTool("execute_sql", mapOf("sql" to "SELECT 1"))
        assertTrue(result.isError == true)
        val text = (result.content.first() as TextContent).text
        assertTrue(text.contains("No active connection"))
    }

    @Test
    fun `execute_sql blocks mutations in read-only mode`() = withMcpClient { client ->
        val conn = connectionRepo.create(
            SnowflakeConnection(
                name = "test",
                account = "xy12345",
                user = "user",
                password = "pass",
            )
        )
        connectionRepo.activate(conn.id)
        assertTrue(settingsRepo.isReadOnly())

        val result = client.callTool("execute_sql", mapOf("sql" to "DROP TABLE users"))
        assertTrue(result.isError == true)
        val text = (result.content.first() as TextContent).text
        assertTrue(text.contains("read-only"))

        val history = queryHistoryRepo.findAll()
        assertEquals(1, history.size)
        assertEquals("BLOCKED", history[0].status)
    }
}
