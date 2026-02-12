package dev.snowflakemcp.web.templates

import dev.snowflakemcp.model.SnowflakeConnection
import dev.snowflakemcp.web.fragments.connectionListFragment
import kotlinx.html.*

fun MAIN.connectionsPage(connections: List<SnowflakeConnection>) {
    h2 { +"Connections" }
    div {
        id = "alert-area"
    }
    div {
        id = "connection-form-area"
        button {
            attributes["hx-get"] = "/api/connections/new"
            attributes["hx-target"] = "#connection-form-area"
            attributes["hx-swap"] = "innerHTML"
            +"New Connection"
        }
    }
    div {
        id = "connection-list"
        connectionListFragment(connections)
    }
}
