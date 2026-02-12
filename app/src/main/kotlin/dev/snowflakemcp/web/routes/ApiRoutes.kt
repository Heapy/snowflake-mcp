package dev.snowflakemcp.web.routes

import dev.snowflakemcp.model.SnowflakeConnection
import dev.snowflakemcp.repository.ConnectionRepository
import dev.snowflakemcp.repository.QueryHistoryRepository
import dev.snowflakemcp.repository.SettingsRepository
import dev.snowflakemcp.service.SnowflakeService
import dev.snowflakemcp.web.fragments.*
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.html.div
import kotlinx.html.stream.appendHTML

fun Routing.apiRoutes(
    queryHistoryRepo: QueryHistoryRepository,
    connectionRepo: ConnectionRepository,
    settingsRepo: SettingsRepository,
    snowflakeService: SnowflakeService,
) {
    get("/api/queries") {
        val page = call.queryParameters["page"]?.toIntOrNull() ?: 1
        val records = queryHistoryRepo.findAll(page)
        val total = queryHistoryRepo.count()
        val totalPages = maxOf(1, ((total + QueryHistoryRepository.PAGE_SIZE - 1) / QueryHistoryRepository.PAGE_SIZE).toInt())
        val html = buildString {
            appendHTML().div { queryHistoryTableFragment(records, page, totalPages) }
        }
        call.respondText(html, ContentType.Text.Html)
    }

    get("/api/connections/new") {
        val html = buildString {
            appendHTML().div { connectionFormFragment() }
        }
        call.respondText(html, ContentType.Text.Html)
    }

    get("/api/connections/{id}/edit") {
        val id = call.pathParameters["id"]?.toIntOrNull()
            ?: return@get call.respondText("Invalid ID", status = HttpStatusCode.BadRequest)
        val conn = connectionRepo.findById(id)
            ?: return@get call.respondText("Not found", status = HttpStatusCode.NotFound)
        val html = buildString {
            appendHTML().div { connectionFormFragment(conn) }
        }
        call.respondText(html, ContentType.Text.Html)
    }

    post("/api/connections") {
        val params = call.receiveParameters()
        val conn = SnowflakeConnection(
            name = params["name"] ?: "",
            account = params["account"] ?: "",
            user = params["user"] ?: "",
            password = params["password"] ?: "",
            role = params["role"]?.ifBlank { null },
            warehouse = params["warehouse"]?.ifBlank { null },
            database = params["database"]?.ifBlank { null },
            schema = params["schema"]?.ifBlank { null },
        )
        connectionRepo.create(conn)
        val connections = connectionRepo.findAll()
        val html = buildString {
            appendHTML().div { connectionListFragment(connections) }
        }
        call.respondText(html, ContentType.Text.Html)
    }

    put("/api/connections/{id}") {
        val id = call.pathParameters["id"]?.toIntOrNull()
            ?: return@put call.respondText("Invalid ID", status = HttpStatusCode.BadRequest)
        val existing = connectionRepo.findById(id)
            ?: return@put call.respondText("Not found", status = HttpStatusCode.NotFound)
        val params = call.receiveParameters()
        val password = params["password"]?.ifBlank { null } ?: existing.password
        val conn = SnowflakeConnection(
            name = params["name"] ?: existing.name,
            account = params["account"] ?: existing.account,
            user = params["user"] ?: existing.user,
            password = password,
            role = params["role"]?.ifBlank { null },
            warehouse = params["warehouse"]?.ifBlank { null },
            database = params["database"]?.ifBlank { null },
            schema = params["schema"]?.ifBlank { null },
        )
        connectionRepo.update(id, conn)
        val connections = connectionRepo.findAll()
        val html = buildString {
            appendHTML().div { connectionListFragment(connections) }
        }
        call.respondText(html, ContentType.Text.Html)
    }

    delete("/api/connections/{id}") {
        val id = call.pathParameters["id"]?.toIntOrNull()
            ?: return@delete call.respondText("Invalid ID", status = HttpStatusCode.BadRequest)
        connectionRepo.delete(id)
        val connections = connectionRepo.findAll()
        val html = buildString {
            appendHTML().div { connectionListFragment(connections) }
        }
        call.respondText(html, ContentType.Text.Html)
    }

    post("/api/connections/{id}/activate") {
        val id = call.pathParameters["id"]?.toIntOrNull()
            ?: return@post call.respondText("Invalid ID", status = HttpStatusCode.BadRequest)
        connectionRepo.activate(id)
        val connections = connectionRepo.findAll()
        val html = buildString {
            appendHTML().div { connectionListFragment(connections) }
        }
        call.respondText(html, ContentType.Text.Html)
    }

    post("/api/test-connection/{id}") {
        val id = call.pathParameters["id"]?.toIntOrNull()
            ?: return@post call.respondText("Invalid ID", status = HttpStatusCode.BadRequest)
        val conn = connectionRepo.findById(id)
            ?: return@post call.respondText("Not found", status = HttpStatusCode.NotFound)
        val result = snowflakeService.testConnection(conn)
        val isSuccess = result.startsWith("Connected")
        val html = buildString {
            appendHTML().div { alertFragment(result, if (isSuccess) "success" else "error") }
        }
        call.respondText(html, ContentType.Text.Html)
    }

    put("/api/settings/read-only") {
        val params = call.receiveParameters()
        val enabled = params["enabled"] == "on"
        settingsRepo.setReadOnly(enabled)
        val html = buildString {
            appendHTML().div { settingsFormFragment(enabled) }
        }
        call.respondText(html, ContentType.Text.Html)
    }
}
