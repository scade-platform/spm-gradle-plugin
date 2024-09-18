package io.scade.gradle.plugins.spm.tasks

import org.gradle.api.provider.Property
import org.gradle.api.file.DirectoryProperty

import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction


abstract class GenerateBridgingTask() : SpmGradlePluginTask() {
    @Internal
    val product: Property<String> = project.objects.property(String::class.java)

    @get:OutputDirectory
    val bridgingSrc: DirectoryProperty = project.objects.directoryProperty()

    @get:OutputDirectory
    val bridgingJavaSrc: DirectoryProperty = project.objects.directoryProperty()

    init {
        bridgingSrc.set(
            product.map {
                buildDir.get().dir("plugins/generate-java-bridging/outputs/$it/main")
            }
        )
        bridgingJavaSrc.set(
            product.map {
                buildDir.get().dir("plugins/generate-java-bridging/outputs/$it/main/java")
            }
        )
    }

    private val pluginAvailable: Boolean
        get() {
            return swift("package", "--scratch-path", buildDirPath, "--package-path", packageDir, "plugin", "--list") {_, out ->
               out.split("\n").find { it.startsWith("‘generate-java-bridging’") } != null
            }
        }

    @TaskAction
    fun run() {
//        println("Command line: ${"package" + " --scratch-path " + buildDirPath + " --package-path " + packageDir + " plugin " + " generate-java-bridging " + " -p " + product.get()}")
        if (pluginAvailable) {
            swift("package", "--scratch-path", buildDirPath, "--package-path", packageDir, "plugin", "generate-java-bridging", "-p", product.get())
        }
    }
}