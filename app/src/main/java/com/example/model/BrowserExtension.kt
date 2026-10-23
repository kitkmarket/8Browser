package com.example.model

import java.util.UUID

enum class ExtensionType {
    SITE,
    FIREFOX
}

data class BrowserExtension(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val fileName: String,
    val archivePath: String,
    val installationPath: String,
    val type: ExtensionType = ExtensionType.SITE,
    val url: String,
    val isEnabled: Boolean = true,
    val isExperimental: Boolean = true,
    val backgroundMode: Boolean = true,
    val manifestSummary: String? = null
)
