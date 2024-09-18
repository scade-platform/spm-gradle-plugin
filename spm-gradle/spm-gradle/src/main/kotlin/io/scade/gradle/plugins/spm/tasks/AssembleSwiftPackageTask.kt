package io.scade.gradle.plugins.spm.tasks

import io.scade.gradle.plugins.spm.SwiftPlatform
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

abstract class AssembleSwiftPackageTask() : SpmGradlePluginTask() {
    @get:Internal
    val platforms: ListProperty<SwiftPlatform> = project.objects.listProperty(SwiftPlatform::class.java)

    @get:Internal
    val archivePlatforms: ListProperty<String> = project.objects.listProperty(String::class.java)

    @get:OutputDirectory
    val outputDirectory: DirectoryProperty = project.objects.directoryProperty()

    @get:Internal
    val platformOutputDirectories: ListProperty<Directory> = project.objects.listProperty(Directory::class.java)

    init {
        archivePlatforms.set(platforms.map {
            it.flatMap { p ->
                when(p) {
                    SwiftPlatform.Android ->
                        listOf("android-x86_64", "android-arm64-v8a")
                    else -> listOf(p.name.lowercase())
                }
            }
        })

        outputDirectory.set(project.layout.buildDirectory.dir("lib"))

        platformOutputDirectories.set(archivePlatforms.map {
            it.map { p -> outputDirectory.get().dir(p) }
        })
    }

    @TaskAction
    fun run() {
        val platformArgs = archivePlatforms.get().flatMap {
            listOf("--platform", it)
        }

        scd("archive",
            "--build-dir", buildDirPath,
            "--path", packageDir,
            "--output", project.layout.buildDirectory.get(),
            *platformArgs.toTypedArray())
    }
}