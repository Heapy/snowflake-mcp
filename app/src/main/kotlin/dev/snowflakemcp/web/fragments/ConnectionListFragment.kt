package dev.snowflakemcp.web.fragments

import dev.snowflakemcp.model.SnowflakeConnection
import kotlinx.html.*

fun FlowContent.connectionListFragment(connections: List<SnowflakeConnection>) {
    if (connections.isEmpty()) {
        p { +"No connections configured. Add one to get started." }
        return
    }
    table {
        attributes["role"] = "grid"
        thead {
            tr {
                th { +"Name" }
                th { +"Account" }
                th { +"User" }
                th { +"Role" }
                th { +"Warehouse" }
                th { +"Status" }
                th { +"Actions" }
            }
        }
        tbody {
            connections.forEach { conn ->
                tr {
                    td { +conn.name }
                    td { +conn.account }
                    td { +conn.user }
                    td { +(conn.role ?: "-") }
                    td { +(conn.warehouse ?: "-") }
                    td {
                        if (conn.isActive) {
                            span {
                                attributes["class"] = "badge badge-success"
                                +"Active"
                            }
                        }
                    }
                    td {
                        div {
                            attributes["style"] = "display: flex; gap: 0.5em;"
                            if (!conn.isActive) {
                                button {
                                    attributes["class"] = "outline"
                                    attributes["hx-post"] = "/api/connections/${conn.id}/activate"
                                    attributes["hx-target"] = "#connection-list"
                                    attributes["hx-swap"] = "innerHTML"
                                    +"Activate"
                                }
                            }
                            button {
                                attributes["class"] = "outline"
                                attributes["hx-get"] = "/api/connections/${conn.id}/edit"
                                attributes["hx-target"] = "#connection-form-area"
                                attributes["hx-swap"] = "innerHTML"
                                +"Edit"
                            }
                            button {
                                attributes["class"] = "outline secondary"
                                attributes["hx-post"] = "/api/test-connection/${conn.id}"
                                attributes["hx-target"] = "#alert-area"
                                attributes["hx-swap"] = "innerHTML"
                                +"Test"
                            }
                            button {
                                attributes["class"] = "outline"
                                attributes["style"] = "color: var(--pico-del-color);"
                                attributes["hx-delete"] = "/api/connections/${conn.id}"
                                attributes["hx-target"] = "#connection-list"
                                attributes["hx-swap"] = "innerHTML"
                                attributes["hx-confirm"] = "Delete connection '${conn.name}'?"
                                +"Delete"
                            }
                        }
                    }
                }
            }
        }
    }
}
