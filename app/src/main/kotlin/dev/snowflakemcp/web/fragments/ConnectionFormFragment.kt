package dev.snowflakemcp.web.fragments

import dev.snowflakemcp.model.SnowflakeConnection
import kotlinx.html.*

fun FlowContent.connectionFormFragment(conn: SnowflakeConnection? = null) {
    val isEdit = conn != null
    val action = if (isEdit) "/api/connections/${conn.id}" else "/api/connections"
    val method = if (isEdit) "hx-put" else "hx-post"

    article {
        header { +(if (isEdit) "Edit Connection" else "New Connection") }
        form {
            attributes[method] = action
            attributes["hx-target"] = "#connection-list"
            attributes["hx-swap"] = "innerHTML"
            attributes["hx-on::after-request"] =
                """if(event.detail.successful) document.getElementById('connection-form-area').innerHTML = '<button hx-get="/api/connections/new" hx-target="#connection-form-area" hx-swap="innerHTML">New Connection</button>'; htmx.process(document.getElementById('connection-form-area'));"""

            label {
                +"Name"
                input {
                    type = InputType.text
                    name = "name"
                    required = true
                    value = conn?.name ?: ""
                }
            }
            label {
                +"Account"
                input {
                    type = InputType.text
                    name = "account"
                    required = true
                    placeholder = "xy12345.us-east-1"
                    value = conn?.account ?: ""
                }
            }
            label {
                +"User"
                input {
                    type = InputType.text
                    name = "user"
                    required = true
                    value = conn?.user ?: ""
                }
            }
            label {
                +"Password"
                input {
                    type = InputType.password
                    name = "password"
                    required = !isEdit
                    value = ""
                    if (isEdit) placeholder = "Leave blank to keep current"
                }
            }
            label {
                +"Role (optional)"
                input {
                    type = InputType.text
                    name = "role"
                    value = conn?.role ?: ""
                }
            }
            label {
                +"Warehouse (optional)"
                input {
                    type = InputType.text
                    name = "warehouse"
                    value = conn?.warehouse ?: ""
                }
            }
            label {
                +"Database (optional)"
                input {
                    type = InputType.text
                    name = "database"
                    value = conn?.database ?: ""
                }
            }
            label {
                +"Schema (optional)"
                input {
                    type = InputType.text
                    name = "schema"
                    value = conn?.schema ?: ""
                }
            }
            div {
                attributes["style"] = "display: flex; gap: 1em;"
                button { type = ButtonType.submit; +(if (isEdit) "Update" else "Create") }
                button {
                    type = ButtonType.button
                    attributes["class"] = "secondary"
                    attributes["onclick"] =
                        """document.getElementById('connection-form-area').innerHTML = '<button hx-get="/api/connections/new" hx-target="#connection-form-area" hx-swap="innerHTML">New Connection</button>'; htmx.process(document.getElementById('connection-form-area'));"""
                    +"Cancel"
                }
            }
        }
    }
}
