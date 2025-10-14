package io.scade.gradle.plugins.spm.tasks

import io.scade.gradle.plugins.spm.TargetPlatform
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction


abstract class AssembleSwiftPackageTask() : SpmGradlePluginTask() {
    @Internal
    val product: Property<String> = project.objects.property(String::class.java)

    @Internal
    val platforms: ListProperty<TargetPlatform> = project.objects.listProperty(TargetPlatform::class.java)

    @Internal
    val linkDependencies: ListProperty<String> = project.objects.listProperty(String::class.java)

    @Internal
    val assembleDebug: Property<Boolean> = project.objects.property(Boolean::class.java)

    @Internal
    val scdOptions: ListProperty<String> = project.objects.listProperty(String::class.java)

    @OutputDirectory
    val outputDirectory: DirectoryProperty = project.objects.directoryProperty()

    init {
        linkDependencies.convention(listOf())
        scdOptions.convention(listOf())
        outputDirectory.set(project.layout.buildDirectory.dir("lib"))
    }

    @TaskAction
    fun run() {
        val linkArgs = linkDependencies.get().flatMap {
            listOf("-l", it)
        }

        scd("archive",
            "--build-dir", buildDirPath,
            "--path", packageDir,
            "--output", project.layout.buildDirectory.get(),
            "--product", product.get(),
            "--configuration", if (assembleDebug.get()) "Debug" else "Release",
            *scdOptions.get().toTypedArray(),
            *linkArgs.toTypedArray(),
            *platformArgs().toTypedArray())
    }

    open fun platformArgs(): List<String> {
        return platforms.get().flatMap {
            listOf("--platform", it.name.lowercase())
        }
    }
}