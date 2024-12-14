package io.scade.gradle.plugins.spm

import io.scade.gradle.plugins.spm.tasks.AssembleSwiftPackageTask
import io.scade.gradle.plugins.spm.tasks.BuildSwiftPackageTask
import io.scade.gradle.plugins.spm.tasks.GenerateBridgingTask
import io.scade.gradle.plugins.spm.tasks.ResolveScdToolTask

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ApplicationPlugin
import org.gradle.api.plugins.JavaApplication

import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer

import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.TaskProvider
import org.gradle.internal.os.OperatingSystem
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.swiftexport.registerSwiftExportTask


fun OperatingSystem.swiftTargetPlatform(): TargetPlatform? {
    return  if (OperatingSystem.current().isMacOsX) TargetPlatform.macOS() else
            if (OperatingSystem.current().isWindows) TargetPlatform.Windows() else
            if (OperatingSystem.current().isLinux) TargetPlatform.Linux() else null
}


open class SpmGradlePlugin: Plugin<Project> {
    open val defaultPlatform: TargetPlatform?
        get() = OperatingSystem.current().swiftTargetPlatform()


    override fun apply(project: Project) {
        val extension = project.extensions.create("swiftpm", SpmGradlePluginExtension::class.java)
        val resolveScdToolTask = project.tasks.register("resolveScdTool", ResolveScdToolTask::class.java)

        resolveScdToolTask.get().scd.set(extension.scd)
        resolveScdToolTask.get().scdAutoUpdate.set(extension.scdAutoUpdate)

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
            it.javaVersion.set(extension.javaVersion)
        }

        val assembleSwiftPackage = project.tasks.register("assembleSwiftPackage", AssembleSwiftPackageTask::class.java) {
            val curPlatform = OperatingSystem.current().swiftTargetPlatform()

            // Workaround to support applying macro while cross-compiling
            //TODO: remove as soon as Swift 6 support is there
            curPlatform?.let { cp ->
                if (platforms.get().find { p -> p.javaClass != cp.javaClass } != null) {
                    it.dependsOn(buildSwiftPackage.get())
                }
            }

            it.scdFile.set(resolveScdToolTask.get().scdFile)

            it.path.set(extension.path)
            it.platforms.set(platforms)
        }

        applyBridgingOutputs(project, generateBridging)
        applyAssembleOutputs(project, assembleSwiftPackage)

        project.tasks.register("cleanSwiftBuildDir", Delete::class.java) {
            it.delete.add(assembleSwiftPackage.get().buildDir)

            project.tasks.named("clean") { clean ->
                clean.dependsOn(it)
            }
        }
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
            val platform = OperatingSystem.current().swiftTargetPlatform()?.name?.lowercase() ?: ""
            app.applicationDefaultJvmArgs = listOf("-Djava.library.path=" + "${task.get().outputDirectory.get().dir(platform)}")

            project.tasks.withType(JavaExec::class.java) {
                it.dependsOn(task)
            }
        }
    }
}
