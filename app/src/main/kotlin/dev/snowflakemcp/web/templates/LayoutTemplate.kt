package dev.snowflakemcp.web.templates

import kotlinx.html.*

fun HTML.layout(title: String, currentPage: String, content: MAIN.() -> Unit) {
    head {
        meta { charset = "utf-8" }
        meta { name = "viewport"; this.content = "width=device-width, initial-scale=1" }
        title { +"$title - Snowflake MCP" }
        link {
            rel = "stylesheet"
            href = "https://cdn.jsdelivr.net/npm/@picocss/pico@2/css/pico.min.css"
        }
        script { src = "https://unpkg.com/htmx.org@2.0.4" }
        style {
            unsafe {
                +"""
                    nav ul li a.active { font-weight: bold; text-decoration: underline; }
                    .badge { display: inline-block; padding: 0.2em 0.6em; border-radius: 4px; font-size: 0.85em; font-weight: 600; }
                    .badge-success { background: #2ecc71; color: white; }
                    .badge-error { background: #e74c3c; color: white; }
                    .badge-blocked { background: #f39c12; color: white; }
                    pre.sql { background: #1a1a2e; color: #e0e0e0; padding: 1em; border-radius: 8px; overflow-x: auto; white-space: pre-wrap; word-break: break-all; }
                    .alert { padding: 1em; border-radius: 8px; margin-bottom: 1em; }
                    .alert-success { background: #d4edda; color: #155724; border: 1px solid #c3e6cb; }
                    .alert-error { background: #f8d7da; color: #721c24; border: 1px solid #f5c6cb; }
                    table { font-size: 0.9em; }
                    td.sql-cell { max-width: 400px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
                """.trimIndent()
            }
        }
    }
    body {
        nav {
            attributes["class"] = "container"
            ul {
                li { strong { +"Snowflake MCP" } }
            }
            ul {
                li {
                    a(href = "/queries") {
                        if (currentPage == "queries") classes = setOf("active")
                        +"Query History"
                    }
                }
                li {
                    a(href = "/connections") {
                        if (currentPage == "connections") classes = setOf("active")
                        +"Connections"
                    }
                }
                li {
                    a(href = "/settings") {
                        if (currentPage == "settings") classes = setOf("active")
                        +"Settings"
                    }
                }
            }
        }
        main {
            attributes["class"] = "container"
            content()
        }
    }
}
