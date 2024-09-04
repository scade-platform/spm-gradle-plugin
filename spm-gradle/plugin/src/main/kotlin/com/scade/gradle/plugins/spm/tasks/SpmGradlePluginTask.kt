package com.scade.gradle.plugins.spm.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.process.ExecResult
import java.io.ByteArrayOutputStream
import java.io.File


abstract class SpmGradlePluginTask : DefaultTask() {
    @get:InputFile
    val scdFile: RegularFileProperty = project.objects.fileProperty()

    @get:Internal
    val path: DirectoryProperty = project.objects.directoryProperty()

    @get:Internal
    val buildDir: DirectoryProperty = project.objects.directoryProperty()

    init {
        buildDir.set(project.layout.buildDirectory.map {
            it.dir("swiftpm/${path.asFile.get().name}")
        })
    }

    @get:Internal
    val packageDir: File
        get() = path.asFile.get().absoluteFile


    @get:Internal
    val buildDirPath: File
        get() {
            buildDir.get().asFile.mkdirs()
            return buildDir.asFile.get().absoluteFile
        }


    fun scd(vararg args: Any): ExecResult {
        return project.exec {
            it.commandLine(scdFile.asFile.get(), *args)
        }
    }

    fun swift(vararg args: Any): ExecResult {
        return project.exec {
            it.commandLine("swift", *args)
        }
    }

    fun<T> swift(vararg args: Any, captureOutput: (ExecResult, String) -> T): T {
        val stdout = ByteArrayOutputStream()
        val res = project.exec {
            it.commandLine("swift", *args)
            it.standardOutput = stdout
        }
        return captureOutput(res, stdout.toString())
    }
}
