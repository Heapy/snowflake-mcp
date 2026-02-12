package dev.snowflakemcp.web.fragments

import kotlinx.html.*

fun FlowContent.alertFragment(message: String, type: String = "success") {
    div {
        attributes["class"] = "alert alert-$type"
        +message
    }
}
