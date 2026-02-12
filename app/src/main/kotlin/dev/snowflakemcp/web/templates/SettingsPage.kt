package dev.snowflakemcp.web.templates

import dev.snowflakemcp.web.fragments.settingsFormFragment
import kotlinx.html.*

fun MAIN.settingsPage(readOnly: Boolean) {
    h2 { +"Settings" }
    div {
        id = "alert-area"
    }
    div {
        id = "settings-form"
        settingsFormFragment(readOnly)
    }
}
