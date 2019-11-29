package com.github.banananet.plugin

import com.github.banananet.Core.app

object NoPlugin : Plugin() {
    override val id: String get() = ""
    override val label: CharSequence get() = app.getText(com.github.banananet.core.R.string.plugin_disabled)
}
