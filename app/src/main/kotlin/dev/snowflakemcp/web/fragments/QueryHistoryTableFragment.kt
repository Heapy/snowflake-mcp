package dev.snowflakemcp.web.fragments

import dev.snowflakemcp.model.QueryRecord
import kotlinx.html.*
import java.time.format.DateTimeFormatter

private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

fun FlowContent.queryHistoryTableFragment(records: List<QueryRecord>, page: Int, totalPages: Int) {
    if (records.isEmpty()) {
        p { +"No queries recorded yet." }
        return
    }
    table {
        attributes["role"] = "grid"
        thead {
            tr {
                th { +"Time" }
                th { +"Connection" }
                th { +"SQL" }
                th { +"Status" }
                th { +"Rows" }
                th { +"Duration" }
                th { +"Query ID" }
            }
        }
        tbody {
            records.forEach { record ->
                tr {
                    td { +record.executedAt.format(formatter) }
                    td { +record.connectionName }
                    td {
                        attributes["class"] = "sql-cell"
                        title = record.sql
                        +record.sql.take(80)
                    }
                    td {
                        span {
                            val badgeClass = when (record.status) {
                                "SUCCESS" -> "badge badge-success"
                                "ERROR" -> "badge badge-error"
                                "BLOCKED" -> "badge badge-blocked"
                                else -> "badge"
                            }
                            attributes["class"] = badgeClass
                            +record.status
                        }
                    }
                    td { +(record.rowCount?.toString() ?: "-") }
                    td { +(record.durationMs?.let { "${it}ms" } ?: "-") }
                    td { +(record.snowflakeQueryId ?: "-") }
                }
            }
        }
    }
    if (totalPages > 1) {
        nav {
            ul {
                if (page > 1) {
                    li {
                        a {
                            href = "#"
                            attributes["hx-get"] = "/api/queries?page=${page - 1}"
                            attributes["hx-target"] = "#query-table"
                            attributes["hx-swap"] = "innerHTML"
                            +"Previous"
                        }
                    }
                }
                li { +"Page $page of $totalPages" }
                if (page < totalPages) {
                    li {
                        a {
                            href = "#"
                            attributes["hx-get"] = "/api/queries?page=${page + 1}"
                            attributes["hx-target"] = "#query-table"
                            attributes["hx-swap"] = "innerHTML"
                            +"Next"
                        }
                    }
                }
            }
        }
    }
}
