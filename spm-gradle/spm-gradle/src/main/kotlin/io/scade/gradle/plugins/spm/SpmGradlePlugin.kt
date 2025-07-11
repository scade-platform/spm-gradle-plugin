package io.scade.gradle.plugins.spm

import io.scade.gradle.plugins.spm.tasks.AssembleSwiftPackageTask
import io.scade.gradle.plugins.spm.tasks.GenerateBridgingTask
import io.scade.gradle.plugins.spm.tasks.ResolveScdToolTask
import org.gradle.api.Action

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaApplication
import org.gradle.api.plugins.ApplicationPlugin
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.TaskProvider
import org.gradle.internal.extensions.stdlib.capitalized

import org.gradle.internal.os.OperatingSystem
import javax.inject.Inject


fun OperatingSystem.swiftTargetPlatform(): TargetPlatform? {
    return  if (OperatingSystem.current().isMacOsX) TargetPlatform.macOS() else
            if (OperatingSystem.current().isWindows) TargetPlatform.Windows() else
            if (OperatingSystem.current().isLinux) TargetPlatform.Linux() else null
}


open class SpmGradlePlugin @Inject constructor (
    objects: ObjectFactory
) : Plugin<Project> {

    open val defaultPlatform: TargetPlatform?
        get() = OperatingSystem.current().swiftTargetPlatform()

    private var resolveScdTask: TaskProvider<ResolveScdToolTask>? = null

    override fun apply(project: Project) {
        val extension = project.extensions.create("swiftpm", SpmGradlePluginExtension::class.java)

        resolveScdTask = project.tasks.register("resolveScdTool", ResolveScdToolTask::class.java) {
            it.scd.set(extension.scd)
            it.scdAutoUpdate.set(extension.scdAutoUpdate)
        }

        registerBridgingTask(project, extension)
        registerAssembleTasks(project, extension)
    }

    open fun registerAssembleTasks(project: Project, extension: SpmGradlePluginExtension) {
        registerAssembleTask(project, extension, "") { task ->
            project.plugins.withType(ApplicationPlugin::class.java) {
                val app = project.extensions.getByType(JavaApplication::class.java)
                val platform = OperatingSystem.current().swiftTargetPlatform()?.name?.lowercase() ?: ""
                app.applicationDefaultJvmArgs = listOf("-Djava.library.path=" + "${task.get().outputDirectory.get().dir(platform)}")
            }

            project.tasks.withType(JavaExec::class.java) {
                it.dependsOn(task)
            }
        }
    }

    open fun registerAssembleTask(project: Project, extension: SpmGradlePluginExtension, variant: String = ""):
            TaskProvider<AssembleSwiftPackageTask> {

        val platforms = extension.platforms.map {
            if (it.isEmpty()) {
                val res = it.toMutableList()
                res.add(defaultPlatform)
                res
            } else {
                it
            }
        }

        val assembleTask = project.tasks.register("assemble${variant.capitalized()}SwiftPackage",
            AssembleSwiftPackageTask::class.java) {

            resolveScdTask?.let { tp ->
                it.scdFile.set(tp.flatMap { t -> t.scdFile })
            }

            it.platforms.set(platforms)
            it.path.set(extension.path)
        }

        val cleanTask = project.tasks.register("clean${variant.capitalized()}SwiftBuildDir", Delete::class.java) {
            it.delete.add(assembleTask.get().buildDir)
        }

        project.tasks.named("clean") {
            it.dependsOn(cleanTask)
        }

        project.afterEvaluate {
            assembleTask.get().linkDependencies.set(extension.dependencies.map {
                resolveDependencies(project, it)
            })
        }

        return assembleTask
    }

    open fun registerAssembleTask(project: Project,
                                  extension: SpmGradlePluginExtension,
                                  variant: String,
                                  configurationAction: Action<TaskProvider<AssembleSwiftPackageTask>>
    ): TaskProvider<AssembleSwiftPackageTask> {
        val task = registerAssembleTask(project, extension, variant)
        configurationAction.execute(task)
        return task
    }

    open fun registerBridgingTask(project: Project, extension: SpmGradlePluginExtension) {
        val task = project.tasks.register("generateBridging", GenerateBridgingTask::class.java) {

            resolveScdTask?.let { tp ->
                it.scdFile.set(tp.flatMap { t -> t.scdFile })
            }

            it.path.set(extension.path)
            it.product.set(extension.product)
            it.javaVersion.set(extension.javaVersion)
        }

        configureBridgingTask(project, task)
    }

    open fun configureBridgingTask(project: Project, task: TaskProvider<GenerateBridgingTask>) {
        project.plugins.withType(JavaPlugin::class.java) {
            val sourceSets = project.extensions.getByType(SourceSetContainer::class.java)
            val main = sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME)

            main.java.srcDirs(task)
        }
    }

    open fun resolveDependencies(project: Project,
                                 dependencies: List<Dependency>): List<String> {

        val conf = project.configurations.detachedConfiguration(
            *dependencies.toTypedArray()
        )
        conf.isTransitive = false
        return conf.resolve().map { it.absolutePath }
    }
}
