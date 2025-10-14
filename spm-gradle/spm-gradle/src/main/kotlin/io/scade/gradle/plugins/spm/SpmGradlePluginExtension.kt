package io.scade.gradle.plugins.spm

import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.dsl.DependencyHandler
import java.io.File
import org.gradle.api.provider.Property
import org.gradle.api.provider.ListProperty
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory


import javax.inject.Inject


@Suppress("ClassName")
sealed interface TargetPlatform {
    val name: String

    val archs: List<String>
        get() = listOf()

    val toolchain: File?
        get() = null

    class macOS(override val toolchain: File? = null): TargetPlatform {
        override val name = "macOS"
    }

    class Android(override val archs: List<String> = listOf(),
                  override val toolchain: File? = null): TargetPlatform {
        override val name = "Android"
    }

    class Windows(): TargetPlatform {
        override val name = "Windows"
    }

    class Linux(): TargetPlatform {
        override val name = "Linux"
    }
}



class DependencyCollector (private val dependencyHandler: DependencyHandler) {
    val dependencies = mutableListOf<Dependency>()

    fun project(path: String) {
        dependencies.add(dependencyHandler.project(mapOf("path" to path)))
    }

    fun link(notation: String) {
        dependencies.add(dependencyHandler.create(notation))
    }

    operator fun invoke(notation: String) {
        dependencies.add(dependencyHandler.create(notation))
    }
}


abstract class SpmGradlePluginExtension @Inject constructor(
    private val dependencyHandler: DependencyHandler,
    objects: ObjectFactory
) {

    abstract val path: DirectoryProperty
    abstract val product: Property<String>

    abstract val platforms: ListProperty<TargetPlatform>
    abstract val javaVersion: Property<Int>
    abstract val scd: RegularFileProperty
    abstract val scdAutoUpdate: Property<Boolean>
    abstract val scdOptions: ListProperty<String>

    val dependencies: ListProperty<Dependency> = objects.listProperty(Dependency::class.java)

    fun dependencies(action: DependencyCollector.() -> Unit) {
        val collector = DependencyCollector(dependencyHandler).apply(action)
        dependencies.addAll(collector.dependencies)
    }
}