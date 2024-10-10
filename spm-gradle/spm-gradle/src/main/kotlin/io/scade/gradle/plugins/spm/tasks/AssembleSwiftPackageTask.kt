package io.scade.gradle.plugins.spm.tasks

import io.scade.gradle.plugins.spm.TargetPlatform
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

abstract class AssembleSwiftPackageTask() : SpmGradlePluginTask() {
    @get:Internal
    val platforms: ListProperty<TargetPlatform> = project.objects.listProperty(TargetPlatform::class.java)


    @get:OutputDirectory
    val outputDirectory: DirectoryProperty = project.objects.directoryProperty()


    init {
        outputDirectory.set(project.layout.buildDirectory.dir("lib"))
    }

    @TaskAction
    fun run() {
        val platformArgs = platforms.get().flatMap {
            when(it) {
                is TargetPlatform.Android -> {
                    val args = mutableListOf("--platform", "android-x86_64", "--platform", "android-arm64-v8a")
                    it.toolchain?.let { path ->
                        args += listOf("--android-swift-toolchain", path.absolutePath)
                    }
                    args
                }
                else ->
                    listOf("--platform", it.name.lowercase())
                }
        }

        scd("archive",
            "--build-dir", buildDirPath,
            "--path", packageDir,
            "--output", project.layout.buildDirectory.get(),
            *platformArgs.toTypedArray())
    }
}