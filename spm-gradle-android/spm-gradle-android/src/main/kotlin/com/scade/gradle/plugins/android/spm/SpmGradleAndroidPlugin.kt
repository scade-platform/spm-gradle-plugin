package com.scade.gradle.plugins.android.spm

import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.tasks.MergeSourceSetFolders

import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.compile.JavaCompile

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

import io.scade.gradle.plugins.spm.SpmGradlePlugin
import io.scade.gradle.plugins.spm.SpmGradlePluginExtension
import io.scade.gradle.plugins.spm.TargetPlatform
import io.scade.gradle.plugins.spm.tasks.GenerateBridgingTask
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.util.PatternSet
import org.gradle.internal.extensions.stdlib.capitalized
import javax.inject.Inject


class SpmGradleAndroidPlugin @Inject constructor (
    objects: ObjectFactory
): SpmGradlePlugin(objects) {

    override val defaultPlatform: TargetPlatform
        get() = TargetPlatform.Android()

    /*
    override fun registerAssembleTasks(project: Project, extension: SpmGradlePluginExtension) {
        project.plugins.withType(AppPlugin::class.java) {
            registerAssembleTask(project, extension, "") { task ->
                val androidComponents =
                    project.extensions.getByType(ApplicationAndroidComponentsExtension::class.java)

                androidComponents.onVariants { variant ->
                    project.logger.lifecycle("Processing variant: ${variant.name}, ${variant.buildType}")
                    variant.sources.jniLibs?.addStaticSourceDirectory(task.get().outputDirectory.get().asFile.path)

                }

                project.tasks.withType(MergeSourceSetFolders::class.java) {
                    it.dependsOn(task)
                }

                val outputJars = project.provider {
                    task.get().outputDirectory.asFileTree.matching(PatternSet().include("*.jar"))
                }

                project.dependencies.add("implementation", project.files(outputJars))
            }
        }
    }
    */


    override fun registerAssembleTasks(project: Project, extension: SpmGradlePluginExtension) {
        project.plugins.withType(AppPlugin::class.java) {
            val androidComponents =
                project.extensions.getByType(ApplicationAndroidComponentsExtension::class.java)

            androidComponents.onVariants { variant ->
                registerAssembleTask(project, extension, variant.name) { task ->
                    variant.sources.jniLibs?.addStaticSourceDirectory(task.get().outputDirectory.get().asFile.path)

                    project.afterEvaluate {
                        it.tasks.named("merge${variant.name.capitalized()}JniLibFolders") { mergeTask ->
                            mergeTask.dependsOn(task)
                        }
                        
                        task.get().linkDependencies.set(extension.dependencies.map { deps ->
                            resolveDependencies(project, deps)
                        })
                    }

                    val outputJars = project.provider {
                        task.get().outputDirectory.asFileTree.matching(PatternSet().include("*.jar"))
                    }

                    project.dependencies.add("${variant.name}Implementation", project.files(outputJars))
                }
            }
        }
    }


    override fun configureBridgingTask(project: Project, task: TaskProvider<GenerateBridgingTask>) {
        project.plugins.withType(AppPlugin::class.java) {
            val androidComponents =
                project.extensions.getByType(ApplicationAndroidComponentsExtension::class.java)

            androidComponents.onVariants { variant ->
                task.get().bridgingSrc.orNull?.let {
                    variant.sources.java?.addStaticSourceDirectory(it.asFile.path)
                }

                task.get().bridgingJavaSrc.orNull?.let {
                    variant.sources.java?.addStaticSourceDirectory(it.asFile.path)
                }
            }
        }

        project.tasks.withType(JavaCompile::class.java) {
            it.dependsOn(task)
        }

        project.tasks.withType(KotlinCompile::class.java) {
            it.dependsOn(task)
        }
    }
}