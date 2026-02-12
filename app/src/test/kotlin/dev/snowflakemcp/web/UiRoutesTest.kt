package dev.snowflakemcp.web

import dev.snowflakemcp.TestDatabase
import dev.snowflakemcp.repository.ConnectionRepository
import dev.snowflakemcp.repository.QueryHistoryRepository
import dev.snowflakemcp.repository.SettingsRepository
import dev.snowflakemcp.service.SnowflakeService
import dev.snowflakemcp.web.routes.apiRoutes
import dev.snowflakemcp.web.routes.uiRoutes
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.routing.routing
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class UiRoutesTest {
    @BeforeEach
    fun setUp() {
        TestDatabase.init()
    }

    private fun ApplicationTestBuilder.configureApp() {
        val connectionRepo = ConnectionRepository()
        val queryHistoryRepo = QueryHistoryRepository()
        val settingsRepo = SettingsRepository()
        val snowflakeService = SnowflakeService(connectionRepo, queryHistoryRepo, settingsRepo)
        application {
            routing {
                uiRoutes(queryHistoryRepo, connectionRepo, settingsRepo)
                apiRoutes(queryHistoryRepo, connectionRepo, settingsRepo, snowflakeService)
            }
        }
    }

    @Test
    fun `queries page renders`() = testApplication {
        configureApp()
        val response = client.get("/queries")
        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.bodyAsText()
        assertTrue(body.contains("Query History"))
        assertTrue(body.contains("Snowflake MCP"))
    }

    @Test
    fun `connections page renders`() = testApplication {
        configureApp()
        val response = client.get("/connections")
        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.bodyAsText()
        assertTrue(body.contains("Connections"))
        assertTrue(body.contains("New Connection"))
    }

    @Test
    fun `settings page renders`() = testApplication {
        configureApp()
        val response = client.get("/settings")
        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.bodyAsText()
        assertTrue(body.contains("Settings"))
        assertTrue(body.contains("Read-Only Mode"))
    }

    @Test
    fun `new connection form fragment`() = testApplication {
        configureApp()
        val response = client.get("/api/connections/new")
        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.bodyAsText()
        assertTrue(body.contains("New Connection"))
        assertTrue(body.contains("Account"))
    }

    @Test
    fun `create and list connections via API`() = testApplication {
        configureApp()
        val createResponse = client.post("/api/connections") {
            header(HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded.toString())
            setBody("name=test-conn&account=xy12345&user=admin&password=secret")
        }
        assertEquals(HttpStatusCode.OK, createResponse.status)
        val body = createResponse.bodyAsText()
        assertTrue(body.contains("test-conn"))
        assertTrue(body.contains("xy12345"))
    }

    @Test
    fun `toggle read-only setting`() = testApplication {
        configureApp()
        val response = client.put("/api/settings/read-only") {
            header(HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded.toString())
            setBody("")
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.bodyAsText()
        assertTrue(body.contains("disabled"))
    }
}
