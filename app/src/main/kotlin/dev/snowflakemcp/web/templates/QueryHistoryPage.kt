package dev.snowflakemcp.web.templates

import dev.snowflakemcp.model.QueryRecord
import dev.snowflakemcp.web.fragments.queryHistoryTableFragment
import kotlinx.html.*

fun MAIN.queryHistoryPage(records: List<QueryRecord>, page: Int, totalPages: Int) {
    h2 { +"Query History" }
    div {
        id = "query-table"
        queryHistoryTableFragment(records, page, totalPages)
    }
}
