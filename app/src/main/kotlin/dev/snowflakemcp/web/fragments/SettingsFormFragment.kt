package dev.snowflakemcp.web.fragments

import kotlinx.html.*

fun FlowContent.settingsFormFragment(readOnly: Boolean) {
    article {
        header { +"Read-Only Mode" }
        p {
            +"When enabled, mutation queries (INSERT, UPDATE, DELETE, CREATE, DROP, etc.) are blocked."
        }
        form {
            attributes["hx-put"] = "/api/settings/read-only"
            attributes["hx-target"] = "#settings-form"
            attributes["hx-swap"] = "innerHTML"
            label {
                input {
                    type = InputType.checkBox
                    name = "enabled"
                    role = "switch"
                    if (readOnly) checked = true
                }
                +"Read-only mode ${if (readOnly) "(enabled)" else "(disabled)"}"
            }
            button { type = ButtonType.submit; +"Save" }
        }
    }
}
