package com.scade.gradle.plugins.spm

import com.scade.gradle.plugins.spm.tasks.AssembleSwiftPackageTask
import com.scade.gradle.plugins.spm.tasks.BuildSwiftPackageTask
import com.scade.gradle.plugins.spm.tasks.GenerateBridgingTask
import com.scade.gradle.plugins.spm.tasks.ResolveScdToolTask

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ApplicationPlugin
import org.gradle.api.plugins.JavaApplication

import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer

import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.TaskProvider
import org.gradle.internal.os.OperatingSystem


fun OperatingSystem.swiftPlatform(): SwiftPlatform? {
    return  if (OperatingSystem.current().isMacOsX) SwiftPlatform.macOS else
            if (OperatingSystem.current().isWindows) SwiftPlatform.Windows else
            if (OperatingSystem.current().isLinux) SwiftPlatform.Linux else null
}


open class SpmGradlePlugin: Plugin<Project> {
    open val defaultPlatform: SwiftPlatform?
        get() = OperatingSystem.current().swiftPlatform()


    override fun apply(project: Project) {
        val extension = project.extensions.create("swiftpm", SpmGradlePluginExtension::class.java)
        val resolveScdToolTask = project.tasks.register("resolveScdTool", ResolveScdToolTask::class.java)

        val platforms = extension.platforms.map {
            if (it.isEmpty()) {
                val res = it.toMutableList()
                res.add(defaultPlatform)
                res
            } else {
                it
            }
        }

        val buildSwiftPackage = project.tasks.register("buildSwiftPackage", BuildSwiftPackageTask::class.java) {
            it.scdFile.set(resolveScdToolTask.get().scdFile)
            it.path.set(extension.path)
        }

        val generateBridging = project.tasks.register("generateBridging", GenerateBridgingTask::class.java) {
            it.scdFile.set(resolveScdToolTask.get().scdFile)
            it.path.set(extension.path)
            it.product.set(extension.product)
        }

        val assembleSwiftPackage = project.tasks.register("assembleSwiftPackage", AssembleSwiftPackageTask::class.java) {
            val curPlatform = OperatingSystem.current().swiftPlatform()
            if (platforms.get().find { p: SwiftPlatform  -> p != curPlatform } != null) {
                it.dependsOn(buildSwiftPackage.get())
            }

            it.scdFile.set(resolveScdToolTask.get().scdFile)

            it.path.set(extension.path)
            it.platforms.set(platforms)
        }

        applyBridgingOutputs(project, generateBridging)
        applyAssembleOutputs(project, assembleSwiftPackage)
    }

    open fun applyBridgingOutputs(project: Project, task: TaskProvider<GenerateBridgingTask>) {
        project.plugins.withType(JavaPlugin::class.java) {
            val sourceSets = project.extensions.getByType(SourceSetContainer::class.java)
            val main = sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME)

            main.java.srcDirs(task)
        }
    }

    open fun applyAssembleOutputs(project: Project, task: TaskProvider<AssembleSwiftPackageTask>) {
        project.plugins.withType(ApplicationPlugin::class.java) {
            val app = project.extensions.getByType(JavaApplication::class.java)
            val platform = OperatingSystem.current().swiftPlatform()?.name?.lowercase() ?: ""
            app.applicationDefaultJvmArgs = listOf("-Djava.library.path=" + "${task.get().outputDirectory.get().dir(platform)}")
        }
    }
}
