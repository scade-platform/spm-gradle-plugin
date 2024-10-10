package com.scade.gradle.plugins.android.spm

import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.android.build.gradle.tasks.MergeSourceSetFolders

import org.gradle.api.Project
import org.gradle.api.DefaultTask
import org.gradle.api.file.Directory
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.configurationcache.extensions.capitalized

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

import io.scade.gradle.plugins.spm.SpmGradlePlugin
import io.scade.gradle.plugins.spm.TargetPlatform
import io.scade.gradle.plugins.spm.tasks.AssembleSwiftPackageTask
import io.scade.gradle.plugins.spm.tasks.GenerateBridgingTask
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.util.PatternSet

class SpmGradleAndroidPlugin: SpmGradlePlugin() {
    override val defaultPlatform: TargetPlatform
        get() = TargetPlatform.Android()

    override fun applyBridgingOutputs(project: Project, task: TaskProvider<GenerateBridgingTask>) {
        project.plugins.withType(AppPlugin::class.java) {
            val androidComponents =
                project.extensions.getByType(ApplicationAndroidComponentsExtension::class.java)

            androidComponents.onVariants { variant ->
                variant.sources.java?.addStaticSourceDirectory(task.get().bridgingSrc.get().asFile.path)
                variant.sources.java?.addStaticSourceDirectory(task.get().bridgingJavaSrc.get().asFile.path)
            }
        }

        project.tasks.withType(JavaCompile::class.java) {
            it.dependsOn(task)
        }

        project.tasks.withType(KotlinCompile::class.java) {
            it.dependsOn(task)
        }
    }


    override fun applyAssembleOutputs(project: Project, task: TaskProvider<AssembleSwiftPackageTask>) {
        project.plugins.withType(AppPlugin::class.java) {
            val androidComponents =
                project.extensions.getByType(ApplicationAndroidComponentsExtension::class.java)

            androidComponents.onVariants { variant ->
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