package io.scade.gradle.plugins.spm

import java.io.File
import org.gradle.api.provider.Property
import org.gradle.api.provider.ListProperty
import org.gradle.api.file.DirectoryProperty


@Suppress("ClassName")
sealed interface TargetPlatform {
    val name: String
    val toolchain: File?
        get() = null

    class macOS(override val toolchain: File? = null): TargetPlatform {
        override val name = "macOS"
    }

    class Android(override val toolchain: File? = null): TargetPlatform {
        override val name = "Android"
    }

    class Windows(): TargetPlatform {
        override val name = "Windows"
    }

    class Linux(): TargetPlatform {
        override val name = "Linux"
    }
}


interface SpmGradlePluginExtension {
    val path: DirectoryProperty
    val product: Property<String>
    val platforms: ListProperty<TargetPlatform>
}