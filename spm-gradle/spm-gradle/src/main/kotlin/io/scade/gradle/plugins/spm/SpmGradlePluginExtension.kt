package io.scade.gradle.plugins.spm

import org.gradle.api.provider.Property
import org.gradle.api.provider.ListProperty
import org.gradle.api.file.DirectoryProperty

enum class SwiftPlatform {
    macOS,
    Windows,
    Linux,
    Android
}

interface SpmGradlePluginExtension {
    val path: DirectoryProperty
    val product: Property<String>
    val platforms: ListProperty<SwiftPlatform>
}


