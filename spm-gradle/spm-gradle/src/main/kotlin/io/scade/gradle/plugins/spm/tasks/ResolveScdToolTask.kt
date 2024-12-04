package io.scade.gradle.plugins.spm.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.FileOutputStream
import java.net.URI
import java.nio.file.Paths

abstract class ResolveScdToolTask() : DefaultTask() {
    @Optional
    @InputFile
    val scd: RegularFileProperty = project.objects.fileProperty()

    @OutputFile
    val scdFile: RegularFileProperty = project.objects.fileProperty()

    init {
        scdFile.convention {
            Paths.get(System.getProperty("user.home")).resolve(
            "Library/Developer/Scade/Toolchains/scd/bin/scd").toFile()
        }
    }

    @TaskAction
    fun run() {
        val scd = scd.orNull?.asFile

        if (scd != null) {
            scdFile.set(scd)

        } else if (!scdFile.asFile.get().exists()) {
            //TODO: check the tool's version

            val pkgVer = "2.7.0"
            val pkgUrl = "https://github.com/scade-platform/scade-build-tool/releases/download/$pkgVer/scd-$pkgVer.pkg"
            val pkgFile = temporaryDir.resolve("scd-$pkgVer.pkg")

            if (!pkgFile.exists()) {
                println("Downloading $pkgUrl...")
                URI(pkgUrl).toURL().openStream().use {
                    it.copyTo(FileOutputStream(pkgFile))
                }
            }

            if (pkgFile.exists()) {
                println("Installing Scade Build Tool $pkgVer...")

                project.exec {
                    it.commandLine("installer", "-pkg", "$pkgFile", "-target", "CurrentUserHomeDirectory")
                }
            }
        }
    }
}