package io.scade.gradle.plugins.spm.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.*
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URI
import java.nio.file.Paths

abstract class ResolveScdToolTask() : DefaultTask() {
    @Optional
    @InputFile
    val scd: RegularFileProperty = project.objects.fileProperty()

    @Optional
    @Input
    val scdAutoUpdate: Property<Boolean> = project.objects.property(Boolean::class.java)

    private val scdFileInternal: RegularFileProperty = project.objects.fileProperty()

    @OutputFile
    val scdFile: Provider<RegularFile> = scdFileInternal

    init {
        scdFileInternal.convention {
            Paths.get(System.getProperty("user.home")).resolve("Library/Developer/Scade/Toolchains/scd/bin/scd").toFile()
        }
    }

    @TaskAction
    fun run() {
        val scd = scd.orNull?.asFile

        if (scd != null) {
            scdFileInternal.set(scd)

        } else if (scdFileInternal.asFile.get().exists()) {
            val cur = getInstalledVersion(scdFileInternal.asFile.get())
            cur?.let {
                println("Scade Build Tool version ${cur.first}.${cur.second}.${cur.third} is found ...")

                val latest = getLatestVersion()

                if ((latest.first > cur.first) ||
                    (latest.first == cur.first && latest.second > cur.second) ||
                    (latest.first == cur.first && latest.second == cur.second && latest.third > cur.third)) {

                    println("New version of the Scade Build Tool is available: ${latest.versionString()}")


                    if(scdAutoUpdate.orElse(false).get()) {
                        println("Updating Scade Build Tool to the latest version ... ")
                        downloadAndInstall(latest)
                    }
                }
            }
        } else {
            downloadAndInstallLatest()
        }
    }

    private fun downloadAndInstall(version: Triple<Int, Int, Int>?) {
        val pkgVer = (version ?: getLatestVersion()).versionString()
        val pkgFileName = "scd-${pkgVer}.pkg"

        val pkgFile = temporaryDir.resolve(pkgFileName)

        val downloadUrl = URI("https://github.com/scade-platform/scade-build-tool/releases/download/${pkgVer}/${pkgFileName}").toURL()

        if (!pkgFile.exists()) {
            println("Downloading from ${downloadUrl}...")
            downloadUrl.openStream().use {
                it.copyTo(FileOutputStream(pkgFile))
            }
        }

        if (pkgFile.exists()) {
            println("Installing Scade Build Tool...")

            project.exec {
                it.commandLine("installer", "-pkg", "$pkgFile", "-target", "CurrentUserHomeDirectory")
            }
        }
    }

    private fun downloadAndInstallLatest() {
        downloadAndInstall(null)
    }

    private fun getLatestVersion() : Triple<Int, Int, Int> {
        val latestUrl = URI("https://github.com/scade-platform/scade-build-tool/releases/latest").toURL()
        val conn = latestUrl.openConnection() as HttpURLConnection
        conn.instanceFollowRedirects = false

        val version = conn.getHeaderField("Location").split("/").last().split('.')
        return Triple(version[0].toInt(), version[1].toInt(), version[2].toInt())
    }

    private fun getInstalledVersion(scd:  java.io.File) : Triple<Int, Int, Int>? {
        val versionInfo = ByteArrayOutputStream().use { out ->
            project.exec {
                it.commandLine(scd.path, "--version")
                it.standardOutput = out
            }
            out.toString()
        }

        val versionRegex = """\D*(?<major>\d*)\.(?<minor>\d*).(?<patch>\d*)""".toRegex()
        val versionMatch = versionRegex.find(versionInfo)

        return versionMatch?.let {
            Triple(it.groups["major"]!!.value.toInt(),
                   it.groups["minor"]!!.value.toInt(),
                   it.groups["patch"]!!.value.toInt())

        } ?: return null
    }
}

private fun Triple<Int, Int, Int>.versionString() : String {
    return "${first}.${second}.${third}"
}
