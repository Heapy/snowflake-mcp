package dev.snowflakemcp.web.routes

import dev.snowflakemcp.repository.ConnectionRepository
import dev.snowflakemcp.repository.QueryHistoryRepository
import dev.snowflakemcp.repository.SettingsRepository
import dev.snowflakemcp.web.templates.connectionsPage
import dev.snowflakemcp.web.templates.layout
import dev.snowflakemcp.web.templates.queryHistoryPage
import dev.snowflakemcp.web.templates.settingsPage
import io.ktor.server.html.respondHtml
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get

fun Routing.uiRoutes(
    queryHistoryRepo: QueryHistoryRepository,
    connectionRepo: ConnectionRepository,
    settingsRepo: SettingsRepository,
) {
    get("/queries") {
        val page = call.queryParameters["page"]?.toIntOrNull() ?: 1
        val records = queryHistoryRepo.findAll(page)
        val total = queryHistoryRepo.count()
        val totalPages = maxOf(1, ((total + QueryHistoryRepository.PAGE_SIZE - 1) / QueryHistoryRepository.PAGE_SIZE).toInt())
        call.respondHtml {
            layout("Query History", "queries") {
                queryHistoryPage(records, page, totalPages)
            }
        }
    }

    get("/connections") {
        val connections = connectionRepo.findAll()
        call.respondHtml {
            layout("Connections", "connections") {
                connectionsPage(connections)
            }
        }
    }

    get("/settings") {
        val readOnly = settingsRepo.isReadOnly()
        call.respondHtml {
            layout("Settings", "settings") {
                settingsPage(readOnly)
            }
        }
    }
}
